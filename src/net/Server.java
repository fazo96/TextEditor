package net;

import gui.DocumentManager;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import operations.Operation;

/**
 * A Server that is able to accept connections from clients and coordinate
 * multi-user work on a single document
 *
 * @author fazo
 */
public class Server implements Runnable, StackProvider {

    private final int port;
    private boolean stopThread;
    private ServerSocket ss;
    private Thread thread;
    private Operation stack;
    private ArrayList<ConnectionToClient> connections;
    private DocumentManager dm;

    /**
     * Starts a Server on the given port. Displays the current document hosted
     * on this server using the given DocumentManager.
     *
     * @param port the TCP port to listen to
     * @param dm a document manager to show the current document (in read only)
     */
    public Server(int port, DocumentManager dm) {
        connections = new ArrayList<>();
        this.dm = dm;
        this.port = port;
        try {
            ss = new ServerSocket(port);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Error while Hosting Server:\n" + ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Starts listening to clients.
     */
    public void start() {
        stopThread = true;
        if (thread == null) {
            thread = new Thread(this);
        }
        thread.start();
    }

    /**
     * Stops all Server activity and closes all connections.
     */
    public void stop() {
        try {
            ss.close();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        stopThread = true;
        connections.forEach(Connection::close);
    }

    /**
     * Called to update the current server hosted document using a new
     * Operation.
     *
     * @param newStack the operation to update to
     * @return the new server operation stack
     */
    public Operation update(Operation newStack) {
        return update(newStack, null);
    }

    /**
     * Same as the other update, but specify a network source for the update
     */
    public Operation update(Operation newStack, ConnectionToClient source) {
        if (stack == null) {
            stack = newStack;
            if (dm != null) {
                dm.clear();
                dm.apply(stack);
            }
        } else if (stack.isValidUpdate(newStack)) {
            stack = newStack;
            if (dm != null) {
                dm.apply(stack);
            }
        } else {
            // Client sent invalid update!
            System.err.println("Client sent an invalid update!");
            if (newStack.getPrevious() != null) {
                System.out.println("New OP is based on " + newStack.getPrevious().getHash());
            }
            if (stack != null) {
                System.out.println("Stack hash is " + stack.getHash());
            }
            source.sendSync();
        }
        sendToAllExcept(OperationConverter.convert(stack), source);
        return stack;
    }

    @Override
    public Operation getStack() {
        return stack;
    }

    @Override
    public void run() {
        stopThread = false;
        while (!stopThread) {
            try {
                ConnectionToClient c = new ConnectionToClient(ss.accept(), this);
                c.start();
                connections.add(c);
            } catch (Exception ex) {
                if (!stopThread) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
     * Sends a String to all clients except the given one
     *
     * @param s the string to send
     * @param c the exception that will not receive the string or null if there
     * is no exception
     */
    public void sendToAllExcept(String s, ConnectionToClient c) {
        connections.stream()
                .filter((Connection t) -> t != c)
                .forEach(conn -> conn.send(s));
    }

    public int getPort() {
        return port;
    }

    public ArrayList<ConnectionToClient> getConnections() {
        return connections;
    }
}
