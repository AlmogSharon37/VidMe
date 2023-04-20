import java.net.Socket;
import java.util.HashMap;

public class Global {
    public static HashMap<String, Socket> Clients;




    public static boolean clientHashMapInit(){
        try{
            Clients = new HashMap<String, Socket>();
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
