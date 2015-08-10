package com.tajinsurance.exceptions;

import com.tajinsurance.dto.PersonSaveAjaxAction;

/**
 * Created by berz on 27.04.14.
 */
public class AgeException extends Exception {

    public AgeException() {
    }

    public AgeException(String msg) {
        super(msg);
    }

    public AgeException(PersonSaveAjaxAction.ErrorCode errorCode){
        super(errorCode.toString());
        this.errorCode = errorCode;
    }

    public PersonSaveAjaxAction.ErrorCode errorCode;
}
