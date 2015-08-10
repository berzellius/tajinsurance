package com.tajinsurance.service;

import com.tajinsurance.domain.CatContractStatus;

import java.util.List;

/**
 * Created by berz on 22.03.14.
 */
public interface CatContractStatusService {
    List<CatContractStatus> getAllCatContractStatuses(String locale);

    CatContractStatus getCatContractStatusById(Long id);

    CatContractStatus getCatContractStatusByCode(CatContractStatus.StatusCode code);
}
