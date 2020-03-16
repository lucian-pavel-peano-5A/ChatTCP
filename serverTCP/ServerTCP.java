/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serverTCP;



/**
 * ServerTCP MultiThreaded java Server che attende per richieste di connessioni 
 * da Clients e li gestisce in modo contemporaneo generando un socket "worker" 
 * per ogni connessione.
 * 
 * @author Prof. Matteo Palitto
 */
import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class ServerTCP {
    

    public static void main(String[] args) {

        int portNumber = 1234;
        EventoReceiver receiver = new EventoReceiver();


        try{
            //metto il server in ascolto alla porta desiderata
            ServerSocket server = new ServerSocket(portNumber);
            System.out.println("Server di Testo in esecuzione...  (CTRL-C quits)\n");

            while(true){
                //Socket Worker e' l'oggetto che si occupa di servire il client
                //che si e' connesso, ne verra' generato un Worker per ogni
                //client e verra' eseguito in un suo Thread personale
                SocketWorker w;
                try {
                    //server.accept returns a client connection
                    w = new SocketWorker(server.accept());
                    w.registraReceiver(receiver);
                    //aggiungo il nuovo Worker nella lista dei Workers
                    receiver.addListener(w);
                    //genero il Thread per l'esecuzione del nuovo Worker
                    Thread t = new Thread(w);
                    //Avvio l'esecuzione del nuovo worker nel Thread
                    t.start();
                } catch (IOException e) {
                    System.out.println("Connessione NON riuscita con client: ");
                    System.exit(-1);
                }
            }
        } catch (IOException e) {
            System.out.println("Error! Porta: " + portNumber + " non disponibile");
            System.exit(-1);
        }

        
    }
}

//L'ultimo messaggio ricevuto e' la risorsa comune condivisa tra i vari Threads
// Con questa Classe ricevo l'ultimo messaggio inviato dai Clients
//e richiedo l'invio a tutti i workers di inviare il messaggio al proprio client
class EventoReceiver {

    //ultimo messaggio inviato dai Clients
    private String messaggio;
    //lista dei workers che viengono creati, uno per ogni Client connesso
    private ArrayList<SocketWorker> workers = new ArrayList<>();
    
    //aggiungo il client alla lista
    void addListener(SocketWorker worker) {
        this.workers.add(worker);
    }
    
    //rimuovo il client dalla lista
    void removeListener(SocketWorker worker) {
        this.workers.remove(worker);
    }
    
    //chiamata dai vari Threads quando ricevono un messaggio da client
    //questo metodo e' sycronized per evitare conflitti tra workers
    //che desiderano accedere alla stessa risorsa (cioe' nel caso in cui
    // vengono ricevuti simultaneamente i messaggi da piu' clients)
    synchronized void setNewMessaggio(String m) {
        //aggiorna l'ultimo messaggio
        this.messaggio = m;
        //chiedi ad ogni worker di inviare il messaggio ricevuto
        for (SocketWorker worker: this.workers) {
            worker.sendMessaggio(this.messaggio);
        }
    }
    
}