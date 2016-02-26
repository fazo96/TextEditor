package net;

import gui.DocumentManager;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import operations.AddOperation;
import operations.Operation;

/**
 *
 * @author fazo
 */
public class Connection implements Runnable {

    private Socket s;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private boolean running;
    private Thread receiver;
    private Server server;
    private DocumentManager dm;

    public Connection(String address, int port, DocumentManager dm) {
        this.dm = dm;
        try {
            s = new Socket(address, port);
        } catch (IOException ex) {
            System.err.println("Connection Failed: " + ex);
            s = null;
            running = false;
            return;
        }
        init();
    }

    public Connection(Socket s, Server server) {
        this.s = s;
        this.server = server;
        init();
    }

    private void init() {
        System.out.println("NET - Init connection...");
        running = false;
        if (isServer()) {
            try {
                System.out.println("NET - Init OOS...");
                oos = new ObjectOutputStream(s.getOutputStream());
                System.out.println("NET - Done Init OOS...");
                System.out.println("NET - Init OIS...");
                ois = new ObjectInputStream(s.getInputStream());
                System.out.println("NET - Done Init OIS...");
            } catch (IOException ex) {
                Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            }
            sendSync();
        } else {
            try {
                System.out.println("NET - Init OIS...");
                ois = new ObjectInputStream(s.getInputStream());
                System.out.println("NET - Done Init OIS...");
                System.out.println("NET - Init OOS...");
                oos = new ObjectOutputStream(s.getOutputStream());
                System.out.println("NET - Done Init OOS...");
            } catch (IOException ex) {
                Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        running = true;
        receiver = new Thread(this);
        receiver.start();
    }

    public void sendSync() {
        System.out.println("OUT --> (SYNC)");
        if (getStack() == null) {
            System.out.println("SYNC | ");
        } else {
            send("SYNC | " + getStack().evaluate());
        }
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

    public void update(Operation newStack, boolean isLocalUpdate) {
        if (newStack == null) {
            return;
        }
        if (isClient()) {
            System.out.println("Processing " + (isLocalUpdate ? "local" : "remote") + " update:");
            if (isLocalUpdate) {
                send(OperationConverter.convert(newStack));
            }
        } else {
            System.out.println("IN <-- (Stack)");
            server.update(newStack, this);
        }
        if (dm != null) {
            dm.apply(newStack);
        }
    }

    @Override
    public void run() {
        Object o = null;
        running = true;
        while (running) {
            try {
                o = ois.readObject();
            } catch (Exception ex) {
                if (isServer()) {
                    System.out.println("A client closed the connection");
                } else {
                    running = false;
                    JOptionPane.showMessageDialog(null, "Connection to the server closed:\n" + ex, "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            if (!running) {
                return;
            }
            if (o == null) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (o instanceof String) {
                String s = (String) o;
                System.out.println("IN <--" + s);
                if (s.startsWith("SYNCREQ")) {
                    sendSync();
                } else if (s.startsWith("SYNC | ")) {
                    // TODO: Handle Sync
                    if (isServer()) {
                        System.err.println("Received SYNC on server. This should not happen");
                    } else {
                        dm.resetTo(new AddOperation(0, s.substring("SYNC | ".length()), null));
                    }
                } else {
                    Operation recv = OperationConverter.read(s, getStack());
                    if (recv == null) {
                        System.out.println("Could not undestand operation: " + s);
                    } else {
                        System.out.println("NET - Decoded Operation successfully");
                        update(recv, false);
                    }
                }
            } else if (o instanceof Operation) {
                System.out.println("IN <-- Operation");
                System.err.println("Receiving Operation instances is not supported anymore. This should not happen");
                // update((Operation) o, false);
            }
        }
    }

    public void close() {
        running = false;
        try {
            s.close();
        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Operation getStack() {
        return isServer() ? server.getStack() : dm.getStack();
    }

    public boolean isServer() {
        return server != null;
    }

    public boolean isClient() {
        return server == null;
    }

    public boolean isOnline() {
        return running;
    }

    public String getAddress() {
        return s.getInetAddress().getHostName();
    }

    public int getPort() {
        return s.getPort();
    }
}
