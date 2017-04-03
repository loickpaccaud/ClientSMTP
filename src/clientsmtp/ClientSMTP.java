package clientsmtp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class ClientSMTP {
    
    public static void main(String[] args) throws IOException, Exception {
        
        
        Connexion co = new Connexion("127.0.0.1", 1100);
        Scanner in = new Scanner(System.in);
        
        String line;
        ArrayList<Message> messages = new ArrayList<>();
        
        
        boolean working = true;
        while (working){
            println("Ajouter un message ? (Y/N)");
            line = in.next();
            switch (line) {
                case "Y":
                    messages.add(readMessage(in));
                    break;
                case "N":
                    working = false;
                    break;
                default:
                    println("Réponse invalide");
                    break;
            }
        }
        print("Termine");
        // co.sendMessages(messages);
    }
    
    public static Message readMessage(Scanner in) {
        ArrayList<String> targets = new ArrayList<>();
        String content = "";
        String line;
        boolean continu = true;
        do {
            println("Ajouter un destinataire (entrer pour passer à la suite)");
            line = in.next();
            if (line.isEmpty())
                continu = false;
            else
                targets.add(line);
        }while (continu);
        
        println("Entrez votre message (terminer par un .)");
        continu = true;
        do {
            line = in.next();
            if (line.equals("."))
                continu = false;
            else
                content += "\r\n" + line;
        }while (continu || content.length() < 2);
        
        return new Message("utilisateur", targets, content.substring(2));
    }
    
    
    public static void println(Object o){
        System.out.println(o);
    }
    public static void print(Object o){
        System.out.print(o);
    }
}    

