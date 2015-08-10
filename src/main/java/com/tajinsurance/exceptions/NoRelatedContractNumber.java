package com.tajinsurance.exceptions;

/**
 * Created by berz on 22.05.14.
 */
public class NoRelatedContractNumber  extends Exception {

    public NoRelatedContractNumber() {
    }

    public  NoRelatedContractNumber(String s){
        super(s);
    }
}
