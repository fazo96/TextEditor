package net;

import gui.DocumentManager;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import operations.Operation;

/**
 *
 * @author fazo
 */
public class Connection implements Runnable {

    private Socket s;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private Operation stack = null;
    private boolean running;
    private Thread receiver;
    private Server server;
    private DocumentManager dm;

    public Connection(String address, int port) {
        try {
            s = new Socket(address, port);
        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            s = null;
            running = false;
            return;
        }
        init();
    }

    public Connection(Socket s, Server server) {
        this.s = s;
        this.server = server;
        stack = server.getStack();
        init();
    }

    public void linkDocumentManager(DocumentManager dm) {
        this.dm = dm;
        if (stack != null) {
            dm.apply(stack);
        }
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
            sendStack();
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
        receiver = new Thread(this);
        receiver.start();
    }

    private void sendStack() {
        if (!isServer()) {
            return;
        }
        System.out.println("NET - Sending Full Stack");
        stack = server.getStack();
        if (stack == null) {
            System.out.println("Can't send null object");
        } else {
            try {
                oos.writeObject(stack);
            } catch (IOException ex) {
                Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void send(String s) {
        if (s == null) {
            System.out.println("Can't send null string");
        } else {
            System.out.println("NET - Writing String to socket:\n" + s);
            try {
                oos.writeObject(s);
            } catch (IOException ex) {
                Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void update(Operation newStack) {
        if (newStack == null) {
            return;
        }
        if (isClient()) {
            if (stack == null) {
                System.out.println("CONN: setting base stack");
                stack = newStack;
                System.out.println("CONN: Sending base stack operation to server...");
                send(OperationConverter.convert(newStack));
            } else if (stack == newStack) {
                System.out.println("CONN: Update wasn't actually an update");
            } else {
                System.out.println("CONN: trying to update stack");
                Operation rebased = newStack.rebaseOn(stack);
                if (rebased == null) {
                    System.out.println("CONN: FATAL: stack update failed");
                } else if (rebased != stack) {
                    System.out.println("CONN: stack update successfull");
                    stack = rebased;
                    System.out.println("CONN: Sending new update to server...");
                    send(OperationConverter.convert(newStack));
                } else {
                    System.out.println("CONN: Update wasn't actually an update");
                }
            }
        } else {
            System.out.println("SERVER: updating stack from client");
            server.update(newStack, this);
            stack = server.getStack();
        }
        if (dm != null) {
            dm.apply(stack);
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
                //Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("A client closed the connection");
                running = false;
            }
            if (o == null) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (o instanceof String) {
                System.out.println("NET - String Incoming:\n" + ((String) o));
                Operation recv = OperationConverter.read((String) o, stack);
                if (recv == null) {
                    System.out.println("Could not undestand operation: " + ((String) o));
                } else {
                    System.out.println("NET - Decoded Operation successfully");
                    update(recv);
                }
            } else if (o instanceof Operation) {
                System.out.println("NET - Received Operation from socket");
                update((Operation) o);
            }
        }
    }

    public boolean isServer() {
        return server != null;
    }

    public boolean isClient() {
        return server == null;
    }
}
