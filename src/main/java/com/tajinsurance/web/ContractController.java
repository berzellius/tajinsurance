package com.tajinsurance.web;

import com.google.gson.Gson;
import com.tajinsurance.domain.*;
import com.tajinsurance.domain.Currency;
import com.tajinsurance.dto.*;
import com.tajinsurance.exceptions.*;
import com.tajinsurance.service.*;
import com.tajinsurance.utils.CodeUtils;
import com.tajinsurance.utils.LanguageUtil;
import com.tajinsurance.utils.UserLoginUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Locale;

@RequestMapping("/contracts")
@Controller
@RooWebScaffold(path = "contracts", formBackingObject = Contract.class)
public class ContractController {
    @Autowired
    ServletContext servletContext;

    @Autowired
    ProductProcessor productProcessor;

    @Autowired
    ProductProcessorTP0 productProcessorTP0;

    @Autowired
    ProductProcessorTP2 productProcessorTP2;

    @Autowired
    ContractService contractService;

    @Autowired
    CatContractService catContractService;

    @Autowired
    CatContractStatusService catContractStatusService;

    @Autowired
    PartnerService partnerService;

    @Autowired
    PersonService personService;

    @Autowired
    UserLoginUtil userLoginUtil;

    @Autowired
    PremiumService premiumService;

    @Autowired
    LanguageUtil languageUtil;

    @Autowired
    RiskService riskService;

    @Autowired
    CodeUtils codeUtils;

    @Autowired
    InsuranceAreaService insuranceAreaService;

    @Autowired
    ExchangeCurrenciesService exchangeCurrenciesService;


    private static final Logger log = Logger.getLogger(ContractController.class);

    @RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(Contract contract, Model uiModel, HttpServletRequest httpServletRequest) {
        try {
            contractService.save(contract);
            uiModel.asMap().clear();
            return "redirect:/contracts/" + encodeUrlPathSegment(contract.getId().toString(), httpServletRequest);
        } catch (EntityNotSavedException e) {
            uiModel.addAttribute("error", e.getMessage());
            return "redirect:/contracts/";
        }
    }


    @RequestMapping(params = "form", produces = "text/html")
    public String createForm(
            Model uiModel, HttpServletRequest httpServletRequest,
            @RequestParam(value = "category", required = false)
            Long categoryId
    ) {

        User currectUser = userLoginUtil.getCurrentLogInUser();
        Locale locale = LocaleContextHolder.getLocale();

        CatContract cc = catContractService.getCatContractById(categoryId);
        if (cc == null) return "redirect:/contracts?select_category";
        //throw new IllegalArgumentException("there is no contract categories with id = "+categoryId);

        Contract preparedContract = contractService.preparedContract(currectUser, cc);

        //populateEditForm(uiModel, preparedContract);

//        uiModel.addAttribute("catContracts", catContractService.getAllowedCatContractsForUser(currectUser, locale.getLanguage()));
//        uiModel.addAttribute("catContractStatuses", catContractStatusService.getAllCatContractStatuses(locale.getLanguage()));
//        uiModel.addAttribute("partners", partnerService.getPartners());

        return "redirect:/contracts/" + encodeUrlPathSegment(preparedContract.getId().toString(), httpServletRequest) + "?form";
        //return "contracts/create";
    }


    @RequestMapping(value = "/{id}", params = "print_receipt", produces = "text/html")
    public String printReceipt(
            Model uiModel,
            @PathVariable("id") Long id

    ) {

        Locale locale = LocaleContextHolder.getLocale();

        try {
            Contract contract = contractService.getContractById(id, locale.getLanguage());

            uiModel.addAttribute("contract", contract);

            uiModel.addAttribute("print_version", "yes");

            BigDecimal pay;
            if (contract.getPremiumAdm() == null) {
                pay = productProcessor.getProductProcessorImplementation(contract).getAllPremiumForContract(contract);
                //pay = riskService.getSumForAllRisksWithoutSaving(contract, contract.getSum(), contract.getLength());
            } else {
                pay = contract.getPremiumAdm();
            }

            if (contract.getCurrency() != null && !contract.getCurrency().equals(exchangeCurrenciesService.basicCurrency())) {
                if (contract.getCurrencyExchange() == null) {
                    contract.setCurrencyExchange(exchangeCurrenciesService.getExchangeToCurrency(contract.getCurrency()));
                    contractService.saveContractState(contract);
                }

                pay = exchangeCurrenciesService.doExchange(pay, contract.getCurrencyExchange());
            }

            uiModel.addAttribute("sumToPay", pay.setScale(0, RoundingMode.HALF_UP));

            switch (contract.getPartner().getPaymentAccept()) {


                case COMPANY:
                    return "contracts/print/receipt/bima";

                case PARTNER:

                    return "contracts/print/receipt/partner";


            }

        } catch (NoEntityException e) {
            e.printStackTrace();
        } catch (CalculatePremiumException e) {
            e.printStackTrace();
        }

        return null;
    }

