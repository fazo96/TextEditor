/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        } else if (o instanceof DelOperation) {
            s += ((DelOperation) o).getStart() + " " + ((DelOperation) o).getEnd() + "|";
        }
        return s;
    }

    public static Operation read(String s, Operation stack) {
        String ss[] = s.split("\\|", 4);
        int basen = Integer.parseInt(ss[0]);
        Operation base = null;
        if (basen == 0) {
            // Base is a null operation
            base = null;
        } else if (stack != null) { // Find base
            base = stack.find(basen);
            if (base == null) {
                // Couldnt find base
                System.err.println("Could not find base");
                return null;
            }
        } else {
            System.err.println("Operation unreadable: missing stack");
            return null;
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

    public void save(Operation o, File f) {
        try {
            PrintWriter writer = new PrintWriter(f, "UTF-8");
            writer.write(o.evaluate());
            writer.close();
        } catch (Exception ex) {
            Logger.getLogger(OperationConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String load(File f) {
        try {
            BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            StringBuilder sb = new StringBuilder();
            bf.lines().forEach((String s) -> sb.append(s).append('\n'));
            return sb.toString();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(OperationConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
