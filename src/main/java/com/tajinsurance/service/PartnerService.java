package com.tajinsurance.service;

import com.tajinsurance.domain.*;
import com.tajinsurance.dto.AjaxPartnerListFilter;
import com.tajinsurance.exceptions.BadNewPartnerDataException;
import com.tajinsurance.exceptions.EntityNotSavedException;
import com.tajinsurance.exceptions.IllegalDataException;
import org.springframework.stereotype.Service;

import javax.persistence.NoResultException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by berz on 24.03.14.
 */
@Service
public interface PartnerService {

    public static enum CalculateConstants{

        SECURITY_SYSTEM_DELTA (BigDecimal.valueOf(0.05));


        private BigDecimal value;

        CalculateConstants(BigDecimal value){
            this.value = value;
        }

        public BigDecimal getValue(){
            return this.value;
        }

    }

    List<PartnerRiskPolicy> getExistsPolicy(CatContract catContract, Partner partner);

    List<Risk> getRisksNotSetPolicy(CatContract catContract, Partner partner);

    void delPartnerRiskPolicy(Long policyId);

    void addPartnerRiskPolicy(PartnerRiskPolicy partnerRiskPolicy);

    PartnerRiskCorrelation getPartnerRiskCorrelationByPartnerAndRisk(Partner partner, Risk risk, CatContract catContract);

    PartnerRiskCorrelation getPartnerRiskCorrelationById(Long corrId);

    void deletePartnerRiskCorrelation(Long corrId);

    void addCorrelationForPartnerCatContractRisk(Long partnerId, Long catContractId, Long riskId, BigDecimal correlation, String extraInfo);

    List<Partner> getPartnersLocalizated(String locale);

    List<Partner> getPartners();

    void savePartner(Partner partner);

    Partner getPartnerById(Long id);

    void validateNewPartner(Partner partner) throws BadNewPartnerDataException;

    void update(Partner partner) throws BadNewPartnerDataException;

    void remove(Partner partner);

    List<Partner> getPartnersLocalizatedByFilter(String language, AjaxPartnerListFilter ajaxFilter);

    List<ProductMoneyProperty> getProductMoneyProperties(CatContract catContract);

    List<PartnerProductMoneyProperty> getPartnerProductMoneyProperties(Partner partner, CatContract catContract);

    List<ProductMoneyProperty> getProductMoneyPropertiesNotSet(CatContract catContract);

    List<ProductMoneyProperty> getPartnerProductMoneyPropertiesNotSet(Partner partner, CatContract catContract);

    ProductMoneyProperty getProductMoneyPropertyById(Long propertyId);

    void savePartnerProductMoneyProperty(PartnerProductMoneyProperty partnerProductMoneyProperty) throws EntityNotSavedException;

    PartnerProductMoneyProperty getPartnerProductMoneyPropertyById(Long productPropertyId);

    void removeProperty(PartnerProductMoneyProperty partnerProductMoneyProperty);

    void updateParametersForProductMoneyProperty(ProductMoneyProperty productMoneyProperty) throws EntityNotSavedException;

    void clearProductPropertyGlobalParams(Long propertyId);

    ProductMoneyProperty getProductMoneyPropertyByPropertyName(ProductMoneyProperty.PropertyType propertyName, CatContract catContract);

    ProductProperty getPartnerProductMoneyPropertyByPropertyName(Partner partner, ProductMoneyProperty.PropertyType propertyType, CatContract catContract) throws NoResultException;

    void addPartnerFranchise(CatContractPartnerFranchise catContractPartnerFranchise) throws IllegalDataException;

    void addProductFranchise(CatContractPartnerFranchise catContractPartnerFranchise) throws IllegalDataException;

    CatContractPartnerFranchise getCatContractPartnerFranchiseById(Long id);

    void deleteCatContractPartnerFranchise(CatContractPartnerFranchise catContractPartnerFranchise);

    void removeAllCatContractPartnerFranchise(Partner partner, CatContract catContract);

    List<CatContractPartnerFranchise> getFranchiseValuesAllowed(CatContract catContract, Partner partner);

    List<CatContractPartnerFranchise> getCatContractPartnerFranchises(Partner partner, CatContract catContract);

    boolean paymentChooseAllowed(Partner partner, CatContract catContract);

    BigDecimal getFranchiseMult(Partner partner, CatContract catContract, Integer franchise);

    CatContractPartnerFranchise getCatContractPartnerFranchiseByPercents(Partner partner, CatContract catContract, Integer franchise);

    BigDecimal getRefundPaymentMult(Partner partner, CatContract catContract, Contract.PaymentType paymentType);

    BigDecimal getSecuritySystemsMult(Partner partner, CatContract catContract, int countSec);

}
