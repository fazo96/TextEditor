package net;

import operations.Operation;

/**
 * An Object able to provide an operation stack
 *
 * @author fazo
 */
public interface StackProvider {

    public Operation getStack();
}
