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

import com.example.omeglewhatsapphybrid.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import Networking.MediaThread;
import Networking.NetworkThread;
import UtilityClasses.Global;
import UtilityClasses.UtilityFunctions;

public class ChoiceCall extends AppCompatActivity {


    FirebaseAuth mAuth;
    DatabaseReference mDatabase;


    ImageButton skipBtn;
    ImageButton homeBtn;

    public ImageButton omegleBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nextorexit_choice_activity);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        //if user is not logged in take him to login page!


        //initialization of ui components and firebase stuff.
        mDatabase = FirebaseDatabase.getInstance().getReference();

        skipBtn = findViewById(R.id.nextCallBtn);
        homeBtn = findViewById(R.id.homeBtn);

        skipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // need to go back to queue
                Global.mediaThread = new MediaThread(null, Global.mediaServerPort, Global.networkServerIp);
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Global.mediaThread.start();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        String toSend = Global.networkThread.buildString("JOINQ", Global.mediaThread.getSocketAddress());
                        Global.networkThread.sendToServer(toSend);
                    }
                });
                t.start();

                //hide everything and show other stuff
                homeBtn.setVisibility(View.INVISIBLE);
                skipBtn.setVisibility(View.INVISIBLE);
                findViewById(R.id.textError).setVisibility(View.INVISIBLE);

                findViewById(R.id.homeBtnInvis).setVisibility(View.VISIBLE);
                findViewById(R.id.inQueueText).setVisibility(View.VISIBLE);


            }
        });

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String toSend = Global.networkThread.buildString("EXIT","NONE");
                Global.networkThread.sendToServer(toSend);
                Intent intent = new Intent(getApplicationContext(), Home.class);
                startActivity(intent);
                finish();

            }
        });

        findViewById(R.id.homeBtnInvis).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeBtn.callOnClick();
            }
        });








    }
}
