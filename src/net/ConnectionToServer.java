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
import operations.AddOperation;
import operations.Operation;

/**
 *
 * @author fazo
 */
public class ConnectionToServer extends Connection {

    private final String address;
    private final int port;

    public ConnectionToServer(String address, int port, DocumentManager dm) {
        super(dm);
        this.address = address;
        this.port = port;
    }

    @Override
    public void start() {
        try {
            socket = new Socket(address, port);
        } catch (IOException ex) {
            System.err.println("Connection Failed: " + ex);
            socket = null;
        }
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
            // Update is local
            send(OperationConverter.convert(newStack));
        } else if ((getStack() == null && !newStack.hasDependencies()) || getStack().isValidUpdate(newStack)) {
            // Update is from network and valid
            getDM().apply(newStack);
        } else {
            // Update is from network but not valid
            System.out.println("Received invalid update. Sending SYNCREQ");
            sendSyncRequest();
        }
    }

    @Override
    protected void onReceiveFail(Exception ex) {
        handleError(ex);
    }

    @Override
    protected void onReceiveString(String s) {
        if (s.startsWith("SYNC | ")) {
            System.out.println("Received SYNC. Syncing document");
            getDM().resetTo(new AddOperation(0, s.substring("SYNC | ".length()), null));
        } else {
            Operation recv = OperationConverter.read(s, getStack());
            if (recv == null) {
                System.out.println("Could not undestand operation: " + s);
                sendSyncRequest();
            } else {
                update(recv, false);
            }
        }
    }

    private void sendSyncRequest() {
        send("SYNCREQ");
    }

    private DocumentManager getDM() {
        return (DocumentManager) sp;
    }
}
