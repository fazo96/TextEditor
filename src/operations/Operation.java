package operations;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * A transformation applied to an empty string or to another transformation
 *
 * @author fazo
 */
public abstract class Operation implements Serializable {

    private Operation previous; // The operation on top of which this one is applied
    private String cache; // cache to store result

    /**
     * Evaluates the text corresponding to this operation.
     *
     * @return the resulting text.
     */
    public String evaluate() {
        if (cache != null) {
            return cache;
        } else {
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
    }

    /**
     * Takes an empty document and builds the text into it.
     *
     * @param d an empty document
     */
    public void build(Document d) {
        if (cache != null) {
            try {
                d.remove(0, d.getLength());
                d.insertString(0, cache, null);
            } catch (BadLocationException ex) {
                Logger.getLogger(Operation.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
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

    }

    protected Operation() {
    }

    protected Operation(Operation previous) {
        this.previous = previous;
    }

    /**
     * Applies this operation to the given String.
     *
     * WARNING: only applies this operation!
     *
     * @param base the string on top of which to apply this
     * @return the result
     */
    protected abstract String applyThis(String base);

    /**
     * Applies this operation to the given document.
     *
     * WARNING: only applies this operation!
     *
     * @param d the document on which to apply this operation
     */
    protected abstract void applyTo(Document d);

    protected Operation rebaseOn(Operation newBase, Operation origNewBase) {
        if (newBase.getPrevious() == null && getPrevious() != null) {
            // Travel back my history
            return this.copy().rebaseOn(getPrevious().rebaseOn(newBase));
        } else if (newBase.getPrevious() != null && getPrevious() != null && newBase.getPrevious() != getPrevious()) {
            // Travel back newbase's history
            return this.copy().rebaseOn(newBase.getPrevious(), newBase).rebaseOn(newBase);
        } else {
            return doRebaseOn(newBase);
        }
    }

    public Operation rebaseOn(Operation newBase) {
        return this.rebaseOn(newBase, newBase);
    }

    protected abstract Operation doRebaseOn(Operation newBase);

    /**
     * Returns an operation that combines two of them if possible, and null
     * otherwise
     *
     * @param next the operation to merge with
     * @return an operation equivalent to this one and the next one, or null if
     * they can't be merged
     */
    public Operation merge(Operation next) {
        if (next.getPrevious() != this) {
            // Maybe shouldn't do this... but oh well
            next = next.rebaseOn(this);
            if (next == null) {
                return null;
            }
        }
        return doMerge(next);
    }

    protected abstract Operation doMerge(Operation next);

    /**
     * An identical copy of this Operation but which maintains the same
     * reference to its previous.
     *
     * @return a copy
     */
    protected abstract Operation copy();

    public int checksum() {
        return evaluate().hashCode();
    }

    public Operation find(int checksum) {
        if (checksum() == checksum) {
            return this;
        }
        if (previous == null) {
            return null;
        }
        return previous.find(checksum);
    }

    public Operation getPrevious() {
        return previous;
    }

    public void setPrevious(Operation previous) {
        this.previous = previous;
    }

    public abstract String getName();

}
