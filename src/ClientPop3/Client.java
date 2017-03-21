package ClientPop3;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Created by Silver on 20-Mar-17.
 */
public class Client {
    private Socket socket;
    private InetAddress server;
    private String timestamp = "";
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
        ArrayList<String> requestSplitted;
        Scanner sc = new Scanner(System.in);
        String request;
        String response;
        Scanner s;


        while (state > 0){
            request= null;
            response = in.readLine();
            System.out.println(response);
            requestSplitted = new ArrayList<>();
            s = new Scanner(response).useDelimiter("\\s+");
            while (s.hasNext()) {
                requestSplitted.add(s.next());
            }
            if (requestSplitted.get(0).equals("+OK")){
                switch (state){
                    case 1:
                        for (int i = 1; i < requestSplitted.size() && timestamp.length() < 1; i++) {
                            String str = requestSplitted.get(i);
                            if(str.substring(0,1).equals("<")
                                    && str.substring(str.length()-1).equals(">")){
                                timestamp = str;
                                System.out.println(timestamp);
                            }
                        }
                        if(timestamp.length() > 0){
                            this.state = 2;
                        } else {
                            System.out.println("error : no timestamp received");
                        }
                        break;
                    case 3:
                        this.state = 5;
                        break;
                    default:
                        break;
                }
            }
            else if (requestSplitted.get(0).equals("-ERR")){
                switch (state){
                    case 3:
                        this.state = 2;
                        break;
                    default:
                        break;
                }
            }

            System.out.println("Veuillez saisir une requète :");
            String str = sc.nextLine();
            request = str;
            System.out.println("Vous avez saisi : " + str);

            if (request.substring(0,4).equals("APOP")){
                switch (state){
                    case 2:
                        requestSplitted = new ArrayList<>();
                        s = new Scanner(request).useDelimiter("\\s+");
                        while (s.hasNext()) {
                            requestSplitted.add(s.next());
                        }
                        try {
                            if(requestSplitted.size() > 2) {
                                out.write((requestSplitted.get(0) + " " + requestSplitted.get(1) + " ").getBytes());
                                out.write(this.getAPOPMD5(requestSplitted.get(2)).getBytes());
                                out.write(("\r\n").getBytes());
                            } else {
                                out.write((request+"\r\n").getBytes());
                            }
                        } catch (NoSuchAlgorithmException e) {
                            System.out.println(e.getMessage());
                        }
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

    public String getAPOPMD5(String pswd) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String str = this.timestamp+pswd;
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] checkSum = md.digest(str.getBytes("UTF-8"));
        return new String(checkSum, "UTF-8");
    }
}
