package com.tajinsurance.service;

import com.tajinsurance.domain.Currency;
import com.tajinsurance.domain.CurrencyExchange;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Calendar;

/**
 * Created by berz on 01.12.14.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ExchangeCurrenciesServiceImpl implements ExchangeCurrenciesService{

    @PersistenceContext
    EntityManager entityManager;

    public ExchangeCurrenciesServiceImpl() {
    }

    @Override
    public List<CurrencyExchange> lastCources(int first, int count) {
        return entityManager.createQuery(
                "SELECT o FROM CurrencyExchange o ORDER BY date DESC ",
                CurrencyExchange.class
        )
            .setFirstResult(first)
            .setMaxResults(count)
            .getResultList();
    }

    @Override
    public List<CurrencyExchange> lastCources() {
        return lastCources(0, 50);
    }

    @Override
    public Currency basicCurrency() {
        return entityManager.find(Currency.class, 1l);
    }

    @Override
    public void addCource(CurrencyExchange currencyExchange) {
        Date date = getTomorrow();

        List<CurrencyExchange> currencyExchangesForToday =
                entityManager.createQuery(
                        "SELECT o FROM CurrencyExchange o WHERE date = :d and currency = :curr",
                        CurrencyExchange.class
                )
                .setParameter("d", date)
                .setParameter("curr", currencyExchange.getCurrency())
                .getResultList();

        if(currencyExchangesForToday.isEmpty() || currencyExchangesForToday.size() == 0){
            currencyExchange.setDate(date);

            entityManager.persist(currencyExchange);
        }
    }

    @Override
    public void remove(Long id) {
        CurrencyExchange currencyExchange = entityManager.find(CurrencyExchange.class, id);

        if(currencyExchange != null) entityManager.remove(currencyExchange);
    }

    @Override
    public List<Currency> getCurrenciesToSet() {
        return entityManager.createQuery(
                "SELECT o FROM Currency o WHERE o != :basic",
                Currency.class
        )
                .setParameter("basic", this.basicCurrency())
                .getResultList();
    }

    @Override
    public BigDecimal getExchangeToCurrency(Currency currency) {

        List<CurrencyExchange> currencyExchanges = entityManager.createQuery(
                "SELECT ce FROM CurrencyExchange ce WHERE ce.date = " +
                        "(SELECT max(o.date) FROM CurrencyExchange o WHERE o.date <= :d AND o.currency = :c) and currency = :c",
                CurrencyExchange.class
        )
                .setParameter("d", dateProcess(new Date()))
                .setParameter("c", currency)
                .getResultList();

        if(currencyExchanges.isEmpty()) return null;

        return currencyExchanges.get(0).getCourse();
    }

    @Override
    public BigDecimal inSomoni(BigDecimal premium, Currency currency) {

        BigDecimal exchange = getExchangeToCurrency(currency);

        return doExchange(premium, exchange);
    }

    @Override
    public BigDecimal doExchange(BigDecimal premium, BigDecimal exchange) {
        if(exchange == null) return null;

        //System.out.println(premium + " | " + exchange + " :: " + premium.multiply(exchange));

        return premium.multiply(exchange).setScale(0, RoundingMode.HALF_UP);
    }

    private Date dateProcess(Date in){
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(in);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    private Date getTomorrow(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, 1);

        return dateProcess(calendar.getTime());
    }

}
