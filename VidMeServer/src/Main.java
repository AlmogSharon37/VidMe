import java.net.InetAddress;
import java.net.UnknownHostException;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {

            Global.clientHashMapInit(); // initialising client hashmap
            String ip = "localhost";
        try {
            InetAddress ipAddress = InetAddress.getLocalHost();
            ip = ipAddress.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        System.out.println(ip);
            Server server = new Server(ip,8820);
            BridgeServer bridgeServer = new BridgeServer(ip, 8821);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    bridgeServer. start();
                }
            }); 
            t.start();
            server.startServer();
        }
    }