package com.tajinsurance.test;


import com.tajinsurance.domain.Contract;
import com.tajinsurance.exceptions.NoEntityException;
import com.tajinsurance.service.ContractService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.transaction.annotation.Transactional;

public class ContainerUnitTests {

    private XmlWebApplicationContext context;

    @Autowired
    ContractService contractService;

    @PersistenceContext
    EntityManager entityManager;


    @Before
    public void setup() {
        context = new XmlWebApplicationContext();
        context.setServletContext(new MockServletContext("src/main/webapp", new FileSystemResourceLoader()));
        context.setConfigLocations(new String[]{
                "classpath*:/WEB-INF/web.xml",
                "classpath*:/WEB-INF/spring/webmvc-config.xml",
                "classpath*:/META-INF/spring/applicationContext.xml",
                "classpath*:/META-INF/persistence.xml"
        });
        context.refresh();
    }

    @Test
    @Transactional
    public void testOk() {
        try {
            Contract contract = contractService.getContractById(1l);
        } catch (NoEntityException e) {
            System.out.println("none");
        }
    }
}
