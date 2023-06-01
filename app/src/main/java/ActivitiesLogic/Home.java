package ActivitiesLogic;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

//import com.facebook.CallbackManager;
import com.example.omeglewhatsapphybrid.R;

import Networking.MediaThread;
import Networking.NetworkThread;
import UtilityClasses.Global;
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

    public ImageButton omegleBtn;

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

            if(Global.FIRST_TIME_CONNECT_SERVER == 1) {
                Global.FIRST_TIME_HOME();
                if(Global.networkServerIp == null)
                    Toast.makeText(Home.this, "server configuration not set, logout and configure.", Toast.LENGTH_SHORT).show();
                else {
                    Global.networkThread = new NetworkThread(user.getUid(), Global.networkServerIp, Global.networkServerPort);
                    Global.networkThread.setCurrentActivity(this);
                    Global.networkThread.start();
                }

            }


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

            omegleBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Global.mediaThread = new MediaThread(null, Global.mediaServerPort, Global.networkServerIp);
                    Global.mediaThread.start();
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            String message = Global.networkThread.buildString("JOINQ", Global.mediaThread.getSocketAddress());
                            Global.networkThread.sendToServer(message);
                        }
                    });
                    t.start();

                    friendsBtn.setVisibility(View.INVISIBLE);
                    omegleBtn.setVisibility(View.INVISIBLE);
                    inboxBtn.setVisibility(View.INVISIBLE);
                    settingsBtn.setVisibility(View.INVISIBLE);
                    findViewById(R.id.Logo).setVisibility(View.INVISIBLE);
                    NameText.setVisibility(View.INVISIBLE);
                    findViewById(R.id.TextHello).setVisibility(View.INVISIBLE);
                    profileBtn.setVisibility(View.INVISIBLE);
                    findViewById(R.id.linearLayoutButtons).setVisibility(View.INVISIBLE);

                    findViewById(R.id.inQueueText).setVisibility(View.VISIBLE);
                    findViewById(R.id.grayBackground).setVisibility(View.VISIBLE);
                    findViewById(R.id.homeBtn).setVisibility(View.VISIBLE);


                }
            });

            findViewById(R.id.homeBtn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    findViewById(R.id.inQueueText).setVisibility(View.INVISIBLE);
                    findViewById(R.id.grayBackground).setVisibility(View.INVISIBLE);
                    findViewById(R.id.homeBtn).setVisibility(View.INVISIBLE);

                    friendsBtn.setVisibility(View.VISIBLE);
                    omegleBtn.setVisibility(View.VISIBLE);
                    inboxBtn.setVisibility(View.VISIBLE);
                    settingsBtn.setVisibility(View.VISIBLE);
                    findViewById(R.id.Logo).setVisibility(View.VISIBLE);
                    NameText.setVisibility(View.VISIBLE);
                    findViewById(R.id.TextHello).setVisibility(View.VISIBLE);
                    profileBtn.setVisibility(View.VISIBLE);
                    String message = Global.networkThread.buildString("EXIT", "NONE");
                    Global.networkThread.sendToServer(message);
                }
            });



        }



}
