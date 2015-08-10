package com.tajinsurance.web;

import com.tajinsurance.domain.*;
import com.tajinsurance.exceptions.NoEntityException;
import com.tajinsurance.service.*;
import com.tajinsurance.utils.UserLoginUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;
import org.springframework.roo.addon.web.mvc.controller.converter.RooConversionService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * A central place to register application converters and formatters. 
 */
@RooConversionService
public class ApplicationConversionServiceFactoryBean extends FormattingConversionServiceFactoryBean {

    @Autowired
    ContractService contractService;

    @Autowired
    CatContractService catContractService;

    @Autowired
    CatContractStatusService catContractStatusService;

    @Autowired
    PersonService personService;

    @Autowired
    PartnerService partnerService;

    @Autowired
    UserLoginUtil userLoginUtil;

    @Autowired
    RiskService riskService;

	@Override
	protected void installFormatters(FormatterRegistry registry) {
		super.installFormatters(registry);
        registry.addConverter(getIdToCatContractConverter());
        registry.addConverter(getStringToCatContractConverter());
        registry.addConverter(getIdToCatContractStatusConverter());
        registry.addConverter(getStringToCatContractStatusConverter());
        registry.addConverter(getIdToPartnerCoverter());
        registry.addConverter(getStringToPartnerConverter());
        registry.addConverter(getIdToPersonConverter());
        registry.addConverter(getStringToPersonConverter());
        registry.addConverter(getStringToDateConverter());
        registry.addConverter(getCatContractToStringConverter());
        registry.addConverter(getPersonToStringConverter());
        registry.addConverter(getIdToUserRoleConverter());
        registry.addConverter(getStringToUserRoleConverter());
        //registry.addConverter(getCatContractListToStringConverter());
        //registry.addConverter(getListUserRolesToStringConverter());
        registry.addConverter(getListToStringConverter());
        registry.addConverter(getCatContractStatusToString());

        registry.addConverter(getRiskToStringConverter());
        registry.addConverter(getLongToRiskConverter());
        //registry.addConverter(getRiskListToStringConverter());
        //registry.addConverter(getListLongToListRiskConverter());
        //registry.addConverter(getListIntegerToListRiskConverter());
        registry.addConverter(getIntegerToRiskConverter());
        registry.addConverter(getStringToRiskConverter());

        registry.addConverter(getCurrencyToStringConverter());
        registry.addConverter(getStringCurrency());
        registry.addConverter(getLongCurrency());

        registry.addConverter(getLongToUserConverter());
        registry.addConverter(getStringToUserConverter());
        registry.addConverter(getPartnerToStringConverter());

        registry.addConverter(getUserToStringConverter());
        //registry.addConverter(getDateToStringConverter());

        registry.addConverter(getLongToPaymentWayConverter());
        registry.addConverter(getStringToPaymentWayConverter());
        registry.addConverter(getPaymentWayToStringConverter());

        registry.addConverter(getTypeOfRiskToStringConverter());
        registry.addConverter(getIdToTypeOfRiskConverter());
        registry.addConverter(getStringToTypeOFRiskConverter());

        registry.addConverter(getStringProductRiskSet());
        registry.addConverter(getIdToProductRiskSet());
		// Register application converters and formatters
	}

    public Converter<Person, String> getPersonToStringConverter(){
        return new Converter<Person, String>() {
            @Override
            public String convert(Person person) {
                return person.toString();
            }
        };
    }

    public Converter<CatContract, String> getCatContractToStringConverter(){
        return new Converter<CatContract, String>() {
            @Override
            public String convert(CatContract catContract) {
                return catContract.getName();
            }
        };
    }

