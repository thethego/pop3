package ServerPop3;

import java.io.*;

import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.security.*;
import java.sql.Timestamp;

/**
 * Created by p1303175 on 06/03/2017.
 */
public class Communication {

    private Socket conn_cli;
    private int ID;
    private int state;
    private String user;
    private Timestamp timestamp;

    public Communication(Socket conn_cli, int ID) {
        this.conn_cli = conn_cli;
        this.ID = ID;
        this.state = 2;
        this.user = "";
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }

    void start() throws IOException {
        try {
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
                    out.write(("+OK alpha POP3 server Ready " + this.getAPOP() + "\r\n").getBytes());
                    System.out.println("Server Connected");
                    state = 3;
                    break;
                default:
                    out.write("-ERR internal error\r\n".getBytes());
                    break;
            }
            while (state > 2 && state < 6) {
                ArrayList<String> requestSplitted = new ArrayList<>();
                try {
                    assert in != null;
                    request = in.readLine();
                    if (request != null) {
                        Scanner s = new Scanner(request).useDelimiter("\\s+");
                        while (s.hasNext()) {
                            requestSplitted.add(s.next());
                        }
                        System.out.println(request);
                    } else throw new Exception();
                } catch (Exception ex) {
                    out.write("-ERR internal error\r\n".getBytes());
                    requestSplitted.add("QUIT");
                }
                if (requestSplitted.get(0).equals("APOP")) {
                    switch (state) {
                        case 3: //AUTHENTIFICATION
                            try {
                                if (requestSplitted.size() > 1 && User.getInstance().isUser(requestSplitted.get(1))) {
                                    if (requestSplitted.get(2).equals(this.getAPOPMD5(requestSplitted.get(1)))) {
                                        user = requestSplitted.get(1);
                                        ArrayList<Integer> list = getMsgSTAT();
                                        out.write(("+OK maildrop has " + list.get(0) + " message(s) (" + list.get(1) + " octets)\r\n").getBytes());
                                        state = 5;
                                    } else throw new Exception("invalid password");
                                } else throw new Exception("invalid user");
                            } catch (Exception e) {
                                out.write(("-ERR " + e.getMessage() + "\r\n").getBytes());
                            }
                            break;
                        default:
                            out.write("-ERR internal error\r\n".getBytes());
                            break;
                    }
                } else if (requestSplitted.get(0).equals("STAT")) {
                    switch (state) {
                        case 3: //AUTHENTIFICATION
                        case 4: //AUTHORISATION
                            out.write("-ERR user not connected\r\n".getBytes());
                            break;
                        case 5: //TRANSACTION
                            try {
                                ArrayList<Integer> list = getMsgSTAT();
                                out.write(("+OK " + list.get(0) + " " + list.get(1) + "\r\n").getBytes());
                            } catch (IOException e) {
                                out.write("-ERR internal error\r\n".getBytes());
                            }
                            break;
                        default:
                            out.write("-ERR internal error\r\n".getBytes());
                            break;
                    }
                } else if (requestSplitted.get(0).equals("LIST")) {
                    switch (state) {
                        case 3: //AUTHENTIFICATION
                        case 4: //AUTHORISATION
                            out.write("-ERR user not connected\r\n".getBytes());
                            break;
                        case 5: //TRANSACTION
                            try {
                                ArrayList<Integer> list = getMsgLIST();
                                out.write(("+OK " + list.get(0) + " messages (" + list.get(1) + " octets)\r\n").getBytes());
                                for (int i = 2; i < list.size(); i += 2) {
                                    out.write((list.get(i) + " " + list.get(i + 1) + "\r\n").getBytes());
                                }
                                out.write((".\r\n").getBytes());
                            } catch (Exception e) {
                                out.write("-ERR\r\n".getBytes());
                            }
                            break;
                        default:
                            out.write("-ERR internal error\r\n".getBytes());
                            break;
                    }

                    //            } else if (requestSplitted[0] == "DELE") {

                } else if (requestSplitted.get(0).equals("RETR")) {
                    switch (state) {
                        case 3: //AUTHENTIFICATION
                        case 4: //AUTHORISATION
                            out.write("-ERR user not connected\r\n".getBytes());
                            break;
                        case 5: //TRANSACTION
                            try {
                                if (requestSplitted.size() > 1) {
                                    ArrayList<String> list = getMsgRETR(Integer.parseInt(requestSplitted.get(1)));
                                    out.write(("+OK " + list.remove(0) + "\r\n").getBytes());
                                    for (String string : list) {
                                        out.write(string.getBytes());
                                    }
                                } else throw new Exception();
                            } catch (Exception e) {
                                out.write("-ERR message invalid\r\n".getBytes());
                            }
                            break;
                        default:
                            out.write("-ERR internal error\r\n".getBytes());
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

            if (state == 6) { //UPDATE
                state = 2;
            }
        } catch (SocketException se){
            System.out.println("Connetcion closed : "+se.getMessage());
        }
    }

    public ArrayList<String> getMsgRETR(int nbMsg) throws Exception{
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
        if(nbBytes>0){
            list.add(0,nbBytes+" octets\r\n");
        } else throw new Exception("internal error");
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

    public ArrayList<Integer> getMsgLIST() throws IOException{
        String path = "./src/ServerPop3/msg/"+user+".txt";
        ArrayList<Integer> list= new ArrayList<>();
        int nbBytes = 0;
        int totBytes = 0;
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line;
        int nbPoint = 0;
        while ((line = br.readLine()) != null) {
            nbBytes += line.getBytes("UTF-8").length;
            if (line.equals(".")) {
                nbPoint++;
                list.add(nbPoint);
                list.add(nbBytes);
                        totBytes+=nbBytes;
                        nbBytes=0;
            }
        }
        list.add(0,nbPoint);
        list.add(1,totBytes);
        return list;
    }

    public String getAPOP(){
        return "<"+this.timestamp.getTime()+"@machine.example>";
    }

    public String getAPOPMD5(String user) throws Exception {
        String pswd = User.getInstance().getPassword(user);
        String str = this.getAPOP()+pswd;
        byte[] checkSum;
        try {
            checkSum = MessageDigest.getInstance("MD5")
                    .digest(str.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new Exception("internal error");
        }
        return new String(checkSum, "UTF-8");
    }
}
