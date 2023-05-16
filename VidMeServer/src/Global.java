import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

public class Global {
    public static HashMap<String, SocketChannel> Clients;
    public static HashMap<String, Boolean> ClientsUnCallable;




    public static boolean clientHashMapInit(){
        try{
            Clients = new HashMap<String, SocketChannel>();
            ClientsUnCallable = new HashMap<String, Boolean>();
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }


    public static void putInClientsHashmap(String uuid, SocketChannel channel){
        Clients.put(uuid, channel);

    }
    public static void removeFromClientsHashmap(String uuid, SocketChannel channel){
        Clients.remove(uuid, channel);

    }

    public static void putInUncallableHashmap(String uuid, String uuid2){
        ClientsUnCallable.put(uuid,true);
        ClientsUnCallable.put(uuid2,true);

    }

    public static void removeFromUncallableHashmap(String uuid, String uuid2){
        ClientsUnCallable.remove(uuid,true);
        ClientsUnCallable.remove(uuid2,true);

    }

}
