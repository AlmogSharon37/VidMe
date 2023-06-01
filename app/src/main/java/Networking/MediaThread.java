package Networking;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.Image;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.camera.view.PreviewView;

import com.google.gson.internal.bind.TreeTypeAdapter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ActivitiesLogic.Home;
import ActivitiesLogic.InCall;
import ActivitiesLogic.ReceivedCall;

public class MediaThread extends Thread{
    private boolean isRunning = true;

    private ImageView cameraSurface;
    private String friendUUID;
    private Handler handler;

    private DatagramChannel clientChannel;
    private int port;


    private String address;
    private ByteBuffer buffer;

    private AudioTrack audioTrack;


    int audioSampleRate = 44100;  // Sample rate in Hz
    int audioChannelConfig = AudioFormat.CHANNEL_OUT_STEREO;
    int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int bufferSize = AudioTrack.getMinBufferSize(audioSampleRate, audioChannelConfig, audioFormat);

    public MediaThread(String friendUUID, int port, String address) {
        this.cameraSurface = null;
        this.friendUUID = friendUUID;
        this.port = port;
        this.address = address;
        this.buffer = ByteBuffer.allocate(32000);
        handler = new Handler();
        audioTrack = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(audioFormat)
                        .setSampleRate(audioSampleRate)
                        .setChannelMask(audioChannelConfig)
                        .build())
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build();

        //audioTrack.setPlaybackRate(sampleRate)
        //audioTrack.setStereoVolume(2.0f, 2.0f);

    }

    @Override
    public void run(){
        try {
            System.out.println("Starting");
            System.out.println(bufferSize);
            clientChannel = DatagramChannel.open();
            clientChannel.configureBlocking(false);
            clientChannel.connect(new InetSocketAddress(address, port));
            while (!clientChannel.isConnected()) {
                // Wait for connection to be established
            }
            System.out.println("listening for messages from the bridgeServer");
            audioTrack.play(); // starting to play audio received by server.
            // Continuously listen for messages from the server
            byte[] message;
            while (isRunning) {
                message = recvMessage();
                if(message.length == 0)
                    continue;
                //System.out.println(message.length);
                handleMessage(message);

            }

        }
        catch (IOException e){
            System.out.println("not working");
            e.printStackTrace();
        }
    }

    private byte[] recvMessage() {
        try {
            buffer.clear();
            clientChannel.receive(buffer);
            buffer.flip();
            byte[] receivedData = new byte[buffer.remaining()];
            buffer.get(receivedData);
            return receivedData;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public String buildString(String action, String... vars){
        String syntaxedStr = action + "|" + friendUUID + "|";
        for (String var:vars) {
            syntaxedStr += var + "|";
        }

        return syntaxedStr;
    }

    public void handleMessage(byte[] bytes){
        //first 4 bytes are the length of the image!
        byte[] lengthBytes = new byte[4];
        System.arraycopy(bytes, 0, lengthBytes, 0, 4);
        int imageLength = 0;
        for (byte b : lengthBytes) {
            imageLength = (imageLength << 8) + (b & 0xFF);
        }

        byte[] imageData = new byte[imageLength];
        System.arraycopy(bytes, 4, imageData, 0, imageLength);

        byte[] recorderData = new byte[bytes.length - imageLength - 4];
        System.arraycopy(bytes, imageLength + 44, recorderData, 0, recorderData.length - 44);

        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);


        if(cameraSurface != null) {
            // Create a Lock object
            Lock lock = new ReentrantLock();

            // Acquire the lock
            lock.lock();
            try {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        cameraSurface.setImageBitmap(bitmap);
                    }
                });

            } finally {
                lock.unlock();
            }

        }
        try {
            audioTrack.write(recorderData, 0, recorderData.length);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void sendBytes(byte[] message){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                ByteBuffer buffer = ByteBuffer.wrap(message);
                try {
                    //System.out.println("sending message to server");
                    clientChannel.send(buffer, new InetSocketAddress(address, port));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }



    public boolean isRunning() {
        return isRunning;
    }

    public void close() {
        isRunning = false;
        audioTrack.stop();
        audioTrack.release();
    }

    public ImageView getCameraSurface() {
        return cameraSurface;
    }

    public void setCameraSurface(ImageView cameraSurface) {
        // Create a Lock object
        Lock lock = new ReentrantLock();

        // Acquire the lock
        lock.lock();
        try {
            this.cameraSurface = cameraSurface;
        } finally {
            lock.unlock();
        }
    }

    public String getSocketAddress() {
        InetSocketAddress localAddress = (InetSocketAddress) clientChannel.socket().getLocalSocketAddress();
        String clientAddress = localAddress.getAddress().getHostAddress();
        int clientPort = localAddress.getPort();
        return clientAddress+":"+String.valueOf(clientPort);
    }

}
