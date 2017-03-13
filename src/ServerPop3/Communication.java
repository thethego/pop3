package ServerPop3;

import java.io.*;

import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by p1303175 on 06/03/2017.
 */
public class Communication {

    private Socket conn_cli;
    private int ID;
    private int state;
    private String user;

    public Communication(Socket conn_cli, int ID) {
        this.conn_cli = conn_cli;
        this.ID = ID;
        this.state = 2;
        this.user = "";
    }

    void start() throws IOException {

        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(conn_cli.getInputStream()));
        } catch (IOException ex) {
            System.out.println("1");
        }
        OutputStream out = null;
        try {
            out = conn_cli.getOutputStream();
        } catch (IOException ex) {
            System.out.println("2");
        }

        String request = null;
        switch (state) {
            case 2: //READY
                out.write("+OK alpha POP3 server Ready\r\n".getBytes());
                System.out.println("Server Connected");
                state = 3;
                break;
            case 3: //AUTHENTIFICATION
                out.write("-ERR\r\n".getBytes());
                break;
            case 4: //AUTHORISATION
                out.write("-ERR\r\n".getBytes());
                break;
            case 5: //TRANSACTION
                out.write("-ERR\r\n".getBytes());
                break;
            case 6: //UPDATE
                out.write("-ERR\r\n".getBytes());
                break;
            default:
                out.write("-ERR\r\n".getBytes());
                break;
        }
        while(state>2 && state<6) {
            try {
                request = in.readLine();
            } catch (IOException ex) {
                System.out.println("3");
            }

            System.out.println(request);
            ArrayList<String> requestSplitted = new ArrayList<>();
            if(request != null) {
                Scanner s = new Scanner(request).useDelimiter("\\s+");
                while (s.hasNext()) {
                    requestSplitted.add(s.next());
                }
            } else {
                requestSplitted.add("QUIT");
            }
//            System.out.println(requestSplitted.get(0));
            if (requestSplitted.get(0).equals("APOP")) {
                switch (state) {
                    case 2: //READY
                        out.write("-ERR\r\n".getBytes());
                        break;
                    case 3: //AUTHENTIFICATION
                        try {
                            this.user=requestSplitted.get(1);
                            ArrayList<Integer> list = getMsgSTAT();
                            out.write(("+OK maildrop has "+list.get(0)+" message(s) ("+list.get(1)+" octets)\r\n").getBytes());
                            state = 5;
                        } catch (IOException e) {
                            out.write("-ERR invalid user\r\n".getBytes());
                        }
                        break;
                    case 4: //AUTHORISATION
                        out.write("-ERR\r\n".getBytes());
                        break;
                    case 5: //TRANSACTION
                        out.write("-ERR\r\n".getBytes());
                        break;
                    case 6: //UPDATE
                        out.write("-ERR\r\n".getBytes());
                        break;
                    default:
                        out.write("-ERR\r\n".getBytes());
                        break;
                }
            } else if (requestSplitted.get(0).equals("STAT")) {
                switch (state) {
                    case 2: //READY
                        out.write("-ERR\r\n".getBytes());
                        break;
                    case 3: //AUTHENTIFICATION
                        out.write("-ERR\r\n".getBytes());
                        break;
                    case 4: //AUTHORISATION
                        out.write("-ERR\r\n".getBytes());
                        break;
                    case 5: //TRANSACTION
                        try {
                            ArrayList<Integer> list = getMsgSTAT();
                            out.write(("+OK "+list.get(0)+" "+list.get(1)+"\r\n").getBytes());
                        } catch (IOException e) {
                            out.write("-ERR\r\n".getBytes());
                        }
                        break;
                    case 6: //UPDATE
                        out.write("-ERR\r\n".getBytes());
                        break;
                    default:
                        out.write("-ERR\r\n".getBytes());
                        break;
                }
//            } else if (requestSplitted[0] == "LIST") {

//            } else if (requestSplitted[0] == "DELE") {

            } else if (requestSplitted.get(0).equals("RETR")) {
                switch (state) {
                    case 2: //READY
                        out.write("-ERR\r\n".getBytes());
                        break;
                    case 3: //AUTHENTIFICATION
                        out.write("-ERR\r\n".getBytes());
                        break;
                    case 4: //AUTHORISATION
                        out.write("-ERR\r\n".getBytes());
                        break;
                    case 5: //TRANSACTION
                        try {
                            ArrayList<String> list = getMsgRETR(Integer.parseInt(requestSplitted.get(1)));
                            out.write(("+OK "+list.remove(0)+"\r\n").getBytes());
                            for (String string : list) {
                                out.write(string.getBytes());//CRLF ???
                            }
                        } catch (IOException e) {
                            out.write("-ERR\r\n".getBytes());
                        }
                        break;
                    case 6: //UPDATE
                        out.write("-ERR\r\n".getBytes());
                        break;
                    default:
                        out.write("-ERR\r\n".getBytes());
                        break;
                }

            } else if (requestSplitted.get(0).equals("QUIT")) {
                switch (state) {
                    case 6: //UPDATE
                        out.write("-ERR\r\n".getBytes());
                        break;
                    default:
                        out.write("+OK alpha POP3 server signing off\r\n".getBytes());
                        state = 6;
                        break;
                }
            } else {
                out.write("-ERR invalid request\r\n".getBytes());
            }
        }
        // on ferme les flux.
        out.close();
        in.close();
        conn_cli.close();

        if(state == 6){ //UPDATE
            state = 2;
        }
    }

    public ArrayList<String> getMsgRETR(int nbMsg) throws IOException{
        String path = "./src/ServerPop3/msg/"+user+".txt";
        ArrayList<String> list= new ArrayList<>();
        int nbBytes = 0;
        BufferedReader br = new BufferedReader(new FileReader(path));
            String line;
            int nbPoint = 1;
            while ((line = br.readLine()) != null && nbPoint<= nbMsg) {
                if(nbPoint == nbMsg) {
                    list.add(line+"\r\n");
                    nbBytes += line.getBytes("UTF-8").length;
                }
                if (line.equals(".")) {
                    nbPoint++;
                }
            }
            list.add(0,nbBytes+" octets\r\n");
            return list;
    }

    public ArrayList<Integer> getMsgSTAT() throws IOException{
        String path = "./src/ServerPop3/msg/"+user+".txt";
        ArrayList<Integer> list= new ArrayList<>();
        int nbBytes = 0;
        int nbPoint = 0;
        BufferedReader br = new BufferedReader(new FileReader(path));
            String line;

            while ((line = br.readLine()) != null) {

                nbBytes += line.getBytes("UTF-8").length;

                if (line.equals(".")) {
                    nbPoint++;
                }
            }
            list.add(nbPoint);
            list.add(nbBytes);
            return list;
    }
}
