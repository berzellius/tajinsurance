package com.tajinsurance.service;

import com.tajinsurance.domain.Contract;
import com.tajinsurance.domain.InsuranceArea;
import com.tajinsurance.domain.InsuranceObject;
import com.tajinsurance.domain.SecuritySystem;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by berz on 14.09.14.
 */
@Service
public interface InsuranceAreaService {

    public InsuranceArea getInsuranceAreaById(Long id);

    public InsuranceObject getInsuranceObjectById(Long id);

    public SecuritySystem getSecuritySystemById(Long id);

    public void saveInsuranceArea(InsuranceArea insuranceArea);

    public void saveInsuranceObject(InsuranceObject insuranceObject);

    public void addSecuritySystemToArea(SecuritySystem securitySystem, InsuranceArea insuranceArea);

    public void deleteInsuranceArea(InsuranceArea insuranceArea);

    public void deleteInsuranceObject(InsuranceObject insuranceObject);

    public void deleteSecuritySystemFromArea(SecuritySystem securitySystem, InsuranceArea insuranceArea);

    List<InsuranceArea> getInsuranceAreasForContract(Contract c);

    List<SecuritySystem> getSecuritySystemsAvailable(InsuranceArea insuranceArea);

    void updateInsuranceArea(InsuranceArea insuranceArea);

    List<InsuranceObject> getInsuranceObjectsWithoutPremium(Contract contract);

    List<SecuritySystem> getAllSecuritySystems();

    void updateObject(InsuranceObject insuranceObject);
}
