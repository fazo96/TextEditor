package net;

/**
 * Interface used to communicate Network Errors to other Objects
 * @author fazo
 */
public interface NetworkErrorListener {
    public void onNetworkError(Exception ex);
}
