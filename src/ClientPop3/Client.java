package ClientPop3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by Silver on 20-Mar-17.
 */
public class Client {
    private Socket socket;
    private InetAddress server;
    private int port;
    private int state;

    public Client(InetAddress server, int port) {
        this.server = server;
        this.port = port;
        this.state=1;
        try {
            this.socket = new Socket(server, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void start() throws IOException {

        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException ex) {
            System.out.println("1");
        }
        OutputStream out = null;
        try {
            out = socket.getOutputStream();
        } catch (IOException ex) {
            System.out.println("2");
        }

        String request;
        String response;
        while (state > 0){
            request= null;
//            request = scanner ...
            response = in.readLine();
            System.out.println(response);

            if (response.substring(0,3).equals("+OK")){
                switch (state){
                    case 1:
                        this.state = 2;
                        break;
                    case 3:
                        this.state = 5;
                        break;
                    default:
                        break;
                }
            }
            else if (response.substring(0,4).equals("-ERR")){
                switch (state){
                    case 3:
                        this.state = 2;
                        break;
                    default:
                        break;
                }
            }
            else if (request.substring(0,4).equals("APOP")){
                switch (state){
                    case 2:
                        out.write(request.getBytes());
                        this.state = 3;
                        break;
                    default:
                        System.out.println("Erreur: non-identifié");
                        break;
                }
            }
            else if ((request.substring(0,4).equals("STAT"))
                    || (request.substring(0,4).equals("LIST"))
                    || (request.substring(0,4).equals("RETR"))){
                switch (state){
                    case 5:
                        out.write(request.getBytes());
                        break;
                    default:
                        System.out.println("Requête non valide");
                        break;
                }
            }
            else if (request.substring(0,4).equals("QUIT")){
                switch (state){
                    default:
                        out.write(request.getBytes());
                        this.state = 0;
                        break;
                }
            }

        }













































































    }
}
