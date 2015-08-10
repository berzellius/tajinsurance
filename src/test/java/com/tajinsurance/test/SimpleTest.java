package com.tajinsurance.test;

import com.tajinsurance.domain.Contract;
import com.tajinsurance.exceptions.NoEntityException;
import com.tajinsurance.service.ContractService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;


/**
 * Created by berz on 16.05.14.
 */


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {
                "file:src/main/resources/META-INF/spring/applicationContext.xml"
        }
 )
@Configurable
@Transactional
public class SimpleTest {

    static {
        System.setProperty("database.driverClassName",  "org.postgresql.Driver");
        System.setProperty("database.url",              "jdbc:postgresql://localhost:5432/tj");
        System.setProperty("database.username",         "postgres");
        System.setProperty("database.password",         "postgres");
    }


    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    ContractService contractService;

    public SimpleTest() {

    }

    @Test
    public void testSome() throws NoEntityException {
        Contract contract = contractService.getContractById(1l);

        System.out.println();
    }
}
