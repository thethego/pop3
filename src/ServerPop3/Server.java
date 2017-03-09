package ServerPop3;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Created by p1303175 on 06/03/2017.
 */
public class Server {

    private int portServeur = 10010;
    private InetAddress addr;
    ServerSocket ss;
    private int ID = 0;

    public Server() throws IOException {
        this.ss = new ServerSocket(portServeur);
    }

    public void connexion() throws IOException {
        System.out.println("Server waiting");
        while(true){
            Socket conn_cli = null;
            try {
                conn_cli = ss.accept();
            } catch (IOException ex) {
                System.out.println("connection timed out");
            }
            Communication com = new Communication(conn_cli, ID++);
            com.start();
        }

    }
}
