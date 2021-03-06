package net;

import gui.DocumentManager;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private ArrayList<NetworkErrorListener> errorHandlers;

    /**
     * Starts a Server on the given port. Displays the current document hosted
     * on this server using the given DocumentManager.
     *
     * @param port the TCP port to listen to
     * @param dm a document manager to show the current document (in read only)
     */
    public Server(int port, DocumentManager dm) {
        connections = new ArrayList<>();
        errorHandlers = new ArrayList<>();
        this.dm = dm;
        this.port = port;
        stopThread = false;
    }

    /**
     * Starts listening to clients.
     */
    public void start() {
        try {
            ss = new ServerSocket(port);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            stopThread = true;
            handleError(ex);
        }
        if (thread == null) {
            thread = new Thread(this);
        }
        thread.start();
    }

    /**
     * Stops all Server activity and closes all connections.
     */
    public void stop() {
        if (ss != null) {
            try {
                ss.close();
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
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

            synchronized (stack) {
                stack = newStack;
            }
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

    public void setStack(Operation s) {
        synchronized (stack) {
            stack = s;
        }
    }

    @Override
    public void run() {
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

    public void addErrorListener(NetworkErrorListener l) {
        errorHandlers.add(l);
    }

    public void removeErrorListener(NetworkErrorListener l) {
        errorHandlers.remove(l);
    }

    private void handleError(Exception ex) {
        errorHandlers.forEach(l -> l.onNetworkError(ex));
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

    public void sendSync() {
        connections.forEach(ConnectionToClient::sendSync);
    }

    public int getPort() {
        return port;
    }

    public ArrayList<ConnectionToClient> getConnections() {
        return connections;
    }
}
