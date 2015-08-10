package com.tajinsurance.dto;

import java.util.ArrayList;

/**
 * Created by berz on 20.05.14.
 */
public class AjaxUserListFilter {
    public AjaxUserListFilter() {
    }

    public String fio;

    public ArrayList<Long> userRoles;

    public String login;

    public String partner;

    public Boolean isEnabled;

    public String orderColumn;

    public String orderType;

    public String email;
}
