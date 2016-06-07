package com.nzion.external;

import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

import lombok.NoArgsConstructor;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nzion.dto.OrderDto;

/**
 * Created by pradyumna on 04-04-2015.
 */
@Component
@NoArgsConstructor
public class ExternalServiceClientImpl implements ExternalServiceClient {

    final static Logger logger = org.apache.log4j.Logger.getLogger(ExternalServiceClientImpl.class);

    @Value("${PHARMACY_SERVER_URL}")
    private String PHARMACY_SERVER_URL;
    @Value("${USERNAME}")
    private String userName;
    @Value("${PASSWORD}")
    private String password;

    @Value("${LAB_SERVER_URL}")
    private String LAB_SERVER_URL;
    @Value("${LAB_USERNAME}")
    private String labUserName;
    @Value("${LAB_PASSWORD}")
    private String labPassword;

    public OrderDto handlePrescriptionOrder(PrescriptionDTO prescriptionDTO, String tenantId) {
        logger.trace("prescriptionDTO "+prescriptionDTO);
        logger.trace("tenantId "+tenantId);
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        try {
            HttpClient httpclient=getCloseableHttpClient();
            factory.setHttpClient(httpclient);
            HttpUriRequest login = RequestBuilder.post()
                    .setUri(new URI(PHARMACY_SERVER_URL+"/ordermgr/control/login"))
                    .addParameter("USERNAME", userName)
                    .addParameter("PASSWORD", password)
                    .addParameter("tenantId", tenantId)
                    .build();
            HttpResponse response = httpclient.execute(login);
            logger.trace("\n\nSuccessfully Logged-In\n\n");
            logger.trace("response login "+response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        RestTemplate rt = new RestTemplate(factory);
        rt.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        rt.getMessageConverters().add(new StringHttpMessageConverter());
        assert tenantId != null;
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("tenantId", tenantId);
        String response = rt.postForObject(PHARMACY_SERVER_URL+"/ordermgr/control/createRxOrder", prescriptionDTO, String.class, vars);
        logger.trace("\n\nOrder Place\n\nresponse order placed  "+response);
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        //mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
        OrderDto orderdto = null;
        try {
        	orderdto =  mapper.readValue(response,  OrderDto.class );
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.trace("\n\n orderdto \n\n\n  "+ orderdto + "\n\n\n\n\n");
        return orderdto;
    }

    @Override
    public void handleLabOrder(LabOrdetDto labOrdetDto, String labTenantId) {
        System.out.println("\n\nPlacing Lab Order\n\n");
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        String labJsonString = gson.toJson(labOrdetDto);
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        try {
            HttpClient httpclient = getCloseableHttpClient();
            factory.setHttpClient(httpclient);
            HttpUriRequest login = RequestBuilder.post()
                    .setUri(new URI(LAB_SERVER_URL+"/j_spring_security_check"))
                    .addParameter("j_username", labUserName)
                    .addParameter("j_password", labPassword)
                    .addParameter("j_tenantId",labTenantId)
                    .build();
            HttpResponse response = httpclient.execute(login);
            System.out.println("\n\nSuccessfully Logged-In\n\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
        RestTemplate rt = new RestTemplate(factory);
        rt.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        rt.getMessageConverters().add(new StringHttpMessageConverter());
        assert labTenantId != null;
        HttpHeaders headers = new HttpHeaders();
        List<MediaType> mediaTypes = new ArrayList<>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        headers.setAccept(mediaTypes);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<String>(labJsonString, headers);
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("tenantId", labTenantId);
        ResponseEntity<String> responseEntity = rt.exchange(LAB_SERVER_URL + "/labMaster/addLabOrderRequest", HttpMethod.POST, requestEntity, String.class);
        System.out.println("\n\nOrder Placed\n\n");
    }

    private CloseableHttpClient getCloseableHttpClient() throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        TrustStrategy acceptingTrustStrategy = new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] certificate, String authType) {
                return true;
            }
        };
        // Trust own CA and all self-signed certs
        SSLContext sslcontext = SSLContexts.custom()
                .loadTrustMaterial(null, acceptingTrustStrategy)
                .build();
        // Allow TLSv1 protocol only
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslcontext,
                new String[]{"TLSv1"},
                null,
                SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        return HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .build();
    }

	@Override
	public String orderPayment(String orderId,String totalAmount, String pharmacyTenanId) {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        try {
            HttpClient httpclient=getCloseableHttpClient();
            factory.setHttpClient(httpclient);
            HttpUriRequest login = RequestBuilder.post()
                    .setUri(new URI(PHARMACY_SERVER_URL+"/ordermgr/control/login"))
                    .addParameter("USERNAME", userName)
                    .addParameter("PASSWORD", password)
                    .addParameter("tenantId", pharmacyTenanId)
                    .build();
            HttpResponse response = httpclient.execute(login);
        } catch (Exception e) {
            e.printStackTrace();
        }
        RestTemplate rt = new RestTemplate(factory);
        rt.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        rt.getMessageConverters().add(new StringHttpMessageConverter());
        assert pharmacyTenanId != null;
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("tenantId", pharmacyTenanId);
        Map<String, String> data = new HashMap<String, String>();
        data.put("orderId", orderId);
        data.put("totalAmount", totalAmount);
        String response = rt.postForObject(PHARMACY_SERVER_URL+"/ordermgr/control/receiveActivePrescriptionPayment", data, String.class, vars);
        return "success";
	}

	@Override
	public String completeOrder(String orderId, String pharmacyTenanId) {
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        try {
            HttpClient httpclient=getCloseableHttpClient();
            factory.setHttpClient(httpclient);
            HttpUriRequest login = RequestBuilder.post()
                    .setUri(new URI(PHARMACY_SERVER_URL+"/ordermgr/control/login"))
                    .addParameter("USERNAME", userName)
                    .addParameter("PASSWORD", password)
                    .addParameter("tenantId", pharmacyTenanId)
                    .build();
            HttpResponse response = httpclient.execute(login);
        } catch (Exception e) {
            e.printStackTrace();
        }
        RestTemplate rt = new RestTemplate(factory);
        rt.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        rt.getMessageConverters().add(new StringHttpMessageConverter());
        assert pharmacyTenanId != null;
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("tenantId", pharmacyTenanId);
        Map<String, String> data = new HashMap<String, String>();
        data.put("orderId", orderId);
        String response = rt.postForObject(PHARMACY_SERVER_URL+"/ordermgr/control/completeActivePrescriptionOrder", data, String.class, vars);
        return "success";
	}
	
	@Override
	public String cancelOrder(String orderId, String pharmacyTenanId) {
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        try {
            HttpClient httpclient=getCloseableHttpClient();
            factory.setHttpClient(httpclient);
            HttpUriRequest login = RequestBuilder.post()
                    .setUri(new URI(PHARMACY_SERVER_URL+"/ordermgr/control/login"))
                    .addParameter("USERNAME", userName)
                    .addParameter("PASSWORD", password)
                    .addParameter("tenantId", pharmacyTenanId)
                    .build();
            HttpResponse response = httpclient.execute(login);
        } catch (Exception e) {
            e.printStackTrace();
        }
        RestTemplate rt = new RestTemplate(factory);
        rt.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        rt.getMessageConverters().add(new StringHttpMessageConverter());
        assert pharmacyTenanId != null;
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("tenantId", pharmacyTenanId);
        Map<String, String> data = new HashMap<String, String>();
        data.put("orderId", orderId);
        String response = rt.postForObject(PHARMACY_SERVER_URL+"/ordermgr/control/cancelActivePrescriptionOrder", data, String.class, vars);
        return "success";
	}


}
