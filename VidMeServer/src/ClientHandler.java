import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler extends Thread{
    private final Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    public void run() {
        try (
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                // process the client's input
                out.println(inputLine.toUpperCase()); // send a response back to the client
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to read or send data to the client");
            System.out.println(e.getMessage());
        }
    }
}

/* Logic explanation:
   -- first client logs into server --
        -- initClient --
        1. client sends server his UUID, and server saves it inside of the global hashmap of clients
        2. server sends ack that all the data has been transfered successfully
        -- 2 scenarios can happen from here:
            1. this client decides to call another person
            2. this client gets called
            so we cant use listen as its blocking and
            we might want to send this client a call from another person



 */