    @RequestMapping(value = "/{id}", produces = "text/html")
    public String show(
            @PathVariable("id") Long id,
            @RequestParam(value = "print", required = false) String print,
            Model uiModel
    ) {

        try {
            Locale locale = LocaleContextHolder.getLocale();

            Contract contract = contractService.getContractById(id, locale.getLanguage());
            if (print != null) {
                contractService.printContract(contract);
                uiModel.addAttribute("print_version", true);
            }


            uiModel.addAttribute("contract", contract);
            Currency currency = contract.getCurrency() != null ? contract.getCurrency() : contract.getCatContract().getCurrency();
            uiModel.addAttribute("cur", currency);
            uiModel.addAttribute("itemId", id);

            List<ContractPremium> validatedPremiums = premiumService.getValidatedCPremiums(contract);
            uiModel.addAttribute("validatedPremiums", validatedPremiums);


            if (validatedPremiums != null) {

                uiModel.addAttribute("premiums", premiumService.getValidatedCPremiums(contract));

                if (contract.getInsuredSumAdm() == null) {
                    BigDecimal insuredSumContract = productProcessor.getProductProcessorImplementation(contract).getIntegralInsuredSumForContract(contract);
                    uiModel.addAttribute("insuredSumContract", insuredSumContract);
                } else {
                    uiModel.addAttribute("insuredSumContract", contract.getInsuredSumAdm());
                }

                if (contract.getPremiumAdm() == null) {
                    BigDecimal premium = BigDecimal.ZERO;


                    if (contract.getCatContract().getProduct().equals(CatContract.Product.CU0)) {
                        for (ContractPremium cp : validatedPremiums) {
                            premium = premium.add(cp.getPremium());
                        }
                    } else {
                        premium = productProcessor.getProductProcessorImplementation(contract).getAllPremiumForContract(contract);
                    }

                    uiModel.addAttribute("premiumSumContract", premium);
                } else {
                    uiModel.addAttribute("premiumSumContract", contract.getPremiumAdm());
                }
            }

            Person p = contract.getPerson();
            if (p != null) uiModel.addAttribute("person", p);

            if (print != null) {

                if (contract.getCatContract() != null) {
                    BigDecimal insured_sum;
                    if (contract.getInsuredSumAdm() == null) {
                        //insured_sum = riskService.getMajorInsuredSumForAllRisks(contract, contract.getSum());
                        insured_sum = productProcessor.getProductProcessorImplementation(contract).getIntegralInsuredSumForContract(contract);
                    } else {
                        insured_sum = contract.getInsuredSumAdm();
                    }


                    BigDecimal max_insured_sum = riskService.getMaxInsuredSum(contract);

                    BigDecimal pay;
                    if (contract.getPremiumAdm() == null) {
                        pay = productProcessor.getProductProcessorImplementation(contract).getAllPremiumForContract(contract);
                    } else {
                        pay = contract.getPremiumAdm();
                    }

                    List<CatContractRisk> risks = riskService.getRisksToCalcForContract(contract);

                    if (contract.getCatContract().getProduct().equals(CatContract.Product.MP0)) {

                        if (contract.getInsuranceCostAdm() == null) {
                            uiModel.addAttribute(
                                    "fullCost",
                                    productProcessor.getProductProcessorImplementation(contract).getFullCost(contract)
                            );
                        } else {
                            uiModel.addAttribute(
                                    "fullCost",
                                    contract.getInsuranceCostAdm()
                            );
                        }


                        if (contract.getCatContract().getUseInsuranceAreas() != null && contract.getCatContract().getUseInsuranceAreas()) {
                            List<InsuranceArea> insuranceAreas = insuranceAreaService.getInsuranceAreasForContract(contract);


                            // Рассчет снижения страховой суммы в отношении товарно-мат ценностей в обороте

                            // страховая сумма по мат ценностям в обороте по всем терриориям
                            BigDecimal propInTurnOverInsSum = BigDecimal.ZERO;

                            for (InsuranceArea insuranceArea : insuranceAreas) {
                                for (InsuranceObject insuranceObject : insuranceArea.getInsuranceObjectList()) {
                                    if (insuranceObject.getRisk().getDet() != null && insuranceObject.getRisk().getDet().equals("TMO")) {
                                        propInTurnOverInsSum = propInTurnOverInsSum.add(insuranceObject.getSum());
                                    }
                                }
                            }

                            if (propInTurnOverInsSum.compareTo(BigDecimal.ZERO) > 0) {
                                BigDecimal propInTurnOverInsSumDelta = propInTurnOverInsSum.divide(BigDecimal.valueOf(contract.getLength()), 0, RoundingMode.HALF_UP);
                                uiModel.addAttribute("propInTurnOverInsSumDelta", propInTurnOverInsSumDelta);
                            }

                            uiModel.addAttribute("insuranceAreas", insuranceAreas);
                        }
                    }

                    uiModel.addAttribute("insured_sum", insured_sum.setScale(0, RoundingMode.HALF_UP));
                    uiModel.addAttribute("insuredSum", insured_sum.setScale(0, RoundingMode.HALF_UP));
                    uiModel.addAttribute("max_insured_sum", max_insured_sum.setScale(0, RoundingMode.HALF_UP));
                    uiModel.addAttribute("sumToPay", pay.setScale(0, RoundingMode.HALF_UP));
                    uiModel.addAttribute("risks", risks);
                    uiModel.addAttribute("length", contract.getLength() * 30);


                    if (contract.getCatContract().getProduct().equals(CatContract.Product.CU0)) {
                        return "contracts/print/cu0";
                    }

                    if (contract.getCatContract().getProduct().equals(CatContract.Product.BA0)) {
                        return "contracts/print/ba0";
                    }

                    if (contract.getCatContract().getProduct().equals(CatContract.Product.MP0)) {
                        return "contracts/print/mp0";
                    }

                    if (contract.getCatContract().getProduct().equals(CatContract.Product.TP0)) {
                        BigDecimal premium = productProcessor.getProductProcessorImplementation(contract).getAllPremiumForContract(contract);
                        if (contract.getCurrencyExchange() == null) {
                            contract.setCurrencyExchange(exchangeCurrenciesService.getExchangeToCurrency(contract.getCurrency()));
                            contractService.saveContractState(contract);
                        }

                        uiModel.addAttribute("premiumSumContractSomoni", exchangeCurrenciesService.doExchange(premium, contract.getCurrencyExchange()));

                        ProductMoneyProperty franchise = partnerService.getProductMoneyPropertyByPropertyName(
                                ProductMoneyProperty.PropertyType.TP0_D_franchise_sum,
                                contract.getCatContract()
                        );

                        ProductMoneyProperty discount = partnerService.getProductMoneyPropertyByPropertyName(
                                ProductMoneyProperty.PropertyType.TP0_D_franchise_discount_percent,
                                contract.getCatContract()
                        );

                        if (
                                franchise != null && discount != null && franchise.getMoneyValue() != null && discount.getMoneyValue() != null
                                        && discount.getMoneyValue().compareTo(BigDecimal.valueOf(100)) < 0
                                ) {
                            uiModel.addAttribute("D_franchise", franchise.getMoneyValue());
                        }

                        return "contracts/print/tp0";
                    }


                }

                return "contracts/print";
            }


            /*System.out.println(locale.getLanguage());*/
        } catch (NoEntityException e) {
            uiModel.addAttribute("error", e.getMessage());
        } catch (CalculatePremiumException e) {
            e.printStackTrace();
        }


        return "contracts/show";
    }


    @RequestMapping(produces = "text/html")
    public String list(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "sortFieldName", required = false) String sortFieldName,
            @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {

        User u = userLoginUtil.getCurrentLogInUser();

        Locale locale = LocaleContextHolder.getLocale();

        AjaxContractListFilter filter = null;

        switch (userLoginUtil.getMaxUserAuthorityCode(u)) {
            case ROLE_USER_PARTNER:
                filter = new AjaxContractListFilter(u.getPartner());
                filter.creator = u;
                break;
            case ROLE_ADMIN_PARTNER:
                filter = new AjaxContractListFilter(u.getPartner());
                break;
        }


        List<Contract> contracts = contractService.getContractsPage(filter, page, size, sortFieldName, sortOrder);

        LinkedHashMap<String, List> listVal = new LinkedHashMap<String, List>();
        listVal.put("catContract", catContractService.getAllowedCatContractsForUser(u, locale.getLanguage()));
        listVal.put("catContractStatus", catContractStatusService.getAllCatContractStatuses(locale.getLanguage()));
        listVal.put("paymentWay", contractService.getAllPaymentWays());

        uiModel.addAttribute("listVal", listVal);

        uiModel.addAttribute("contracts", contractService.localizeContractsList(contracts, locale.getLanguage()));

        uiModel.addAttribute("maxPages", contractService.countPages(filter, page, size, sortFieldName, sortOrder));

        return "contracts/clist";
    }


