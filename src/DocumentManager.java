
import operations.Operation;
import operations.AddOperation;
import operations.DelOperation;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
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
    private Document d;

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
        if (de.getType() == DocumentEvent.EventType.INSERT) {
            String what = "";
            try {
                what = de.getDocument().getText(de.getOffset(), de.getLength());
            } catch (BadLocationException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
            }
            latest = new AddOperation(de.getOffset(), what, latest);
        }
        debug();
    }

    @Override
    public void removeUpdate(DocumentEvent de) {
        if (de.getType() == DocumentEvent.EventType.REMOVE) {
            latest = new DelOperation(de.getOffset(), de.getLength() + de.getOffset(), latest);
        }
        debug();
    }

    @Override
    public void changedUpdate(DocumentEvent de) {
        // Java's PlainDocument doesn't emit changedUpdate events
    }

    public DocumentManager(Document d) {
        this.d = d;
        d.addDocumentListener(this);
    }

    public void apply(Operation o) {
        // Todo implement this
        // make sure user doesn't touch document while operations are being applied
    }

    public Operation getLatest() {
        return latest;
    }

}
