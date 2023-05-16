package ActivitiesLogic;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.Image;
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

import java.io.ByteArrayOutputStream;
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
                Bitmap bitmap = toBitmap(image);
                System.out.println("WOWOWOW");
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
                byte[] compressedData = outputStream.toByteArray();
                // do something with the bytes
                System.out.println(compressedData.length);


                image.close();
            }
        });

        cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, imageAnalysis, preview);
    }




    private Bitmap toBitmap(ImageProxy image) {
        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];
        //U and V are swapped
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 75, out);

        byte[] imageBytes = out.toByteArray();
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }


}
