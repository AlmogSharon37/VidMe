package ActivitiesLogic;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.omeglewhatsapphybrid.R;

import Adapters.RequestAdapter;
import UtilityClasses.Global;
import UtilityClasses.User;
import UtilityClasses.UtilityFunctions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class Inbox extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    FirebaseUser currentUser;

    ProgressBar progressBar;
    RecyclerView recyclerView;
    ImageButton backBtn;


    ArrayList<User> userRequests;
    RequestAdapter.OnClickListener onUserClickListener;
    RequestAdapter.OnClickListener onUserClickAcceptListener;
    RequestAdapter.OnClickListener onUserClickDenyListener;
    RequestAdapter requestAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inbox_activity);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        Global.networkThread.setCurrentActivity(this);

        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recycler);
        backBtn = findViewById(R.id.backButton);


        userRequests = new ArrayList<User>();
        currentUser = mAuth.getCurrentUser();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Home.class);
                startActivity(intent);
                finish();
            }
        });

        onUserClickListener = new RequestAdapter.OnClickListener() {
            @Override
            public void onClick(int position) {
                Toast.makeText(Inbox.this, "clicked on user "+ userRequests.get(position).getUsername(), Toast.LENGTH_SHORT ).show();
            }
        };

        onUserClickAcceptListener = new RequestAdapter.OnClickListener() {
            @Override
            public void onClick(int position) {
                System.out.println("calling remove with these params - " + userRequests.get(position).getUid() + ", " + currentUser.getUid() );
                UtilityFunctions.removeRequestFromUser(mDatabase, userRequests.get(position).getUid(), currentUser.getUid());
                UtilityFunctions.addFriendToUser(mDatabase, userRequests.get(position).getUid(), currentUser.getUid());

            }
        };
        onUserClickDenyListener = new RequestAdapter.OnClickListener() {
            @Override
            public void onClick(int position) {
                UtilityFunctions.removeRequestFromUser(mDatabase, userRequests.get(position).getUid(), currentUser.getUid());

            }
        };




        getRequests();


    }



    /* algorithm explanation
        we first fetch all the friend uuids from the current user's friends list
        then we fetch all the USERS with the uuids we fetched earlier
        we put these users into the friends array! :)
     */

    private void getRequests(){
        mDatabase.getReference("Users/"+currentUser.getUid()+"/requests").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap<String,Boolean> requests = new HashMap<String, Boolean>();
                userRequests.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    requests.put(((String)dataSnapshot.getKey()),true);
                    System.out.println("adding reqeust to requests");

                }
                System.out.println("finished setting up requests");
                mDatabase.getReference("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            if (requests.containsKey(dataSnapshot.getKey())) {
                                userRequests.add(dataSnapshot.getValue(User.class));
                            }

                        }
                        System.out.println("finished all");
                        requestAdapter = new RequestAdapter(userRequests, Inbox.this, onUserClickListener, onUserClickAcceptListener, onUserClickDenyListener);
                        recyclerView.setAdapter(requestAdapter);
                        recyclerView.setLayoutManager(new LinearLayoutManager(Inbox.this));
                        progressBar.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}

        });

    }








}
