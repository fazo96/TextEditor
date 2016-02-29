package net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import operations.Operation;

/**
 * A network connection over a TCP Socket to exchange Operations.
 *
 * @author fazo
 */
public abstract class Connection implements Runnable, StackProvider {

    protected Socket socket;
    protected ObjectInputStream ois;
    protected ObjectOutputStream oos;
    private boolean running;
    private Thread receiver;
    protected StackProvider sp;
    private ArrayList<NetworkErrorListener> errorHandlers;

    public Connection(StackProvider sp) {
        this.sp = sp;
        errorHandlers = new ArrayList<>();
    }

    /**
     * Starts receiving messages. This method assumes an already open socket and
     * correctly initialized Object Streams.
     * @throws java.lang.Exception if something went wrong when starting the connection
     */
    protected void start() throws Exception {
        System.out.println("NET - Init connection...");
        running = true;
        receiver = new Thread(this);
        receiver.start();
    }

    public void send(String s) {
        if (s == null) {
            System.out.println("Can't send null string");
        } else {
            System.out.println("OUT --> " + s);
            try {
                oos.writeObject(s);
            } catch (IOException ex) {
                // Dead connection
                running = false;
            }
        }
    }

    /**
     * Called when an Update arrives.
     *
     * @param newStack the Update
     * @param isLocalUpdate wether the Update came from a local source (this
     * instance of the application) or the network
     */
    public abstract void update(Operation newStack, boolean isLocalUpdate);

    /**
     * Called when the receiving stream breaks.
     *
     * @param ex the Java Exception related to the break
     */
    protected abstract void onReceiveFail(Exception ex);

    /**
     * Called when a String arrives from the network.
     *
     * @param s the string that arrived
     */
    protected abstract void onReceiveString(String s);

    @Override
    public void run() {
        Object o = null;
        running = true;
        while (running) {
            try {
                o = ois.readObject();
            } catch (Exception ex) {
                onReceiveFail(ex);
            }
            if (!running) {
                // This is in place in case readObject blocked for too long and
                // the condition allowing this loop is not true anymore
                return;
            }
            if (o == null) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (o instanceof String) {
                System.out.println("IN <--" + (String) o);
                onReceiveString((String) o);
            } else {
                System.err.println("Unknown Object received.");
            }
        }
    }

    /**
     * Closes this connection: stops sending and receiving
     */
    public void close() {
        running = false;
        errorHandlers.clear();
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ex) {
                handleError(ex);
                Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public Operation getStack() {
        return sp.getStack();
    }

    public void addErrorListener(NetworkErrorListener l) {
        errorHandlers.add(l);
    }

    public void removeErrorListener(NetworkErrorListener l) {
        errorHandlers.remove(l);
    }

    protected void handleError(Exception ex) {
        errorHandlers.forEach(l -> l.onNetworkError(ex));
    }

    /**
     * @return Wether this connection is estimated to be Online.
     */
    public boolean isOnline() {
        return running;
    }

    protected void setRunning(boolean r) {
        this.running = r;
    }

    public String getAddress() {
        return socket.getInetAddress().getHostName();
    }

    public int getPort() {
        return socket.getPort();
    }
}
