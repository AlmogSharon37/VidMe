import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class BridgeServer {

    private DatagramChannel bridgeChannel;
    private int portNumber;
    private String ip;
    private Selector selector;
    private ByteBuffer buffer;

    private Charset charset;

    public BridgeServer(String ip, int port){
        try {
            this.ip = ip;
            this.portNumber = port;
            bridgeChannel = DatagramChannel.open();
            bridgeChannel.bind(new InetSocketAddress(ip, portNumber));
            bridgeChannel.configureBlocking(false); // set non-blocking mode
            selector = Selector.open();
            bridgeChannel.register(selector, SelectionKey.OP_READ);
            charset = StandardCharsets.UTF_8;

        }
        catch (IOException e){
            e.printStackTrace();
        }
    }


    public void start() {
        try {
            while (true) {
                // wait for events
                selector.select();

                // process all events
                for (SelectionKey key : selector.selectedKeys()) {
                    if (key.isReadable()) {
                        // read data from the channel
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        InetSocketAddress senderAddress = (InetSocketAddress) bridgeChannel.receive(buffer);;
                        buffer.flip();

                        // process the received data
                        byte[] data = new byte[buffer.remaining()];
                        buffer.get(data);
                        System.out.println("Received data: " + new String(data));
                    }
                }

                // clear the selected keys
                selector.selectedKeys().clear();
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}