    private String listByFilterObject(
            AjaxContractListFilter filter,
            Integer page,
            Integer size,
            String sortFieldName,
            String sortOrder,
            Boolean excel,
            Model uiModel,
            HttpServletResponse response
    ) {
        User u = userLoginUtil.getCurrentLogInUser();

        switch (userLoginUtil.getMaxUserAuthorityCode(u)) {
            case ROLE_USER_PARTNER:
                if (filter == null) {
                    filter = new AjaxContractListFilter();
                }
                filter.creator = u;
                break;
            case ROLE_ADMIN_PARTNER:
                if (filter == null) {
                    filter = new AjaxContractListFilter();
                }
                filter.partner = u.getPartner();
                break;
        }


        Locale locale = LocaleContextHolder.getLocale();

        List<Contract> contracts = contractService.getContractsPage(filter, page, size, sortFieldName, sortOrder);


        if (excel) {
            List<Contract> cts = contractService.localizeContractsList(contracts, locale.getLanguage());

            if (cts == null) return null;

            String filename = "contracts_for_admin.xls";


            uiModel.addAttribute("data", cts);

            response.setHeader("Content-Type", "application/vnd.ms-excel; charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + filename);
            return "excel/contracts/export/common";
        }

        uiModel.addAttribute("contracts", contractService.localizeContractsList(contracts, locale.getLanguage()));

        LinkedHashMap<String, List> listVal = new LinkedHashMap<String, List>();
        listVal.put("catContract", catContractService.getAllowedCatContractsForUser(u, locale.getLanguage()));
        listVal.put("catContractStatus", catContractStatusService.getAllCatContractStatuses(locale.getLanguage()));
        listVal.put("paymentWay", contractService.getAllPaymentWays());

        uiModel.addAttribute("listVal", listVal);


        uiModel.addAttribute("maxPages", contractService.countPages(filter, page, size, sortFieldName, sortOrder));
        //uiModel.addAttribute("maxPages", contractService.countPages(size));

        return "ajax/contracts/clist";
    }

    @RequestMapping(produces = "text/html", method = RequestMethod.POST, params = "ajaxList")
    public String listByFilterString(
            @RequestParam(value = "filter", required = true) String filter,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "sortFieldName", required = false) String sortFieldName,
            @RequestParam(value = "sortOrder", required = false) String sortOrder,
            @RequestParam(value = "excel", required = false) Boolean excel,
            Model uiModel,
            HttpServletResponse response
    ) {
        Gson gson = new Gson();

        try {
            if (excel == null) excel = false;

            User u = userLoginUtil.getCurrentLogInUser();
            UserRole.AuthorityCode maxAutLvl = userLoginUtil.getMaxUserAuthorityCode(u);
            Locale locale = LocaleContextHolder.getLocale();

            JSONObject json = (JSONObject) new JSONParser().parse(filter);

            SimpleDateFormat sf = new SimpleDateFormat("dd.MM.yyyy");

            AjaxContractListFilter ajaxFilter = new AjaxContractListFilter();
            ajaxFilter.partner = u.getPartner();

            try {
                ajaxFilter.number = (String) json.get("number");
                if (ajaxFilter.number == "") ajaxFilter.number = null;
            } catch (RuntimeException e) {
                ajaxFilter.number = null;
            }

            try {
                ajaxFilter.personMask = (String) json.get("person");
                if (ajaxFilter.personMask == "") ajaxFilter.personMask = null;
            } catch (RuntimeException e) {
                ajaxFilter.personMask = null;
            }

            try {
                ArrayList<String> cats = (ArrayList<String>) json.get("catContract");
                if (cats.size() > 0) {
                    ajaxFilter.categories = new ArrayList<Long>();
                    for (String ct : cats) ajaxFilter.categories.add(Long.decode(ct));
                }
            } catch (RuntimeException e) {
                ajaxFilter.categories = null;
            }

            try {
                ArrayList<String> st = (ArrayList<String>) json.get("catContractStatus");
                if (st.size() > 0) {
                    ajaxFilter.statuses = new ArrayList<Long>();
                    for (String s : st) ajaxFilter.statuses.add(Long.decode(s));
                }
            } catch (RuntimeException e) {
                ajaxFilter.statuses = null;
            }

            try {
                ArrayList<String> ptw = (ArrayList<String>) json.get("paymentWay");
                if (ptw.size() > 0) {
                    ajaxFilter.paymentWays = new ArrayList<Long>();
                    for (String s : ptw) ajaxFilter.paymentWays.add(Long.decode(s));
                }
            } catch (RuntimeException e) {
                ajaxFilter.paymentWays = null;
            }

            try {
                ajaxFilter.startDateFrom = sf.parse((String) json.get("startDateFrom"));
            } catch (java.text.ParseException e) {
                ajaxFilter.startDateFrom = null;
            }

            try {
                ajaxFilter.startDateTo = sf.parse((String) json.get("startDateTo"));
            } catch (java.text.ParseException e) {
                ajaxFilter.startDateTo = null;
            }

            try {
                ajaxFilter.creatorStr = (String) json.get("creator");
                if (ajaxFilter.creatorStr == "") ajaxFilter.creatorStr = null;
            } catch (RuntimeException e) {
                ajaxFilter.creatorStr = null;
            }

            try {
                ajaxFilter.partnerStr = (String) json.get("partner");
                if (ajaxFilter.partnerStr == "") ajaxFilter.partnerStr = null;
            } catch (RuntimeException e) {
                ajaxFilter.partnerStr = null;
            }


            try {
                ajaxFilter.orderColumn = (String) json.get("orderColumn");
                if (ajaxFilter.orderColumn == "") ajaxFilter.orderColumn = null;
            } catch (RuntimeException e) {
                ajaxFilter.orderColumn = null;
            }

            try {
                ajaxFilter.orderType = (String) json.get("orderType");

                if (
                        (!ajaxFilter.orderType.equals("ASC") && !ajaxFilter.orderType.equals("DESC"))
                                || ajaxFilter.orderType == ""
                        ) {
                    ajaxFilter.orderType = null;
                }

            } catch (RuntimeException e) {
                ajaxFilter.orderType = null;
            }

            ajaxFilter.isPaid = (Boolean) json.get("isPaid");
            ajaxFilter.isApp = (Boolean) json.get("isApp");
            ajaxFilter.isPrinted = (Boolean) json.get("isPrinted");


            String filename = null;

            if (excel &&
                    (maxAutLvl == UserRole.AuthorityCode.ROLE_ADMIN || maxAutLvl == UserRole.AuthorityCode.ROLE_USER)) {

                return listByFilterObject(ajaxFilter, page, size, sortFieldName, sortOrder, true, uiModel, response);


            }

            return listByFilterObject(ajaxFilter, page, size, sortFieldName, sortOrder, false, uiModel, response);
        } catch (ParseException e) {
            e.printStackTrace();
            return listByFilterObject(null, page, size, sortFieldName, sortOrder, false, uiModel, response);
        }


    }


