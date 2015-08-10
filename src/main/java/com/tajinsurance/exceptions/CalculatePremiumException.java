package com.tajinsurance.exceptions;

import java.math.BigDecimal;

/**
 * Created by berz on 29.04.14.
 */
public class CalculatePremiumException extends Exception {

    public BigDecimal maxSum;

    public BigDecimal minSum;

    public Integer minTerm;

    public Reason getReason() {
        return reason;
    }

    public void setReason(Reason reason) {
        this.reason = reason;
    }

    public enum Reason{
        NO_LENGTH, NO_TARIF
    }

    private Reason reason;

    public CalculatePremiumException() {
    }

    public CalculatePremiumException(String s) {
        super(s);
    }

    public CalculatePremiumException(String s, Reason reason){
        super(s);
        setReason(reason);
    }

    public CalculatePremiumException(String s, BigDecimal minSum, BigDecimal maxSum, Integer minTerm){
        super(s);

        this.maxSum = maxSum;
        this.minSum = minSum;
        this.minTerm = minTerm;


    }
}
