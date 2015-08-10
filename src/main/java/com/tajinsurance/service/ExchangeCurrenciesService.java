package com.tajinsurance.service;

import com.tajinsurance.domain.Currency;
import com.tajinsurance.domain.CurrencyExchange;
import org.springframework.stereotype.Service;

import java.util.List;
import java.math.BigDecimal;
/**
 * Created by berz on 01.12.14.
 */
@Service
public interface ExchangeCurrenciesService {
    List<CurrencyExchange> lastCources(int first, int count);

    List<CurrencyExchange> lastCources();

    Currency basicCurrency();

    void addCource(CurrencyExchange currencyExchange);

    void remove(Long id);

    List<Currency> getCurrenciesToSet();


    BigDecimal getExchangeToCurrency(Currency currency);

    BigDecimal inSomoni(BigDecimal premium, Currency currency);

    BigDecimal doExchange(BigDecimal premium, BigDecimal exchange);
}
