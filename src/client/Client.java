package client;

import client.GUI.ClientController;
import javafx.application.Platform;
import msg.MessageProtocol;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class Client implements Runnable {

    private String name;
    private String host;
    private int port;
    private Socket socket;
    private ClientController c;

    private BufferedReader in;
    private PrintWriter out;

    private boolean listening;

    public Client( String host, Integer port, ClientController c) throws IOException {
        this.host = Objects.requireNonNullElse(host, "localhost");
        this.port = Objects.requireNonNullElse(port, 5050);
        this.c = c;
        this.name = "";
        socket = new Socket(this.host, this.port);
        listening = false;
    }

    /**
     * Setzt den Namen des Clients
     * @param name ist der Name
     */
    public void setName(String name ) {
        this.name = name;
        this.c.setName(name);
    }

    /**
     * Fährt den Client herrunter
     */
    public void shutdown() {
        if ( listening ) {
            try {
                listening = false;
                out.println(MessageProtocol.EXIT);
                out.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sendet eine Message an der Server
     * @param msg ist die Nachricht
     */
    public void send( String msg) {
        this.out.println(msg);
    }


    /**
     * Wird ausgeführt wenn der Client läuft
     */
    @Override
    public void run() {
        listening = true;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(socket.getOutputStream(), true);

            String msg;
            while ( (msg = in.readLine()) != null )  {
                int ind = msg.indexOf(" ");
                if ( ind == -1 ) {
                    ind = msg.length();
                }
                System.out.println("msg: " + msg);
                String command = msg.substring(0, ind);
                System.out.println("Command client: " + command);
                int x, y;
                switch (command) {
                    case MessageProtocol.EXIT:
                        this.shutdown();
                        break;
                    case MessageProtocol.ENMSET:
                        this.c.foundEnm();
                        this.out.println(MessageProtocol.NAMES + " " + this.c.getName());
                        break;
                    case MessageProtocol.ENMUNSET:
                        this.c.enmDisconnected();
                        break;
                    case MessageProtocol.STARTREMOVE:
                        this.c.startRemove();
                        break;
                    case MessageProtocol.REMOVE:
                        x = Integer.parseInt(msg.substring(msg.indexOf("x")+2, msg.indexOf(",")));
                        y = Integer.parseInt(msg.substring(msg.indexOf("y")+2));
                        this.c.removeTile(x,y);
                        break;
                    case MessageProtocol.SETUSED:
                        x = Integer.parseInt(msg.substring(msg.indexOf("x")+2, msg.indexOf(",")));
                        y = Integer.parseInt(msg.substring(msg.indexOf("y")+2));
                        this.c.setEnmUsed(x,y);
                        break;
                    case MessageProtocol.SETUNUSED:
                        x = Integer.parseInt(msg.substring(msg.indexOf("x")+2, msg.indexOf(",")));
                        y = Integer.parseInt(msg.substring(msg.indexOf("y")+2));
                        this.c.setEnmUnused(x,y);
                        break;
                    case MessageProtocol.PLACED:
                        x = Integer.parseInt(msg.substring(msg.indexOf("x")+2, msg.indexOf(",")));
                        y = Integer.parseInt(msg.substring(msg.indexOf("y")+2));
                        this.c.setEnmStein(x,y);
                        break;
                    case MessageProtocol.MOVED:
                        int startX, startY;
                        try {
                            startX = Integer.parseInt(msg.substring(msg.indexOf("sx")+3, msg.indexOf(",")));
                            msg = msg.substring(msg.indexOf(",")+1);
                            startY = Integer.parseInt(msg.substring(msg.indexOf("sy")+3, msg.indexOf(",")));
                            msg = msg.substring(msg.indexOf(",")+1);
                            x = Integer.parseInt(msg.substring(msg.indexOf("x")+2, msg.indexOf(",")));
                            y = Integer.parseInt(msg.substring(msg.indexOf("y")+2));
                            this.c.moveEnmStein(startX, startY, x, y);
                        }
                        catch ( Exception ex ) {
                            ex.printStackTrace();
                        }
                        break;
                    case MessageProtocol.NAMES:
                        this.c.setEnmName(msg.substring(msg.indexOf(" ")+1));
                        break;
                    case MessageProtocol.SETTOBLACK:
                        this.c.setToBlack();
                        break;
                    case MessageProtocol.SETTOWHITE:
                        this.c.setToWhite();
                        break;
                    case MessageProtocol.LOSE:
                        this.c.setLose();
                        break;
                }
            }
        } catch (IOException e) {
            // Server disconnected
            System.out.println("Server disconnected");
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    c.srvDisconnected();
                }
            });
        }
    }
}
