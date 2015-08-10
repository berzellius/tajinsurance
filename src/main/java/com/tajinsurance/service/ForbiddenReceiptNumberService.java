package com.tajinsurance.service;

import com.tajinsurance.domain.ForbiddenReceiptNumber;
import com.tajinsurance.exceptions.AddForbiddenReceiptNumberException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by berz on 07.07.14.
 */
@Service
public interface ForbiddenReceiptNumberService {

    public List<ForbiddenReceiptNumber> getNumbersForCurrentMonth();

    public void addNumberForCurrentMonth(Integer number) throws AddForbiddenReceiptNumberException;

    public void deleteNumberForCurrentMonth(Integer number);

    public void deleteNumberById(Long id);

    public void addNewGeneration();

    public Integer getMaxGeneration();

}
