/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
public class ConnectionToServer extends Connection {

    public ConnectionToServer(String address, int port, DocumentManager dm) {
        super(dm);
        try {
            socket = new Socket(address, port);
        } catch (IOException ex) {
            System.err.println("Connection Failed: " + ex);
            socket = null;
        }
    }

    @Override
    public void start() {
        try {
            System.out.println("NET - Init OIS...");
            ois = new ObjectInputStream(socket.getInputStream());
            System.out.println("NET - Done Init OIS...");
            System.out.println("NET - Init OOS...");
            oos = new ObjectOutputStream(socket.getOutputStream());
            System.out.println("NET - Done Init OOS...");
        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        super.start();
    }

    @Override
    public void update(Operation newStack, boolean isLocalUpdate) {
        if (newStack == null) {
            return;
        }
        if (isLocalUpdate) {
            send(OperationConverter.convert(newStack));
        }
        getDM().apply(newStack);
    }

    @Override
    protected void onReceiveFail(Exception ex) {
        JOptionPane.showMessageDialog(null, "Connection to the server closed:\n" + ex, "Error", JOptionPane.ERROR_MESSAGE);
        close();
    }

    @Override
    protected void onReceiveString(String s) {
        if (s.startsWith("SYNC | ")) {
            // TODO: Handle Sync
            /*if (isServer()) {
                System.err.println("Received SYNC on server. This should not happen.");
            } else {*/
            System.out.println("Received SYNC. Syncing document");
            getDM().resetTo(new AddOperation(0, s.substring("SYNC | ".length()), null));
        } else {
            Operation recv = OperationConverter.read(s, getStack());
            if (recv == null) {
                System.out.println("Could not undestand operation: " + s);
                send("SYNCREQ");
            } else {
                update(recv, false);
            }
        }
    }
    
    private DocumentManager getDM(){
        return (DocumentManager) sp;
    }
}
