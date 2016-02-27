package net;

import gui.Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.stream.Collectors;
import operations.AddOperation;
import operations.DelOperation;
import operations.NullOperation;
import operations.Operation;

/**
 * String to/from Operation convertion tools and File I/O tools for Operations.
 *
 * Java's Serialization isn't used because in this case, the fingerprint of the
 * previous operation is used instead of serializing every operation in a stack
 *
 * @author fazo
 */
public class OperationConverter {

    /**
     * Converts a single operation to a String representation.
     *
     * @param o the operation to convert
     * @return a string representation of the given operation
     */
    public static String convert(Operation o) {
        String hash;
        if (o.getPrevious() != null) {
            hash = o.getPrevious().getHash();
        } else {
            hash = Operation.getNullhash();
        }
        String s = hash + "|" + o.getName() + '|';
        if (o instanceof AddOperation) {
            s += ((AddOperation) o).getIndex() + "|" + ((AddOperation) o).getText();
        } else if (o instanceof DelOperation) {
            s += ((DelOperation) o).getStart() + " " + ((DelOperation) o).getEnd() + "|";
        }
        return s;
    }

    /**
     * reads an operation in string representation to turn it into an Operation
     * instance.
     *
     * @param s the string to parse
     * @param stack the stack to look in to find the base for the operation in
     * string form
     * @return the resulting Operation or null if the base of the operation was
     * not found in the stack
     */
    public static Operation read(String s, Operation stack) {
        String ss[] = s.split("\\|", 4);
        String  bases = ss[0].trim();
        Operation base = null;
        if (bases.equals(Operation.getNullhash())) {
            // Base is a null operation
            base = null;
        } else if (stack != null) { // Find base
            base = stack.find(bases);
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

    /**
     * Saves the result of a given Operation to a given File in text format.
     *
     * @param o the operation to save
     * @param f the destination file
     * @throws java.lang.Exception if the operation was not successfull
     */
    public static void save(Operation o, File f) throws Exception {
        try {
            PrintWriter writer = new PrintWriter(f, "UTF-8");
            writer.write(o.evaluate());
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            throw ex;
        }
    }

    /**
     * Reads a text File and returns the contents as a String.
     *
     * @param f the file to read
     * @return the string contained in the file
     * @throws java.io.FileNotFoundException if the operation was not
     * successfull
     */
    public static String load(File f) throws FileNotFoundException {
        try {
            BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            return bf.lines().collect(Collectors.joining("\n"));
        } catch (FileNotFoundException ex) {
            throw ex;
        }
    }
}
