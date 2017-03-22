package ClientPop3;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Silver on 20-Mar-17.
 */
public class Main {
    public static void main(String[] args) {
        try {
            Client client = new Client(InetAddress.getByName("127.0.0.1"),10010);
            client.start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
