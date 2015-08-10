package com.tajinsurance.service;

import com.tajinsurance.domain.*;
import com.tajinsurance.dto.AjaxContractListFilter;
import com.tajinsurance.exceptions.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by berz on 27.02.14.
 */
@Service
public interface ContractService {
    List<Contract> localizeContractsList(List<Contract> cts, String locale);

    /**
     * Контракт по id
     * @param id
     * @return
     */
    Contract getContractById(Long id) throws NoEntityException;

    Contract getContractById(Long id, String locale) throws NoEntityException;

    /**
     * Получить список контрактов
     * @param first - начиная с какого контракта
     * @param count - количество
     * @param sortFieldName - поле для сортировки
     * @param sortOrder - порядок сортировки
     * @return
     */
    List<Contract> getContracts(int first, int count, String sortFieldName, String sortOrder);

    /**
     * Получить список контрактов
     * @param first - начиная с какого контракта
     * @param count - количество
     * @return
     */
    List<Contract> getContracts(int first, int count);

    /**
     *
     * @param numPage
     * @param itemsToPage
     * @param sortFieldName
     * @param sortOrder
     * @return
     */
    List<Contract> getContractsPage(Integer numPage, Integer itemsToPage, String sortFieldName, String sortOrder);

    List<Contract> getContractsPage(AjaxContractListFilter filter, Integer numPage, Integer itemsToPage, String sortFieldName, String sortOrder);

    /**
     * Количество контрактов
     * @return
     */
    Long countContracts();

    Long countContracts(AjaxContractListFilter filter, String sortFieldName, String sortOrder);


    /**
     * Сущность, подготовленная для первоначального заполнения
     * @return
     */
    Contract preparedContract(User u, CatContract cc);

    /**
     * Проверить, допустим ли расчет риска для договора
     * @param risk - риск
     * @param contract - договор
     * @return
     */
    boolean checkIfTheRiskIsAllowed(Risk risk, Contract contract);


    /**
     * Бизнес логика: печать договора
     * @param c
     */
    Date printContract(Contract c);

    Date doDefaultPrintContract(Contract c);

    /**
     * Бизнес логика: отменить договор
     */
    void cancelContract(Contract c);
    /**
     * Количество страниц
     * @param size - контрактов на страницу
     * @return
     */
    Integer countPages(Integer size);

    Integer countPages(AjaxContractListFilter filter, Integer page, Integer size, String sortFieldName, String sortOrder);

    void save(Contract contract) throws EntityNotSavedException;

    void update(Contract contract, Boolean managerMode) throws NoEntityException, CalculatePremiumException, NoPersonToContractException, BadContractDataException, NoRelatedContractNumber;

    void delete(Long id) throws NoEntityException;

    Contract dropPrintClaimFlag(Contract c);

    void setPersonToContract(Contract contract);

    List<PaymentWay> getAllPaymentWays();

    PaymentWay getPaymentWayById(Long id);

    /**
     * Присвоить номер квитанции
     * @param contract - договор
     * @param month - месяц ГГГГММ
     */
    void setReceiptNumber(Contract contract, Integer month);

    void setReceiptNumber(Contract contract);

    ContractImage getContractImageById(Long id);

    void acceptContract(Contract contract);

    void deleteImage(ContractImage contractImage);

    void updateImage(ContractImage contractImage);

    List<Contract> getAllContracts(String locale);

    List<Contract> getAllContracts(String language, Partner partner);

    List<Contract> getAllContracts(String language, User user);


    void saveContractState(Contract contract);


}
