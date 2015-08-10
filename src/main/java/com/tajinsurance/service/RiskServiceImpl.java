package com.tajinsurance.service;

import com.tajinsurance.domain.*;
import com.tajinsurance.dto.CatContractRiskToCreate;
import com.tajinsurance.exceptions.CalculatePremiumException;
import com.tajinsurance.exceptions.NoEntityException;
import com.tajinsurance.utils.LanguageUtil;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by berz on 30.03.14.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class RiskServiceImpl implements RiskService {
    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    LanguageUtil languageUtil;

    @Autowired
    PartnerService partnerService;

    @Autowired
    CatContractService catContractService;

    @Autowired
    PremiumService premiumService;

    @Autowired
    RiskService riskService;

    public RiskServiceImpl() {
    }

    @Override
    public Risk getRiskById(Long id) throws NoEntityException {
        Session session = entityManager.unwrap(Session.class);
        Risk r = (Risk) session.get(Risk.class, id);

        if (r == null) throw new NoEntityException("contract not found: id=" + id);
        return r;
    }

    @Override
    public BigDecimal getRateForRisk(Risk risk, CatContract cc) {
        Session session = entityManager.unwrap(Session.class);
        List<CatContractRisk> catContractRiskList = session.createCriteria(CatContractRisk.class)
                .add(Restrictions.eq("risk", risk))
                .add(Restrictions.eq("catContract", cc))
                .list();
        if (catContractRiskList.size() == 0) return null;

        return catContractRiskList.get(0).getRate();
    }

    @Override
    public CatContractRisk getRisk(Risk risk, CatContract cc, Partner partner) {
        Session session = entityManager.unwrap(Session.class);

        if (
                cc.getSettingsWay() == null ||
                        cc.getSettingsWay().equals(CatContract.SettingsWay.PARTNER)
                ) {
            List<CatContractRisk> catContractRiskList = session.createCriteria(CatContractRisk.class)
                    .add(Restrictions.eq("risk", risk))
                    .add(Restrictions.eq("catContract", cc))
                    .add(Restrictions.eq("partner", partner))
                    .list();
            if (catContractRiskList.size() == 0) return null;

            return catContractRiskList.get(0);
        }

        if(cc.getSettingsWay().equals(CatContract.SettingsWay.GLOBAL))
        {
            List<CatContractRisk> catContractRiskList = entityManager.createQuery(
                    "SELECT o FROM CatContractRisk o WHERE catContract = :cc AND risk = :r AND partner is null ",
                    CatContractRisk.class
            )
                    .setParameter("cc", cc)
                    .setParameter("r", risk)
                    .getResultList();


            if (catContractRiskList.size() == 0) return null;

            return catContractRiskList.get(0);
        }

        return null;
    }

    @Override
    public List<Risk> getAllRisks(String locale) {
        List<Risk> risks = entityManager.createQuery("SELECT o FROM Risk o WHERE deleted = false", Risk.class).getResultList();
        if (risks != null) {
            for (Risk r : risks) {
                r = (Risk) languageUtil.getLocalizatedObject(r, locale);
            }
            return risks;
        } else return new LinkedList<Risk>();
    }

    @Override
    public List<Risk> getAllRisksByType(String locale, TypeOfRisk typeOfRisk) {
        List<Risk> risks = entityManager.createQuery("SELECT o FROM Risk o WHERE deleted = false AND typeOfRisk = :t", Risk.class)
                .setParameter("t", typeOfRisk)
                .getResultList();
        if (risks != null) {
            for (Risk r : risks) {
                r = (Risk) languageUtil.getLocalizatedObject(r, locale);
            }
            return risks;
        } else return new LinkedList<Risk>();
    }

    @Override
    public List<Risk> getAllRisksByType(TypeOfRisk typeOfRisk) {
        List<Risk> risks = entityManager.createQuery("SELECT o FROM Risk o WHERE deleted = false AND typeOfRisk = :t", Risk.class)
                .setParameter("t", typeOfRisk)
                .getResultList();
        if (risks != null) {
            return risks;
        } else return new LinkedList<Risk>();
    }

    @Override
    public List<TypeOfRisk> getAllRiskTypes() {
        List<TypeOfRisk> typeOfRiskList = entityManager.createQuery("SELECT o FROM TypeOfRisk o", TypeOfRisk.class).getResultList();

        return typeOfRiskList;
    }

    @Override
    public TypeOfRisk getTypeOfRiskById(Long id) {
        TypeOfRisk typeOfRisk = entityManager.find(TypeOfRisk.class, id);

        return typeOfRisk;
    }

    @Override
    public List<Risk> getRisksFromIds(List<Long> riskIds) {
        Query query = entityManager.createQuery("SELECT o FROM Risk o WHERE deleted = false AND id IN :risks", Risk.class);
        query.setParameter("risks", riskIds);
        return query.getResultList();
    }

    @Override
    public Risk save(Risk risk) {
        risk.setDeleted(false);
        entityManager.persist(risk);
        entityManager.flush();
        entityManager.refresh(risk);

        return risk;
    }

    @Override
    public void update(Risk risk) {

        if (risk.getDeleted() == null) risk.setDeleted(false);

        entityManager.merge(risk);
    }

    @Override
    public void delete(Risk risk) {
        risk.setDeleted(true);
        entityManager.merge(risk);
    }

    @Override
    public Object getPotentialRiskTermPartnersAndCatContracts(Risk risk, String locale) {
        String nativeQuery = "SELECT p.id as partner, cc.id catContract FROM " +
                "partner p JOIN partner_cat_contract pcc" +
                "       ON p.id = pcc.partner_id " +
                "   JOIN cat_contract cc " +
                "       ON pcc.cat_contract_id = cc.id " +
                "   WHERE NOT EXISTS " +
                "(" +
                "   SELECT * FROM cat_contract_risk ccr " +
                "   WHERE ccr.partner = p.id " +
                "       AND ccr.cat_contract_id =  cc.id" +
                "           AND ccr.risk_id = :risk" +
                ")" +
                "   AND EXISTS " +
                "   (" +
                "       SELECT * FROM cat_contract_to_risk cc2r " +
                "       WHERE cc2r.cc_id = cc.id " +
                "           AND cc2r.risk_id = :risk " +
                ") AND p.removed = false AND cc.deleted = false";


        Query q = entityManager.createNativeQuery(nativeQuery).setParameter("risk", risk);
        List<Object[]> result = q.getResultList();

        List<CatContractRiskToCreate> res = new LinkedList<CatContractRiskToCreate>();

        for (Object[] row : result) {
            res.add(new CatContractRiskToCreate(
                    (Partner) languageUtil.getLocalizatedObject(
                            partnerService.getPartnerById(Long.decode(row[0].toString())),
                            locale
                    ),
                    (CatContract) languageUtil.getLocalizatedObject(
                            catContractService.getCatContractById(Long.decode(row[1].toString())),
                            locale
                    )
            ));
        }
        return res;
    }

    @Override
    public List<CatContractRisk> getExistRisks(Risk risk, String locale) {
        List<CatContractRisk> risks = entityManager.createQuery("SELECT o FROM CatContractRisk o WHERE risk = :risk")
                .setParameter("risk", risk)
                .getResultList();
        for (CatContractRisk r : risks) {
            r = (CatContractRisk) languageUtil.getLocalizatedObject(r, locale);
        }

        return risks;
    }

    @Override
    public List<CatContractRisk> getExistRisks(Partner partner, CatContract cc) {
        List<CatContractRisk> risks;
        if (partner != null) {

            risks = entityManager.createQuery("SELECT o FROM CatContractRisk o " +
                    "WHERE partner = :partner AND catContract = :catContract", CatContractRisk.class)
                    .setParameter("partner", partner)
                    .setParameter("catContract", cc)
                    .getResultList();
        } else {
            risks = entityManager.createQuery("SELECT o FROM CatContractRisk o " +
                    "WHERE partner is null AND catContract = :catContract", CatContractRisk.class)
                    .setParameter("catContract", cc)
                    .getResultList();
        }
        return risks;
    }


    @Override
    public List<Risk> getPotentialRiskTermRisks(Partner partner, CatContract cc) {
        String nativeQuery = "SELECT risk.* FROM risk WHERE " +
                "   NOT EXISTS(" +
                "       SELECT * FROM cat_contract_risk ccr WHERE " +
                "       ccr.risk_id = risk.id" +
                (
                        (partner != null) ?
                                "       AND ccr.partner = :p" :
                                "       AND ccr.partner is null"
                )
                +
                "       AND ccr.cat_contract_id = :cc" +
                "   )" +
                "   AND" +
                "   EXISTS(" +
                "       SELECT * FROM cat_contract_to_risk cc2r WHERE" +
                "       cc2r.cc_id = :cc " +
                "       AND cc2r.risk_id = risk.id" +
                ") AND risk.deleted = false";

        //System.out.println(nativeQuery);
        Query q = entityManager.createNativeQuery(nativeQuery, Risk.class);

        if (partner != null) {
            q.setParameter("p", partner);
        }
        q.setParameter("cc", cc);

        return q.getResultList();

    }

    @Override
    public BigDecimal getSumForAllRisksWithoutSaving(Contract contract, BigDecimal sum, Integer l) throws CalculatePremiumException {
        List<CatContractRisk> risks = getRisksToCalcForContract(contract);

        BigDecimal all = BigDecimal.ZERO;
        for (CatContractRisk r : risks) {
            BigDecimal delta = BigDecimal.ZERO;

            ContractPremium cp = new ContractPremium();

            cp.setRisk(r.getRisk());
            cp.setContract(contract);
            cp.setInsuredSum(sum);

            delta = premiumService.calculatePremiumWOSaving(cp, l);

            all = all.add(delta);
        }

        return all.setScale(0, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal getSumForAllRisksWithoutSaving(Contract contract) throws CalculatePremiumException {
        return getSumForAllRisksWithoutSaving(contract, contract.getSum(), contract.getLength());
    }

    @Override
    public BigDecimal getAllInsuredSumForAllRisk(Contract contract, BigDecimal sum) {
        BigDecimal all = BigDecimal.ZERO;

        List<CatContractRisk> risks = getRisksToCalcForContract(contract);

        for (CatContractRisk r : risks) {
            BigDecimal delta = BigDecimal.ZERO;


            delta = r.getRate().multiply(sum);


            all = all.add(delta);
        }

        return all.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal getMajorInsuredSumForAllRisks(Contract contract, BigDecimal sum) {
        BigDecimal major = BigDecimal.ZERO;

        List<CatContractRisk> risks = getRisksToCalcForContract(contract);

        for (CatContractRisk r : risks) {
            if (r.getRate().multiply(sum).compareTo(major) == 1) major = r.getRate().multiply(sum);
        }

        return major.setScale(2, RoundingMode.HALF_UP);

    }

    @Override
    public BigDecimal getMaxSum(Contract contract) {
        BigDecimal max = BigDecimal.ZERO;

        List<CatContractRisk> risks = getRisksToCalcForContract(contract);

        for (CatContractRisk r : risks) {
            if (
                    r.getMaxSum() != null &&
                            r.getRate() != null &&
                            (
                                    r.getMaxSum().divide(r.getRate(), 2, RoundingMode.HALF_UP).compareTo(max) == -1 ||
                                            max.equals(BigDecimal.ZERO)
                            )
                    ) {
                max = r.getMaxSum().divide(r.getRate(), 2, RoundingMode.HALF_UP);
            }
        }

        return max;
    }

    @Override
    public BigDecimal getMaxInsuredSum(Contract contract) {
        BigDecimal max = BigDecimal.ZERO;

        List<CatContractRisk> risks = getRisksToCalcForContract(contract);

        for (CatContractRisk r : risks) {
            if (
                    r.getMaxSum() != null &&
                            (
                                    r.getMaxSum().compareTo(max) == -1 ||
                                            max.equals(BigDecimal.ZERO)
                            )
                    ) {
                max = r.getMaxSum();
            }
        }

        return max;
    }

    @Override
    public BigDecimal getMinSum(Contract contract) {
        BigDecimal min = BigDecimal.ZERO;

        List<CatContractRisk> risks = getRisksToCalcForContract(contract);

        for (CatContractRisk r : risks) {
            if (
                    r.getMinSum() != null &&
                            r.getRate() != null &&
                            r.getMinSum().divide(r.getRate(), 2, RoundingMode.HALF_UP).compareTo(min) == 1
                    ) {
                min = r.getMinSum().divide(r.getRate(), 2, RoundingMode.HALF_UP);
            }
        }

        return min;
    }

    @Override
    public BigDecimal getMinInsuredSum(Contract contract) {
        BigDecimal min = BigDecimal.ZERO;

        List<CatContractRisk> risks = getRisksToCalcForContract(contract);

        for (CatContractRisk r : risks) {
            if (
                    r.getMinSum() != null &&
                            r.getMinSum().compareTo(min) == 1
                    ) {
                min = r.getMinSum();
            }
        }

        return min;
    }

    @Override
    public Integer getMinTerm(Contract contract) {
        return contract.getCatContract().getMinTerm();
    }

    @Override
    public boolean riskIsAllowed(CatContract catContract, Partner partner, Risk risk) {
        assert (catContract != null);
        assert (partner != null);
        assert (risk != null);

        List<CatContractRisk> risks = entityManager.createQuery("SELECT o FROM CatContractRisk o " +
                "WHERE partner = :partner AND catContract = :catContract AND risk = :risk", CatContractRisk.class)
                .setParameter("partner", partner)
                .setParameter("catContract", catContract)
                .setParameter("risk", risk)
                .getResultList();

        return (risks != null && risks.size() > 0);
    }

    @Override
    public List<Risk> getRisksForInsuranceArea() {
        return entityManager.createQuery("SELECT o FROM Risk o WHERE typeOfRisk = :t AND deleted = false", Risk.class)
                .setParameter("t", riskService.getTypeOfRiskById(2l)).getResultList();
    }

    @Override
    public List<Risk> getRisksForInsuranceArea(Partner p, CatContract catContract) {
        if (catContract.getSettingsWay() == null || catContract.getSettingsWay().equals(CatContract.SettingsWay.PARTNER)) {

            return entityManager.createQuery("SELECT o FROM Risk o WHERE typeOfRisk = :t AND deleted = false AND " +
                    "EXISTS (SELECT o1 FROM CatContractRisk o1 WHERE o1.partner = :p AND o1.risk = o AND o1.catContract = :cc)", Risk.class)
                    .setParameter("t", riskService.getTypeOfRiskById(2l))
                    .setParameter("p", p)
                    .setParameter("cc", catContract)
                    .getResultList();
        }

        if(catContract.getSettingsWay().equals(CatContract.SettingsWay.GLOBAL)){
            return entityManager.createQuery("SELECT o FROM Risk o WHERE typeOfRisk = :t AND deleted = false AND " +
                    "EXISTS (SELECT o1 FROM CatContractRisk o1 WHERE o1.partner is null AND o1.risk = o AND o1.catContract = :cc)", Risk.class)
                    .setParameter("t", riskService.getTypeOfRiskById(2l))
                    .setParameter("cc", catContract)
                    .getResultList();
        }

        return null;
    }

    @Override
    public List<PartnerRiskCorrelation> getPartnerRiskCorrelations(Partner partner, CatContract catContract) {
        List<PartnerRiskCorrelation> partnerRiskCorrelations = entityManager
                .createQuery(
                        "SELECT o FROM PartnerRiskCorrelation o WHERE partner = :p AND catContract = :cc",
                        PartnerRiskCorrelation.class
                )
                .setParameter("p", partner)
                .setParameter("cc", catContract)
                .getResultList();

        return partnerRiskCorrelations;
    }

    @Override
    public List<Risk> getPotentialPartnerRiskCorrelations(Partner partner, CatContract catContract) {
        List<Risk> risks = entityManager
                .createQuery(
                        "SELECT o FROM Risk o WHERE " +
                                "EXISTS (SELECT o1 FROM CatContractRisk o1 WHERE o1.partner is null AND o1.catContract = :cc AND o1.risk = o)" +
                                "AND NOT EXISTS (SELECT o2 FROM PartnerRiskCorrelation o2 WHERE o2.partner = :p AND o2.catContract = :cc AND o2.risk = o)",
                        Risk.class
                )
                .setParameter("cc", catContract)
                .setParameter("p", partner)
                .getResultList();

        return risks;
    }

    @Override
    public void addNewRisksSet(Partner partner, CatContract catContract, String name, List<Risk> risks) {
        ProductRiskSet productRiskSet = new ProductRiskSet();

        productRiskSet.setName(name);
        productRiskSet.setPartner(partner);
        productRiskSet.setCatContract(catContract);

        productRiskSet.setRisks(risks);
        productRiskSet.setDeleted(false);

        entityManager.persist(productRiskSet);
    }

    @Override
    public List<ProductRiskSet> getProductRiskSets(Partner partner, CatContract catContract) {
        return entityManager
                .createQuery(
                    "SELECT o FROM ProductRiskSet o WHERE partner = :p AND catContract = :cc AND deleted != true",
                    ProductRiskSet.class
                )
                .setParameter("p", partner)
                .setParameter("cc", catContract)
                .getResultList();
    }

    @Override
    public void deleteRisksSet(Long setId) {
        entityManager.createQuery("UPDATE ProductRiskSet o SET deleted = true WHERE id = :id")
                .setParameter("id", setId)
                .executeUpdate();
    }

    @Override
    public ProductRiskSet getProductRiskSetById(Long id) {
        return entityManager.find(ProductRiskSet.class, id);
    }

    @Override
    public Risk getRiskByDet(String ext) {
        Query q = entityManager.createQuery("SELECT o FROM Risk o WHERE det = :d").setParameter("d", ext);

        if(q.getResultList().isEmpty()) return null;
        else return (Risk) q.getSingleResult();
    }

    @Override
    public void newCatContractRisk(CatContractRisk ccr) {
        entityManager.persist(ccr);
    }

    @Override
    public CatContractRisk getCatContractRiskById(Long id) {
        return entityManager.find(CatContractRisk.class, id);
    }

    @Override
    public void removeCatContractRisk(CatContractRisk ccr) {
        entityManager.remove(ccr);
    }

    @Override
    public List<CatContractRisk> getRisksToCalcForContract(Contract contract) {

        switch (contract.getCatContract().getProduct()){
            case BA0:
                    if(contract.getProductRiskSet() != null){
                        return entityManager
                                .createQuery(
                                        "SELECT o FROM CatContractRisk o WHERE partner = :p AND catContract = :cc AND risk IN (" +
                                                ":risks" +
                                                ")",
                                        CatContractRisk.class
                                )
                                .setParameter("p", contract.getPartner())
                                .setParameter("cc", contract.getCatContract())
                                .setParameter("risks", contract.getProductRiskSet().getRisks())
                                .getResultList();
                    }
                    else{
                        return entityManager.createQuery("SELECT o FROM CatContractRisk o WHERE partner = :p AND catContract = :cc")
                                .setParameter("p", contract.getPartner())
                                .setParameter("cc", contract.getCatContract()).getResultList();
                    }
            case MP0:

                return entityManager.createQuery("SELECT o FROM CatContractRisk o WHERE partner is null AND catContract = :cc")
                        .setParameter("cc", contract.getCatContract()).getResultList();

            case TP0:

                return entityManager.createQuery("SELECT o FROM CatContractRisk o WHERE partner is null AND catContract = :cc")
                        .setParameter("cc", contract.getCatContract()).getResultList();

            case TP2:

                return entityManager.createQuery("SELECT o FROM CatContractRisk o WHERE partner is null AND catContract = :cc")
                        .setParameter("cc", contract.getCatContract()).getResultList();


            default:

                return entityManager.createQuery("SELECT o FROM CatContractRisk o WHERE partner = :p AND catContract = :cc")
                        .setParameter("p", contract.getPartner())
                        .setParameter("cc", contract.getCatContract()).getResultList();

        }


    }

    @Override
    public List<CatContractRisk> getRisksForCatContractToAnyPartnerIfPossible(CatContract catContract){
        switch (catContract.getProduct()){
            case MP0:

                return entityManager.createQuery("SELECT o FROM CatContractRisk o WHERE partner is null AND catContract = :cc")
                        .setParameter("cc", catContract).getResultList();

            case TP0:

                return entityManager.createQuery("SELECT o FROM CatContractRisk o WHERE partner is null AND catContract = :cc")
                        .setParameter("cc", catContract).getResultList();

            default:
                throw new IllegalArgumentException("Cant get risks for " + catContract.getProduct() + "without Partner!");
        }
    }

    @Override
    public List<Risk> getParentRisksForCatContractToAnyPartnerIfPossible(CatContract catContract){
        switch (catContract.getProduct()){

            case TP0:

                return entityManager.createQuery("SELECT o FROM Risk o WHERE EXISTS (" +
                            "SELECT o1 FROM Risk o1 WHERE o1.parentRisk = o AND EXISTS (" +
                                "SELECT o2 FROM CatContractRisk o2 WHERE o2.risk = o1 AND o2.partner is null AND o2.catContract = :cc" +
                            ")" +
                        ")", Risk.class)
                        .setParameter("cc", catContract).getResultList();

            default:
                throw new IllegalArgumentException("Cant get parent risks for " + catContract.getProduct() + "without Partner!");
        }
    }

}
