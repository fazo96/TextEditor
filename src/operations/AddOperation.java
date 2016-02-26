package operations;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fazo
 */
public class AddOperation extends Operation {

    private final int index;
    private final String text;

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
    public void applyTo(OperationApplier d) {
        try {
            d.insert(index, text);
        } catch (Exception ex) {
            Logger.getLogger(AddOperation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected String applyThis(String base) {
        System.out.println("- ADD\nINDEX: " + index);
        System.out.println("LEN: " + text.length());
        System.out.println("BASE: " + base);
        if (index == base.length()) {
            return base + text;
        }
        String s = base.substring(0, index) + text;
        if (base.length() > index) {
            s += base.substring(index);
        }
        return s;
    }

    @Override
    protected Operation copy() {
        return new AddOperation(index, text, getPrevious());
    }

    public int getIndex() {
        return index;
    }

    public String getText() {
        return text;
    }

    @Override
    public String getName() {
        return "ADD";
    }

}
