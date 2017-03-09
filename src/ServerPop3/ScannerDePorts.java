package ServerPop3;
import java.net.*;

/**
 * Created by p1303175 on 06/03/2017.
 */
public class ScannerDePorts {

    public static void scan(int min, int max){
        for(int i=min;i<max;i++){
            try{
                DatagramSocket cli = new DatagramSocket(i);
                cli.close();

                // return i;
            }
            catch (SocketException ex) {
                System.out.println(i);
            }
        }
        //return -1;
    }
}
