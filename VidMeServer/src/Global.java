import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

public class Global {
    public static HashMap<String, SocketChannel> Clients;




    public static boolean clientHashMapInit(){
        try{
            Clients = new HashMap<String, SocketChannel>();
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
