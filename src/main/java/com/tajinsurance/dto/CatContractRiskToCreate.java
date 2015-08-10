package com.tajinsurance.dto;

import com.tajinsurance.domain.CatContract;
import com.tajinsurance.domain.Partner;

/**
 * Created by berz on 23.04.14.
 */
public class CatContractRiskToCreate {
    public Partner partner;
    public Long pid;
    public CatContract catContract;
    public Long ccid;

    public CatContractRiskToCreate() {
    }

    public CatContractRiskToCreate(Partner p, CatContract cc) {
        this.partner = p;
        this.pid = p.getId();
        this.catContract = cc;
        this.ccid = cc.getId();
    }

    public Long getPid(){
        return this.pid;
    }

    public Long getCcid(){
        return this.ccid;
    }

    @Override
    public String toString(){
        return partner.toString() + " -> " + catContract.toString();
    }
}
