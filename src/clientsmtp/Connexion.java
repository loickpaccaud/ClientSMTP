package ClientSMTP;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import javax.net.SocketFactory;

public class Connexion {
    
    private final Socket socket;
    private final BufferedReader in;
    private final BufferedWriter out;
    private String statut = "CONNECTED";
    
    public Connexion(String target, int port) throws IOException, Exception {
        
        SocketFactory SF = (SocketFactory) SocketFactory.getDefault();
        
        socket = SF.createSocket(InetAddress.getByName(target), port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        
        String greeting = in.readLine();
        System.out.println(greeting);
        
    }

    public void ehlo(String Domain) throws Exception{ 
        if(!statut.equals("CONNECTED"))
            throw new Exception("Connexion impossible");
        
        this.write("EHLO " + Domain);
       
        String reponse = in.readLine();

        if(!reponse.equals("250 OK"))
            throw new Exception(reponse);
        
        statut = "WAIT_EHLO";

    }
    
    public void mail() throws Exception{ 
        String adresse = "";
        this.write("MAIL FROM:<" + adresse+">");
        
        String reponse = in.readLine();

        if (!reponse.equals("250 OK"))
            throw new Exception("Erreur serveur");
        
        statut = "WAIT_RCPT";
    }
    
    public void rcpt(String[] receiver) throws Exception{ 
        for (String adresse : receiver) {
            this.write("RCPT TO:<"+adresse+">");
            
            String reponse = in.readLine();

            if (!reponse.equals("250 OK"))
                System.out.println("No such user : "+adresse);
        }
        
        statut = "WAIT_DATA";
    }

    public void rset() throws Exception{
        this.write("RSET");
        
        String reponse = in.readLine();

        if (!reponse.equals("250 OK"))
            throw new Exception("Erreur serveur");
    }

    public void data(String message) throws Exception{
        this.write("DATA");
        
        String reponse = in.readLine();

        if (!reponse.equals("250 OK"))
            throw new Exception("Erreur serveur");
        else
            this.write(message + ".");
        
        statut = "CONNECTED";
    }

    public void noop() throws Exception {
        this.write("NOOP");
        
        String reponse = in.readLine();

        if (!reponse.equals("250 OK"))
            throw new Exception("Erreur serveur");

    }

    public void quit() throws Exception{
        this.write("QUIT");
        
        String reponse = in.readLine();

        if (!reponse.startsWith("250 OK"))
            throw new Exception("Erreur serveur");

        statut = "CLOSED";
        socket.close();
    }
    
    public void write(String str) throws IOException {
        out.write(str+"\r\n", 0, str.length());
        out.flush();
    }
    
}
