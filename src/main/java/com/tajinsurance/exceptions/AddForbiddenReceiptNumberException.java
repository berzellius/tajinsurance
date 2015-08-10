package com.tajinsurance.exceptions;

/**
 * Created by berz on 28.07.14.
 */
public class AddForbiddenReceiptNumberException extends Exception {

    public AddForbiddenReceiptNumberException(){

    }

    public AddForbiddenReceiptNumberException(String msg){
        super(msg);
    }

    public AddForbiddenReceiptNumberException(Reason reason){
        this.reason = reason;
    }

    public Reason reason;

    public enum Reason{
        EXISTS,
        USED
    }

}
