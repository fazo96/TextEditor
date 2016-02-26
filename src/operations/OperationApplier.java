/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package operations;

/**
 * An interface that is used to apply Operations to custom structures
 *
 * @author fazo
 */
public interface OperationApplier {

    /**
     * Called by operations when they want to insert a string at a given offset
     * into the document
     *
     * @param offset the offset in characters, with 0 being the start of the
     * document
     * @param s the string to insert
     * @throws Exception when the offset is not valid
     */
    public void insert(int offset, String s) throws Exception;

    /**
     * Called by operations to delete all text between two offsets
     *
     * @param from the character number from where to start deleting
     * @param to the character number from where to stop deleting
     * @throws Exception if the offsets are not right
     */
    public void remove(int from, int to) throws Exception;

    /**
     * Called to wipe the document clear, turning it into an empty string
     */
    public void clear();

    /**
     * Should return the string representing the current state of the document
     *
     * @return a string
     */
    public String getText();
}
