package com.tajinsurance.dto;

/**
 * Created by berz on 25.03.14.
 */
public class PersonSaveAjaxAction extends AjaxAction {
    public PersonContentObject person;

    public Boolean ageProblem;


    public enum ErrorCode{
        AGE_PROBLEM_18, FILL_ALL
    }

    public ErrorCode errorCode;

}
