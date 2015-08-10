package com.tajinsurance.web;

import com.tajinsurance.domain.Currency;
import com.tajinsurance.domain.CurrencyExchange;
import com.tajinsurance.service.CatContractService;
import com.tajinsurance.service.ExchangeCurrenciesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * Created by berz on 01.12.14.
 */
@RequestMapping("/exchange")
@Controller
public class CurrencyExchangeController {

    @Autowired
    ExchangeCurrenciesService exchangeCurrenciesService;

    @Autowired
    CatContractService catContractService;

    @RequestMapping(method = RequestMethod.GET)
    public String list(
            Model uiModel
    ){

        List<CurrencyExchange> currencyExchangeList = exchangeCurrenciesService.lastCources();

        List<Currency> currenciesToSet = exchangeCurrenciesService.getCurrenciesToSet();

        uiModel.addAttribute("currencies", currenciesToSet);
        uiModel.addAttribute("currencyExchangeList", currencyExchangeList);

        System.out.println(exchangeCurrenciesService.getExchangeToCurrency(catContractService.getCurrencyById(2l)));

        return "exchange/list";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String add(
            CurrencyExchange currencyExchange
    ){
        exchangeCurrenciesService.addCource(currencyExchange);

        return "redirect:/exchange";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String delete(
            @PathVariable("id") Long id
    ){

        exchangeCurrenciesService.remove(id);

        return "redirect:/exchange";
    }

}
