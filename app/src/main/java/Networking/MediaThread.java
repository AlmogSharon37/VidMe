package Networking;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.camera.view.PreviewView;

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

    public MediaThread(String friendUUID, int port, String address) {
        this.cameraSurface = null;
        this.friendUUID = friendUUID;
        this.port = port;
        this.address = address;
        this.buffer = ByteBuffer.allocate(32000);
    }

    @Override
    public void run(){
        try {
            System.out.println("Starting");
            clientChannel = DatagramChannel.open();
            System.out.println("opened");
            clientChannel.configureBlocking(false);
            clientChannel.connect(new InetSocketAddress(address, port));
            System.out.println("getting into server");
            while (!clientChannel.isConnected()) {
                // Wait for connection to be established
            }
            System.out.println("got into server");
            // Continuously listen for messages from the server
            byte[] message;
            while (isRunning) {
                message = recvMessage();
                if(message.length == 0)
                    continue;
                System.out.println(message.length);
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
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        if(cameraSurface != null) {
            // Create a Lock object
            Lock lock = new ReentrantLock();

            // Acquire the lock
            lock.lock();
            try {
                cameraSurface.setImageBitmap(bitmap);
            } finally {
                lock.unlock();
            }

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
