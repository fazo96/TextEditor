package gui;

import operations.Operation;
import operations.AddOperation;
import operations.DelOperation;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import net.Connection;
import net.OperationConverter;
import net.StackProvider;
import operations.OperationApplier;

/**
 * The bridge between Java's Document and Operations.
 *
 * It turns document events into Operations.
 *
 * @author fazo
 */
public class DocumentManager implements DocumentListener, OperationApplier, StackProvider {

    private Operation latest = null;
    private Document doc;
    private Connection conn;
    private boolean listen = true;

    // Start document listener implementation
    
    @Override
    public void insertUpdate(DocumentEvent de) {
        if (listen && de.getType() == DocumentEvent.EventType.INSERT) {
            String what = "";
            try {
                what = de.getDocument().getText(de.getOffset(), de.getLength());
            } catch (BadLocationException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
            }
            latest = new AddOperation(de.getOffset(), what, latest);
            sendViaConnection();
        }
    }

    @Override
    public void removeUpdate(DocumentEvent de) {
        if (listen && de.getType() == DocumentEvent.EventType.REMOVE) {
            latest = new DelOperation(de.getOffset(), de.getLength() + de.getOffset(), latest);
            sendViaConnection();
        }
    }

    @Override
    public void changedUpdate(DocumentEvent de) {
        // Java's PlainDocument (used in text areas) doesn't emit changedUpdate events
    }
    
    // End document listener implementation

    private void sendViaConnection() {
        if (conn != null) {
            conn.update(latest, true);
        }
    }

    public DocumentManager(Document doc) {
        this.doc = doc;
        doc.addDocumentListener(this);
    }

    public void apply(Operation o) {
        if (o != null && latest != o) {
            listen = false;
            if (latest == null) {
                o.build(this);
                latest = o;
            } else if (latest.isValidUpdate(o)) {
                try {
                    o.apply(this, latest);
                    latest = o;
                } catch (Exception ex) {
                    Logger.getLogger(DocumentManager.class.getName()).log(Level.SEVERE, null, ex);
                    //JOptionPane.showMessageDialog(null, "Error when applying operation to document:\n" + ex, "Error", JOptionPane.ERROR_MESSAGE);
                    // Rebuilding document
                    wipe();
                    o.applyTo(this);
                    latest = o;
                }
            } else {
                JOptionPane.showMessageDialog(null, "Error when applying operation to document:\nUpdate was not valid", "Error", JOptionPane.ERROR_MESSAGE);
            }
            listen = true;
        }
    }

    public void resetTo(Operation o) {
        wipe();
        apply(o);
    }

    public void linkConnection(Connection c) {
        this.conn = c;
    }

    public void unlinkConnection() {
        this.conn = null;
    }

    public void wipe() {
        if (latest != null) {
            apply(new DelOperation(0, latest.evaluate().length(), latest));
            latest = null;
        }
    }

    @Override
    public Operation getStack() {
        return latest;
    }

    public Connection getLinkedConnection() {
        return conn;
    }

    public boolean isListening() {
        return listen;
    }

    public void setListen(boolean listen) {
        this.listen = listen;
    }

    // OperationApplier implementation
    @Override
    public void insert(int offset, String s) throws Exception {
        doc.insertString(offset, s, null);
    }

    @Override
    public void remove(int from, int to) throws Exception {
        doc.remove(from, to - from);
    }

    @Override
    public void clear() {
        try {
            doc.remove(0, doc.getLength());
        } catch (BadLocationException ex) {
            // Should never happen
            Logger.getLogger(DocumentManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getText() {
        try {
            return doc.getText(0, doc.getLength());
        } catch (BadLocationException ex) {
            // Should never happen
            Logger.getLogger(DocumentManager.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

}
