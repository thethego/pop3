package ServerPop3;

/**
 * Created by theo on 20/03/17.
 */

import java.io.*;

import org.jdom2.*;
import org.jdom2.input.*;

import java.util.HashMap;
import java.util.List;
import java.util.Iterator;

public class User {
    private org.jdom2.Document document;
    private Element racine;
    private HashMap<String,String> listUsers;
    private static User instance = null;

    private User() {
        SAXBuilder sxb = new SAXBuilder();
        try {
            document = sxb.build(new File("src/ServerPop3/users/users.xml"));
        } catch (Exception e) {
            System.out.println("error : "+e.getMessage());
        }
        racine = document.getRootElement();
        listUsers = new HashMap<>();
                List<Element> listUsersXMl = this.racine.getChildren("user");
        Iterator i = listUsersXMl.iterator();
        while(i.hasNext()) {
            Element curent = (Element)i.next();
            listUsers.put(curent.getChild("username").getText(),
                    curent.getChild("password").getText());
        }
    }

    public static User getInstance() {
        if(instance == null){
            instance = new User();
        }
        return instance;
    }

    public HashMap<String,String> getClients() {
        return this.listUsers;
    }

    public boolean isUser(String user){
        return this.getClients().get(user) != null;
    }

    public String getPassword(String user) {
        return this.getClients().get(user);
    }
}
