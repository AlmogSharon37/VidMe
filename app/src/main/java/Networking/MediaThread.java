package Networking;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import androidx.camera.view.PreviewView;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

import ActivitiesLogic.Home;
import ActivitiesLogic.InCall;
import ActivitiesLogic.ReceivedCall;

public class MediaThread extends Thread{
    private boolean isRunning = true;

    private PreviewView CameraSurface;
    private String friendUUID;
    private Handler handler;

    private DatagramChannel clientChannel;
    private int port;
    private String address;

    public MediaThread(PreviewView cameraSurface, String friendUUID, int port, String address) {
        CameraSurface = cameraSurface;
        this.friendUUID = friendUUID;
        this.port = port;
        this.address = address;
    }

    @Override
    public void run(){
        try {
            System.out.println("Starting");
            clientChannel = DatagramChannel.open();
            clientChannel.configureBlocking(false);
            clientChannel.connect(new InetSocketAddress(address, port));
            while (!clientChannel.isConnected()) {
                // Wait for connection to be established
            }


            //need to start send thread here! -------------

            // Continuously listen for messages from the server
            String message;
            while (isRunning) {
                message = recvMessage();
                handleMessage(message);

            }

        }
        catch (IOException e){
            System.out.println("not working");
            e.printStackTrace();
        }
    }


    public String buildString(String action, String... vars){
        String syntaxedStr = action + "|" + friendUUID + "|";
        for (String var:vars) {
            syntaxedStr += var + "|";
        }

        return syntaxedStr;
    }

    public void handleMessage(String message){
        // need to trigger events based on the info
        String[] all =  message.split("\\|");
        String[] vars = null;
        vars = Arrays.copyOfRange(all, 1, all.length);


        String action = all[0];
        String toServer;

    }



    public boolean isRunning() {
        return isRunning;
    }

    public void close() {
        isRunning = false;
    }

    public PreviewView getCameraSurface() {
        return CameraSurface;
    }

    public void setCameraSurface(PreviewView cameraSurface) {
        CameraSurface = cameraSurface;
    }
}
