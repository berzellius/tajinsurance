package com.tajinsurance.exceptions;

/**
 * Created by berz on 13.10.14.
 */
public class IllegalImageDataException extends IllegalDataException {
    public IllegalImageDataException(String s) {
        super(s);
    }

    public IllegalImageDataException(String s, Reason reason){
        super(s, reason);
    }
}
