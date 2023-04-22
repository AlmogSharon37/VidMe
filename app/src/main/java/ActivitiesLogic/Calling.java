package ActivitiesLogic;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
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

import UtilityClasses.Global;
import UtilityClasses.User;
import UtilityClasses.UtilityFunctions;

public class Calling extends AppCompatActivity {

    FirebaseAuth mAuth;

    DatabaseReference mDatabase;

    ImageView friendProfilePic;
    TextView friendName;

    ImageButton declineCallBtn;

    String friendUuid;
    String friendNameStr, friendProfilePicStr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calling_activity);

        mAuth = FirebaseAuth.getInstance();


        //initialization of ui components and firebase stuff.
        mDatabase = FirebaseDatabase.getInstance().getReference();
        friendUuid = getIntent().getStringExtra("friendUuid");

        friendName = findViewById(R.id.callerName);
        friendProfilePic = findViewById(R.id.callerProfilePic);
        declineCallBtn = findViewById(R.id.declineBtn);


        //  getting caller info from database and putting it on the UI
        mDatabase.child("Users").child(friendUuid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User caller = snapshot.getValue(User.class);
                friendNameStr = caller.getUsername();
                friendProfilePicStr = caller.getProfilePic();
                friendName.setText(friendNameStr);

                UtilityFunctions.setImageOnImageView(Calling.this, Uri.parse(friendProfilePicStr), friendProfilePic);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        declineCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handler handler = Global.networkThread.getHandler();
                Activity activity = Global.networkThread.getCurrentActivity();
                handler.removeCallbacksAndMessages(null);
                String message = Global.networkThread.buildString("CALLDECLINE", friendUuid);
                Thread sendToServer = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Global.networkThread.sendToServer(message);
                    }
                });
                sendToServer.start();

                Intent intent = new Intent(activity, activity.getClass())
                        .putExtra("friendUid",friendUuid)
                                .putExtra("friendImg", friendProfilePicStr)
                                        .putExtra("friendName", friendNameStr);

                activity.startActivity(intent);
                //speed up the process of waiting till the call request is automatically denied

            }
        });





    }
}
