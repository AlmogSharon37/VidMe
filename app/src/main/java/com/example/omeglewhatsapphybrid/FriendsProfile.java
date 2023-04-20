package com.example.omeglewhatsapphybrid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class FriendsProfile extends AppCompatActivity {


    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    FirebaseStorage mStorage;

    FirebaseUser currentUser = null;

    TextView nameTextBig;
    ImageView profilePicBtn;
    ImageButton backBtn;

    ImageButton chatBtn;
    ImageButton phoneBtn;
    ImageButton unfriendBtn;

    String friendUid, friendName, friendImg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_profile_activity);
        mAuth = FirebaseAuth.getInstance();

        //initialization of ui components and firebase stuff.
        mDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();

        nameTextBig = findViewById(R.id.UsernameBigText);
        profilePicBtn = findViewById(R.id.profilePicBtn);
        backBtn = findViewById(R.id.backButton);
        chatBtn = findViewById(R.id.chatBtn);
        phoneBtn = findViewById(R.id.phoneBtn);
        unfriendBtn = findViewById(R.id.unfriendBtn);

        friendUid = getIntent().getStringExtra("friendUid");
        friendName = getIntent().getStringExtra("friendName");
        friendImg = getIntent().getStringExtra("friendImg");


        currentUser = mAuth.getCurrentUser();
        String currentUid = currentUser.getUid();

        //updating buttons to show friend data!
        nameTextBig.setText(friendName);
        UtilityFunctions.setImageOnImageView(this, Uri.parse(friendImg), profilePicBtn);



        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Friends.class);
                startActivity(intent);
                finish();
            }
        });

        chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Chat.class)
                        .putExtra("friendUid", friendUid)
                        .putExtra("friendImg", friendImg)
                        .putExtra("friendName", friendName);
                startActivity(intent);
                finish();
            }
        });

        unfriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UtilityFunctions.removeFriendFromUser(mDatabase, currentUid, friendUid);
                Intent intent = new Intent(getApplicationContext(), Friends.class);
                startActivity(intent);
                finish();
            }
        });






    }


}
