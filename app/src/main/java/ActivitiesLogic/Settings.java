package ActivitiesLogic;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.omeglewhatsapphybrid.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import UtilityClasses.Global;

public class Settings extends AppCompatActivity {

    FirebaseAuth mAuth;

    DatabaseReference mDatabase;

    ImageButton backBtn;
    Button logoutBtn;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        mAuth = FirebaseAuth.getInstance();


        //initialization of ui components and firebase stuff.
        mDatabase = FirebaseDatabase.getInstance().getReference();

        Global.networkThread.setCurrentActivity(this);

        backBtn = findViewById(R.id.backButton);
        logoutBtn = findViewById(R.id.logoutBtn);

        //updating text to show current logged in user!
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String currentUid = currentUser.getUid();
        DatabaseReference userRef = mDatabase.child("Users").child(currentUid);


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Home.class);
                startActivity(intent);
                finish();
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Global.FIRST_TIME_HOME();
                Global.networkThread.closeThread();
                Global.networkThread = null;

                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

    }
}
