package ActivitiesLogic;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.omeglewhatsapphybrid.R;
import UtilityClasses.UtilityFunctions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class Register extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;


    EditText nameEditText;
    EditText emailEditText;
    EditText passwordEditText;
    Button goToLoginBtn;
    ImageButton registerBtn;


    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //initialization of all items in activity
        setContentView(R.layout.signup_activity);


        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();


        nameEditText = findViewById(R.id.editTextTextHello);
        emailEditText = findViewById(R.id.editTextTextEmailAddress);
        passwordEditText = findViewById(R.id.editTextTextPassword);
        goToLoginBtn = findViewById(R.id.omegleBtn);
        registerBtn = findViewById(R.id.RegisterBtn);


        goToLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email,password,name;
                email = String.valueOf(emailEditText.getText());
                password = String.valueOf(passwordEditText.getText());
                name = String.valueOf(nameEditText.getText());

                if(TextUtils.isEmpty(email)){
                    Toast.makeText(Register.this, "you have to enter an email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    Toast.makeText(Register.this, "you have to enter a password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(name)){
                    Toast.makeText(Register.this, "you have to enter a name", Toast.LENGTH_SHORT).show();
                    return;
                }


                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {

                                    UserProfileChangeRequest addName = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(name)
                                            .build();
                                    FirebaseUser user = task.getResult().getUser();
                                    user.updateProfile(addName);

                                    String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                                    UtilityFunctions.writeNewUser(mDatabase, uid, name,"",email);

                                    Toast.makeText(Register.this, "Account created!!!.",
                                            Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), Login.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // If sign Up fails, display a message to the user.
                                    Toast.makeText(Register.this, task.getException().getLocalizedMessage(),
                                            Toast.LENGTH_SHORT).show();

                                }
                            }
                        });



            }
        });

    }




}
