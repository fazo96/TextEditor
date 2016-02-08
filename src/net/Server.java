package net;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import operations.Operation;

/**
 *
 * @author fazo
 */
public class Server implements Runnable {

    private final int port;
    private boolean stopThread;
    private ServerSocket ss;
    private Thread thread;
    private Operation stack;
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
    
    public Operation update(Operation newStack){
        return update(newStack, null);
    }

    public Operation update(Operation newStack, Connection source) {
        if (stack == null) {
            stack = newStack;
        } else {
            Operation rebased = newStack.rebaseOn(stack);
            if (rebased == null) {
                System.out.println("FATAL: update failed");
            } else {
                System.out.println("NET - successfully updated stack");
                stack = rebased;
            }
        }
        sendToAllExcept(OperationConverter.convert(stack), source);
        return stack;
    }

    public Operation getStack() {
        return stack;
    }

    @Override
    public void run() {
        stopThread = false;
        while (!stopThread) {
            try {
                connections.add(new Connection(ss.accept(), this));
            } catch (Exception ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            ss.close();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendToAllExcept(String s, Connection c) {
        connections.stream()
                .filter((Connection t) -> t != c)
                .forEach(conn -> conn.send(s));
    }

    public int getPort() {
        return port;
    }

    public ArrayList<Connection> getConnections() {
        return connections;
    }
}
