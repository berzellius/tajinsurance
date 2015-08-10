package com.tajinsurance.service;

import com.tajinsurance.domain.Contract;
import com.tajinsurance.domain.ContractImage;
import com.tajinsurance.exceptions.IllegalDataException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
/**
 * Created by berz on 16.09.14.
 */
public interface ProductProcessorMP0 extends ProductProcessor {




    void processFile(MultipartFile file, ContractImage contractImage, Contract contract) throws IOException, IllegalDataException;
}
