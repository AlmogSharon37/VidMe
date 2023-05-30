import javax.xml.crypto.Data;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

public class Global {
    public static HashMap<String, SocketChannel> Clients;
    public static HashMap<String, Boolean> ClientsUnCallable;
    public static HashMap<String, String> clientAddresses;

    public static HashMap<String, String> calls;
    public static HashMap<String, String> callsInverted;
    public static String client1;
    public static String client2;



    public static boolean clientHashMapInit(){
        try{
            Clients = new HashMap<String, SocketChannel>();
            ClientsUnCallable = new HashMap<String, Boolean>();
            calls = new HashMap<>();
            callsInverted = new HashMap<>();
            clientAddresses = new HashMap<>();
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

    public static HashMap<String,String> getWhichHashmap(String client){
        if(calls.containsKey(client))
            return calls;
        else if(callsInverted.containsKey(client))
            return callsInverted;
        return null;
    }

    public static void addToCalls(String client1, String client2){
        calls.put(client1, client2);
        callsInverted.put(client2, client1);
    }

    public static void removeFromCalls(String client1, String client2){
        HashMap<String,String> where = getWhichHashmap(client1);
        where.remove(client1);
        where = getWhichHashmap(client2);
        where.remove(client2);
    }


    public static String testWithEmulators(String a){
        if(client1 == null || client2 == null)
            return null;
        if(a.equals(client1))
            return client2;
        return client1;
    }


}
