package Networking;

import android.widget.Switch;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class RecvThread implements Runnable{

    private String userUUID;

    private SocketChannel clientSocket;
    private int port;
    private String address;
    private ByteBuffer buffer;

    public RecvThread(String userUUID, String addr, int port){
        this.userUUID = userUUID;
        this.address = addr;
        this.port = port;
    }


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
            while (true) {
                int bytesRead = clientSocket.read(buffer);
                if (bytesRead > 0) {
                    buffer.flip();
                    byte[] bytes = new byte[bytesRead];
                    buffer.get(bytes, 0, bytesRead);
                    message = new String(bytes);
                    System.out.println("Received message: " + message);
                    buffer.clear();
                    handleMessage(message);

                }

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
        String[] vars = Arrays.copyOfRange(all, 3, all.length);
        String action = all[2];
        String toServer;
                switch (action){

            case "INIT":
                //need to send server our UUID
                toServer = buildString("INIT", userUUID);
                sendToServer(toServer);
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


}
