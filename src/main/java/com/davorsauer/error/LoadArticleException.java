package com.davorsauer.error;

/**
 * Created by davor on 06/05/15.
 */
public class LoadArticleException extends Exception {

    public LoadArticleException(String message) {
        super(message);
    }

    public LoadArticleException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoadArticleException(Throwable cause) {
        super(cause);
    }
}
