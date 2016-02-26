/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

/**
 * Utility functions
 * @author fazo
 */
public class Utils {
/**
 * Parses a port number written in a string
 * @param port the string to parse
 * @return the equivalent integer
 * @throws Exception if the string isn't a valid port number
 */
    public static int parsePort(String port) throws Exception {
        int p;
        try {
            p = Integer.parseInt(port.trim());
        } catch (Exception ex) {
            throw ex;
        }
        if (p < 1 || p > 65535) {
            throw new Exception("Invalid port number");
        }
        return p;
    }
    
    public static int defaultPort(){
        return 1245;
    }
}
