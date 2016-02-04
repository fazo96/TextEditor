
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author fazo
 */
public class DelOperation extends Operation {

    private int from, to;

    /**
     * Creates a delete operation. Deletes all text between the given indexes.
     *
     * @param from start deleting from this index
     * @param to stop deleting at this index
     * @param previous the operation on top of which this one applies
     */
    public DelOperation(int from, int to, Operation previous) {
        this.to = to;
        this.from = from;
    }

    @Override
    public void applyTo(Document d) {
        // Delete
        try {
            d.remove(from, to);
        } catch (BadLocationException ex) {
            Logger.getLogger(Operation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected String applyThis(String base) {
        return base.substring(0, from) + base.substring(to);
    }

    public int getStart() {
        return from;
    }

    public int getEnd() {
        return to;
    }

    @Override
    protected Operation copy() {
        return new DelOperation(from, to, getPrevious());
    }

    @Override
    protected Operation doRebaseOn(Operation newBase) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
