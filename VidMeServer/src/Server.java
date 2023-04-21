import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

public class Server {

    private ServerSocketChannel serverSocket;
    private int portNumber;
    private String ip;
    private Selector selector;
    private ByteBuffer buffer;
    private Charset charset;

    public Server(String ip, int port) { //initialisation
        try {
            System.out.println("Server is starting");
            this.ip = ip;
            this.portNumber = port;
            serverSocket = ServerSocketChannel.open();
            serverSocket.socket().bind(new InetSocketAddress(portNumber));
            serverSocket.configureBlocking(false); // set non-blocking mode
            selector = Selector.open();
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);
            buffer = ByteBuffer.allocate(1024);
            charset = StandardCharsets.UTF_8;

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public void startServer() {
        System.out.println("now it real start");
        try{
        while (true) {
            selector.select(); // wait for an event on the registered channels
            Set<SelectionKey> selectedKeys = selector.selectedKeys(); // this is all of the requests made on every client
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();

                if (key.isAcceptable()) { // meaning the
                    handleAccept(key);
                }

                else if (key.isReadable()) {
                    handleRecv(key);
                }
                else if (key.isWritable()) {
                    handleSend(key);
                }

                keyIterator.remove();
            }
        }
    } catch (Exception e) {
        System.out.println("Looks like my code isnt working... Welp, time to fix it :)");
        System.out.println(e.getMessage());
         }

    }


    private void handleAccept(SelectionKey key){
        try {
            ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
            SocketChannel clientChannel = serverChannel.accept();
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_READ);
            System.out.println("Accepted connection from " + clientChannel.socket().getRemoteSocketAddress());
            String toSend = MessageBuilder.buildString("INIT");
            sendToClient(clientChannel, toSend);
            // now ask the client to give his uuid to add to the server hashmap! :)


        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    private void handleRecv(SelectionKey key){
        try{
            String message = recvFromClient(key);
            handleMessage(message, key);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    private void handleSend(SelectionKey key){
        try{
            SocketChannel clientChannel = (SocketChannel) key.channel();
            ByteBuffer responseBuffer = (ByteBuffer) key.attachment();
            clientChannel.write(responseBuffer);
            if (!responseBuffer.hasRemaining()) {
                clientChannel.register(selector, SelectionKey.OP_READ);
            }

        }
        catch (IOException e){
            e.printStackTrace();
        }
    }


    private String recvFromClient(SelectionKey key){
        try{
            SocketChannel clientChannel = (SocketChannel) key.channel();
            buffer.clear();
            int numBytes = clientChannel.read(buffer);

            if (numBytes == -1) {
                key.cancel();
                clientChannel.close();
                return "CLOSE";
            }

            else {
                String message = new String(buffer.array(), 0, numBytes, charset);
                System.out.println("Received message: " + message);
                return message;
            }

        }
        catch (IOException e){
            e.printStackTrace();
            return "ERR";
        }
    }

    private void sendToClient(SelectionKey key, String syntaxedMsg){
        try {
            SocketChannel clientChannel = (SocketChannel) key.channel();
            ByteBuffer callBuffer = charset.encode(syntaxedMsg);
            clientChannel.register(selector, SelectionKey.OP_WRITE, callBuffer);
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    private void sendToClient(SocketChannel channel, String syntaxedMsg){
        try {

            ByteBuffer callBuffer = charset.encode(syntaxedMsg);
            channel.register(selector, SelectionKey.OP_WRITE, callBuffer);
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }


    public String handleMessage(String message, SelectionKey key){
        SocketChannel clientChannel = (SocketChannel) key.channel();
        //LENGTH|IDENTIFIER|ACTION|VAR1|VAR2|...|VARN|\n

        String[] all =  message.split("\\|");
        String[] vars = Arrays.copyOfRange(all, 3, all.length);
        String action = all[2];
        switch (action){

            case "INIT":
                //client sent init so variable0 is his uuid! need to save it inside our hashmap
                Global.Clients.put(vars[0], clientChannel);
                sendToClient(key, MessageBuilder.buildString("ACK"));

                break;

        }




        return null;
    }

}
