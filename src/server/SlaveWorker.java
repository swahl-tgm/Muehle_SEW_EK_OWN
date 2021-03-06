package server;

import msg.MessageProtocol;

import javax.naming.SizeLimitExceededException;
import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SlaveWorker implements Runnable {

    private Socket socket;
    private SlaveWorker enm;
    private Server callback;

    private PrintWriter out;
    private BufferedReader in;

    private boolean listening = false;

    private String name;

    // Setter / Getter
    public String getName() {
        return name;
    }


    public SlaveWorker(Server callback, Socket socket, String name) {
        this.callback = callback;
        this.socket = socket;
        this.enm = null;
        this.name = name;
    }

    /**
     * Setzt den Gegner. Einen {@link SlaveWorker} um einfach mit dem anderen Client kommunizieren zu können
     * @param enm ist der andere SlaveWorker
     */
    public void setEnm( SlaveWorker enm) {
        this.enm = enm;
        if ( enm != null) {
            out.println(MessageProtocol.ENMSET);
        }
        else {
            out.println(MessageProtocol.ENMUNSET);
        }
    }

    /**
     * @return Gibt den {@link SlaveWorker} des gegners zurück
     */
    public SlaveWorker getEnm() {
        return this.enm;
    }

    /**
     * Fährt den ServiceWorker herrunter
     */
    private void shutdown() {
        try {
            this.listening = false;
            this.callback.remove(this);
            this.out.close();
            this.in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Sendet eine Nachricht an den Client
     * @param msg Nachricht
     */
    private void send( String msg) {
        this.out.println(msg);
    }

    /**
     * Wird ausgeführt während der SlaveWorker läuft, reagiert auf Befehele und leitet diese an den Client weiter
     */
    @Override
    public void run() {
        try {
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.out = new PrintWriter(this.socket.getOutputStream(), true);

            listening = true;
            while ( listening ) {
                System.out.println("Slave am hören");
                String msg;
                while ((msg = in.readLine()) != null ) {
                    int ind = msg.indexOf(" ");
                    if ( ind == -1 ) {
                        ind = msg.length();
                    }
                    String command = msg.substring(0, ind);
                    System.out.println("Command in slave: " + command + ", whole message: " + msg);
                    switch (command) {
                        case MessageProtocol.EXIT:
                            this.shutdown();
                            break;
                        case MessageProtocol.REMOVE:
                            this.enm.send(msg);
                            break;
                        case MessageProtocol.SETUSED:
                            this.enm.send(msg);
                            break;
                        case MessageProtocol.PLACED:
                            this.enm.send(msg);
                            break;
                        case MessageProtocol.MOVED:
                            this.enm.send(msg);
                            break;
                        case MessageProtocol.SETUNUSED:
                            this.enm.send(msg);
                            break;
                        case MessageProtocol.STARTREMOVE:
                            this.enm.send(msg);
                            break;
                        case MessageProtocol.SETTOBLACK:
                            // auf schwarz setzen
                            this.send(MessageProtocol.SETTOBLACK);
                            break;
                        case MessageProtocol.NAMES:
                            this.enm.send(msg);
                            this.enm.send(MessageProtocol.SETTOBLACK);
                            this.send(MessageProtocol.SETTOWHITE);
                            break;
                        case MessageProtocol.LOSE:
                            this.enm.send(msg);
                            break;
                        case MessageProtocol.WIN:
                            this.enm.send(msg);
                            break;
                    }
                }
            }
        } catch (IOException e) {
            // shutdown?
            //this.shutdown();
        }
    }
}
