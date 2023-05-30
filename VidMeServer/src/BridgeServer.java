import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

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
                String message;
                // wait for events
                selector.select();

                // process all events
                for (SelectionKey key : selector.selectedKeys()) {
                    if (key.isReadable()) {
                        // read data from the channel
                        ByteBuffer buffer = ByteBuffer.allocate(32000);
                        InetSocketAddress senderAddress = (InetSocketAddress) bridgeChannel.receive(buffer);
                        if(Global.client1 == null)
                            Global.client1 = senderAddress.getHostString() + ":" + senderAddress.getPort();
                        else if(Global.client2 == null && !Global.client1.equals(senderAddress.getHostString() + ":" + senderAddress.getPort()))
                            Global.client2 = senderAddress.getHostString() + ":" + senderAddress.getPort();
                        buffer.flip();
                        //System.out.println(Global.client1);
                        //System.out.println(Global.client2);
                        // process the received data
                        byte[] data = new byte[ buffer.remaining()];
                        buffer.get(data);
                        handleSend(senderAddress, data);
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



    public void handleSend(InetSocketAddress address, byte[] info){
        String addressStr = address.getHostString() + ":" + address.getPort();
        //System.out.println("udp server got message from " + addressStr + ": " + info.length );
        //we need to redirect the info to the other client
        HashMap<String, String> which = Global.getWhichHashmap(addressStr);
        if(which == null)
            return;
        String addressToSendTo = which.get(addressStr);

        String[] addressPort = addressToSendTo.split(":");
        InetSocketAddress clientSocketAddress = new InetSocketAddress(addressPort[0], Integer.parseInt(addressPort[1]));
        System.out.println(clientSocketAddress.toString());
        //String toSendAddress = Global.testWithEmulators(addressStr);
        //if(toSendAddress != null){
         //   String[] addressPort = toSendAddress.split(":");

           // InetSocketAddress clientSocketAddress = new InetSocketAddress(addressPort[0], Integer.parseInt(addressPort[1]));
           // try {
            //    System.out.println("sending " + info.length + " bytes to - " + toSendAddress + " from ---> " + addressStr);
           //     bridgeChannel.send(ByteBuffer.wrap(info), clientSocketAddress);
          //  } catch (IOException e) {
           //     throw new RuntimeException(e);
          //  }
       // }

        // Send the response packet to the client
        try {
            bridgeChannel.send(ByteBuffer.wrap(info), clientSocketAddress);
            System.out.println("sending " + info.length + " bytes to - " + clientSocketAddress + " from ---> " + addressStr);
        } catch (IOException e) {
               throw new RuntimeException(e);
          }
    }



}
