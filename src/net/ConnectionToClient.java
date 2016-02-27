package net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import operations.Operation;

/**
 * A connection to a Client.
 *
 * @author fazo
 */
public class ConnectionToClient extends Connection {

    /**
     * Starts a new connection to a client with the given Socket, and linked to
     * the given Server.
     *
     * @param s the socket to communicate with the client (Must be open
     * already!)
     * @param server the Server that will coordinate this client. It is assumed
     * that the Server already knows about this Client
     */
    public ConnectionToClient(Socket s, Server server) {
        super(server);
        this.socket = s;
    }

    @Override
    public void start() {
        try {
            System.out.println("NET - Init OOS...");
            oos = new ObjectOutputStream(socket.getOutputStream());
            System.out.println("NET - Done Init OOS...");
            System.out.println("NET - Init OIS...");
            ois = new ObjectInputStream(socket.getInputStream());
            System.out.println("NET - Done Init OIS...");
        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }
        super.start();
        sendSync();
    }

    /**
     * Sends a "SYNC" message with the latest version of the document, to resync
     * desynced clients
     */
    public void sendSync() {
        System.out.println("OUT --> (SYNC)");
        if (getStack() == null) {
            System.out.println("SYNC | ");
        } else {
            send("SYNC | " + getStack().evaluate());
        }
    }

    @Override
    public void update(Operation newStack, boolean isLocalUpdate) {
        System.out.println("IN <-- (Stack)");
        getServer().update(newStack, this);
    }

    @Override
    protected void onReceiveFail(Exception ex) {
        System.out.println("A client closed the connection:\n" + ex);
    }

    @Override
    protected void onReceiveString(String s) {
        if (s.startsWith("SYNCREQ")) {
            System.out.println("Received SYNCREQ. Sending SYNC now");
            sendSync();
        } else {
            Operation recv = OperationConverter.read(s, getStack());
            if (recv == null) {
                System.out.println("Could not undestand operation: " + s);
            } else {
                update(recv, false);
            }
        }
    }

    public Server getServer() {
        return (Server) sp;
    }

}
