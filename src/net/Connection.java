package net;

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
public class Connection {

    private final Socket s;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    public Connection(Socket s) {
        this.s = s;
        try {
            ois = new ObjectInputStream(s.getInputStream());
            oos = new ObjectOutputStream(s.getOutputStream());
        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void send(Operation o) {

    }

    private void send(String s) {

    }
}
