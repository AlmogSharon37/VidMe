package Networking;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class RecvThread implements Runnable{

    private Socket clientSocket;
    private int port;
    private String address;

    public RecvThread(String addr, int port){
        this.address = addr;
        this.port = port;
    }


    @Override
    public void run() {
        try{
            System.out.println("Starin");
            clientSocket = new Socket(address, port);
            // Create an input stream to receive messages from the server
            InputStream inputStream = clientSocket.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

            //need to start send thread here! -------------

            // Continuously listen for messages from the server
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Received message from server: " + message);
            }

        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
