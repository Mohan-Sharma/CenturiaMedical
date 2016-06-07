package com.nzion.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nzion.dto.CoPayment;
import com.nzion.dto.CoPaymentDetail;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: USER
 * Date: 4/22/15
 * Time: 7:55 AM
 * To change this template use File | Settings | File Templates.
 */
public class AfyaServiceConsumer {
	
    static String PORTAL_URL = null;
    static {
        Properties properties = new Properties();
        try {
            String profileName = System.getProperty("profile.name") != null ? System.getProperty("profile.name") : "dev";
            properties.load(AfyaServiceConsumer.class.getClassLoader().getResourceAsStream("application-"+profileName+".properties"));
            PORTAL_URL = (String)properties.get("PORTAL_SERVER_URL");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static CoPayment getServiceOrModuleDataByServiceId(String hisModuleId, String benefitId, String groupId, Set<String> serviceIds){
        if(UtilValidator.isEmpty(benefitId) || UtilValidator.isEmpty(serviceIds) )
            return new CoPayment();
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        List<MediaType> mediaTypes = new ArrayList<MediaType>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(mediaTypes);
        //MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
        //body.add("serviceIds",serviceIds);
        HttpEntity<?> requestEntity = new HttpEntity<Object>(httpHeaders);

        String serviceParam = "";
        for (String s : serviceIds) {
            serviceParam = serviceParam.concat(s).concat(",");
        }
        serviceParam = serviceParam.substring(0, serviceParam.length() - 1);

        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL + "/anonV1/insuranceMaster/getServiceOrModuleDataByServiceId?hisModuleId={hisModuleId}&benefitId={benefitId}&groupId={groupId}&serviceIds={serviceParam}",
                HttpMethod.GET, requestEntity, String.class,hisModuleId,benefitId,groupId,serviceParam);
        String json = responseEntity.getBody();

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
        CoPayment coPayment = null;
        try {
            coPayment =  mapper.readValue(json,  CoPayment.class );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return coPayment;

        /*CoPayment coPayment = new CoPayment();
        coPayment.setTotalCopayAmount(new BigDecimal(100));
        coPayment.setTotalDeductableAmount(new BigDecimal(50));
        List li = new ArrayList();
        li.add(new CoPaymentDetail("117",null,null,null,null,"GROSS",null));
        li.add(new CoPaymentDetail("118",null,null,null,null,"GROSS",null));
        coPayment.setCoPaymentDetailList(li);
        return coPayment;*/

    }
    public static String getHISModuleNameByBenefitId(String benefitId){
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        List<MediaType> mediaTypes = new ArrayList<MediaType>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(mediaTypes);
        HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL + "/anon/insuranceMaster/getBenefitNameById?benefitId={benefitId}", HttpMethod.GET, requestEntity, String.class, benefitId);
        String json = responseEntity.getBody();
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
        Map result = null;
        try {
            result = mapper.readValue(json, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(result.size() >0)
            return (String)result.get("benefitName");
        return null;
        /*Gson gson = new GsonBuilder().serializeNulls().create();
        Map<String, Object> map = new HashMap<String, Object>();
        map = (Map<String, Object>) gson.fromJson(json, map.getClass());
        if(map.size() >0)
            return (String)map.get("benefitName");
        return null;*/
    }
    public static Map<String, Object> getUserLoginByUserName(String userName) {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        List<MediaType> mediaTypes = new ArrayList<MediaType>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(mediaTypes);
        HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/getUserLoginByName?userName={userName}", HttpMethod.GET, requestEntity, String.class, userName);
        String json = responseEntity.getBody();
        Map<String, Object> result = new HashMap<String, Object>();
        Gson gson = new GsonBuilder().serializeNulls().create();
        result = (Map<String, Object>) gson.fromJson(json, result.getClass());
        return result;
    }

    public static Map<String, Object> getUserLoginByTenantId(String tenantId){
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        List<MediaType> mediaTypes = new ArrayList<MediaType>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(mediaTypes);
        HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/getAdminUserLoginByTenantId?tenantId={tenantId}", HttpMethod.GET, requestEntity, String.class, tenantId);
        String json = responseEntity.getBody();
        Map<String, Object> result = new HashMap<String, Object>();
        Gson gson = new GsonBuilder().serializeNulls().create();
        result = (Map<String, Object>) gson.fromJson(json, result.getClass());
        return result;
    }

    public static Map<String, Object> getUserLoginBySessionId(String sessionId) {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        List<MediaType> mediaTypes = new ArrayList<MediaType>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(mediaTypes);
        HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/getUserLoginBySessionId?sessionId={sessionId}", HttpMethod.GET, requestEntity, String.class, sessionId);
        String json = responseEntity.getBody();
        Map<String, Object> result = new HashMap<String, Object>();
        Gson gson = new GsonBuilder().serializeNulls().create();
        result = (Map<String, Object>) gson.fromJson(json, result.getClass());
        return result;
    }

    /*
    * Modified To read consents from portal by Mohan Sharma 22 Aug 15
    * */
    public static HttpHeaders getHttpHeader(){
        HttpHeaders headers = new HttpHeaders();
        List<MediaType> mediaTypes = new ArrayList<MediaType>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        headers.setAccept(mediaTypes);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public static List<Map<String, Object>> getPatientPrivacyPolicyConsents(String afyaId) {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/getPatientConsentForAfyaId?afyaId={afyaId}", HttpMethod.GET, requestEntity, String.class, afyaId);
        String json = responseEntity.getBody();
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        Gson gson = new GsonBuilder().serializeNulls().create();
        result = (List<Map<String, Object>>) gson.fromJson(json, result.getClass());
        return result;
    }
    
    public static List<Map<String,Object>> getPaymentGatewayTransactionById(String paymentId){
    	RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
    	HttpHeaders httpHeaders = getHttpHeader();
    	HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
    	ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/getPaymentGatewayTransactionById?paymentId={paymentId}", HttpMethod.GET, requestEntity, String.class, paymentId);
    	String json = responseEntity.getBody();
    	List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        Gson gson = new GsonBuilder().serializeNulls().create();
        result = (List<Map<String, Object>>) gson.fromJson(json, result.getClass());
        return result;
    }
    
    
    public static List<Map<String,Object>> getAllSmartClinicPharmacy(String tenantId){
    	RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
    	HttpHeaders httpHeaders = getHttpHeader();
    	HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
    	ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/getAllSmartClinicPharmacy?tenantId={tenantId}", HttpMethod.GET, requestEntity, String.class, tenantId);
    	String json = responseEntity.getBody();
    	List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        Gson gson = new GsonBuilder().serializeNulls().create();
        result = (List<Map<String, Object>>) gson.fromJson(json, result.getClass());
        return result;
    }

    /*
    * Added by Mohan Sharma to allow ssl
    * */

    public static HttpComponentsClientHttpRequestFactory getHttpComponentsClientHttpRequestFactory(){
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        HttpClient httpclient=getCloseableHttpClient();
        factory.setHttpClient(httpclient);
        return factory;
    }

    private static CloseableHttpClient getCloseableHttpClient() {
        TrustStrategy acceptingTrustStrategy = new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] certificate, String authType) {
                return true;
            }
        };
        SSLContext sslcontext = null;
        try {
            sslcontext = SSLContexts.custom()
                    .loadTrustMaterial(null, acceptingTrustStrategy)
                    .build();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslcontext,
                new String[]{"TLSv1"},
                null,
                SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        return HttpClients.custom().setSSLSocketFactory(sslsf).build();

    }

    public static ArrayList<HashMap<String, Object>> getAllAdminByTenantId() {
        String tenantId = Infrastructure.getPractice().getTenantId();
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        List<MediaType> mediaTypes = new ArrayList<MediaType>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(mediaTypes);
        HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL + "/anon/getAllAdminByTenantId?tenantId="+tenantId, HttpMethod.GET, requestEntity, String.class);
        String json = responseEntity.getBody();
        ArrayList<HashMap<String, Object>> result = new ArrayList<HashMap<String, Object>>();
        Gson gson = new GsonBuilder().serializeNulls().create();
        result = (ArrayList<HashMap<String, Object>>)gson.fromJson(json, result.getClass());
        return result;
    }

    public static Map<String, Object> getDoctorDetFromPortal(String tenantId, String doctorId) {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        List<MediaType> mediaTypes = new ArrayList<MediaType>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(mediaTypes);
        HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/getDoctorDetFromPortal?tenantId={tenantId}&doctorId={doctorId}", HttpMethod.GET, requestEntity, String.class,tenantId,doctorId);
        String json = responseEntity.getBody();
        Map<String, Object> result = new HashMap<String, Object>();
        Gson gson = new GsonBuilder().serializeNulls().create();
        result = (Map<String, Object>) gson.fromJson(json, result.getClass());
        return result;
    }

    public static void updateCancelStatusInPortal(String paymentId) {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        List<MediaType> mediaTypes = new ArrayList<MediaType>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(mediaTypes);
        HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/cancelPaymentTransaction?paymentId={paymentId}", HttpMethod.POST, requestEntity, String.class, paymentId);
       /* String json = responseEntity.getBody();
        Map<String, Object> result = new HashMap<String, Object>();
        Gson gson = new GsonBuilder().serializeNulls().create();
        result = (Map<String, Object>) gson.fromJson(json, result.getClass());
        return result;*/
    }

    public static Map<String, Object> getPaymentGateWayTransactionDetByPaymentId(String paymentId) {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        List<MediaType> mediaTypes = new ArrayList<MediaType>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(mediaTypes);
        HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/getPaymentGateWayTransactionDetByPaymentId?paymentId={paymentId}", HttpMethod.GET, requestEntity, String.class, paymentId);
        String json = responseEntity.getBody();
        Map<String, Object> result = new HashMap<String, Object>();
        Gson gson = new GsonBuilder().serializeNulls().create();
        result = (Map<String, Object>) gson.fromJson(json, result.getClass());
        return result;
    }

    public static List<Map<String,Object>> getAllTenantFromPortal(){
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/getAllTenantFromPortal", HttpMethod.GET, requestEntity, String.class);
        String json = responseEntity.getBody();
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        Gson gson = new GsonBuilder().serializeNulls().create();
        result = (List<Map<String, Object>>) gson.fromJson(json, result.getClass());
        return result;
    }

}
