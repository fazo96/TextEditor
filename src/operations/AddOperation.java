package operations;


import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.html.HTML;
import sun.awt.geom.AreaOp;

/**
 *
 * @author fazo
 */
public class AddOperation extends Operation {

    private int index;
    private String text;

    /**
     * Creates an insert operation. Inserts the given text at the given index
     *
     * @param from insert text at this index
     * @param text the text to insert
     * @param previous the operation on top of which this one applies
     */
    public AddOperation(int from, String text, Operation previous) {
        super(previous);
        this.index = from;
        this.text = text;
    }

    @Override
    public void applyTo(Document d) {
        try {
            d.insertString(index, text, null);
        } catch (BadLocationException ex) {
            Logger.getLogger(Operation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected String applyThis(String base) {
        return base.substring(0, index) + text + base.substring(index + text.length());
    }

    @Override
    protected Operation doRebaseOn(Operation newBase) {// Do the actual rebase
        if (newBase instanceof NullOperation) return new AddOperation(index, text, newBase);
        if (newBase instanceof AddOperation) {
            AddOperation ao = (AddOperation) newBase;
            if (ao.index <= index) {
                return new AddOperation(index + ao.text.length(), text, newBase);
            }
        } else if (newBase instanceof DelOperation) {
            DelOperation del = (DelOperation) newBase;
            if (del.getStart() < index) { // Start deleting before this
                if (del.getEnd() <= index) { // End deleting before this too
                    return new AddOperation(index - (del.getEnd() - del.getStart()), text, newBase);
                } else {
                    return new AddOperation(del.getStart(), text, newBase);
                }
            } else {
                return new AddOperation(index, text, newBase);
            }
        }
        return null;
    }

    @Override
    protected Operation copy() {
        return new AddOperation(index, text, getPrevious());
    }

    @Override
    protected Operation doMerge(Operation next) {
        if (next instanceof NullOperation) return this.copy();
        if (next instanceof AddOperation) {
            AddOperation n = (AddOperation) next;
            if (n.index >= index && n.index <= index + text.length()) {
                String newText = text.substring(0, n.index - index) + n.text + text.substring(n.index - index);
                return new AddOperation(index, newText, getPrevious());
            }
        } else if (next instanceof DelOperation) {
            DelOperation n = (DelOperation) next;
            if (n.getStart() >= index && n.getEnd() <= index + text.length()) {
                String newText = text.substring(0, n.getStart() - index) + text.substring(n.getEnd() - index);
                return new AddOperation(index, newText, getPrevious());
            }
        }
        return null;
    }

    public int getIndex() {
        return index;
    }

    public String getText() {
        return text;
    }
    
    
}
