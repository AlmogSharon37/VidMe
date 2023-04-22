package ActivitiesLogic;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.omeglewhatsapphybrid.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

import UtilityClasses.Global;
import UtilityClasses.User;
import UtilityClasses.UtilityFunctions;

public class ReceivedCall extends AppCompatActivity {

    FirebaseAuth mAuth;

    DatabaseReference mDatabase;

    ImageView callerProfilePic;
    TextView callerName;

    ImageButton acceptCallBtn;
    ImageButton declineCallBtn;

    String callerUuid;
    String callerNameStr, callerProfilePicStr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.received_call_activity);

        mAuth = FirebaseAuth.getInstance();


        //initialization of ui components and firebase stuff.
        mDatabase = FirebaseDatabase.getInstance().getReference();
        callerUuid = getIntent().getStringExtra("callerUuid");

        callerName = findViewById(R.id.callerName);
        callerProfilePic = findViewById(R.id.callerProfilePic);
        acceptCallBtn = findViewById(R.id.acceptBtn);
        declineCallBtn = findViewById(R.id.declineBtn);


        //  getting caller info from database and putting it on the UI
        mDatabase.child("Users").child(callerUuid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User caller = snapshot.getValue(User.class);
                callerNameStr = caller.getUsername();
                callerProfilePicStr = caller.getProfilePic();
                callerName.setText(callerNameStr);

                UtilityFunctions.setImageOnImageView(ReceivedCall.this, Uri.parse(callerProfilePicStr), callerProfilePic);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        acceptCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("works please take me back now!");
            }
        });

        declineCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handler handler = Global.networkThread.getHandler();
                Activity activity = Global.networkThread.getCurrentActivity();
                handler.removeCallbacksAndMessages(null);
                String message = Global.networkThread.buildString("CALLDECLINE", callerUuid);
                Thread sendToServer = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Global.networkThread.sendToServer(message);
                    }
                });
                sendToServer.start();

                Intent intent = new Intent(activity, activity.getClass());
                activity.startActivity(intent);
                //speed up the process of waiting till the call request is automatically denied

            }
        });





    }
}
