package com.kagu.mymonitoring.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kagu.mymonitoring.ErrorActivity;
import com.kagu.mymonitoring.R;
import com.kagu.mymonitoring.adapter.DailyReportAdapter;
import com.kagu.mymonitoring.adapter.ListStudentAdapter;
import com.kagu.mymonitoring.entity.DailyReport;
import com.kagu.mymonitoring.entity.User;
import com.kagu.mymonitoring.qa.AllDailyReportActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ListStudentActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ListStudentAdapter listStudentAdapter;
    private List<User> users;
    EditText search_users;
    TextView titleForm;
    FirebaseUser user;

    private FirebaseAuth firebaseAuth;
    String myId, type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_student);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        firebaseAuth = FirebaseAuth.getInstance();
        recyclerView = findViewById(R.id.rv_student);
        titleForm = findViewById(R.id.titleForm);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        recyclerView.setLayoutManager(linearLayoutManager);
        users = new ArrayList<>();

        checkUserStat();
        readUsers();

        user = firebaseAuth.getCurrentUser();
        //get data
        Query query1 = FirebaseDatabase.getInstance().getReference("Users").orderByChild("id").equalTo(user.getUid());
        query1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    type = "" + ds.child("type").getValue();

                    if (type.equals("Quality Assurance"))
                        titleForm.setText(R.string.dataStudent); //only qa show by project, for lead qa, show all
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view1 -> {
            onSupportNavigateUp();
        });
        search_users = findViewById(R.id.search_users);
        search_users.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchUsers(charSequence.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void searchUsers(String querySearch) {
        //path of all posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);


                    if (user != null) {
                        if (Objects.equals(snapshot.child("type").getValue(String.class), "Student")||
                                Objects.equals(snapshot.child("type").getValue(String.class), "Non Active Student")) {

                            if (user.getFullname().toLowerCase().contains(querySearch.toLowerCase()) ||
                                    user.getProjectName().toLowerCase().contains(querySearch.toLowerCase())) {
                                users.add(user);
                            }
                        }
                    }
                    listStudentAdapter = new ListStudentAdapter(ListStudentActivity.this, users);
                    recyclerView.setAdapter(listStudentAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
//if error
                Toast.makeText(ListStudentActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserStat() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            myId = user.getUid();
        } else {
            startActivity(new Intent(ListStudentActivity.this, ErrorActivity.class));
        }
    }


    private void readUsers() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    User user = snapshot.getValue(User.class);
                    if (Objects.equals(snapshot.child("type").getValue(String.class), "Student")||
                            Objects.equals(snapshot.child("type").getValue(String.class), "Non Active Student")) {
                        users.add(user);
                    }

                }

                listStudentAdapter = new ListStudentAdapter(ListStudentActivity.this, users);
                recyclerView.setAdapter(listStudentAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}