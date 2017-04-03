package ClientSMTP;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class Connexion {
    
    private final SSLSocket ss;
    private final BufferedReader in;
    private final BufferedWriter out;
    private String statut = "AUTHENTIFICATION";
    private String timestamp;

    
    public Connexion(String target, int port) throws IOException, Exception {
        SSLSocketFactory SSF = (SSLSocketFactory) SSLSocketFactory.getDefault();
        ss = (SSLSocket) SSF.createSocket(InetAddress.getByName(target), port);
        
        String[] allCipherSuites = ss.getSupportedCipherSuites();
        
        //On met les CipherSuites sans certificat dans un String[]
        ArrayList<String> anonCipherSuites = new ArrayList<>();
        for(String cipherSuites : allCipherSuites){
            if(cipherSuites.contains("anon"))
                anonCipherSuites.add(cipherSuites);
        }
        String[] cipherSuites = new String[anonCipherSuites.size()];
        cipherSuites = anonCipherSuites.toArray(cipherSuites);
        
        ss.setEnabledCipherSuites(cipherSuites);
        
        in = new BufferedReader(new InputStreamReader(ss.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(ss.getOutputStream()));
        
        String greeting = in.readLine();
        System.out.println(greeting);
        Matcher timestampMatcher = Pattern.compile("(<.+>)").matcher(greeting);
        if (timestampMatcher.find())
            timestamp = timestampMatcher.group();
        else
            throw new Exception("Can't find timestamp");
    }

    public void apop(String userName, String key) throws Exception{
        if(!statut.equals("AUTHENTIFICATION"))
            throw new Exception("Connexion impossible");
        
        
        
        out.write("APOP " + userName + " " + md5((timestamp + key).getBytes()) + "\r\n");
        out.flush();
        
        String reponse = in.readLine();

        if(!reponse.equals("+OK"))
            throw new Exception(reponse);

        statut = "TRANSACTION";
       
    }

    public String retr(Integer id) throws Exception{
        if(!statut.equals("TRANSACTION"))
            throw new Exception("Commande impossible");

        
        String message = "";
        
        out.write("RETR "+id+"\r\n");
        out.flush();
        String reponse = in.readLine();

        if(!reponse.startsWith("+OK "))
            throw new Exception(reponse); 

         String tmp = "";
         while (!(tmp = in.readLine()).equals("."))
             message += tmp+"\r\n";

        return message;
    }

    public int[] stat() throws Exception{
        if(!statut.equals("TRANSACTION"))
            throw new Exception("Commande impossible");

        
        String[] tab;
        
        out.write("STAT\r\n");
        out.flush();
        String reponse = in.readLine();

        tab = reponse.split(" ");

        if(!reponse.startsWith("+OK "))
            throw new Exception(reponse);
        
        return new int[]{Integer.parseInt(tab[1]),Integer.parseInt(tab[2])};
    }

    public void noop() throws Exception {
        if (!statut.equals("TRANSACTION"))
            throw new Exception("Commande impossible");

        out.write("NOOP\r\n");
        out.flush();
        String reponse = in.readLine();

        if (!reponse.equals("+OK"))
            throw new Exception("Erreur serveur");

    }

    public void quit() throws Exception{
        out.write("QUIT\r\n");
        out.flush();
        String reponse = in.readLine();

        if (!reponse.startsWith("+OK"))
            throw new Exception("Erreur serveur");

        statut = "CLOSED";
        ss.close();
    }
    
    private static String md5(byte[] raw) throws NoSuchAlgorithmException{
        
        byte[] digest = MessageDigest.getInstance("MD5").digest(raw);
        StringBuilder builder = new StringBuilder();
        for (byte b: digest){
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

}
