package ActivitiesLogic;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.example.omeglewhatsapphybrid.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import UtilityClasses.Global;

public class InCall extends AppCompatActivity {


    FirebaseAuth mAuth;

    DatabaseReference mDatabase;

    PreviewView cameraPictureBig;
    PreviewView cameraPictureSmall;

    ImageButton reportBtn;
    ImageButton muteBtn;
    ImageButton flipBtn;

    ImageButton declineCallBtn;

    FirebaseUser currentUser;
    String friendUuid;
    String friendProfilePicStr;

    int currentLensFacing = 0;
    int currentCameraSurface = 0; // 0 is small, 1 is big
    PreviewView currentCameraPreview = null;

    ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    int REQUEST_CAMERA_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.in_call_activity);

        mAuth = FirebaseAuth.getInstance();


        //initialization of ui components and firebase stuff.
        mDatabase = FirebaseDatabase.getInstance().getReference();
        friendUuid = getIntent().getStringExtra("friendUuid");

        cameraPictureBig = findViewById(R.id.BigCamera);
        cameraPictureSmall = findViewById(R.id.smallCamera);

        reportBtn = findViewById(R.id.reportBtn);
        muteBtn = findViewById(R.id.muteBtn);
        declineCallBtn = findViewById(R.id.stopCallBtn);
        flipBtn = findViewById(R.id.flipCameraBtn);

        currentUser = mAuth.getCurrentUser();
        currentCameraPreview = cameraPictureSmall;


        GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if(currentCameraSurface == 0){
                    currentCameraSurface = 1;
                    currentCameraPreview = cameraPictureBig;
                }
                else{
                    currentCameraSurface = 0;
                    currentCameraPreview = cameraPictureSmall;
                }

                try {
                    startCameraX(cameraProviderFuture.get(), currentLensFacing, currentCameraPreview);
                }
                catch (ExecutionException | InterruptedException ex) {
                    ex.printStackTrace();
                }
                return true;
            }
        });

        cameraPictureSmall.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });

        declineCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handler handler = Global.networkThread.getHandler();
                Activity activity = Global.networkThread.getCurrentActivity();
                handler.removeCallbacksAndMessages(null);
                String message = Global.networkThread.buildString("CALLSTOP", friendUuid);

                Global.networkThread.sendToServer(message);




                Intent intent = new Intent(activity, Friends.class);
                activity.startActivity(intent);

            }
        });


        flipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLensFacing == 1){
                    currentLensFacing = 0;}
                else currentLensFacing = 1;
                    try {
                        startCameraX(cameraProviderFuture.get(), currentLensFacing, currentCameraPreview);
                    }
                    catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }

            }
        });



        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try{
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                startCameraX(cameraProvider, currentLensFacing, cameraPictureSmall); // default
            } catch (ExecutionException e){
                e.printStackTrace();
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }



        }, getExecutor());

    }

    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    private void startCameraX(ProcessCameraProvider cameraProvider, int lensFacing, PreviewView surface) {
        cameraProvider.unbindAll();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build();

        //preview use case
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(surface.getSurfaceProvider());

        //image analysis use case (this is used to get each frame and send it to the server!)
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(getExecutor(), new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(ImageProxy image) {
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                // do something with the bytes
                String cameraData = Base64.getEncoder().encodeToString(bytes);
                image.close();
            }
        });

        cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, imageAnalysis, preview);
    }


}
