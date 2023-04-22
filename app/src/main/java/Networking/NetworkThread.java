package Networking;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

import ActivitiesLogic.ReceivedCall;

public class NetworkThread extends Thread{
    private boolean isRunning = true;

    private String userUUID;
    private Activity currentActivity;
    private Handler handler;

    private SocketChannel clientSocket;
    private int port;
    private String address;
    private ByteBuffer buffer;

    public NetworkThread(String userUUID, String addr, int port){
        this.userUUID = userUUID;
        this.address = addr;
        this.port = port;
        handler = new Handler();
    }

    public SocketChannel getClientSocket() {
        return clientSocket;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public void setCurrentActivity(Activity activity) {
        this.currentActivity = activity;
    }
    public void closeThread() {
        this.isRunning = false;
    }

    public Handler getHandler(){return handler;}
    public Activity getCurrentActivity(){return currentActivity;}

    @Override
    public void run() {
        try {
            System.out.println("Starting");
            clientSocket = SocketChannel.open();
            clientSocket.configureBlocking(false);
            clientSocket.connect(new InetSocketAddress(address, port));
            while (!clientSocket.finishConnect()) {
                // Wait for connection to be established

            }

            buffer = ByteBuffer.allocate(1024);

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
        String syntaxedStr = "C|" + action + "|";
        for (String var:vars) {
            syntaxedStr += var + "|";
        }

        return syntaxedStr.length() + "|" + syntaxedStr;
    }

    public void handleMessage(String message){
        // need to trigger events based on the info
        String[] all =  message.split("\\|");
        String[] vars = null;
        if(all.length >= 3)
            vars = Arrays.copyOfRange(all, 3, all.length);


        String action = all[2];
        String toServer;
                switch (action){

            case "INIT":
                //need to send server our UUID
                toServer = buildString("INIT", userUUID);
                sendToServer(toServer);
                break;

            case "CALL":
                //caller uuid is in vars[0]
                String callerUuid = vars[0];
                //need to load recieved_call_activity so client can accept/decline the call
                //in this state the client shouldnt get any messages since server blocks them because client is "BUSY"\
                Intent acceptDeclineIntent = new Intent(currentActivity, ReceivedCall.class)
                        .putExtra("callerUuid", callerUuid);

                currentActivity.startActivity(acceptDeclineIntent);

                // Post a delayed message to switch back to the previous activity after 5 seconds
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent previousActivityIntent = new Intent(currentActivity, currentActivity.getClass());
                        currentActivity.startActivity(previousActivityIntent);
                    }
                }, 10000); // 10 second delay
                break;


            case "CALLDECLINE":
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent previousActivityIntent = new Intent(currentActivity, currentActivity.getClass());
                        currentActivity.startActivity(previousActivityIntent);
                    }
                }, 0);



                break;




        }
    }

    public void sendToServer(String message){
        try {
            if (message != null && !message.equals("")) {
                buffer.put(message.getBytes());
                buffer.flip();
                while (buffer.hasRemaining()) {
                    clientSocket.write(buffer);
                }
                buffer.clear();
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }

    }

    public String recvMessage() {
        int bytesRead = 0;
        try {
            bytesRead = clientSocket.read(buffer);
            if (bytesRead > 0) {
                buffer.flip();
                byte[] bytes = new byte[bytesRead];
                buffer.get(bytes, 0, bytesRead);
                String message = new String(bytes);
                buffer.clear();
                return message;

            }
        }
        catch (IOException ex) {
            ex.printStackTrace();

        }
        return "-1|S|ERR";

    }
}
