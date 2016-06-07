package com.nzion.external;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Created by Nthdimenzion on 4/7/2015.
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = "classpath:applicationContext.xml")
public class OfbizServiceClientTest {

    /*@Value("${PHARMACY_SERVER_URL}")
    private String PHARMACY_SERVER_URL;
    @Value("${USERNAME}")
    private String userName;
    @Value("${PASSWORD}")
    private String password;

    @Test
    public void whenApplicationProperitesAreInjected_thenApplicationPropertiesMustNotBeNull(){
        assertNotNull(PHARMACY_SERVER_URL);
        assertNotNull(userName);
        assertNotNull(password);
        assertThat(PHARMACY_SERVER_URL, is("https://5.9.249.197:6443"));
        assertThat(userName, is("admin"));
        assertThat(password, is("ofbiz"));
    }*/
}
