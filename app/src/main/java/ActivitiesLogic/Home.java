package ActivitiesLogic;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

//import com.facebook.CallbackManager;
import com.example.omeglewhatsapphybrid.R;

import Networking.RecvThread;
import UtilityClasses.UtilityFunctions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Home extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;


    ProgressBar progressBar;

    TextView NameText;
    ImageView profileBtn;

    ImageButton inboxBtn;
    ImageButton friendsBtn;
    ImageButton settingsBtn;

    ImageButton omegleBtn;

        @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.home_activity);
            mAuth = FirebaseAuth.getInstance();
            FirebaseUser user = mAuth.getCurrentUser();
            //if user is not logged in take him to login page!
            if(user == null){
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }

            //initialization of ui components and firebase stuff.
            mDatabase = FirebaseDatabase.getInstance().getReference();

            progressBar = findViewById(R.id.progressBar);

            NameText = findViewById(R.id.TextName);
            profileBtn = findViewById(R.id.profilePicBtn);
            inboxBtn = findViewById(R.id.inboxBtn);
            friendsBtn = findViewById(R.id.friendsBtn);
            settingsBtn = findViewById(R.id.settingsBtn);
            omegleBtn = findViewById(R.id.omegleBtn);


            NameText.setText(user.getDisplayName());
            UtilityFunctions.setImageOnImageView(this, user.getPhotoUrl(), profileBtn);

            profileBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), Profile.class);
                    startActivity(intent);
                    finish();
                }
            });
            settingsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), Settings.class);
                    startActivity(intent);
                    finish();
                }
            });
            inboxBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), Inbox.class);
                    startActivity(intent);
                    finish();
                }
            });

            friendsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), Friends.class);
                    startActivity(intent);
                    finish();
                }
            });
            System.out.println("herlo");
            RecvThread t = new RecvThread("10.0.0.10", 8820 );
            Thread recv = new Thread(t);
            recv.start();


        }



}
