/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net;

import operations.AddOperation;
import operations.DelOperation;
import operations.NullOperation;
import operations.Operation;

/**
 * Converts Operations to and from String.
 *
 * Java's Serialization isn't used because in this case, the fingerprint of the
 * previous operation is used instead of serializing all the stack
 *
 * @author fazo
 */
public class OperationConverter {

    public static String convert(Operation o) {
        int c;
        if (o.getPrevious() != null) {
            c = o.getPrevious().checksum();
        } else {
            c = 0;
        }
        String s = c + "|" + o.getName() + '|';
        if (o instanceof AddOperation) {
            s += ((AddOperation) o).getIndex() + "|" + ((AddOperation) o).getText();
        }
        if (o instanceof DelOperation) {
            s += ((DelOperation) o).getStart() + " " + ((DelOperation) o).getEnd() + "|";
        }
        return s;
    }

    public static Operation read(String s, Operation stack) {
        String ss[] = s.split("\\|", 4);
        int basen = Integer.parseInt(ss[0]);
        Operation base;
        if (basen == 0) {
            // Base is a null operation
            base = null;
        } else { // Find base
            base = stack.find(basen);
            if (base == null) {
                // Couldnt find base
                System.err.println("Could not find base");
                return null;
            }
        }
        switch (ss[1]) {
            case "ADD":
                int index = Integer.parseInt(ss[2]);
                return new AddOperation(index, ss[3], base);
            case "DEL":
                String sss[] = ss[2].split(" ");
                if (sss.length != 2) {
                    System.err.println("Invalid parameter count: " + s);
                    return null;
                }
                return new DelOperation(Integer.parseInt(sss[0]), Integer.parseInt(sss[1]), base);
            case "NULL":
                return new NullOperation(base);
        }
        System.err.println("Unknown Operation \"" + ss[2] + "\"");
        return null;
    }
}
