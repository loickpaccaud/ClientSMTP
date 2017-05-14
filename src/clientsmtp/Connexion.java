package clientsmtp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
    
    private Iterator<Message> messagesIterator;
    private Message currentMessage;
    private Iterator<String> targetsIterator;
    private String currentTarget;
    private int validTargets = 0;
    
    public Connexion(String target, int port) throws IOException, Exception {
        
        SocketFactory SF = (SocketFactory) SocketFactory.getDefault();
        
        socket = new Socket(InetAddress.getByName(target), port);
        //socket.setSoTimeout(2000);
        // socket = SF.createSocket(InetAddress.getByName(target), port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        errors = new ArrayList<>();
        
        String greeting = in.readLine();
    }

    public Iterator<Exception> getErrors(){
        return errors.iterator();
    }

    public void sendMessages(List<Message> messages){
        String line;

        messagesIterator = messages.iterator();

        errors.clear();

        try {
            write("EHLO");
            statut = WAIT_EHLO;
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
                        statut = CLOSED;
                        break;
                    default:
                        errors.add(new Exception(String.format("Message serveur : \"%s\" (%s)", line, "CLOSED")));
                        statut = CLOSED;
                }
                break;
            case CONNECTED:
                switch (messageStatut){
                    case 220:
                        write("EHLO");
                        statut = WAIT_EHLO;
                        break;
                    default:
                        errors.add(new Exception(String.format("Message serveur : \"%s\" (%s)", line, "CONNECTED")));
                        statut = CLOSED;
                }
                break;
            case WAIT_EHLO:
                switch (messageStatut){
                    case 250:
                        currentMessage = messagesIterator.next();
                        targetsIterator = currentMessage.getTargets().iterator();
                        currentTarget = null;
                        write("MAIL FROM:<" + currentMessage.getSource() + ">");
                        
                        statut = WAIT_FROM;
                        break;
                    default:
                        errors.add(new Exception(String.format("Message serveur : \"%s\" (%s)", line, "WAIT_EHLO")));
                        statut = CLOSED;
                }
                break;
            case WAIT_FROM:
                switch (messageStatut){
                    case 250:
                        if (! targetsIterator.hasNext()){
                            write("RSET");
                            statut = WAIT_RSET;
                        }
                        currentTarget = targetsIterator.next();
                        write("RCPT TO:<" + currentTarget+">");
                        
                        validTargets = 0;
                        statut = WAIT_RCPT;
                        break;
                    default:
                        errors.add(new Exception(String.format("Message serveur : \"%s\" (%s)", line, "WAIT_FROM")));
                        statut = CLOSED;
                }
                break;
            case WAIT_RSET:
                switch (messageStatut){
                    case 250:
                        if (messagesIterator.hasNext()){
                            currentMessage = messagesIterator.next();
                            targetsIterator = currentMessage.getTargets().iterator();
                            currentTarget = null;
                            write("MAIL FROM:<" + currentMessage.getSource() + ">");
                            
                            statut = WAIT_FROM;
                        }else{
                            write("QUIT");
                            
                            statut = CLOSED;
                        }
                        break;
                    default:
                        errors.add(new Exception(String.format("Message serveur : \"%s\" (%s)", line, "WAIT_RSET")));
                        statut = CLOSED;
                }
                break;
            case WAIT_RCPT:
                switch (messageStatut){
                    case 250:
                        validTargets++;
                        if (targetsIterator.hasNext()){
                            currentTarget = targetsIterator.next();
                            write("RCPT TO:<" + currentTarget + ">");
                        }else{
                            write("DATA");
                            
                            statut = WAIT_DATA;
                        }
                        break;
                    case 550:
                    case 553:
                    case 501:
                    case 450:
                        errors.add(new Exception(String.format("Message serveur (mauvais destinataire) : \"%s\" (%s)", line, "WAIT_RSET")));
  
                        if (targetsIterator.hasNext()){
                            currentTarget = targetsIterator.next();
                            write("RCPT TO:<" + currentTarget + ">");
                        }else if (validTargets > 0){
                            write("DATA");
                            
                            statut = WAIT_DATA;
                        }else{
                            write("RSET");
                            
                            errors.add(new Exception("Aucun destinataire valide, abandon du message"));  
                            statut = WAIT_RSET;
                        }
                        break;
                    default:
                        errors.add(new Exception(String.format("Message serveur : \"%s\" (%s)", line, "WAIT_RCPT")));
                        statut = CLOSED;
                }
                break;
            case WAIT_DATA:
                switch (messageStatut){
                    case 354:
                        String[] content = currentMessage.getContent().split("\r\n");
                        for (String messageLine : content){
                            write(messageLine);
                        }
                        write(".");
                        
                        statut = WAIT_DATA_VALID;
                        break;
                    default:
                        errors.add(new Exception(String.format("Message serveur : \"%s\" (%s)", line, "WAIT_DATA")));
                        statut = CLOSED;
                }
                break;
            case WAIT_DATA_VALID:
                switch (messageStatut){
                    case 250:
                        if (messagesIterator.hasNext()){
                            currentMessage = messagesIterator.next();
                            targetsIterator = currentMessage.getTargets().iterator();
                            currentTarget = null;
                            write("MAIL FROM:<" + currentMessage.getSource() + ">");
                            
                            statut = WAIT_FROM;
                        }else{
                            write("QUIT");
                            
                            statut = CLOSED;
                        }
                        break;
                    default:
                        errors.add(new Exception(String.format("Message serveur : \"%s\" (%s)", line, "WAIT_DATA_VALID")));
                        statut = CLOSED;
                }
                break;
            default:
                throw new Exception(String.format("État incohérent : \"%s\" (%s)", statut));
        }
    }






    private void write(String str) throws IOException {
        out.write(str+"\r\n");
        out.flush();
    }

    private int getStatut(String line) {
        /*Matcher m = Pattern.compile("^\\d+").matcher(line);
        if (m.matches()){
            return Integer.parseInt(m.group());
        }
        return -1;*/
        String[] mes = line.split(" ");
        try{
            return Integer.parseInt(mes[0]);
        }catch(Exception e){
            return -1;
        }
    }

    private boolean isOk(String str){
        return str.length() == 3 && str.equals("250")
                || str.length() > 3 && str.substring(0, 2).equals("250") && str.charAt(3) == ' ';
    }
    
}
