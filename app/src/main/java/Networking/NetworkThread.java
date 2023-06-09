package Networking;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import androidx.camera.view.PreviewView;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Base64;

import ActivitiesLogic.ChoiceCall;
import ActivitiesLogic.Friends;
import ActivitiesLogic.Home;
import ActivitiesLogic.InCall;
import ActivitiesLogic.InCallRandom;
import ActivitiesLogic.Login;
import ActivitiesLogic.ReceivedCall;
import UtilityClasses.Global;

public class NetworkThread extends Thread{
    private boolean isRunning = true;

    private String userUUID;
    private Activity currentActivity;
    private Handler handler;

    private PreviewView friendCamera;

    private SocketChannel clientSocket;
    private int port;
    private String address;
    private ByteBuffer buffer;

    public PreviewView getFriendCamera() {
        return friendCamera;
    }

    public void setFriendCamera(PreviewView friendCamera) {
        this.friendCamera = friendCamera;
    }

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
            System.out.println(getSocketAddress());

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
        String syntaxedStr = "C|" + action + "|" + userUUID + "|";
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
        if(!action.equals("ERR"))
            System.out.println(message);
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

                // Post a delayed message to switch back to the previous activity after x seconds
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendToServer(buildString("CALLDECLINE", callerUuid));
                        Intent previousActivityIntent = new Intent(currentActivity, Home.class);
                        currentActivity.startActivity(previousActivityIntent);
                    }
                }, 10000); // 10 second delay
                break;


            case "CALLDECLINE":
                if(Global.mediaThread != null)
                Global.mediaThread.close();
                Global.mediaThread = null;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        handler.removeCallbacksAndMessages(null); // we might be the ones being called, so we have to stop the
                                                                        // delay to go back to the previous activity!

                        Intent previousActivityIntent = new Intent(currentActivity, Home.class);
                        currentActivity.startActivity(previousActivityIntent);
                    }
                });
                break;

            case "CALLACCEPT":
                //frienduuid is in vars[0]
                String friendUuid = vars[0];
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        handler.removeCallbacksAndMessages(null);
                        //we called so we have to cancel delay to go back to the previous activity!
                        Intent previousActivityIntent = new Intent(currentActivity, InCall.class)
                                .putExtra("friendUuid",friendUuid);
                        currentActivity.startActivity(previousActivityIntent);
                    }
                });
                break;

            case "CALLBUSY":
                Global.mediaThread.close();
                Global.mediaThread = null;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Intent previousActivityIntent = new Intent(currentActivity, Home.class);
                        currentActivity.startActivity(previousActivityIntent);
                        Toast.makeText(currentActivity, "This user is offline or busy with a call", Toast.LENGTH_SHORT).show();
                    }
                });
                break;

            case "CALLSTOP":
                Global.mediaThread.close();
                Global.mediaThread = null;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Intent previousActivityIntent = new Intent(currentActivity, Home.class);
                        currentActivity.startActivity(previousActivityIntent);
                    }
                });
                break;

            case "STARTCALL":
                friendUuid = vars[0];
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        handler.removeCallbacksAndMessages(null);
                        //we called so we have to cancel delay to go back to the previous activity!
                        Intent previousActivityIntent = new Intent(currentActivity, InCallRandom.class)
                                .putExtra("friendUuid",friendUuid);
                        currentActivity.startActivity(previousActivityIntent);
                    }
                });
                break;


            case "CALLNEXT":
                //presending the client a choice, if he wants to get back in queue or exit
                //both choices make the client go back to home screen, but one causes a press to the omegle button on click (to get in the queue)
                Global.mediaThread.close();
                Global.mediaThread = null;
                // take the client to a different activity, and ask there
                //TODO
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        handler.removeCallbacksAndMessages(null);
                        Intent previousActivityIntent = new Intent(currentActivity, ChoiceCall.class);
                        currentActivity.startActivity(previousActivityIntent);
                    }
                });
                break;






        }
    }

    public void sendToServer(String message){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
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
        });
        t.start();


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

    public String getSocketAddress() {
        InetSocketAddress localAddress = (InetSocketAddress) clientSocket.socket().getLocalSocketAddress();
        String clientAddress = localAddress.getAddress().getHostAddress();
        int clientPort = localAddress.getPort();
        return clientAddress+":"+String.valueOf(clientPort);
    }
}
