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
            serverSocket.socket().bind(new InetSocketAddress(ip,portNumber));
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
        try {
            while (true) {
                selector.select(); // wait for an event on the registered channels
                Set<SelectionKey> selectedKeys = selector.selectedKeys(); // this is all of the requests made on every client
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();

                    if (key.isAcceptable()) { // meaning the
                        handleAccept(key);
                    } else if (key.isReadable()) {
                        handleRecv(key);
                    } else if (key.isWritable()) {
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


    private void handleAccept(SelectionKey key) {
        try {
            ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
            SocketChannel clientChannel = serverChannel.accept();
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_READ);
            System.out.println("Accepted connection from " + clientChannel.socket().getRemoteSocketAddress());
            String toSend = MessageBuilder.buildString("INIT");
            sendToClient(clientChannel, toSend);
            // now ask the client to give his uuid to add to the server hashmap! :)


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRecv(SelectionKey key) {
        try {
            String message = recvFromClient(key);
            System.out.println("Received message: " + message);
            handleMessage(message, key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void handleSend(SelectionKey key) {
        try {
            SocketChannel clientChannel = (SocketChannel) key.channel();
            ByteBuffer responseBuffer = (ByteBuffer) key.attachment();
            clientChannel.write(responseBuffer);
            if (!responseBuffer.hasRemaining()) {
                clientChannel.register(selector, SelectionKey.OP_READ);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String recvFromClient(SelectionKey key) {
        try {
            SocketChannel clientChannel = (SocketChannel) key.channel();
            buffer.clear();
            int numBytes = clientChannel.read(buffer);

            if (numBytes == -1) {
                key.cancel();
                clientChannel.close();
                return "-1|C|CLOSE";
            } else {
                String message = new String(buffer.array(), 0, numBytes, charset);
                return message;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return "-1|C|ERR";
        }
    }

    private void sendToClient(SelectionKey key, String syntaxedMsg) {
        try {
            SocketChannel clientChannel = (SocketChannel) key.channel();
            ByteBuffer callBuffer = charset.encode(syntaxedMsg);
            clientChannel.register(selector, SelectionKey.OP_WRITE, callBuffer);
            System.out.println("Server sends to " + clientChannel.socket().getRemoteSocketAddress() + ": " + syntaxedMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void sendToClient(SocketChannel channel, String syntaxedMsg) { // overloading function
        try {

            ByteBuffer callBuffer = charset.encode(syntaxedMsg);
            channel.register(selector, SelectionKey.OP_WRITE, callBuffer);
            System.out.println("Server sends to " + channel.socket().getRemoteSocketAddress() + ": " + syntaxedMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public String handleMessage(String message, SelectionKey key) {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        //LENGTH|IDENTIFIER|ACTION|VAR1|VAR2|...|VARN|\n

        String[] all = message.split("\\|");
        String[] vars = null;
        String currentUuid = "";
        if (all.length > 3) {
            vars = Arrays.copyOfRange(all, 3, all.length);
            currentUuid = vars[0];
        }

        String action = all[2];
        switch (action) {

            case "INIT":
                //client sent init so variable0 is his uuid! need to save it inside our hashmap
                Global.putInClientsHashmap(currentUuid, clientChannel);
                sendToClient(key, MessageBuilder.buildString("ACK"));
                break;

            case "CALL":
                String toCallUuid = vars[1];
                //client sent call so variable1 is the uuid of who to call! need to call him and put both of them
                //in the uncallable hashmap since they cant be called
                //client mediaThread address is in vars[2]
                if (Global.Clients.containsKey(toCallUuid) && !Global.ClientsUnCallable.containsKey(toCallUuid)) {
                    //meaning this client is online and not busy
                    Global.clientAddresses.put(currentUuid, vars[2]); // saving first clients address to sync up bridge call
                    sendToClient(Global.Clients.get(toCallUuid), MessageBuilder.buildString("CALL", currentUuid)); // send to called client call request from caller
                    Global.putInUncallableHashmap(currentUuid, toCallUuid); // add them both to busy hashmap
                    System.out.println("added them both to uncallable");
                } else sendToClient(clientChannel, MessageBuilder.buildString("CALLBUSY"));

                break;

            case "CALLDECLINE":
                //client to send him the decline is in vars[1]
                String toSendDecline = vars[1];
                //need to send him that the call has been declined and remove both from the uncallable hashmap
                Global.removeFromUncallableHashmap(toSendDecline, currentUuid);
                Global.clientAddresses.remove(toSendDecline); // removing the first clients address as its no longer needed
                sendToClient(Global.Clients.get(toSendDecline), MessageBuilder.buildString("CALLDECLINE"));
                break;

            case "CALLACCEPT":
                //client to send him the accept is in vars[1]
                //client mediaThread address is in vars[2]
                String toSendAccept = vars[1];
                //need to send him that the call has been accepted
                Global.addToCalls(vars[2], Global.clientAddresses.get(toSendAccept)); // now they are synced and ready to transfer messages between eachother!
                System.out.println(Global.calls.get(vars[2]));
                System.out.println(Global.callsInverted.get(Global.clientAddresses.get(toSendAccept)));
                sendToClient(Global.Clients.get(toSendAccept), MessageBuilder.buildString("CALLACCEPT", vars[0]));
                break;

            case "CALLSTOP":
                //client to send him the stop is in vars[1]
                String toSendStop = vars[1];
                //need to send him that the call has been stopped
                sendToClient(Global.Clients.get(toSendStop), MessageBuilder.buildString("CALLSTOP", vars[0]));
                Global.removeFromUncallableHashmap(currentUuid, toSendStop);
                if(Global.clientAddresses.containsKey(currentUuid)){
                    // means he is the key for the callsInverted
                    String client1 = Global.callsInverted.get(Global.clientAddresses.get(currentUuid));
                    String client2 = Global.calls.get(client1);
                    Global.removeFromCalls(client1, client2);
                    Global.clientAddresses.remove(currentUuid);
                }
                else{
                    //means it contains toSendStop
                    String client1 = Global.callsInverted.get(Global.clientAddresses.get(toSendStop));
                    String client2 = Global.calls.get(client1);
                    Global.removeFromCalls(client1, client2);
                    Global.clientAddresses.remove(toSendStop);
                }

                break;

            case "CLOSE":
                if (Global.ClientsUnCallable.containsKey(currentUuid))
                    Global.removeFromUncallableHashmap(currentUuid, "NONE");
                Global.removeFromClientsHashmap(currentUuid, clientChannel);
                break;




        }
        return null;
    }

}
