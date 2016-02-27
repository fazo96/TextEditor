package operations;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A transformation applied to an empty string or to another transformation
 *
 * @author fazo
 */
public abstract class Operation implements Serializable {

    private static String nullhash;
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
                previous.clearCache();
            } else {
                base = "";
            }
            base = applyThis(base);
            cache = base;
            return base;
        }
    }

    /**
     * Builds the result text of this operation using the given OperationApplier
     *
     * @param d a new operation applier
     */
    public void build(OperationApplier d) {
        if (cache != null) {
            d.clear();
            try {
                d.insert(0, cache);
            } catch (Exception ex) {
                Logger.getLogger(Operation.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            if (previous != null) {
                previous.build(d);
                previous.clearCache();
            }
            applyTo(d);
            cache = d.getText();
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
    public abstract void applyTo(OperationApplier d);

    /**
     * Applies all Operations that are after the lastApplied argument.
     *
     * @param d the OperationApplier to apply to
     * @param lastApplied the last applied operation to the document
     * @throws Exception if lastApplied is not in the operation stack
     */
    public void apply(OperationApplier d, Operation lastApplied) throws Exception {
        String las = lastApplied.getHash();
        if (las.equals(getHash())) {
            return;
        }
        if (las.equals(getHashOfPrevious())) {
            applyTo(d);
        } else if (previous != null) {
            previous.apply(d, lastApplied);
            applyTo(d);
        } else {
            throw new Exception("Can't apply this operation. Please rebuild document as an alternative");
        }
    }

    /**
     * An identical copy of this Operation but which maintains the same
     * reference to its previous.
     *
     * @return a copy
     */
    protected abstract Operation copy();

    /**
     * Returns a code that uniquely identifies this operation by its result
     *
     * TODO: Reimplement this using MD5, SHA or something!
     *
     * @return a checksum
     */
    public String getHash() {
        String s = evaluate();
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(s.getBytes("UTF-8"));
            return javax.xml.bind.DatatypeConverter.printHexBinary(bytes);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Operation.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Operation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public boolean isValidUpdate(Operation o) {
        return o.getHashOfPrevious().equals(getHash());
    }

    public Operation find(String hash) {
        if (getHash().equals(hash)) {
            return this;
        }
        if (previous == null) {
            return null;
        }
        return previous.find(hash);
    }

    /**
     * Wether this operation has the given operation as ancestor or is the given
     * operation.
     *
     * @param o the operation to check
     * @return true or false
     */
    public boolean contains(Operation o) {
        if (o == null) {
            return true;
        }
        if (o == this) {
            return true;
        }
        if (previous == null) {
            return false;
        }
        return previous.contains(o);
    }

    /**
     * Clears the cache. Used internally to save memory when evaluating long
     * lists of operations
     */
    private void clearCache() {
        cache = null;
    }

    public String getHashOfPrevious() {
        if (previous != null) {
            return previous.getHash();
        } else {
            return getNullhash();
        }
    }

    public Operation getPrevious() {
        return previous;
    }

    public void setPrevious(Operation previous) {
        this.previous = previous;
    }

    public abstract String getName();

    /**
     *
     * @return the hash representing an operation resulting to a null string
     */
    public static String getNullhash() {
        if (nullhash == null) {
            nullhash = new NullOperation(null).getHash();
        }
        return nullhash;
    }

}