    @RequestMapping(method = RequestMethod.POST, params = "upload_file")
    public String uploadFile(
            @RequestParam(value = "contract_id")
            Long contractId,
            @RequestParam(value = "upload", required = false)
            MultipartFile uploadFile,
            @RequestParam(value = "description", required = false)
            String description,
            Model uiModel,
            HttpServletRequest httpServletRequest
    ) {
        Contract contract = null;
        try {
            contract = contractService.getContractById(contractId);

            User user = userLoginUtil.getCurrentLogInUser();
            UserRole.AuthorityCode auth = userLoginUtil.getMaxUserAuthorityCode(user);

            ContractImage contractImage = new ContractImage();
            //contractImage.setPath(codeUtils.getUploadImagesDirPath());
            contractImage.setDescription(description);

            productProcessor.getProductProcessorImplementation(contract).updateContract(
                    contract,
                    uploadFile,
                    contractImage,
                    auth.equals(UserRole.AuthorityCode.ROLE_ADMIN) || auth.equals(UserRole.AuthorityCode.ROLE_USER)
            );
        } catch (NoEntityException e) {
            uiModel.addAttribute("error", e.getMessage());
        } catch (CalculatePremiumException e) {
            uiModel.addAttribute("reason", e.getMessage());

            if (e.getMessage().equals("maximum_sum_failed")) {
                uiModel.addAttribute("max_sum", e.maxSum);
            }

            if (e.getMessage().equals("minimum_sum_failed")) {
                uiModel.addAttribute("min_sum", e.minSum);
            }

            if (e.getMessage().equals("minimum_term_failed")) {
                uiModel.addAttribute("min_term", e.minTerm);
            }

        } catch (NoPersonToContractException e) {
            uiModel.addAttribute("reason", e.getMessage());
        } catch (BadContractDataException e) {
            uiModel.addAttribute("reason", e.getMessage());
        } catch (NoRelatedContractNumber e) {
            uiModel.addAttribute("reason", e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalDataException e) {
            if (e instanceof IllegalImageDataException) {
                uiModel.addAttribute("image_reason", e.getReason().getMsgCode());
            }
        }

        return "redirect:/contracts/" + encodeUrlPathSegment(contract.getId().toString(), httpServletRequest) + "?form";
    }

    @RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(
            Contract contract,
            /*@RequestParam(value = "upload", required = false)
            MultipartFile uploadFile,*/
            Model uiModel,
            HttpServletRequest httpServletRequest) {

        User user = userLoginUtil.getCurrentLogInUser();
        UserRole.AuthorityCode auth = userLoginUtil.getMaxUserAuthorityCode(user);


        if (contract.getPerson() != null) {
            contractService.setPersonToContract(contract);
        }


        try {

            productProcessor.getProductProcessorImplementation(contract).updateContract(
                    contract,
                    /*uploadFile,*/
                    auth.equals(UserRole.AuthorityCode.ROLE_ADMIN) || auth.equals(UserRole.AuthorityCode.ROLE_USER)
            );
            //if(contract.getEndDate() == null) return "redirect:/contracts/" + encodeUrlPathSegment(contract.getId().toString(), httpServletRequest) + "?form&reason=period_not_calc";

            uiModel.asMap().clear();

        } catch (NoEntityException e) {
            uiModel.addAttribute("error", e.getMessage());
        } catch (CalculatePremiumException e) {
            uiModel.addAttribute("reason", e.getMessage());

            if (e.getMessage().equals("maximum_sum_failed")) {
                uiModel.addAttribute("max_sum", e.maxSum);
            }

            if (e.getMessage().equals("minimum_sum_failed")) {
                uiModel.addAttribute("min_sum", e.minSum);
            }

            if (e.getMessage().equals("minimum_term_failed")) {
                uiModel.addAttribute("min_term", e.minTerm);
            }

        } catch (NoPersonToContractException e) {
            uiModel.addAttribute("reason", e.getMessage());
        } catch (BadContractDataException e) {
            uiModel.addAttribute("reason", e.getMessage());
        } catch (NoRelatedContractNumber e) {
            uiModel.addAttribute("reason", e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalDataException e) {
            // todo надо делать все исключения, порождающие пользовательские сообщения об ошибке,
            // делать потомками IllegalDataException. И добавять их обработку здесь
        }
        return "redirect:/contracts/" + encodeUrlPathSegment(contract.getId().toString(), httpServletRequest) + "?form";
    }


    @RequestMapping(value = "/{id}", params = "form", produces = "text/html")
    public String updateForm(@PathVariable("id") Long id,
                             Model uiModel,
                             @RequestParam(value = "image_reason", required = false) String image_reason,
                             @RequestParam(value = "reason", required = false) String reason,
                             @RequestParam(value = "max_sum", required = false) String max_sum,
                             @RequestParam(value = "min_sum", required = false) String min_sum,
                             @RequestParam(value = "min_term", required = false) String min_term
    ) {
        try {

            Locale locale = LocaleContextHolder.getLocale();

            Contract c = contractService.getContractById(id, locale.getLanguage());


            // Показать предупреждения и сообщения об ошибках
            Integer maxAgeMale = productProcessor
                    .getProductProcessorImplementation(c)
                    .getMaxAgeForMale();
            Integer maxAgeFemale = productProcessor
                    .getProductProcessorImplementation(c)
                    .getMaxAgeForFemale();
            showAttensions(uiModel, reason, image_reason, max_sum, min_sum, min_term, maxAgeMale, maxAgeFemale);

            User currentUser = userLoginUtil.getCurrentLogInUser();

            if (userLoginUtil.getMaxUserAuthorityCode(currentUser) == UserRole.AuthorityCode.ROLE_USER || userLoginUtil.getMaxUserAuthorityCode(currentUser) == UserRole.AuthorityCode.ROLE_ADMIN) {
                uiModel.addAttribute("managerMode", true);
            }


            populateEditForm(uiModel, c);

            imagesForContract(uiModel, c);


            List<RiskAjax> risks = catContractService.getAllowedRisksForCatContract(c.getCatContract());


            List<ContractPremiumAjax> validatedPremiums = premiumService.getValidatedPremiums(c);
            if (validatedPremiums != null) {
                BigDecimal premium = BigDecimal.ZERO;
                /*for (ContractPremiumAjax contractPremium : validatedPremiums) {
                    premium = premium.add(contractPremium.getPremium());
                }*/

                if (c.getCatContract().getProduct().equals(CatContract.Product.CU0)) {
                    for (ContractPremiumAjax contractPremium : validatedPremiums) {
                        premium = premium.add(contractPremium.getPremium());
                    }
                } else premium = productProcessor.getProductProcessorImplementation(c).getAllPremiumForContract(c);


                if (c.getPremiumAdm() == null) {
                    uiModel.addAttribute("allPremium", premium.setScale(0, RoundingMode.HALF_UP));
                } else {
                    uiModel.addAttribute("allPremium", c.getPremiumAdm().setScale(0, RoundingMode.HALF_UP));
                }


                BigDecimal insuredSumContract = productProcessor.getProductProcessorImplementation(c).getIntegralInsuredSumForContract(c);

                if (c.getInsuredSumAdm() == null) {
                    uiModel.addAttribute("insuredSumContract", insuredSumContract.setScale(0, RoundingMode.HALF_UP));
                } else {
                    uiModel.addAttribute("insuredSumContract", c.getInsuredSumAdm().setScale(0, RoundingMode.HALF_UP));
                }
            }

            List<CatContractRisk> catContractRiskList = riskService.getRisksToCalcForContract(c);

            if (catContractRiskList == null || catContractRiskList.size() == 0) {
                uiModel.addAttribute("no_risks", true);
            }


            uiModel.addAttribute("allowedRisks", risks);

            if (c.getClaimSigned() != null && c.getClaimSigned()) uiModel.addAttribute("claimSigned", true);


            Currency currency = c.getCurrency() != null ? c.getCurrency() : c.getCatContract().getCurrency();
            uiModel.addAttribute("cur", currency);


            // uiModel.addAttribute("catContracts", catContractService.getAllowedCatContractsForUser(currectUser, locale.getLanguage()));
            uiModel.addAttribute("catContract", c.getCatContract());
            uiModel.addAttribute("catContractStatuses", catContractStatusService.getAllCatContractStatuses(locale.getLanguage()));
            // uiModel.addAttribute("partners", partnerService.getPartners());

            if (c.getCatContract().getUseInsuranceAreas() != null && c.getCatContract().getUseInsuranceAreas()) {
                List<InsuranceArea> insuranceAreas = insuranceAreaService.getInsuranceAreasForContract(c);
                uiModel.addAttribute("insuranceAreas", insuranceAreas);

                List<Risk> risksForAreas = riskService.getRisksForInsuranceArea(c.getPartner(), c.getCatContract());
                uiModel.addAttribute("risksForAreas", risksForAreas);

                HashMap<Long, List<SecuritySystem>> securitySystemsAvailableForAreas = new HashMap<Long, List<SecuritySystem>>();

                for (InsuranceArea insuranceArea : insuranceAreas) {
                    List<SecuritySystem> securitySystemsAvailable = insuranceAreaService.getSecuritySystemsAvailable(insuranceArea);
                    securitySystemsAvailableForAreas.put(insuranceArea.getId(), securitySystemsAvailable);
                }

                uiModel.addAttribute("securitySystemsAvailableForAreas", securitySystemsAvailableForAreas);


            }


            if (c.getCatContract().getProduct().equals(CatContract.Product.MP0)) {
                if (currentUser.getPartner() != null) {
                    // Если исп-ся франшиза, загружаем допустимые значения
                    List<CatContractPartnerFranchise> franchises = partnerService.getFranchiseValuesAllowed(c.getCatContract(), currentUser.getPartner());
                    uiModel.addAttribute("franchises", franchises);

                    // Разрешено ли выбирать способ возмещения?
                    if (partnerService.paymentChooseAllowed(currentUser.getPartner(), c.getCatContract())) {
                        uiModel.addAttribute("paymentChooseAllowed", true);
                    }
                }
            }

            if (c.getCatContract().getProduct().equals(CatContract.Product.BA0)) {
                List<ProductRiskSet> productRiskSets = riskService.getProductRiskSets(c.getPartner(), c.getCatContract());
                uiModel.addAttribute("productRiskSets", productRiskSets);
            }

            if (c.getCatContract().getProduct().equals(CatContract.Product.TP0)) {
                uiModel.addAttribute("risksMustBeAddedToContract", productProcessor
                        .getProductProcessorImplementation(c)
                        .getRisksMustBeAddedToContract(c)
                );

                uiModel.addAttribute("maxLength", productProcessorTP0.getMaxLength());


                // Если исп-ся франшиза, загружаем допустимые значения
                List<CatContractPartnerFranchise> franchises = partnerService.getFranchiseValuesAllowed(c.getCatContract(), null);
                uiModel.addAttribute("franchises", franchises);

                loadPotentialSellers(uiModel, currentUser, locale);

                return "contracts/create/tp0";
            }

            if (c.getCatContract().getProduct().equals(CatContract.Product.TP2)) {
                Date payInDate = null;
                if (c.getAppDate() != null) {
                    Calendar pidCal = Calendar.getInstance();
                    pidCal.setTime(c.getAppDate());
                    pidCal.add(Calendar.DATE, 1);
                    payInDate = pidCal.getTime();
                }

                uiModel.addAttribute("payInDate", payInDate);

                PartnerProductMoneyProperty partnerProductMoneyPropertyUseExtPack = (PartnerProductMoneyProperty) partnerService.getPartnerProductMoneyPropertyByPropertyName(currentUser.getPartner(), ProductMoneyProperty.PropertyType.TP2_use_extended_pack, c.getCatContract());

                if (partnerProductMoneyPropertyUseExtPack != null && partnerProductMoneyPropertyUseExtPack.getUseProperty()) {
                    uiModel.addAttribute("useExtendedPack", true);
                } else {
                    uiModel.addAttribute("useExtendedPack", false);
                }

                loadPotentialSellers(uiModel, currentUser, locale);

                return "contracts/create/tp2";
            }

            if(c.getCatContract().getProduct().equals(CatContract.Product.RU0)){
                return "contracts/create/ru0";
            }

        } catch (NoEntityException e) {
            uiModel.addAttribute("error", e.getMessage());
        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (CalculatePremiumException e) {
            e.printStackTrace();
        }


        return "contracts/create";
    }

    private void loadPotentialSellers(Model uiModel, User currectUser, Locale locale) {
        if (userLoginUtil.isUserHasAuthority(currectUser, UserRole.AuthorityCode.ROLE_ADMIN_PARTNER)) {

            List<UserRole.AuthorityCode> roles = new LinkedList<UserRole.AuthorityCode>();
            roles.add(UserRole.AuthorityCode.ROLE_USER_PARTNER);

            AjaxUserListFilter ajaxUserListFilter = new AjaxUserListFilter();
            ajaxUserListFilter.isEnabled = true;

            List<User> potentialSellers = userLoginUtil.getUsersList(currectUser.getPartner().getId(), roles, false, locale.getLanguage());
            uiModel.addAttribute("sellers", potentialSellers);

            //System.out.println(potentialSellers + " " + potentialSellers.size());
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        try {
            contractService.cancelContract(contractService.getContractById(id));
            uiModel.asMap().clear();
            uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
            uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        } catch (NoEntityException e) {
            uiModel.addAttribute("error", e.getMessage());
        }
        return "redirect:/contracts";
    }

    void showAttensions(Model uiModel, String reason, String image_reason, String max_sum, String min_sum, String min_term, Integer maxAgeMale, Integer maxAgeFemale) {

        uiModel.addAttribute("maxAgeMale", maxAgeMale);
        uiModel.addAttribute("maxAgeFemale", maxAgeFemale);


        if (reason != null) {
            uiModel.addAttribute("errorMsg", reason);
        }

        if (max_sum != null) {
            uiModel.addAttribute("max_sum", max_sum);
        }

        if (min_sum != null) {
            uiModel.addAttribute("min_sum", min_sum);
        }

        if (min_term != null) {
            uiModel.addAttribute("min_term", min_term);
        }

        if (image_reason != null) {
            uiModel.addAttribute("image_reason", image_reason);
        }
    }

    void populateEditForm(Model uiModel, Contract contract) {
        // Если есть флаг необходимости печати заявления, сбрасываем его и кидаем параметр print_claim во viewer
        if (contract.isPrintClaimFlag() != null && contract.isPrintClaimFlag()) {
            uiModel.addAttribute("print_claim", true);

            contract = contractService.dropPrintClaimFlag(contract);

        }


        uiModel.addAttribute("contract", contract);
        uiModel.addAttribute("person_id", (contract.getPerson() == null ? "0" : contract.getPerson().getId().toString()));
        uiModel.addAttribute("person", (contract.getPerson() == null ? "" : contract.getPerson()));

        if (contract.getCatContract().getProduct().equals(CatContract.Product.TP0)) {
            uiModel.addAttribute("territories", Contract.InsuranceTerritory.values());
        }

        switch (contract.getCatContractStatus().getCode()) {
            case BEGIN:
                uiModel.addAttribute("printable", false);
                break;
            case NEW:
                uiModel.addAttribute("status_new", true);
                uiModel.addAttribute("printable", true);
                break;
            default:
                uiModel.addAttribute("printable", true);
                break;
        }


        List<PaymentWay> paymentWays = contractService.getAllPaymentWays();

        uiModel.addAttribute("paymentWays", paymentWays);
    }

    void imagesForContract(Model uiModel, Contract contract) {
        // Возможность загрузки картинок
        if (
                !contract.getCatContractStatus().getCode().equals(CatContractStatus.StatusCode.BEGIN) &&
                        contract.getCatContract().getProduct().equals(CatContract.Product.MP0)
                ) {
            uiModel.addAttribute("canLoadImages", true);
        }

        // картинки
        if (!contract.getContractImages().isEmpty()) {
            List<ContractImage> contractImages = contract.getContractImages();

            for (ContractImage contractImage : contract.getContractImages()) {
                if (contractImage.getExtension() == null)
                    contractImage.setExtension(FilenameUtils.getExtension(contractImage.getPath()));
            }

            uiModel.addAttribute("images", contract.getContractImages());
        }
    }

    String encodeUrlPathSegment(String pathSegment, HttpServletRequest httpServletRequest) {
        String enc = httpServletRequest.getCharacterEncoding();
        if (enc == null) {
            enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
        }
        try {
            pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
        } catch (UnsupportedEncodingException uee) {
        }
        return pathSegment;
    }

    @RequestMapping(params = "select_category")
    String selectCategory(Model uiModel) {
        User currectUser = userLoginUtil.getCurrentLogInUser();

        Locale locale = LocaleContextHolder.getLocale();

        uiModel.addAttribute("contractCategories", catContractService.getAllowedCatContractsForUser(currectUser, locale.getLanguage()));

        return "contracts/select_category";
    }

    @RequestMapping(params = "print_receipt", method = RequestMethod.GET)
    @ResponseBody
    byte[] printReceiptContract(
            @RequestParam(value = "id") Long id
    ) {

        Gson gson = new Gson();
        ContractPrintAjax cpa = new ContractPrintAjax();

        try {
            Contract c = contractService.getContractById(id);
            if (
                    (c.getReceiptNumber() == null /*||
                            (c.getReceiptMonth() != null && c.getReceiptMonth() < codeUtils.getDigitsForMonth(null))*/
                    ) &&
                            c.getPartner().getPaymentAccept().equals(Partner.PaymentAccept.COMPANY)
                    ) {

                contractService.setReceiptNumber(c, /*codeUtils.getDigitsForMonth(c.getStartDate())*/ null);
            }
            cpa.success = true;
        } catch (NoEntityException e) {
            cpa.success = false;
            cpa.message = "Cant find Contract entity #" + id;
        } finally {
            String g = gson.toJson(cpa);
            try {
                return g.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                return g.getBytes();
            }
        }
    }


    @RequestMapping(params = "print", method = RequestMethod.GET)
    @ResponseBody
    byte[] printContract(
            @RequestParam(value = "id") Long id
    ) {

        Gson gson = new Gson();
        ContractPrintAjax cpa = new ContractPrintAjax();

        try {
            Contract c = contractService.getContractById(id);
            cpa.printDate = contractService.printContract(c);

            cpa.success = true;
        } catch (NoEntityException e) {
            cpa.success = false;
            cpa.message = "Cant find Contract entity #" + id;
        } finally {
            String g = gson.toJson(cpa);
            try {
                return g.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                return g.getBytes();
            }
        }

    }

    @RequestMapping(params = "get_risks_sum", method = RequestMethod.GET, value = "/{id}")
    @ResponseBody
    byte[] getAllRisksSum(
            @PathVariable(value = "id")
            Long contractId,
            @RequestParam(value = "length")
            Integer length,
            @RequestParam(value = "sum", required = false)
            BigDecimal sum,
            @RequestParam(value = "franchise", required = false)
            Integer franchise,
            @RequestParam(value = "paymentType", required = false)
            Contract.PaymentType paymentType,
            @RequestParam(value = "productRiskSet", required = false)
            ProductRiskSet productRiskSet
    ) {
        GetAllRisksPremiumAjax getAllRisksPremiumAjax = new GetAllRisksPremiumAjax();
        Gson gson = new Gson();

        try {
            Contract contract = contractService.getContractById(contractId);
            Contract calcContract = new Contract();
            calcContract.setCatContract(contract.getCatContract());
            calcContract.setPartner(contract.getPartner());
            calcContract.setLength(length);

            if (productRiskSet != null) calcContract.setProductRiskSet(productRiskSet);
            if (sum != null) calcContract.setSum(sum);
            if (franchise != null) calcContract.setFranchise(franchise);
            if (paymentType != null) calcContract.setPaymentType(paymentType);
            //BigDecimal all = riskService.getSumForAllRisksWithoutSaving(contract, sum, length);
            BigDecimal all = productProcessor
                    .getProductProcessorImplementation(contract)
                    .getAllPremiumForContractWithoutSaving(
                            calcContract,
                            insuranceAreaService.getInsuranceAreasForContract(contract)
                    );

            getAllRisksPremiumAjax.sum = all;
            getAllRisksPremiumAjax.success = true;
        } catch (NoEntityException e) {
            getAllRisksPremiumAjax.sum = null;
            getAllRisksPremiumAjax.success = false;
        } catch (CalculatePremiumException e) {
            getAllRisksPremiumAjax.sum = null;
            getAllRisksPremiumAjax.success = false;
            getAllRisksPremiumAjax.message = e.getMessage();
            getAllRisksPremiumAjax.minSum = e.minSum;
            getAllRisksPremiumAjax.maxSum = e.maxSum;
            getAllRisksPremiumAjax.minTerm = e.minTerm;
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            String g = gson.toJson(getAllRisksPremiumAjax);
            try {
                return g.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                return g.getBytes();
            }
        }


    }

    @RequestMapping(params = "cancel", method = RequestMethod.GET)
    @ResponseBody
    byte[] cancel(
            @RequestParam(value = "id", required = true) Long id
    ) {
        Gson gson = new Gson();
        ContractCancelAjax cca = new ContractCancelAjax();
        try {
            Contract c = contractService.getContractById(id);

            contractService.cancelContract(c);

            cca.success = true;
        } catch (NoEntityException e) {
            cca.success = false;
            cca.message = "Cant find Contract entity #" + id;
        } finally {
            String g = gson.toJson(cca);
            try {
                return g.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                return g.getBytes();
            }
        }
    }


    @RequestMapping(value = "export_for_excel_new")
    public String getNewExcelExport(
            Model uiModel, HttpServletResponse response
    ) {
        Locale locale = LocaleContextHolder.getLocale();
        User user = userLoginUtil.getCurrentLogInUser();
        UserRole.AuthorityCode maxAutLvl = userLoginUtil.getMaxUserAuthorityCode(user);

        String filename = null;


        if (maxAutLvl == UserRole.AuthorityCode.ROLE_ADMIN || maxAutLvl == UserRole.AuthorityCode.ROLE_USER) {
            List<Contract> cts = contractService.getAllContracts(locale.getLanguage());

            if (cts == null) return null;

            filename = "contracts_for_admin.xls";


            uiModel.addAttribute("data", cts);

            response.setHeader("Content-Type", "application/vnd.ms-excel; charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + filename);
            return "excel/contracts/export/common";
        }


        if (maxAutLvl == UserRole.AuthorityCode.ROLE_ADMIN_PARTNER) {
            List<Contract> cts = contractService.getAllContracts(locale.getLanguage(), user.getPartner());

            if (cts == null) return null;

            filename = "contracts_for_partner.xls";


            uiModel.addAttribute("data", cts);

            response.setHeader("Content-Type", "application/vnd.ms-excel; charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + filename);
            return "excel/contracts/export/common";
        }

        if (maxAutLvl == UserRole.AuthorityCode.ROLE_USER_PARTNER) {
            List<Contract> cts = contractService.getAllContracts(locale.getLanguage(), user);

            if (cts == null) return null;

            filename = "contracts.xls";


            uiModel.addAttribute("data", cts);

            response.setHeader("Content-Type", "application/vnd.ms-excel; charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + filename);
            return "excel/contracts/export/common";
        }


        return null;
    }


    @RequestMapping(params = "calc")
    public String getCalc(Model uiModel) {

        User user = userLoginUtil.getCurrentLogInUser();

        Locale locale = LocaleContextHolder.getLocale();

        List<CatContract> catContracts = catContractService.getAllowedCatContractsForUser(user, locale.getLanguage());

        uiModel.addAttribute("catContracts", catContracts);

        return "contracts/calc";
    }

    @RequestMapping(params = "calculate", method = RequestMethod.POST)
    @ResponseBody
    byte[] getAllRisksSumForCatContract(
            @RequestParam(value = "length")
            Integer length,
            @RequestParam(value = "sum", required = false)
            BigDecimal sum,
            @RequestParam(value = "risk")
            Long catContractId,
            @RequestParam(value = "risksum[]", required = false)
            ArrayList<BigDecimal> risksums,
            @RequestParam(value = "riskid[]", required = false)
            ArrayList<Long> riskids,
            @RequestParam(value = "securitySystems[]", required = false)
            List<Long> securitySystemsIds,
            @RequestParam(value = "franchise", required = false)
            Integer franchise,
            @RequestParam(value = "paymentType", required = false)
            Contract.PaymentType paymentType,
            @RequestParam(value = "productRiskSet", required = false)
            ProductRiskSet productRiskSet,
            @RequestParam(value = "age", required = false)
            Integer age,
            @RequestParam(value = "insuranceTerritory", required = false)
            Contract.InsuranceTerritory insuranceTerritory,
            @RequestParam(value = "sport", required = false)
            Boolean sport,
            @RequestParam(value = "tp2Extended", required = false)
            Boolean tp2Extended

    ) {
        User user = userLoginUtil.getCurrentLogInUser();


        GetAllRisksPremiumAjax getAllRisksPremiumAjax = new GetAllRisksPremiumAjax();
        Gson gson = new Gson();

        try {
            CatContract cc = catContractService.getCatContractById(catContractId);
            Contract contract = new Contract();
            contract.setCatContract(cc);
            contract.setPartner(user.getPartner());
            contract.setLength(length);

            if (productRiskSet != null)
                contract.setProductRiskSet(productRiskSet);

            if (sum != null) contract.setSum(sum);

            if (cc.getProduct().equals(CatContract.Product.CU0) || cc.getProduct().equals(CatContract.Product.BA0)) {

                List<CatContractRisk> ccrl = riskService.getExistRisks(user.getPartner(), cc);

               /* if (ccrl.size() > 0) {

                } else {
                    getAllRisksPremiumAjax.success = false;
                    getAllRisksPremiumAjax.message = "no_parametres_for_risk";
                }*/

                if (ccrl.isEmpty()) throw new CalculatePremiumException("no_parametres_for_risk");
                else {
                    //BigDecimal all = riskService.getSumForAllRisksWithoutSaving(contract);
                    BigDecimal all = productProcessor.getProductProcessorImplementation(contract).getAllPremiumForContractWithoutSaving(contract, null);
                    getAllRisksPremiumAjax.sum = all;
                    getAllRisksPremiumAjax.insuredSum = productProcessor.getProductProcessorImplementation(contract).getIntegralInsuredSumForContract(contract); //riskService.getMajorInsuredSumForAllRisks(contract, sum);
                    getAllRisksPremiumAjax.success = true;
                }
            }

            if (cc.getProduct().equals(CatContract.Product.MP0)) {
                contract.setPaymentType(paymentType);
                contract.setFranchise(franchise);

                InsuranceArea insuranceArea = new InsuranceArea();

                if (!riskids.isEmpty() && !risksums.isEmpty() && riskids.size() == risksums.size()) {
                    List<InsuranceObject> insuranceObjects = new LinkedList<InsuranceObject>();

                    for (Integer i = 0; i < riskids.size(); i++) {

                        Risk risk = riskService.getRiskById(riskids.get(i));

                        if (risk != null) {
                            InsuranceObject insuranceObject = new InsuranceObject();
                            insuranceObject.setRisk(risk);
                            insuranceObject.setSum(risksums.get(i));

                            insuranceObjects.add(insuranceObject);

                        }
                    }

                    insuranceArea.setInsuranceObjectList(insuranceObjects);


                } else throw new CalculatePremiumException("");

                List<SecuritySystem> securitySystems = new LinkedList<SecuritySystem>();

                if (securitySystemsIds != null && !securitySystemsIds.isEmpty()) {
                    for (Long ssId : securitySystemsIds) {
                        SecuritySystem secSys = insuranceAreaService.getSecuritySystemById(ssId);

                        if (secSys != null) securitySystems.add(secSys);

                    }

                    insuranceArea.setSecuritySystems(securitySystems);
                }

                List<InsuranceArea> insuranceAreas = new LinkedList<InsuranceArea>();
                insuranceAreas.add(insuranceArea);


                getAllRisksPremiumAjax.sum = productProcessor.getProductProcessorImplementation(contract)
                        .getAllPremiumForContractWithoutSaving(contract, insuranceAreas);
                getAllRisksPremiumAjax.insuredSum = productProcessor.getProductProcessorImplementation(contract)
                        .getIntegralInsuredSumForContractWithoutSaving(contract, insuranceAreas);
                getAllRisksPremiumAjax.success = true;


            }

            if (cc.getProduct().equals(CatContract.Product.TP0)) {

                contract.setInsuranceTerritory(insuranceTerritory);
                contract.setFranchise(franchise);
                contract.setSport(sport != null ? sport : false);


                BigDecimal insuredSum = BigDecimal.ZERO;
                BigDecimal premium = BigDecimal.ZERO;

                if (!riskids.isEmpty() && !risksums.isEmpty() && riskids.size() == risksums.size()) {

                    for (Integer i = 0; i < riskids.size(); i++) {

                        Risk risk = riskService.getRiskById(riskids.get(i));

                        if (risk != null && risksums.get(i) != null) {

                            insuredSum = insuredSum.add(productProcessorTP0.getInsuredSumFromRisk(risk, cc, risksums.get(i)));

                            premium = premium.add(productProcessorTP0.getPremiumWOSavingWithAge(contract, cc, risk, risksums.get(i), age));

                        }
                    }


                    getAllRisksPremiumAjax.insuredSum = insuredSum;
                    getAllRisksPremiumAjax.sum = premium;
                    getAllRisksPremiumAjax.success = true;

                } else throw new CalculatePremiumException("");

            }

            if (cc.getProduct().equals(CatContract.Product.TP2)) {
                contract.setLength(length);
                contract.setTp2Extended(tp2Extended);


                ProductMoneyProperty productMoneyProperty = partnerService.getProductMoneyPropertyByPropertyName(ProductMoneyProperty.PropertyType.fixed_insurance_sum, cc);
                if (productMoneyProperty != null && productMoneyProperty.getMoneyValue() != null) {
                    contract.setSum(productMoneyProperty.getMoneyValue());

                    Risk risk = null;
                    if (contract.getTp2Extended() != null && contract.getTp2Extended()) {
                        risk = riskService.getRiskByDet("EXT");
                    } else risk = riskService.getRiskByDet("STD");

                    List<CatContractRisk> catContractRiskList = riskService.getExistRisks(null, contract.getCatContract());

                    CatContractRisk catContractRiskForContract = null;

                    if (!catContractRiskList.isEmpty()) {
                        for (CatContractRisk catContractRisk : catContractRiskList) {
                            if (catContractRisk.getLength().equals(contract.getLength()) &&
                                    catContractRisk.getRisk().equals(risk))
                                catContractRiskForContract = catContractRisk;
                        }
                    }


                    if (risk != null && catContractRiskForContract != null) {

                        BigDecimal premium = catContractRiskForContract.getMonthTarif().setScale(2, RoundingMode.HALF_UP);

                        getAllRisksPremiumAjax.sum = premium;
                        getAllRisksPremiumAjax.insuredSum = contract.getSum();
                        getAllRisksPremiumAjax.success = true;

                    }
                }
            }

        } catch (CalculatePremiumException e) {
            getAllRisksPremiumAjax.sum = null;
            getAllRisksPremiumAjax.success = false;
            getAllRisksPremiumAjax.message = e.getMessage();

            getAllRisksPremiumAjax.minSum = e.minSum;
            getAllRisksPremiumAjax.maxSum = e.maxSum;
            getAllRisksPremiumAjax.minTerm = e.minTerm;
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            String g = gson.toJson(getAllRisksPremiumAjax);
            try {
                return g.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                return g.getBytes();
            }
        }


    }

    @RequestMapping(params = "accept", method = RequestMethod.GET, value = "/{id}")
    public String getAllRisksSum(
            @PathVariable(value = "id")
            Long contractId,
            HttpServletRequest httpServletRequest
    ) {
        Contract contract = null;
        try {
            contract = contractService.getContractById(contractId);

            assert contract.getContractImages() != null && contract.getContractImages().size() > 0;
            assert contract.getCatContractStatus().getCode().equals(CatContractStatus.StatusCode.WAIT_BIMA);
            assert contract.getCatContract().getProduct().equals(CatContract.Product.MP0);

            contractService.acceptContract(contract);

        } catch (NoEntityException e) {
            e.printStackTrace();
        } finally {
            return "redirect:/contracts/" + encodeUrlPathSegment(contract.getId().toString(), httpServletRequest) + "?form";
        }

    }


    @RequestMapping(value = "/{id}", params = "get_ins_areas", method = RequestMethod.GET)
    public String getInsuranceObjects(
            @PathVariable(value = "id")
            Long contractId,
            Model uiModel,
            HttpServletResponse httpServletResponse
    ) {
        try {
            httpServletResponse.setHeader("Cache-Control", "no-cache");

            Contract contract = contractService.getContractById(contractId);

            uiModel.addAttribute("contract", contract);


            User currectUser = userLoginUtil.getCurrentLogInUser();

            if (userLoginUtil.getMaxUserAuthorityCode(currectUser) == UserRole.AuthorityCode.ROLE_USER || userLoginUtil.getMaxUserAuthorityCode(currectUser) == UserRole.AuthorityCode.ROLE_ADMIN) {
                uiModel.addAttribute("managerMode", true);
            }

            if (contract.getCatContract().getUseInsuranceAreas() != null && contract.getCatContract().getUseInsuranceAreas()) {
                List<InsuranceArea> insuranceAreas = insuranceAreaService.getInsuranceAreasForContract(contract);
                uiModel.addAttribute("insuranceAreas", insuranceAreas);

                List<Risk> risksForAreas = riskService.getRisksForInsuranceArea(contract.getPartner(), contract.getCatContract());
                uiModel.addAttribute("risksForAreas", risksForAreas);

                HashMap<Long, List<SecuritySystem>> securitySystemsAvailableForAreas = new HashMap<Long, List<SecuritySystem>>();

                for (InsuranceArea insuranceArea : insuranceAreas) {
                    List<SecuritySystem> securitySystemsAvailable = insuranceAreaService.getSecuritySystemsAvailable(insuranceArea);
                    securitySystemsAvailableForAreas.put(insuranceArea.getId(), securitySystemsAvailable);
                }

                uiModel.addAttribute("securitySystemsAvailableForAreas", securitySystemsAvailableForAreas);


            }

            return "contracts/insurance_objects";
        } catch (NoEntityException e) {
            e.printStackTrace();
            return null;
        }
    }


    @RequestMapping(params = "get_calc_form", method = RequestMethod.GET)
    public String getCalcForm(
            @RequestParam(value = "cc_id")
            Long catContractId,
            Model uiModel
    ) {
        if (catContractId == null) return "contracts/calc/form/cu0";

        CatContract catContract = catContractService.getCatContractById(catContractId);

        User user = userLoginUtil.getCurrentLogInUser();

        switch (catContract.getProduct()) {
            case CU0:
                return "contracts/calc/form/cu0";

            case BA0:
                if (user.getPartner() != null)
                    uiModel.addAttribute("productRiskSet", riskService.getProductRiskSets(user.getPartner(), catContract));

                return "contracts/calc/form/ba0";
            case MP0:


                if (user.getPartner() != null) {
                    List<Risk> risksToCalc = riskService.getRisksForInsuranceArea(user.getPartner(), catContract);

                    uiModel.addAttribute("risks", risksToCalc);

                    List<SecuritySystem> systems = insuranceAreaService.getAllSecuritySystems();

                    uiModel.addAttribute("secsystems", systems);

                    if (user.getPartner() != null) {
                        // Если исп-ся франшиза, загружаем допустимые значения
                        List<CatContractPartnerFranchise> franchises = partnerService.getFranchiseValuesAllowed(catContract, user.getPartner());
                        uiModel.addAttribute("franchises", franchises);

                        // Разрешено ли выбирать способ возмещения?
                        if (partnerService.paymentChooseAllowed(user.getPartner(), catContract)) {
                            uiModel.addAttribute("paymentChooseAllowed", true);
                        }
                    }
                } else return null;

                return "contracts/calc/form/mp0";

            case TP0:

                if (user.getPartner() != null) {
                    Contract contract = new Contract();
                    contract.setCatContract(catContract);


                    List<Risk> risksToCalc = productProcessor.getProductProcessorImplementation(contract)
                            .getRiskSomeSellerCanUse(catContract, user.getPartner());

                    uiModel.addAttribute("risks", risksToCalc);
                    HashMap<Risk, BigDecimal> fixedSums = productProcessorTP0.getFixedSumsForRisks(risksToCalc, catContract, user.getPartner());
                    HashMap<Risk, String> possibleValues = productProcessorTP0.getAddDataForRiskSumFields(risksToCalc, catContract);

                    uiModel.addAttribute("fixedSums", fixedSums);
                    uiModel.addAttribute("possibleValues", possibleValues);

                    List<CatContractPartnerFranchise> franchises = partnerService.getFranchiseValuesAllowed(catContract, null);
                    uiModel.addAttribute("franchises", franchises);

                    uiModel.addAttribute("territories", Contract.InsuranceTerritory.values());

                } else return null;

                return "contracts/calc/form/tp0";

            case TP2:
                return "contracts/calc/form/tp2";

            case RU0:
                return "contracts/calc/form/ru0";

            default:
                return "contracts/calc/form/cu0";

        }
    }


    @RequestMapping(value = "/{id}", params = "premiums_window", method = RequestMethod.GET)
    public String premiumsWindow(
            @PathVariable(value = "id")
            Long contractId,
            @RequestParam(value = "reason", required = false)
            String reason,
            Model uiModel
    ) {

        try {
            Contract contract = contractService.getContractById(contractId);
            if (!contract.getCatContract().getProduct().equals(CatContract.Product.TP0))
                return null;

            List<Risk> riskSellerCanUse = null;
            BigDecimal allPremiumForContract = null;
            try {
                riskSellerCanUse = productProcessor
                        .getProductProcessorImplementation(contract)
                        .getRisksSellerCanUse(contract);

                allPremiumForContract = productProcessor.getProductProcessorImplementation(contract).getAllPremiumForContract(contract);
            } catch (CalculatePremiumException e) {
                uiModel.addAttribute("cannotWorkReason", e.getReason());
                return "contract/premiums/window";
            }

            uiModel.addAttribute("risksAllowed", riskSellerCanUse);
            uiModel.addAttribute("contract", contract);

            List<ContractPremium> contractPremiumList = premiumService.getValidatedCPremiums(contract);

            uiModel.addAttribute("premiums", contractPremiumList);

            BigDecimal allPremium = BigDecimal.ZERO;
            BigDecimal allInsSum = BigDecimal.ZERO;

            for (ContractPremium contractPremium : contractPremiumList) {
                allPremium = allPremium.add(contractPremium.getPremium());
                allInsSum = allInsSum.add(contractPremium.getInsuredSum());
            }

            if (contract.getCatContract().getProduct().equals(CatContract.Product.CU0)) {
                uiModel.addAttribute("allPremium", allPremium);
            } else {
                uiModel.addAttribute("allPremium", allPremiumForContract);
            }

            uiModel.addAttribute("insuredSumContract", allInsSum);
            uiModel.addAttribute("cur", contract.getCurrency());

            // Фиксированные риски, которые не были добавлены
            List<Risk> toBeAdded = productProcessor.getProductProcessorImplementation(contract)
                    .getRisksMustBeAddedToContract(contract);

            uiModel.addAttribute("toBeAdded", toBeAdded);

            if (reason != null) {
                uiModel.addAttribute("reason", reason);
            }

        } catch (NoEntityException e) {
            return null;
        }


        return "contract/premiums/window";
    }

    @RequestMapping(value = "/{id}", params = "premium_add", method = RequestMethod.POST)
    public String premiumAdd(
            @PathVariable(value = "id")
            Long contractId,
            @RequestParam(value = "risk")
            Long riskId,
            @RequestParam(value = "sum")
            BigDecimal sum,
            Model uiModel
    ) {

        try {
            Contract contract = contractService.getContractById(contractId);
            Risk risk = riskService.getRiskById(riskId);

            ContractPremium contractPremium = new ContractPremium();
            contractPremium.setRisk(risk);
            contractPremium.setInsuredSum(sum);
            contractPremium.setContract(contract);
            contractPremium.setValidated(true);

            try {
                productProcessor.getProductProcessorImplementation(contract).addPremiumToContract(contract, contractPremium);
            } catch (CalculatePremiumException e) {
                uiModel.addAttribute("reason", e.getMessage());
            }

        } catch (NoEntityException e) {
            return null;
        }

        return "redirect:/contracts/".concat(contractId.toString()).concat("?premiums_window");
    }

    @RequestMapping(value = "/{id}", params = "delete_premium", method = RequestMethod.DELETE)
    public String deletePremium(
            @PathVariable(value = "id")
            Long contractId,
            @RequestParam(value = "id")
            Long premiumId
    ) {


        try {
            User currentUser = userLoginUtil.getCurrentLogInUser();
            Contract contract = contractService.getContractById(contractId);

            if (contract.getPartner().equals(currentUser.getPartner()) && (contract.getClaimSigned() == null || !contract.getClaimSigned())) {

                premiumService.deletePremium(premiumId);
            }
        } catch (NoEntityException e) {
            return null;
        }

        return "redirect:/contracts/".concat(contractId.toString()).concat("?premiums_window");
    }

    @RequestMapping(value = "/{id}", params = "load_sum_area")
    public String loadSumArea(
            @PathVariable(value = "id")
            Long contractId,
            @RequestParam(value = "risk_id")
            Long riskId,
            Model uiModel
    ) {
        try {
            Contract contract = contractService.getContractById(contractId);
            Risk risk = riskService.getRiskById(riskId);

            if (!contract.getCatContract().getProduct().equals(CatContract.Product.TP0)) return null;

            List<PartnerRiskPolicy> partnerRiskPolicyList = partnerService.getExistsPolicy(contract.getCatContract(), contract.getPartner());

            for (PartnerRiskPolicy partnerRiskPolicy : partnerRiskPolicyList) {
                if (risk.getParentRisk() != null && partnerRiskPolicy.getRisk().equals(risk.getParentRisk())) {
                    uiModel.addAttribute("policy", partnerRiskPolicy);
                }
            }

            uiModel.addAttribute("possibleValues",
                    productProcessor.getProductProcessorImplementation(contract).getPossibleValues(riskService.getRiskById(riskId))
            );
        } catch (NoEntityException e) {
            return null;
        }

        return "contract/tp0/sumarea";
    }
}
