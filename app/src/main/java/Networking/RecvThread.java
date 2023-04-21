package Networking;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class RecvThread implements Runnable{

    private SocketChannel clientSocket;
    private int port;
    private String address;

    public RecvThread(String addr, int port){
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

            ByteBuffer buffer = ByteBuffer.allocate(1024);

            //need to start send thread here! -------------

            // Continuously listen for messages from the server
            while (true) {
                int bytesRead = clientSocket.read(buffer);
                if (bytesRead > 0) {
                    buffer.flip();
                    byte[] bytes = new byte[bytesRead];
                    buffer.get(bytes, 0, bytesRead);
                    System.out.println("Received message: " + new String(bytes));
                    buffer.clear();
                }

            }
        }
        catch (IOException e){
            System.out.println("not wokint");
            e.printStackTrace();
        }
    }
}
