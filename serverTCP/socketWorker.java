/*
 * socketWorker.java ha il compito di gestire la connessione di un Client al Server
 * Elabora il testo ricevuto che in questo caso viene semplicemente inoltrato
 * a tutti i Clients connessi al Server.
 * Questo avviene accedendo alla risorsa comune messa a disposizone dalla classe
 * EventReceiver
 * 
 */
package serverTCP;

import java.net.*;
import java.io.*;

/**
 *
 * @author Prof. Matteo Palitto
 */
class SocketWorker implements Runnable, EventoListener, EventoPublisher {
    private Socket client;
    private PrintWriter out = null;
    EventoReceiver receiver;

    //Constructor: inizializza le variabili
    SocketWorker(Socket client) {
        this.client = client;
        System.out.println("Connesso con: " + client);
    }
    
    @Override
    //Questo metodo e' invocato dal metodo setNewMessaggio nella 
    //classe EventReceiver
    //e rappresenta la richiesta di inviare il messaggio che e' stato appena 
    //ricevuto da uno dei client connessi
    public void sendMessaggio(String messaggio) {
        
        //Invia lo stesso messaggio appena ricevuto 
        out.println("Server->> " + messaggio);
        
    }

    // Questa e' la funzione che viene lanciata quando il nuovo "Thread" viene generato
    public void run(){
        
        BufferedReader in = null;
        try{
          // connessione con il socket per ricevere (in) e mandare(out) il testo
          in = new BufferedReader(new InputStreamReader(client.getInputStream()));
          out = new PrintWriter(client.getOutputStream(), true);
        } catch (IOException e) {
          System.out.println("Errore: in|out fallito");
          System.exit(-1);
        }

        String line = "";
        int clientPort = client.getPort(); //il "nome" del mittente (client)
        while(line != null){
          try{
            line = in.readLine();
            //il nuovo messaggio e' stato ricevuto e lo andiamo ad inserire
            //nella variabile "messaggio" della classe EventReceiver
            //il quale aggiornera' la variabile e richiedera' l'invio a ogni
            //Worker, ognuno al proprio client
            receiver.setNewMessaggio(line);
            //scrivi messaggio ricevuto su terminale
            System.out.println(clientPort + ">> " + line);
           } catch (IOException e) {
            System.out.println("lettura da socket fallito");
            System.exit(-1);
           }
        }
        try {
            client.close();
            System.out.println("connessione con client: " + client + " terminata!");
        } catch (IOException e) {
            System.out.println("Errore connessione con client: " + client);
        }
    }

    @Override
    public void registraReceiver(EventoReceiver r) {
        this.receiver = r;
    }

    @Override
    public void messaggioReceived(String m) {
        this.receiver.setNewMessaggio(m);
    }
}

interface EventoListener {
    public void sendMessaggio(String m);
}

interface EventoPublisher {
    public void registraReceiver(EventoReceiver r);
    public void messaggioReceived(String m);
}