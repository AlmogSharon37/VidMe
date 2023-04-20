package ActivitiesLogic;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.omeglewhatsapphybrid.R;

import Adapters.UsersAdapter;
import UtilityClasses.User;
import UtilityClasses.UtilityFunctions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class Friends extends AppCompatActivity {


    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    FirebaseUser currentUser;

    ProgressBar progressBar;
    RecyclerView recyclerView;
    ImageButton backBtn;
    ImageButton addFriendBtn;


    ArrayList<User> friends;
    UsersAdapter.OnClickListener onUserClickListener;
    UsersAdapter usersAdapter;

    AlertDialog.Builder dialogBuilder;
    AlertDialog dialog;
    EditText findFriendText;
    Button findFriendBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friends_activity);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recycler);
        backBtn = findViewById(R.id.backButton);
        addFriendBtn = findViewById(R.id.addFriendBtn);


        friends = new ArrayList<User>(); // user's friends list will be here!!
        currentUser = mAuth.getCurrentUser();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Home.class);
                startActivity(intent);
                finish();
            }
        });

        //this means clicked on the user!
        onUserClickListener = new UsersAdapter.OnClickListener() {
            @Override
            public void onClick(int position) {
                Intent intent = new Intent(getApplicationContext(), FriendsProfile.class)
                        .putExtra("friendUid", friends.get(position).getUid())
                        .putExtra("friendImg", friends.get(position).getProfilePic())
                        .putExtra("friendName", friends.get(position).getUsername());
                startActivity(intent);
                finish();
            }
        };

        addFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popUpAddFriendMenu();
            }
        });



        getFriendsList();


    }



    /* algorithm explanation
        we first fetch all the friend uuids from the current user's friends list
        then we fetch all the USERS with the uuids we fetched earlier
        we put these users into the friends array! :)
     */

    private void getFriendsList(){

        mDatabase.getReference("Users/"+currentUser.getUid()+"/friends").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                friends.clear();
                HashMap<String,Boolean> friendUids = new HashMap<String, Boolean>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    friendUids.put(((String)dataSnapshot.getKey()),true);
                    System.out.println("adding user to friendsuid");

                }
                System.out.println("finished setting up friendUids");
                mDatabase.getReference("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            if (friendUids.containsKey(dataSnapshot.getKey())) {
                                friends.add(dataSnapshot.getValue(User.class));
                            }

                        }
                        System.out.println("finished all");
                        usersAdapter = new UsersAdapter(friends, Friends.this, onUserClickListener);
                        recyclerView.setAdapter(usersAdapter);
                        recyclerView.setLayoutManager(new LinearLayoutManager(Friends.this));
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

    private void popUpAddFriendMenu(){
        dialogBuilder = new AlertDialog.Builder(this);
        final View addFriendMenuView = getLayoutInflater().inflate(R.layout.add_friend_menu, null);
        findFriendText = addFriendMenuView.findViewById(R.id.editTextTextPersonName);
        findFriendBtn = addFriendMenuView.findViewById(R.id.addBtn);
        findFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get the user with the name on the text
                //if there is user, send him a friend request, if not give an error
                String name = findFriendText.getText().toString();
                Query query = mDatabase.getReference("Users").orderByChild("username").equalTo(name);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            for (DataSnapshot child:snapshot.getChildren()) {
                                UtilityFunctions.sendFriendRequest(mDatabase, child.getKey(), currentUser.getUid());
                                break;
                            }
                           // UtilityFunctions.sendFriendRequest(mDatabase, "asd", currentUser.getUid());
                            Toast.makeText(Friends.this, "sent friend request!", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(Friends.this, "name doesnt exist", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }

        });
        dialogBuilder.setView(addFriendMenuView);
        dialog = dialogBuilder.create();
        dialog.show();
    }








}



