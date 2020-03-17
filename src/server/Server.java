package server;

import java.lang.invoke.SerializedLambda;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private Integer port = 5050;
    private ServerSocket serverSocket;

    private boolean lis = true;

    private ExecutorService executorService = Executors.newCachedThreadPool();
    private ConcurrentHashMap<String, SlaveWorker> userList = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Boolean> userMatchedList = new ConcurrentHashMap<>();

    /**
     * @param port Port über den der Server läuft (default 5050)
     */
    public Server( Integer port ) {
        if (port != null) this.port = port;

        this.listen();
    }

    /**
     * Entfernt einen {@link SlaveWorker} aus der Liste {@link #userList} sowie {@link #userMatchedList}
     * @param worker ist der Worker der entfernt wird
     */
    public void remove( SlaveWorker worker ) {
        SlaveWorker enm = worker.getEnm();
        userList.remove(worker.getName());
        userMatchedList.remove(worker.getName());

        // set enm to nothing
        enm.setEnm(null);
        userMatchedList.remove(enm.getName());
        userMatchedList.put(enm.getName(), false);
    }


    /**
     * "Run" Methode des Server, hört dauerhaft nach ankommenten Clients
     */
    public void listen() {
        try {
            serverSocket = new ServerSocket(port);
            int slaveCounter = 0;

            while ( this.lis ) {
                Socket clientSocket = serverSocket.accept();
                SlaveWorker client = new SlaveWorker(this, clientSocket, "Client" + slaveCounter);
                executorService.submit(client);
                userList.put("Client" + slaveCounter, client);
                userMatchedList.put("Client" + slaveCounter, false);
                // Gegner setzen
                if ( slaveCounter > 0 ) {
                    matchSlaveWorker(client);
                }
                System.out.println("New Slave added: Client"+slaveCounter);
                slaveCounter++;
            }
        }
        catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }

    /**
     * Matched zwei Clients zusammen um ein SPiel zu beginnen
     * @param toMatch ist der SlaveWorker der mit einem anderen gematched wird
     * @return ture wenn erforgleich, false wenn nicht
     */
    synchronized private boolean matchSlaveWorker(SlaveWorker toMatch) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Enumeration<String> key = this.userMatchedList.keys();

        boolean out = false;
        String name;
        SlaveWorker sw;
        while ( key.hasMoreElements() ) {
            name = key.nextElement();
            if ( !name.equals(toMatch.getName()) ) {
                if ( !this.userMatchedList.get(name) ) {
                    // take this slaveworker
                    sw = this.userList.get(name);
                    toMatch.setEnm(sw);
                    sw.setEnm(toMatch);

                    System.out.println("Matched " +name + " with " + toMatch.getName() );

                    this.userMatchedList.remove(name);
                    this.userMatchedList.put(name, true);

                    this.userMatchedList.remove(toMatch.getName());
                    this.userMatchedList.put(toMatch.getName(), true);
                    return true;
                }

            }
        }

        return false;

    }


    public static void main(String[] args) {
        new Server(null);
    }
}
