package ActivitiesLogic;

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

import UtilityClasses.Actions;
import com.example.omeglewhatsapphybrid.R;

import UtilityClasses.Global;
import UtilityClasses.UtilityFunctions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class Profile extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    FirebaseStorage mStorage;

    FirebaseUser currentUser = null;

    TextView nameTextBig;
    ImageView profilePicBtn;
    ImageButton backBtn;

    TextView nameTextAcc;
    TextView emailTextAcc;
    TextView passwordTextAcc;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);
        mAuth = FirebaseAuth.getInstance();

        //initialization of ui components and firebase stuff.
        mDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();

        Global.networkThread.setCurrentActivity(this);

        nameTextBig = findViewById(R.id.UsernameBigText);
        profilePicBtn = findViewById(R.id.profilePicBtn);
        backBtn = findViewById(R.id.backButton);
        nameTextAcc = findViewById(R.id.accInfoUsername);
        emailTextAcc = findViewById(R.id.accInfoEmail);
        passwordTextAcc = findViewById(R.id.accInfoPassword);

        //updating text to show current logged in user!
        currentUser = mAuth.getCurrentUser();
        String currentUid = currentUser.getUid();
        nameTextBig.setText(currentUser.getDisplayName());
        nameTextAcc.setText(currentUser.getDisplayName());
        emailTextAcc.setText(currentUser.getEmail());

        UtilityFunctions.setImageOnImageView(this, currentUser.getPhotoUrl(), profilePicBtn);



        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Home.class);
                startActivity(intent);
                finish();
            }
        });

        profilePicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoIntent = new Intent(Intent.ACTION_PICK);
                photoIntent.setType("image/*");
                startActivityForResult(photoIntent, Actions.CHANGE_PROFILE_PICTURE);
            }
        });





    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Actions.CHANGE_PROFILE_PICTURE && resultCode == RESULT_OK && data!=null){
            Uri imagePath = data.getData();
            getImageInImageView(imagePath);
            uploadImage(imagePath);

        }
    }

    private void getImageInImageView(Uri imagePath) {
        Bitmap bitmap = null;
        try{
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagePath);
        }
        catch(IOException e){
            e.printStackTrace();
        }
         profilePicBtn.setImageBitmap(bitmap);
    }

    private void uploadImage(Uri imagePath){
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading Image..");
        progressDialog.show();
        mStorage.getReference("images/" + currentUser.getUid()).putFile(imagePath).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    //need to add the image url into user profilePic attribute in database
                    task.getResult().getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            mDatabase.getReference("Users/"+currentUser.getUid()+"/profilePic").setValue(task.getResult().toString());
                            UserProfileChangeRequest addProfileUri = new UserProfileChangeRequest.Builder()
                                    .setPhotoUri(task.getResult())
                                    .build();
                            currentUser.updateProfile(addProfileUri);
                        }
                    });
                    Toast.makeText(Profile.this, "Uploaded Image!", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(Profile.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
                progressDialog.cancel();
            }
        });


    }






}