    public Converter<Long, CatContract> getIdToCatContractConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, com.tajinsurance.domain.CatContract>() {
            @Override
            public com.tajinsurance.domain.CatContract convert(Long id) {
                return catContractService.getCatContractById(id);
            }
        };
    }

    public Converter<String, CatContract> getStringToCatContractConverter(){
        return new Converter<String, CatContract>() {
            @Override
            public CatContract convert(String s) {
                Long id = Long.decode(s);
                return catContractService.getCatContractById(id);
            }
        };
    }

    public Converter<Long, CatContractStatus> getIdToCatContractStatusConverter(){
        return new Converter<Long, CatContractStatus>() {
            @Override
            public CatContractStatus convert(Long id){
                return catContractStatusService.getCatContractStatusById(id);
            }
        };
    }

    public Converter<String, CatContractStatus> getStringToCatContractStatusConverter(){
        return new Converter<String, CatContractStatus>() {
            @Override
            public CatContractStatus convert(String s) {
                Long id = Long.decode(s);
                return catContractStatusService.getCatContractStatusById(id);
            }
        };
    }

    public Converter<Long, Partner> getIdToPartnerCoverter(){
        return new Converter<Long, Partner>() {
            @Override
            public Partner convert(Long id) {
                return partnerService.getPartnerById(id);
            }
        };
    }

    public Converter<String, Partner> getStringToPartnerConverter(){
        return new Converter<String, Partner>() {
            @Override
            public Partner convert(String s) {
                Long id = Long.decode(s);
                return partnerService.getPartnerById(id);
            }
        };
    }

    public Converter<Long, Person> getIdToPersonConverter(){
        return new Converter<Long, Person>() {
            @Override
            public Person convert(Long id) {
                return personService.getPersonById(id);
            }
        };
    }

    public Converter<String, Person> getStringToPersonConverter(){
        return new Converter<String, Person>() {
            @Override
            public Person convert(String s) {
                Long id = Long.decode(s);
                return personService.getPersonById(id);
            }
        };
    }

    public Converter<String, Date> getStringToDateConverter(){
        return new Converter<String, Date>() {
            @Override
            public Date convert(String s) {
                try {
                    Date dt = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse(s);
                    return dt;
                } catch (ParseException e) {
                    try {
                        Date dt = new SimpleDateFormat("dd.MM.yyyy").parse(s);
                        return dt;
                    } catch (ParseException e1) {
                        return null;
                    }
                }
            }
        };
    }

    public Converter<Date,String> getDateToStringConverter(){
        return new Converter<Date, String>() {
            @Override
            public String convert(Date date) {
                return  new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(date);
            }
        };
    }

    public Converter<Long, UserRole> getIdToUserRoleConverter(){
        return new Converter<Long, UserRole>() {
            @Override
            public UserRole convert(Long id) {
                return userLoginUtil.getUserRoleById(id);
            }
        };
    }

    public Converter<String, UserRole> getStringToUserRoleConverter(){
        return new Converter<String, UserRole>() {
            @Override
            public UserRole convert(String s) {
                return userLoginUtil.getUserRoleById(Long.decode(s));
            }
        };
    }

    /*public Converter<List<UserRole>, String> getListUserRolesToStringConverter(){
        return new Converter<List<UserRole>, String>() {
            @Override
            public String convert(List<UserRole> userRoles) {
                Locale locale = LocaleContextHolder.getLocale();
                String s = "";
                for(UserRole r : userLoginUtil.getRolesLocalizations(userRoles, locale.getLanguage())){
                    s += ((s == "")? "" : ", ") + r.toString();
                }
                return s;
            }
        };
    }*/

    public Converter<List<Object>, String> getListToStringConverter(){
        return new Converter<List<Object>, String>() {
            @Override
            public String convert(List<Object> objects) {
                String s = "";
                for(Object o : objects){
                    s += ((s == "")? "" : ", ") + o.toString();
                }
                return s;
            }
        };
    }



    /*public Converter<List<CatContract>, String> getCatContractListToStringConverter(){
        return new Converter<List<CatContract>, String>() {
            @Override
            public String convert(List<CatContract> catContracts) {
                Locale locale = LocaleContextHolder.getLocale();
                String s = "";
                for(CatContract catContract : catContracts){
                    s += ((s == "")? "" : ", ") + catContract.toString();
                }
                return s;
            }
        };
    }*/

    public Converter<CatContractStatus, String> getCatContractStatusToString(){
        return  new Converter<CatContractStatus, String>() {
            @Override
            public String convert(CatContractStatus catContractStatus) {
                return catContractStatus.getValue();
            }
        };
    }

    public Converter<Risk, String> getRiskToStringConverter(){
        return new Converter<Risk, String>() {
            @Override
            public String convert(Risk risk) {
                return risk.toString();
            }
        };
    }

    public Converter<Long, Risk> getLongToRiskConverter(){
        return new Converter<Long, Risk>() {
            @Override
            public Risk convert(Long id) {
                try {
                    return riskService.getRiskById(id);
                } catch (NoEntityException e) {
                    return null;
                }
            }
        };
    }



    public Converter<Currency, String> getCurrencyToStringConverter(){
        return new Converter<Currency, String>() {
            @Override
            public String convert(Currency currency) {
                return currency.toString();
            }
        };
    }

    public Converter<Long, User> getLongToUserConverter(){
        return new Converter<Long, User>() {
            @Override
            public User convert(Long aLong) {
                return userLoginUtil.getUserById(aLong);
            }
        };
    }

    public Converter<String, User> getStringToUserConverter(){
        return new Converter<String, User>() {
            @Override
            public User convert(String s) {
                Long id = Long.decode(s);
                return userLoginUtil.getUserById(id);
            }
        };
    }

    public Converter<Partner, String> getPartnerToStringConverter(){
        return new Converter<Partner, String>() {
            @Override
            public String convert(Partner partner) {
                return partner.getValue();
            }
        };
    }

    public Converter<List<Long>, List<Risk>> getListLongToListRiskConverter(){
        return new Converter<List<Long>, List<Risk>>() {
            @Override
            public List<Risk> convert(List<Long> ids) {
                List<Risk> risks = new LinkedList<Risk>();

                for(Long id : ids){
                    Risk r = getLongToRiskConverter().convert(id);
                    if(r != null) risks.add(r);
                }

                return risks;
            }
        };
    }

    public Converter<List<Integer>, List<Risk>> getListIntegerToListRiskConverter(){
        return new Converter<List<Integer>, List<Risk>>() {


            @Override
            public List<Risk> convert(List<Integer> integers) {
                List<Risk> risks = new LinkedList<Risk>();

                for(Integer id : integers){
                    Risk r = getLongToRiskConverter().convert(Long.valueOf(id.longValue()));
                    if(r != null) risks.add(r);
                }

                return risks;
            }
        };
    }

    public Converter<Integer, Risk> getIntegerToRiskConverter(){
        return new Converter<Integer, Risk>() {
            @Override
            public Risk convert(Integer integer) {
                try {
                    return riskService.getRiskById(Long.valueOf(integer.longValue()));
                } catch (NoEntityException e) {
                    return null;
                }
            }
        };
    }

    public Converter<String, Risk> getStringToRiskConverter(){
        return new Converter<String, Risk>() {
            @Override
            public Risk convert(String s) {
                try {
                    return riskService.getRiskById(Long.decode(s));
                } catch (NoEntityException e) {
                    return null;
                }
            }
        };
    }



    /*public Converter<List<Risk>, String> getRiskListToStringConverter(){
        return new Converter<List<Risk>, String>() {
            @Override
            public String convert(List<Risk> risks) {
                String s = "";

                for(Object r : risks){
                    if(r instanceof Risk) s += (s == ""? "" : ", ") + r.toString();
                }
                return s;
            }
        };
    }*/

    public Converter<User, String> getUserToStringConverter(){
        return new Converter<User, String>() {
            @Override
            public String convert(User user) {
                return (user.getFio() != null)? user.getFio() : user.getId().toString() + "_" + user.getUsername();
            }
        };
    }

    public Converter<Long, PaymentWay> getLongToPaymentWayConverter(){
        return new Converter<Long, PaymentWay>() {
            @Override
            public PaymentWay convert(Long aLong) {
                return contractService.getPaymentWayById(aLong);
            }
        };
    }

    public Converter<String, PaymentWay> getStringToPaymentWayConverter(){
        return new Converter<String, PaymentWay>() {
            @Override
            public PaymentWay convert(String s) {
                Long id = Long.decode(s);
                return contractService.getPaymentWayById(id);
            }
        };
    }

    public Converter<PaymentWay, String> getPaymentWayToStringConverter(){
        return new Converter<PaymentWay, String>() {
            @Override
            public String convert(PaymentWay paymentWay) {
                return paymentWay.getWay();
            }
        };
    }

    public Converter<TypeOfRisk, String> getTypeOfRiskToStringConverter(){
        return new Converter<TypeOfRisk, String>() {
            @Override
            public String convert(TypeOfRisk typeOfRisk) {
                return typeOfRisk.toString();
            }
        };
    }

    public Converter<Long, TypeOfRisk> getIdToTypeOfRiskConverter(){
        return new Converter<Long, TypeOfRisk>() {
            @Override
            public TypeOfRisk convert(Long aLong) {
                return riskService.getTypeOfRiskById(aLong);
            }
        };
    }

    public Converter<String, TypeOfRisk> getStringToTypeOFRiskConverter(){
        return new Converter<String, TypeOfRisk>() {
            @Override
            public TypeOfRisk convert(String s) {
                Long id = Long.decode(s);
                return riskService.getTypeOfRiskById(id);
            }
        };
    }

    public Converter<Long, ProductRiskSet> getIdToProductRiskSet(){
        return new Converter<Long, ProductRiskSet>() {
            @Override
            public ProductRiskSet convert(Long id) {
                return riskService.getProductRiskSetById(id);
            }
        };
    }

    public Converter<String, ProductRiskSet> getStringProductRiskSet(){
        return new Converter<String, ProductRiskSet>() {
            @Override
            public ProductRiskSet convert(String s) {
                return riskService.getProductRiskSetById(Long.decode(s));
            }
        };
    }

    public Converter<Long, Currency> getLongCurrency(){
        return new Converter<Long, Currency>() {
            @Override
            public Currency convert(Long id) {
                return catContractService.getCurrencyById(id);
            }
        };
    }

    public Converter<String, Currency> getStringCurrency(){
        return new Converter<String, Currency>() {
            @Override
            public Currency convert(String s) {
                Long id = Long.decode(s);
                return catContractService.getCurrencyById(id);
            }
        };
    }
}
