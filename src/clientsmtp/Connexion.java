package clientsmtp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.SocketFactory;

public class Connexion {

    private static final int CLOSED = 0;
    private static final int CONNECTED = 1;
    private static final int WAIT_EHLO = 2;
    private static final int WAIT_FROM = 3;
    private static final int WAIT_RSET = 4;
    private static final int WAIT_RCPT = 5;
    private static final int WAIT_DATA = 6;
    private static final int WAIT_DATA_VALID = 7;


    private final Socket socket;
    private final BufferedReader in;
    private final BufferedWriter out;
    private int statut = CONNECTED;
    private List<Exception> errors;
    
    public Connexion(String target, int port) throws IOException, Exception {
        
        SocketFactory SF = (SocketFactory) SocketFactory.getDefault();
        
        socket = SF.createSocket(InetAddress.getByName(target), port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        errors = new ArrayList<>();
        
        String greeting = in.readLine();
        System.out.println(greeting);
    }

    public Exception[] getErrors(){
        return (Exception[]) errors.toArray();
    }

    public void send_messages(List<Message> messages){
        String line;


        errors.clear();

        try {
            write("EHLO");
            while (statut != CLOSED){
                line = in.readLine();
                handleMessage(line);
            }
        }catch(Exception e){
            errors.add(e);
        }

    }

    private void handleMessage(String line) throws Exception {
        int messageStatut = getStatut(line);
        switch (statut){
            case CLOSED:
                switch (messageStatut){
                    case 250:
                        // @todo
                        break;
                    default:
                        errors.add(new Exception(String.format("Message serveur : \"%s\"", line)));
                        // @todo
                }
                break;
            case CONNECTED:
                switch (messageStatut){
                    case 250:
                        // @todo
                        break;
                    default:
                        errors.add(new Exception(String.format("Message serveur : \"%s\"", line)));
                        // @todo
                }
                break;
            case WAIT_EHLO:
                switch (messageStatut){
                    case 250:
                        // @todo
                        break;
                    default:
                        errors.add(new Exception(String.format("Message serveur : \"%s\"", line)));
                        // @todo
                }
                break;
            case WAIT_FROM:
                switch (messageStatut){
                    case 250:
                        // @todo
                        break;
                    default:
                        errors.add(new Exception(String.format("Message serveur : \"%s\"", line)));
                        // @todo
                }
                break;
            case WAIT_RSET:
                switch (messageStatut){
                    case 250:
                        // @todo
                        break;
                    default:
                        errors.add(new Exception(String.format("Message serveur : \"%s\"", line)));
                        // @todo
                }
                break;
            case WAIT_RCPT:
                switch (messageStatut){
                    case 250:
                        // @todo
                        break;
                    default:
                        errors.add(new Exception(String.format("Message serveur : \"%s\"", line)));
                        // @todo
                }
                break;
            case WAIT_DATA:
                switch (messageStatut){
                    case 250:
                        // @todo
                        break;
                    default:
                        errors.add(new Exception(String.format("Message serveur : \"%s\"", line)));
                        // @todo
                }
                break;
            case WAIT_DATA_VALID:
                switch (messageStatut){
                    case 250:
                        // @todo
                        break;
                    default:
                        errors.add(new Exception(String.format("Message serveur : \"%s\"", line)));
                        // @todo
                }
                break;
            default:
                throw new Exception(String.format("État incohérent : \"%s\"", statut));
        }
    }


    public void ehlo(String Domain) throws Exception{ 
        if(statut != CONNECTED)
            throw new Exception("Connexion impossible");
        
        this.write("EHLO " + Domain);
       
        String reponse = in.readLine();

        if(!reponse.equals("250 OK"))
            throw new Exception(reponse);
        
        statut = WAIT_EHLO;

    }
    
    public void mail() throws Exception{ 
        String adresse = "";
        this.write("MAIL FROM:<" + adresse+">");
        
        String reponse = in.readLine();

        if (!reponse.equals("250 OK"))
            throw new Exception("Erreur serveur");
        
        statut = WAIT_RCPT;
    }
    
    public void rcpt(String[] receiver) throws Exception{ 
        for (String adresse : receiver) {
            this.write("RCPT TO:<"+adresse+">");
            
            String reponse = in.readLine();

            if (!reponse.equals("250 OK"))
                System.out.println("No such user : "+adresse);
        }
        
        statut = WAIT_DATA;
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
        
        statut = CONNECTED;
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

        statut = CLOSED;
        socket.close();
    }





    private void write(String str) throws IOException {
        out.write(str+"\r\n", 0, str.length());
        out.flush();
    }

    private int getStatut(String line) {
        Matcher m = Pattern.compile("^\\d+").matcher(line);
        if (m.matches()){
            return Integer.parseInt(m.group());
        }
        return -1;
    }

    private boolean isOk(String str){
        return str.length() == 3 && str.equals("250")
                || str.length() > 3 && str.substring(0, 2).equals("250") && str.charAt(3) == ' ';
    }
    
}
