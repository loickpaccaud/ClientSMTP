package clientsmtp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

public class ClientSMTP {
    
    private static final HashMap<String, String> resolver = new HashMap<>();
    
    public static void main(String[] args) throws IOException, Exception {
        
        // Add servers
        resolver.put("gmail.com", "127.0.0.1");
        resolver.put("hotmail.fr", "134.214.116.171");
        resolver.put("nutt.com", "134.214.117.134");
        
        HashMap<String, ArrayList<Message>> toDeliver = new HashMap<>();
        
        Scanner in = new Scanner(System.in);
        
        String line;
        ArrayList<Message> messages = new ArrayList<>();
        
        
        boolean working = true;
        while (working){
            println("Ajouter un message ? (Y/N)");
            line = in.next();
            switch (line.toUpperCase()) {
                case "Y":
                    readMessage(in, toDeliver);
                    break;
                case "N":
                    working = false;
                    break;
                default:
                    println("Réponse invalide");
                    break;
            }
        }
        println("Envoi des messages");
        toDeliver.forEach((ip, emails) -> {
            try {
                Connexion co = new Connexion(ip, 25);
                co.sendMessages(emails);
                
                Iterator<Exception> errors = co.getErrors();
                if(!errors.hasNext()){
                    println("Aucune erreur");
                }else{
                    while (errors.hasNext())
                        println(errors.next().getMessage());
                }
            } catch (Exception ex) {
                println(String.format("Impossible de contacter le serveur à l'adresse \"%s\"", ip));
            }
        });
        
    }
    
    public static void readMessage(Scanner in, HashMap<String, ArrayList<Message>> messages) {
        HashMap<String, ArrayList<String>> targetsByIp = new HashMap<>();
        String content = "";
        String line;
        boolean continu = true;
        do {
            println("Ajouter un destinataire (entrer un . pour passer à la suite)");
            line = in.next();
            if (line.equals("."))
                continu = false;
            else if (line.matches(".+@.+")){
                String server = line.substring(line.indexOf("@") + 1);
                String ip = resolver.get(server);
                if (ip != null){
                    if (!targetsByIp.containsKey(ip)){
                        targetsByIp.put(ip, new ArrayList<>());
                    }
                    targetsByIp.get(ip).add(line);
                }else{
                    println("Impossible d'associer cet email à un serveur");
                }
            }
        }while (continu);
        
        println("Entrez votre message (terminer par un .)");
        continu = true;
        do {
            line = in.nextLine();
            if (line.equals("."))
                continu = false;
            else
                content += "\r\n" + line;
        }while (continu || content.length() < 2);
        
        final String finalContent = content;
        
        targetsByIp.forEach((String ip, ArrayList<String> targets) ->{
            if (!messages.containsKey(ip))
                messages.put(ip, new ArrayList<Message>());
            messages.get(ip).add(new Message("utilisateur@domaine.fr", targets, finalContent));
        });
    }
    
    
    public static void println(Object o){
        System.out.println(o);
    }
    public static void print(Object o){
        System.out.print(o);
    }
}    

