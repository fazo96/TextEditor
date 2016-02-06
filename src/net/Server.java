package net;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fazo
 */
public class Server implements Runnable {

    private final int port;
    private boolean stopThread;
    private ServerSocket ss;
    private Thread thread;
    private ArrayList<Connection> connections;

    public Server(int port) {
        connections = new ArrayList<>();
        this.port = port;
        try {
            ss = new ServerSocket(port);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void start() {
        if (thread == null) {
            thread = new Thread(this);
        }
        thread.start();
    }

    public void stop() {
        stopThread = true;
        if (thread != null) {
            thread.interrupt(); // TODO: check effects of this
        }
    }

    @Override
    public void run() {
        stopThread = false;
        while (!stopThread) {
            try {
                connections.add(new Connection(ss.accept()));
            } catch (Exception ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public int getPort() {
        return port;
    }

    public ArrayList<Connection> getConnections() {
        return connections;
    }
}
