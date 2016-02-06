/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package operations;

import javax.swing.text.Document;

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
    protected void applyTo(Document d) {
    }

    @Override
    protected Operation doRebaseOn(Operation newBase) {
        return new NullOperation(newBase);
    }

    @Override
    protected Operation doMerge(Operation next) {
        return next;
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
