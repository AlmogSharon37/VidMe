import java.io.IOException;
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
import java.util.Iterator;
import java.util.Set;

public class Server {

    private ServerSocketChannel serverSocket;
    private int portNumber;
    private Selector selector;
    private ByteBuffer buffer;
    private Charset charset;

    public Server(int port) { //initialisation
        try {
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
        try{
        while (true) {
            selector.select(); // wait for an event on the registered channels
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();

                if (key.isAcceptable()) {
                    ServerSocketChannel serverChannel = (ServerSocketChannel)key.channel();
                    SocketChannel clientChannel = serverChannel.accept();
                    clientChannel.configureBlocking(false);
                    clientChannel.register(selector, SelectionKey.OP_READ);
                    System.out.println("Accepted connection from " + clientChannel.socket().getRemoteSocketAddress());


                } else if (key.isReadable()) {
                    SocketChannel clientChannel = (SocketChannel) key.channel();
                    buffer.clear();
                    int numBytes = clientChannel.read(buffer);

                    if (numBytes == -1) {
                        key.cancel();
                        clientChannel.close();

                    }

                    else {
                        String message = new String(buffer.array(), 0, numBytes, charset);
                        System.out.println("Received message: " + message);
                        // process the message and prepare a response
                        String response = message.toUpperCase();
                        clientChannel.register(selector, SelectionKey.OP_WRITE, ByteBuffer.wrap(response.getBytes(charset)));
                    }
                } else if (key.isWritable()) {
                    SocketChannel clientChannel = (SocketChannel) key.channel();
                    ByteBuffer responseBuffer = (ByteBuffer) key.attachment();
                    clientChannel.write(responseBuffer);
                    if (!responseBuffer.hasRemaining()) {
                        clientChannel.register(selector, SelectionKey.OP_READ);
                    }
                }

                keyIterator.remove();
            }
        }
    } catch (Exception e) {
        System.out.println("Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
        System.out.println(e.getMessage());
         }

    }

}
