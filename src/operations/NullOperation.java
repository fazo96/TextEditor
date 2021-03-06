/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package operations;

/**
 *
 * @author fazo
 */
public class NullOperation extends Operation {

    public NullOperation(Operation previous) {
        super(previous);
    }

    @Override
    protected String applyThis(String base) {
        return base;
    }

    @Override
    public void applyTo(OperationApplier d) {
    }

    @Override
    protected Operation copy() {
        return new NullOperation(getPrevious());
    }

    @Override
    public String getName() {
        return "NULL";
    }

}
