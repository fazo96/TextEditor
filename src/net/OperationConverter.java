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
import java.io.UnsupportedEncodingException;
import java.util.stream.Collectors;
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
     * @throws java.io.FileNotFoundException if the operation was not successfull
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
