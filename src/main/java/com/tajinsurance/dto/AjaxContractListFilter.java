package com.tajinsurance.dto;

import com.tajinsurance.domain.Partner;
import com.tajinsurance.domain.User;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by berz on 07.04.14.
 */
public class AjaxContractListFilter {

    public Partner partner;

    public User creator;

    public String number;

    public ArrayList<Long> categories;

    public ArrayList<Long> statuses;

    public ArrayList<Long> paymentWays;

    public Date startDateFrom;

    public Date startDateTo;

    public String personMask;

    public Boolean isPaid;

    public Boolean isApp;

    public Boolean isPrinted;

    public String partnerStr;

    public String creatorStr;

    public String orderColumn;

    public String orderType;

    public AjaxContractListFilter() {
    }

    public AjaxContractListFilter(Partner p){
        this.partner = p;
    }

    @Override
    public String toString(){
        String s = "";

        if(partner != null) s += " partner: " + partner.toString() + "; ";

        if(creator != null) s += " creator: " + creator.toString() + "; ";

        return s;
    }

}
