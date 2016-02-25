package gui;

import operations.Operation;
import operations.AddOperation;
import operations.DelOperation;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import net.Connection;
import net.OperationConverter;

/**
 * The bridge between Java's Document and Operations.
 *
 * It turns document events into Operations.
 *
 * @author fazo
 */
public class DocumentManager implements DocumentListener {

    private Operation latest = null;
    private Document doc;
    private Connection conn;
    private boolean listen = true;

    private void debug() {
        if (latest != null) {
            String s;
            System.out.println("\n---\n");
            System.out.println(s = OperationConverter.convert(latest));
            latest = OperationConverter.read(s, latest.getPrevious());
            System.out.println(OperationConverter.convert(latest));
            System.out.println("RESULT: " + latest.evaluate());
        } else {
            System.out.println("Nothing to debug");
        }
    }

    @Override
    public void insertUpdate(DocumentEvent de) {
        if (!listen) {
            return;
        }
        if (de.getType() == DocumentEvent.EventType.INSERT) {
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
        if (!listen) {
            return;
        }
        if (de.getType() == DocumentEvent.EventType.REMOVE) {
            latest = new DelOperation(de.getOffset(), de.getLength() + de.getOffset(), latest);
            sendViaConnection();
        }
    }

    @Override
    public void changedUpdate(DocumentEvent de) {
        // Java's PlainDocument doesn't emit changedUpdate events
    }

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
        if (o == null) {
            return;
        }
        if (latest != o) {
            listen = false;
            if (latest == null) {
                o.build(doc);
                latest = o;
            } else {
                Operation rebased = o.rebaseOn(latest);
                if (rebased == null) {
                    System.out.println("FATAL: rebase failed");
                } else {
                    try {
                        rebased.apply(doc, latest);
                    } catch (Exception ex) {
                        Logger.getLogger(DocumentManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    latest = rebased;
                }
            }
            listen = true;
        }
    }
    
    public void resetTo(Operation o){
        wipe();
        apply(o);
    }

    public void linkConnection(Connection c) {
        this.conn = c;
    }

    public void unlinkConnection(){
        this.conn = null;
    }
    
    public void wipe() {
        if (latest != null) {
            apply(new DelOperation(0, latest.evaluate().length(), latest));
            latest = null;
        }
    }

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

}
