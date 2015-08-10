package com.tajinsurance.exceptions;

/**
 * Created by berz on 08.10.14.
 */
public class IllegalDataException extends Exception {

    public void setReason(Reason reason) {
        this.reason = reason;
    }

    public enum Reason{

        WRONG_EMAIL("wrong_email"),
        FRANCHISE_WRONG_PERCENT("wrong_fr_percent"),
        FRANCHISE_DISCOUNT_WRONG("wrong_fr_discount"),

        WRONG_IMAGE_FORMAT("wrong_image_format"),
        WRONG_FILE_SIZE("wrong_file_size"),
        FRANCHISE_WRONG_MONEY("wrong_fr_money");

        private String msgCode;

        Reason(String msgCode){
            this.msgCode = msgCode;
        }

        public String getMsgCode(){
            return this.msgCode;
        }
    }

    private Reason reason;

    public Reason getReason(){
        return this.reason;
    }

    public IllegalDataException(String s) {
        super(s);
    }

    public IllegalDataException(String s, Reason r){
        super(s);
        this.setReason(r);
    }




}
