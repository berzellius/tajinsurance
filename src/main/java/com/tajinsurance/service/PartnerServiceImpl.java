package com.tajinsurance.service;

import com.tajinsurance.domain.*;
import com.tajinsurance.dto.AjaxPartnerListFilter;
import com.tajinsurance.exceptions.BadNewPartnerDataException;
import com.tajinsurance.exceptions.EntityNotSavedException;
import com.tajinsurance.exceptions.IllegalDataException;
import com.tajinsurance.exceptions.NoEntityException;
import com.tajinsurance.utils.LanguageUtil;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Created by berz on 24.03.14.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class PartnerServiceImpl implements PartnerService {
    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    LanguageUtil languageUtil;

    @Autowired
    CatContractService catContractService;

    @Autowired
    RiskService riskService;

    public PartnerServiceImpl() {
    }

    @Override
    public List<PartnerRiskPolicy> getExistsPolicy(CatContract catContract, Partner partner) {
        return entityManager.createQuery(
                "SELECT o FROM PartnerRiskPolicy o WHERE catContract = :cc AND partner = :p",
                PartnerRiskPolicy.class
        )
                .setParameter("cc", catContract)
                .setParameter("p", partner)
                .getResultList();
    }

    @Override
    public List<Risk> getRisksNotSetPolicy(CatContract catContract, Partner partner) {
        // только для продуктов с "глобальным" наследованием настроек
        if (catContract.getSettingsWay().equals(CatContract.SettingsWay.PARTNER)) return null;



        List<Risk> risks = entityManager.createQuery(
              " SELECT o FROM Risk o " +
                      "WHERE EXISTS" +
                      "   ( " +
                      "       SELECT ccr FROM CatContractRisk ccr WHERE ccr.partner is null AND ccr.catContract = :cc AND ccr.risk.parentRisk = o " +
                      "   )" +
                      "   AND NOT EXISTS " +
                      "   (" +
                      "       SELECT prp FROM PartnerRiskPolicy prp WHERE " +
                      "           prp.catContract = :cc AND prp.partner = :p AND prp.risk = o" +
                      "   )",
                Risk.class
        )
                .setParameter("cc", catContract)
                .setParameter("p", partner)
                .getResultList();

        return risks;
    }

    @Override
    public void delPartnerRiskPolicy(Long policyId) {
        PartnerRiskPolicy partnerRiskPolicy = entityManager.find(PartnerRiskPolicy.class, policyId);

        if (partnerRiskPolicy == null) return;

        entityManager.remove(partnerRiskPolicy);
    }

    @Override
    public void addPartnerRiskPolicy(PartnerRiskPolicy partnerRiskPolicy) {
        /*if (
                riskService
                        .getRisk(
                                partnerRiskPolicy.getRisk(),
                                partnerRiskPolicy.getCatContract(),
                                partnerRiskPolicy.getPartner()
                        )
                        .getMandatory()
                ) {
            partnerRiskPolicy.setPolicy(PartnerRiskPolicy.Policy.FIXED);
        }*/

        if(partnerRiskPolicy.getRisk().getMandatory() != null &&
                partnerRiskPolicy.getRisk().getMandatory()){
            partnerRiskPolicy.setPolicy(PartnerRiskPolicy.Policy.FIXED);
        }

        entityManager.persist(partnerRiskPolicy);
    }

    @Override
    public PartnerRiskCorrelation getPartnerRiskCorrelationByPartnerAndRisk(Partner partner, Risk risk, CatContract catContract) {
        PartnerRiskCorrelation partnerRiskCorrelation = null;

        try {
            partnerRiskCorrelation = entityManager.createQuery(
                    "SELECT o FROM PartnerRiskCorrelation o WHERE partner = :p AND catContract = :cc AND risk = :r",
                    PartnerRiskCorrelation.class
            )
                    .setParameter("cc", catContract)
                    .setParameter("p", partner)
                    .setParameter("r", risk)
                    .getSingleResult();
        } catch (EmptyResultDataAccessException e) {
            //
        } finally {
            return partnerRiskCorrelation;
        }

    }

    @Override
    public PartnerRiskCorrelation getPartnerRiskCorrelationById(Long corrId) {
        return entityManager.find(PartnerRiskCorrelation.class, corrId);
    }

    @Override
    public void deletePartnerRiskCorrelation(Long corrId) {
        PartnerRiskCorrelation partnerRiskCorrelation = this.getPartnerRiskCorrelationById(corrId);

        if (partnerRiskCorrelation != null) entityManager.remove(partnerRiskCorrelation);
    }

    @Override
    public void addCorrelationForPartnerCatContractRisk(Long partnerId, Long catContractId, Long riskId, BigDecimal correlation, String extraInfo) {
        Partner partner = this.getPartnerById(partnerId);
        CatContract catContract = catContractService.getCatContractById(catContractId);
        Risk risk;
        try {
            risk = riskService.getRiskById(riskId);
        } catch (NoEntityException e) {
            risk = null;
        }

        if (partner != null && catContract != null && risk != null) {
            PartnerRiskCorrelation partnerRiskCorrelation = new PartnerRiskCorrelation();
            partnerRiskCorrelation.setPartner(partner);
            partnerRiskCorrelation.setCatContract(catContract);
            partnerRiskCorrelation.setRisk(risk);

            if (correlation != null && correlation.compareTo(BigDecimal.ZERO) == 1) {
                partnerRiskCorrelation.setCorrelation(correlation);
                partnerRiskCorrelation.setExtraInfo(extraInfo);

                entityManager.persist(partnerRiskCorrelation);
            }
        }
    }

    @Override
    public List<Partner> getPartnersLocalizated(String locale) {
        List<Partner> partners = entityManager.createQuery("SELECT o FROM Partner o WHERE removed = false", Partner.class).getResultList();
        for (Partner p : partners) {
            p = (Partner) languageUtil.getLocalizatedObject(p, locale);
        }

        return partners;
    }

    @Override
    public List<Partner> getPartners() {
        List<Partner> partners = entityManager.createQuery("SELECT o FROM Partner o WHERE removed = false", Partner.class).getResultList();
        return partners;
    }

    @Override
    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    public void savePartner(Partner partner) {
        partner.setRemoved(false);
        entityManager.persist(partner);
    }

    @Override
    public Partner getPartnerById(Long id) {
        Session session = entityManager.unwrap(Session.class);
        Partner p = (Partner) session.get(Partner.class, id);
        return p;
    }

    @Override
    public void validateNewPartner(Partner partner) throws BadNewPartnerDataException {
        if (partner.getValue() == "") throw new BadNewPartnerDataException("empty_name");

        if (partner.getCatContracts() == null || partner.getCatContracts().size() == 0)
            throw new BadNewPartnerDataException("empty_cat_contracts");
    }

    @Override
    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    public void update(Partner partner) throws BadNewPartnerDataException {
        if (partner.getCatContracts() == null) throw new BadNewPartnerDataException("empty_cat_contracts");
        partner.setRemoved(false);
        entityManager.merge(partner);
        //entityManager.flush();
        //entityManager.refresh(partner);


    }

    @Override
    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    public void remove(Partner partner) {
        partner.setRemoved(true);
        entityManager.merge(partner);
    }

    @Override
    public List<Partner> getPartnersLocalizatedByFilter(String language, AjaxPartnerListFilter ajaxFilter) {

        String jpaQuery = "SELECT o FROM Partner o WHERE removed = false ";


        if (ajaxFilter.partner != null) {
            jpaQuery += " AND LOWER(value) LIKE LOWER(:partner) ";
        }

        if (ajaxFilter.catContracts != null) {
            jpaQuery += " AND EXISTS( SELECT o1 FROM o.catContracts o1 WHERE id IN (:cc) ) ";
        }


        if (ajaxFilter.orderColumn != null) {
            jpaQuery += " ORDER BY " + ajaxFilter.orderColumn + " ";

            jpaQuery += (ajaxFilter.orderType != null) ? ajaxFilter.orderType : "DESC";
        }


        Query q = entityManager.createQuery(jpaQuery, Partner.class);

        if (ajaxFilter.partner != null) q.setParameter("partner", ajaxFilter.partner + "%");

        if (ajaxFilter.catContracts != null) q.setParameter("cc", ajaxFilter.catContracts);

        List<Partner> partners = q.getResultList();


        for (Partner p : partners) {
            p = (Partner) languageUtil.getLocalizatedObject(p, language);
        }

        return partners;
    }

    @Override
    public List<ProductMoneyProperty> getProductMoneyProperties(CatContract catContract) {
        Query query = entityManager.createQuery(
                "select o from ProductMoneyProperty o where catContract = :cc and source = :src and (moneyValue is not null or useProperty is not null)",
                ProductMoneyProperty.class
        )
                .setParameter("cc", catContract)
                .setParameter("src", ProductMoneyProperty.Source.GLOBAL);

        List<ProductMoneyProperty> productMoneyProperties = query.getResultList();

        for (ProductMoneyProperty productMoneyProperty : productMoneyProperties) {
            if (
                    productMoneyProperty.getPropertyName().equals(ProductMoneyProperty.PropertyType.MP0_money_level_1) ||
                            productMoneyProperty.getPropertyName().equals(ProductMoneyProperty.PropertyType.MP0_money_level_2)
                    ) {
                productMoneyProperty.setMoneyValue(productMoneyProperty.getMoneyValue().setScale(0, RoundingMode.HALF_DOWN));
            }
        }

        return productMoneyProperties;
    }

    @Override
    public List<PartnerProductMoneyProperty> getPartnerProductMoneyProperties(Partner partner, CatContract catContract) {
        if (partner == null)
            throw new IllegalArgumentException("not allowed to use getPartnerProductMoneyProperties() with null partner !");

        Query query = entityManager.createQuery(
                "SELECT o FROM PartnerProductMoneyProperty o WHERE partner = :p AND productMoneyProperty = " +
                        "ANY(SELECT o1 FROM ProductMoneyProperty o1 WHERE catContract = :cc AND (source = :src OR source = null ) )",
                PartnerProductMoneyProperty.class
        )
                .setParameter("p", partner)
                .setParameter("cc", catContract)
                .setParameter("src", ProductMoneyProperty.Source.PARTNER);

        List<PartnerProductMoneyProperty> partnerProductMoneyProperties = query.getResultList();

        for (PartnerProductMoneyProperty partnerProductMoneyProperty : partnerProductMoneyProperties) {
            if (
                    partnerProductMoneyProperty.getProductMoneyProperty().getPropertyName().equals(ProductMoneyProperty.PropertyType.MP0_money_level_1) ||
                            partnerProductMoneyProperty.getProductMoneyProperty().getPropertyName().equals(ProductMoneyProperty.PropertyType.MP0_money_level_2)
                    ) {
                partnerProductMoneyProperty.setMoneyValue(partnerProductMoneyProperty.getMoneyValue().setScale(0, RoundingMode.HALF_DOWN));
            }
        }

        return partnerProductMoneyProperties;
    }

    @Override
    public List<ProductMoneyProperty> getProductMoneyPropertiesNotSet(CatContract catContract) {
        Query query = entityManager.createQuery(
                "select o from ProductMoneyProperty o where catContract = :cc and source = :src and (moneyValue is null and useProperty is null)",
                ProductMoneyProperty.class
        )
                .setParameter("cc", catContract)
                .setParameter("src", ProductMoneyProperty.Source.GLOBAL);

        List<ProductMoneyProperty> productMoneyProperties = query.getResultList();

        return productMoneyProperties;
    }

    @Override
    public List<ProductMoneyProperty> getPartnerProductMoneyPropertiesNotSet(Partner partner, CatContract catContract) {
        Query query = entityManager.createQuery(
                "SELECT o FROM ProductMoneyProperty o WHERE NOT EXISTS (" +
                        "SELECT o1 FROM PartnerProductMoneyProperty o1 WHERE partner = :p AND productMoneyProperty = o)" +
                        " AND catContract = :cc AND (source = :src OR source = null)"
        )
                .setParameter("p", partner)
                .setParameter("cc", catContract)
                .setParameter("src", ProductMoneyProperty.Source.PARTNER);

        List<ProductMoneyProperty> propertyTypeList = query.getResultList();

        return propertyTypeList;
    }

    @Override
    public ProductMoneyProperty getProductMoneyPropertyById(Long propertyId) {
        return entityManager.find(ProductMoneyProperty.class, propertyId);
    }

    @Override
    public void savePartnerProductMoneyProperty(PartnerProductMoneyProperty partnerProductMoneyProperty) throws EntityNotSavedException {


        if (partnerProductMoneyProperty.getProductMoneyProperty().getPropertyName().equals(ProductMoneyProperty.PropertyType.MP0_money_level_1)) {

            if (partnerProductMoneyProperty.getMoneyValue() == null ||
                    partnerProductMoneyProperty.getMoneyValue().compareTo(BigDecimal.ZERO) < 1
                    ) {
                throw new EntityNotSavedException("value_less_or_equals_zero");
            }

            if (partnerProductMoneyProperty.getUseProperty() != null) {
                partnerProductMoneyProperty.setUseProperty(null);
            }

            try {
                PartnerProductMoneyProperty partnerProductMoneyProperty1 = (PartnerProductMoneyProperty) getPartnerProductMoneyPropertyByPropertyName(
                        partnerProductMoneyProperty.getPartner(),
                        ProductMoneyProperty.PropertyType.MP0_money_level_2,
                        partnerProductMoneyProperty.getProductMoneyProperty().getCatContract());
                if (partnerProductMoneyProperty1 != null) {
                    if (partnerProductMoneyProperty1.getMoneyValue().compareTo(partnerProductMoneyProperty.getMoneyValue()) < 1)
                        throw new EntityNotSavedException("MP0_level_1_level_2");
                }
            } catch (NoResultException e) {
                // that's ok
            }

        }

        if (partnerProductMoneyProperty.getProductMoneyProperty().getPropertyName().equals(ProductMoneyProperty.PropertyType.MP0_money_level_2)) {

            if (
                    partnerProductMoneyProperty.getMoneyValue() == null ||
                            partnerProductMoneyProperty.getMoneyValue().compareTo(BigDecimal.ZERO) < 1
                    ) {
                throw new EntityNotSavedException("value_less_or_equals_zero");
            }


            if (partnerProductMoneyProperty.getUseProperty() != null) {
                partnerProductMoneyProperty.setUseProperty(null);
            }

            try {
                PartnerProductMoneyProperty partnerProductMoneyProperty1 = (PartnerProductMoneyProperty) getPartnerProductMoneyPropertyByPropertyName(
                        partnerProductMoneyProperty.getPartner(),
                        ProductMoneyProperty.PropertyType.MP0_money_level_1,
                        partnerProductMoneyProperty.getProductMoneyProperty().getCatContract());
                if (partnerProductMoneyProperty1 != null) {
                    if (partnerProductMoneyProperty1.getMoneyValue().compareTo(partnerProductMoneyProperty.getMoneyValue()) > -1)
                        throw new EntityNotSavedException("MP0_level_1_level_2");
                }

            } catch (NoResultException e) {
                //that's ok
            }

        }


        /* security or refund k */
        if (
                partnerProductMoneyProperty.getProductMoneyProperty().getPropertyName().equals(ProductMoneyProperty.PropertyType.MP0_security_k) ||
                        partnerProductMoneyProperty.getProductMoneyProperty().getPropertyName().equals(ProductMoneyProperty.PropertyType.MP0_refund_payment_k) ||
                        partnerProductMoneyProperty.getProductMoneyProperty().getPropertyName().equals(ProductMoneyProperty.PropertyType.correlation)
                ) {


            /* Если не стоит чекбокс "использовать параметр", то форсим */
            if (partnerProductMoneyProperty.getUseProperty() == null || !partnerProductMoneyProperty.getUseProperty()) {
                partnerProductMoneyProperty.setMoneyValue(null);
                partnerProductMoneyProperty.setUseProperty(false);
            }
            /* Если стоит чекбокс "использовать параметр", то проверяем на ненулевое значение */
            if (partnerProductMoneyProperty.getUseProperty() != null && partnerProductMoneyProperty.getUseProperty()) {

                if (
                        partnerProductMoneyProperty.getMoneyValue() == null ||
                                partnerProductMoneyProperty.getMoneyValue().compareTo(BigDecimal.ZERO) < 1) {
                    throw new EntityNotSavedException("value_less_or_equals_zero");
                }


            }
        }

        /* franchise k */
        if (partnerProductMoneyProperty.getProductMoneyProperty().getPropertyName().equals(ProductMoneyProperty.PropertyType.MP0_franchise_k)) {

            /* Если не стоит чекбокс "использовать параметр", то форсим */
            if (partnerProductMoneyProperty.getUseProperty() == null || !partnerProductMoneyProperty.getUseProperty()) {
                partnerProductMoneyProperty.setMoneyValue(null);
                partnerProductMoneyProperty.setUseProperty(false);
            }

            /* Если стоит чекбокс "использовать параметр", то проверяем на ненулевое значение */
            if (partnerProductMoneyProperty.getUseProperty() != null && partnerProductMoneyProperty.getUseProperty()) {

                if (
                        partnerProductMoneyProperty.getMoneyValue() != null
                        ) {
                    partnerProductMoneyProperty.setMoneyValue(null);
                }


            }

        }

        if(partnerProductMoneyProperty.getProductMoneyProperty().getPropertyName().equals(ProductMoneyProperty.PropertyType.TP2_use_extended_pack)){
            partnerProductMoneyProperty.setMoneyValue(null);
        }

        entityManager.persist(partnerProductMoneyProperty);
    }

    @Override
    public PartnerProductMoneyProperty getPartnerProductMoneyPropertyById(Long productPropertyId) {
        return entityManager.find(PartnerProductMoneyProperty.class, productPropertyId);
    }


    @Override
    public void removeProperty(PartnerProductMoneyProperty partnerProductMoneyProperty) {
        if (partnerProductMoneyProperty.getProductMoneyProperty().getPropertyName().equals(ProductMoneyProperty.PropertyType.MP0_franchise_k)) {
            //  Удаляем параметр "использование коэфф-в франшизы". Теперь ранее введенные значения франшизы не нужны
            this.removeAllCatContractPartnerFranchise(
                    partnerProductMoneyProperty.getPartner(),
                    partnerProductMoneyProperty.getProductMoneyProperty().getCatContract()
            );
        }

        entityManager.remove(partnerProductMoneyProperty);
    }

    @Override
    public void updateParametersForProductMoneyProperty(ProductMoneyProperty productMoneyProperty) throws EntityNotSavedException {

        if (
                productMoneyProperty.getMoneyValue() == null ||
                        (
                                (
                                    productMoneyProperty.getMoneyValue().compareTo(BigDecimal.ZERO) < 1 &&
                                            !productMoneyProperty.getPropertyName().equals(ProductMoneyProperty.PropertyType.TP0_D_franchise_discount_percent)
                                )
                                        ||
                                        (
                                                productMoneyProperty.getPropertyName().equals(ProductMoneyProperty.PropertyType.TP0_D_franchise_discount_percent) &&
                                                        productMoneyProperty.getMoneyValue().compareTo(BigDecimal.ZERO) < 0
                                        )
                        )
                ) throw new EntityNotSavedException("value_less_or_equals_zero");

        if (productMoneyProperty.getUseProperty() != null)
            productMoneyProperty.setUseProperty(null);


        if (productMoneyProperty.getPropertyName().equals(ProductMoneyProperty.PropertyType.MP0_money_level_1)) {
            ProductMoneyProperty productMoneyProperty1 = getProductMoneyPropertyByPropertyName(ProductMoneyProperty.PropertyType.MP0_money_level_2, productMoneyProperty.getCatContract());

            if (
                    productMoneyProperty1 != null &&
                            productMoneyProperty1.getMoneyValue() != null &&
                            productMoneyProperty1.getMoneyValue().compareTo(productMoneyProperty.getMoneyValue()) < 1
                    ) throw new EntityNotSavedException("MP0_level_1_level_2");
        }

        if (productMoneyProperty.getPropertyName().equals(ProductMoneyProperty.PropertyType.MP0_money_level_2)) {
            ProductMoneyProperty productMoneyProperty1 = getProductMoneyPropertyByPropertyName(ProductMoneyProperty.PropertyType.MP0_money_level_1, productMoneyProperty.getCatContract());
            if (
                    productMoneyProperty1 != null &&
                            productMoneyProperty1.getMoneyValue() != null &&
                            productMoneyProperty1.getMoneyValue().compareTo(productMoneyProperty.getMoneyValue()) > -1
                    ) throw new EntityNotSavedException("MP0_level_1_level_2");
        }

        if(productMoneyProperty.getPropertyName().equals(ProductMoneyProperty.PropertyType.TP0_D_franchise_discount_percent)){
            if(productMoneyProperty.getMoneyValue().compareTo(BigDecimal.valueOf(100)) > -1){
                throw new EntityNotSavedException("TP0_discount_perc_must_be_less_100");
            }
        }

        entityManager.merge(productMoneyProperty);
    }

    @Override
    public void clearProductPropertyGlobalParams(Long propertyId) {
        ProductMoneyProperty productMoneyProperty = this.getProductMoneyPropertyById(propertyId);

        if (productMoneyProperty != null) {
            productMoneyProperty.setUseProperty(null);
            productMoneyProperty.setMoneyValue(null);

            entityManager.merge(productMoneyProperty);
        }
    }

    @Override
    public ProductMoneyProperty getProductMoneyPropertyByPropertyName(ProductMoneyProperty.PropertyType propertyName, CatContract catContract) {
        ProductMoneyProperty productMoneyProperty = null;
        try {
            productMoneyProperty = entityManager.createQuery(
                    "SELECT o FROM ProductMoneyProperty o WHERE propertyName = :pn AND catContract = :cc",
                    ProductMoneyProperty.class
            )
                    .setParameter("pn", propertyName)
                    .setParameter("cc", catContract)
                    .getSingleResult();
        } catch (EmptyResultDataAccessException e) {
            //System.out.println("empty result");
        } finally {
            return productMoneyProperty;
        }
    }

    @Override
    public ProductProperty getPartnerProductMoneyPropertyByPropertyName(Partner partner, ProductMoneyProperty.PropertyType propertyType, CatContract catContract) throws NoResultException {
        PartnerProductMoneyProperty partnerProductMoneyProperty = null;

        ProductMoneyProperty productMoneyProperty = getProductMoneyPropertyByPropertyName(propertyType, catContract);

        if (productMoneyProperty == null) return null;

        if (
                productMoneyProperty.getSource() == null ||
                        productMoneyProperty.getSource().equals(ProductMoneyProperty.Source.PARTNER)) {

            try {
                partnerProductMoneyProperty = entityManager.createQuery("SELECT o FROM PartnerProductMoneyProperty o WHERE partner = :p " +
                        "AND o.productMoneyProperty.propertyName = :name AND o.productMoneyProperty.catContract = :cc", PartnerProductMoneyProperty.class)
                        .setParameter("p", partner)
                        .setParameter("name", propertyType)
                        .setParameter("cc", catContract).getSingleResult();
            } catch (EmptyResultDataAccessException e) {
                //
            } finally {
                return partnerProductMoneyProperty;
            }
        }

        if (productMoneyProperty.getSource().equals(ProductMoneyProperty.Source.GLOBAL)) {
            return productMoneyProperty;
        }

        return null;

    }


    @Override
    public void addPartnerFranchise(CatContractPartnerFranchise catContractPartnerFranchise) throws IllegalDataException {

        if (catContractPartnerFranchise.getFranchisePercent() == null)
            throw new IllegalDataException("franchise percent is null", IllegalDataException.Reason.FRANCHISE_WRONG_PERCENT);
        if (catContractPartnerFranchise.getDiscount() == null)
            throw new IllegalDataException("discount is null", IllegalDataException.Reason.FRANCHISE_DISCOUNT_WRONG);

        if (!(catContractPartnerFranchise.getFranchisePercent() >= 0 && catContractPartnerFranchise.getFranchisePercent() < 100)) {
            throw new IllegalDataException(catContractPartnerFranchise.getFranchisePercent() + "% franchise is not valid", IllegalDataException.Reason.FRANCHISE_WRONG_PERCENT);
        }

        if (!(
                catContractPartnerFranchise.getDiscount().compareTo(BigDecimal.ZERO) >= 0 &&
                        catContractPartnerFranchise.getDiscount().compareTo(BigDecimal.ONE) == -1
        )) {
            throw new IllegalDataException(catContractPartnerFranchise.getDiscount() + " is not valid discount", IllegalDataException.Reason.FRANCHISE_DISCOUNT_WRONG);
        }

        entityManager.persist(catContractPartnerFranchise);
    }

    @Override
    public void addProductFranchise(CatContractPartnerFranchise catContractPartnerFranchise) throws IllegalDataException {
        if (catContractPartnerFranchise.getFranchiseMoney() == null)
            throw new IllegalDataException("franchise money is null", IllegalDataException.Reason.FRANCHISE_WRONG_MONEY);
        if (catContractPartnerFranchise.getDiscount() == null)
            throw new IllegalDataException("discount is null", IllegalDataException.Reason.FRANCHISE_DISCOUNT_WRONG);


        if (!(
                catContractPartnerFranchise.getDiscount().compareTo(BigDecimal.ZERO) >= 0 &&
                        catContractPartnerFranchise.getDiscount().compareTo(BigDecimal.ONE) == -1
        )) {
            throw new IllegalDataException(catContractPartnerFranchise.getDiscount() + " is not valid discount", IllegalDataException.Reason.FRANCHISE_DISCOUNT_WRONG);
        }

        entityManager.persist(catContractPartnerFranchise);
    }

    @Override
    public CatContractPartnerFranchise getCatContractPartnerFranchiseById(Long id) {
        return entityManager.find(CatContractPartnerFranchise.class, id);
    }

    @Override
    public void deleteCatContractPartnerFranchise(CatContractPartnerFranchise catContractPartnerFranchise) {
        entityManager.remove(catContractPartnerFranchise);
    }

    @Override
    public void removeAllCatContractPartnerFranchise(Partner partner, CatContract catContract) {
        entityManager.createQuery("DELETE FROM CatContractPartnerFranchise o WHERE catContract = :cc AND partner = :p")
                .setParameter("cc", catContract)
                .setParameter("p", partner)
                .executeUpdate();
    }

    @Override
    public List<CatContractPartnerFranchise> getFranchiseValuesAllowed(CatContract catContract, Partner partner) {
        if (partner != null) {
            PartnerProductMoneyProperty franchiseProperty = (PartnerProductMoneyProperty) this.getPartnerProductMoneyPropertyByPropertyName(partner, ProductMoneyProperty.PropertyType.MP0_franchise_k, catContract);
            if (franchiseProperty != null && franchiseProperty.getUseProperty() != null && franchiseProperty.getUseProperty()) {
                List<CatContractPartnerFranchise> franchises = getCatContractPartnerFranchises(partner, catContract);
                return franchises;
            } else return null;
        } else {
            List<CatContractPartnerFranchise> franchises = getCatContractPartnerFranchises(null, catContract);
            return franchises;
        }
    }

    @Override
    public List<CatContractPartnerFranchise> getCatContractPartnerFranchises(Partner partner, CatContract catContract) {
        Query q;

        if (partner != null) {
            q = entityManager.createQuery("SELECT o FROM CatContractPartnerFranchise o " +
                    "WHERE catContract = :cc AND partner =:p ORDER BY franchisePercent DESC", CatContractPartnerFranchise.class)
                    .setParameter("cc", catContract)
                    .setParameter("p", partner);
        }
        else{
            q = entityManager.createQuery("SELECT o FROM CatContractPartnerFranchise o " +
                    "WHERE catContract = :cc AND partner is null ORDER BY franchisePercent DESC", CatContractPartnerFranchise.class)
                    .setParameter("cc", catContract);
        }
        return q.getResultList();
    }

    @Override
    public boolean paymentChooseAllowed(Partner partner, CatContract catContract) {
        PartnerProductMoneyProperty paymentProp = (PartnerProductMoneyProperty) getPartnerProductMoneyPropertyByPropertyName(partner, ProductMoneyProperty.PropertyType.MP0_refund_payment_k, catContract);

        return (paymentProp != null && paymentProp.getUseProperty() != null && paymentProp.getUseProperty());
    }

    @Override
    public BigDecimal getFranchiseMult(Partner partner, CatContract catContract, Integer franchise) {
        if (franchise != null) {
            PartnerProductMoneyProperty franchiseProp = (PartnerProductMoneyProperty) this.getPartnerProductMoneyPropertyByPropertyName(partner, ProductMoneyProperty.PropertyType.MP0_franchise_k, catContract);

            if (franchiseProp != null && franchiseProp.getUseProperty() != null && franchiseProp.getUseProperty()) {
                CatContractPartnerFranchise catContractPartnerFranchise = this.getCatContractPartnerFranchiseByPercents(partner, catContract, franchise);

                if (catContractPartnerFranchise != null)
                    return BigDecimal.ONE.add(catContractPartnerFranchise.getDiscount().negate());
                else return BigDecimal.ONE;
            } else return BigDecimal.ONE;
        } else return BigDecimal.ONE;
    }

    @Override
    public CatContractPartnerFranchise getCatContractPartnerFranchiseByPercents(Partner partner, CatContract catContract, Integer franchise) {
        Query q = entityManager.createQuery(
                "SELECT o FROM CatContractPartnerFranchise o  WHERE catContract = :cc AND partner = :p AND franchisePercent = :fr",
                CatContractPartnerFranchise.class
        ).setParameter("cc", catContract)
                .setParameter("p", partner)
                .setParameter("fr", franchise);

        if (!q.getResultList().isEmpty()) return (CatContractPartnerFranchise) q.getSingleResult();
        else return null;
    }

    @Override
    public BigDecimal getRefundPaymentMult(Partner partner, CatContract catContract, Contract.PaymentType paymentType) {
        if (paymentType != null) {
            PartnerProductMoneyProperty refundProp = (PartnerProductMoneyProperty) this.getPartnerProductMoneyPropertyByPropertyName(partner, ProductMoneyProperty.PropertyType.MP0_refund_payment_k, catContract);

            if (refundProp != null && refundProp.getUseProperty() != null && refundProp.getUseProperty() && refundProp.getMoneyValue() != null) {
                if (paymentType.equals(Contract.PaymentType.BY_FIRST_RISK)) return refundProp.getMoneyValue();
                else return BigDecimal.ONE;
            } else return BigDecimal.ONE;
        } else return BigDecimal.ONE;
    }

    @Override
    public BigDecimal getSecuritySystemsMult(Partner partner, CatContract catContract, int countSec) {
        BigDecimal q;

        PartnerProductMoneyProperty secProp = (PartnerProductMoneyProperty) this.getPartnerProductMoneyPropertyByPropertyName(partner, ProductMoneyProperty.PropertyType.MP0_security_k, catContract);


        if (secProp != null && secProp.getMoneyValue() != null && secProp.getUseProperty() != null && secProp.getUseProperty()) {
            q = secProp.getMoneyValue();

            return q.add(
                    CalculateConstants.SECURITY_SYSTEM_DELTA.getValue()
                            .negate()
                            .multiply(
                                    BigDecimal.valueOf(countSec)
                            )
            );
        } else return BigDecimal.ONE;

    }
}
