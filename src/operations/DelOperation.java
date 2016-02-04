package operations;

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

    private int start, end;

    /**
     * Creates a delete operation. Deletes all text between the given indexes.
     *
     * @param from start deleting from this index
     * @param to stop deleting at this index
     * @param previous the operation on top of which this one applies
     */
    public DelOperation(int from, int to, Operation previous) {
        this.end = to;
        this.start = from;
    }

    @Override
    public void applyTo(Document d) {
        // Delete
        try {
            d.remove(start, end);
        } catch (BadLocationException ex) {
            Logger.getLogger(Operation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected String applyThis(String base) {
        return base.substring(0, start) + base.substring(end);
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    @Override
    protected Operation copy() {
        return new DelOperation(start, end, getPrevious());
    }

    @Override
    protected Operation doRebaseOn(Operation newBase) {
        if (newBase instanceof NullOperation) {
            return new DelOperation(start, end, newBase);
        }
        if (newBase instanceof AddOperation) {
            AddOperation ao = (AddOperation) newBase;
            if (ao.getIndex() < start) {
                return new DelOperation(start + ao.getText().length(), end + ao.getText().length(), newBase);
            } else if (ao.getIndex() < end) {
                return new NullOperation(newBase);
            } else {
                return new DelOperation(start, end, newBase);
            }
        } else if (newBase instanceof DelOperation) {
            // TODO: review/REWRITE this incredible mess
            DelOperation del = (DelOperation) newBase;
            if (del.getStart() <= start) {
                if (del.getEnd() < end) {
                    return new DelOperation(start - (del.getEnd() - del.getStart()), end - (del.getEnd() - del.getStart()), newBase);
                } else if (del.getEnd() > end) {
                    return new NullOperation(newBase);
                } else {
                    return new DelOperation(del.getEnd(), end - (del.getEnd() - del.getStart()), newBase);
                }
            } else if (del.getStart() < end) {
                return new DelOperation(start - (del.getEnd() - del.getStart()), end - (del.getEnd() - del.getStart()), newBase);
            } else {
                return new NullOperation(newBase);
            }
        }
        return null;
    }

    @Override
    protected Operation doMerge(Operation next) {
        if (next instanceof NullOperation) {
            return this.copy();
        }
        // TODO: implement this
        return null;
    }

}
