// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {

            Global.clientHashMapInit(); // initialising client hashmap

            Server server = new Server("10.0.0.8", 8820);
            server.startServer();
        }
    }