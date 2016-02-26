package operations;

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

    private final int start, end;

    /**
     * Creates a delete operation. Deletes all text between the given indexes.
     *
     * @param from start deleting from this index
     * @param to stop deleting at this index
     * @param previous the operation on top of which this one applies
     */
    public DelOperation(int from, int to, Operation previous) {
        super(previous);
        this.end = to;
        this.start = from;
    }

    @Override
    public void applyTo(OperationApplier d) {
        // Delete
        try {
            d.remove(start, end);
        } catch (Exception ex) {
            System.err.println("Could not apply DelOperation to OperationApplier:\nOperationApplier is " + d.getText().length() + " chars long\nOperation deletes " + (end - start) + " chars from " + start + "\n" + ex);
        }
    }

    @Override
    protected String applyThis(String base) {
        System.out.println("- DEL\nSTART: " + start + "\nEND:" + end + "\nLEN: " + base.length() + "\nBASE: " + base);
        if (start == 0) {
            if (end == base.length()) {
                return "";
            }
            return base.substring(end);
        }
        if (end == base.length()) {
            return base.substring(0, start);
        }
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
    public String getName() {
        return "DEL";
    }

}
