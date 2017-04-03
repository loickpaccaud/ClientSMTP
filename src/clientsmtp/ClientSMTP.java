package clientsmtp;

import java.io.IOException;
import java.util.Scanner;

public class ClientSMTP {
    

    public static void main(String[] args) throws IOException, Exception {
        
        Connexion co = new Connexion("127.0.0.1", 1100);
        Scanner in = new Scanner(System.in);

        System.out.println("Fonctions : APOP / RETR / STAT / NOOP / QUIT");
        boolean pasclosed = true;
        while (pasclosed) {
            String clavier = in.nextLine();
            String[] tab = clavier.split(" ");

            switch(tab[0].toUpperCase()){
                case "APOP":
                    try{
                        co.apop(tab[1],tab[2]);
                        System.out.println("Connexion établie");
                    } catch(Exception e){
                        System.out.println(e.getMessage());
                    }
                    break;
                case "RETR":
                    try{
                       String mes = co.retr(Integer.parseInt(tab[1]));
                       System.out.println(mes);
                    } catch(Exception e){
                        System.out.println(e.getMessage());
                    }
                    break;
                case "STAT":
                    try{
                        int[] mes = co.stat();
                        System.out.println(""+mes[0]+" "+mes[1]);
                    } catch(Exception e){
                        System.out.println(e.getMessage());
                    }
                    break;
                case "NOOP":
                    try{
                        co.noop();
                        System.out.println("+OK");
                    } catch(Exception e){
                        System.out.println(e.getMessage());
                    }
                    break;
                case "QUIT":
                    try{
                        co.quit();
                        System.out.println("+OK");
                        pasclosed = false;
                    } catch(Exception e){
                        System.out.println(e.getMessage());
                    }
                    break;
                default :
                    System.out.println("Commande pas encore implémentée :(");
                    break;
                    
            }

        }
        System.out.println("Fin de la transaction");
    }
}    

