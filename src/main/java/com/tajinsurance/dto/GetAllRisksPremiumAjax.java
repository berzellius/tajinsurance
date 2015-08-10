package com.tajinsurance.dto;

import java.math.BigDecimal;

/**
 * Created by berz on 24.04.14.
 */
public class GetAllRisksPremiumAjax extends AjaxAction {
    public BigDecimal sum;
    public BigDecimal insuredSum;

    public BigDecimal minSum;
    public BigDecimal maxSum;
    public Integer minTerm;
}
