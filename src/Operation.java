
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * A transformation applied to an empty string or to another transformation
 *
 * @author fazo
 */
public class Operation {

    private Operation previous; // The operation on top of which this one is applied
    private int from = -1, to = -1; // start and end of deletion
    private String text, cache; // Text to insert and cache to store result

    /**
     * Creates a delete operation. Deletes all text between the given indexes.
     *
     * @param from start deleting from this index
     * @param to stop deleting at this index
     * @param previous the operation on top of which this one applies
     */
    public Operation(int from, int to, Operation previous) {
        this.to = to;
        this.from = from;
        this.text = null;
        this.previous = previous;
    }

    /**
     * Creates an insert operation. Inserts the given text at the given index
     *
     * @param from insert text at this index
     * @param text the text to insert
     * @param previous the operation on top of which this one applies
     */
    public Operation(int from, String text, Operation previous) {
        this(from, from, previous);
        this.text = text;
    }

    /**
     * Evaluates the text corresponding to this operation.
     *
     * @return the resulting text.
     */
    public String evaluate() {
        String base;
        if (previous != null) {
            base = previous.evaluate();
        } else {
            base = "";
        }
        base = applyThis(base);
        cache = base;
        return base;
    }

    /**
     * Takes an empty document and builds the text into it.
     *
     * @param d an empty document
     */
    public void build(Document d) {
        if (previous != null) {
            previous.build(d);
        }
        applyTo(d);
        try {
            cache = d.getText(0, d.getLength());
        } catch (BadLocationException ex) {
            Logger.getLogger(Operation.class.getName()).log(Level.SEVERE, null, ex);
            cache = null;
        }
    }

    private String applyThis(String base) {
        if (from == to) {
            // Insert
            base = base.substring(0, from) + text + base.substring(from);
        } else {
            // Delete
            base = base.substring(0, from) + base.substring(to);
        }
        return base;
    }

    /**
     * Applies this operation to the given document.
     *
     * WARNING: only applies this operation!
     *
     * @param d the document on which to apply this operation
     */
    public void applyTo(Document d) {
        if (from == to) {
            // Insert
            try {
                d.insertString(from, text, null);
            } catch (BadLocationException ex) {
                Logger.getLogger(Operation.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            // Delete
            try {
                d.remove(from, to);
            } catch (BadLocationException ex) {
                Logger.getLogger(Operation.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
