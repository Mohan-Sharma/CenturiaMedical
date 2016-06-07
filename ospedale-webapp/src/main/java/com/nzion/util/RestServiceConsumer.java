package com.nzion.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.nzion.domain.Patient;
import com.nzion.domain.Provider;
import com.nzion.domain.emr.VisitTypeSoapModule;
import com.nzion.dto.*;
import com.nzion.service.dto.ServiceMasterDto;
import com.nzion.zkoss.dto.UserLoginDto;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.*;
import com.nzion.domain.Enumeration;

/**
 * Created by Mohan Sharma on 3/31/2015.
 */
public class RestServiceConsumer {
    static String PORTAL_URL = null;
    static String PORTAL_AUTHENTICATION = null;
    static {
        Properties properties = new Properties();
        try {
            String profileName = System.getProperty("profile.name") != null ? System.getProperty("profile.name") : "dev";
            properties.load(RestServiceConsumer.class.getClassLoader().getResourceAsStream("application-"+profileName+".properties"));
            PORTAL_URL = (String)properties.get("PORTAL_SERVER_URL");
            PORTAL_AUTHENTICATION = (String)properties.get("PORTAL_AUTHENTICATION");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String checkIfPatientExistInPortalAndCreateIfNotExist(Patient patient, String tenantId) {
        PatientDto patientDto = new PatientDto();
        patientDto.setPropertiesToPatientDto(patient);
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        String patientJsonString = gson.toJson(patientDto);
        System.out.println(patientJsonString);
        try {
            RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
            HttpHeaders headers = getHttpHeader();
            HttpEntity<String> requestEntity = new HttpEntity<String>(patientJsonString, headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/patient/retrieveAfyaId?tenantId={tenantId}&facilityType={facilityType}", HttpMethod.POST, requestEntity, String.class, tenantId, "CLINIC");
            String afyaId = responseEntity.getBody();
            System.out.println("***********************************************************************");
            System.out.println("***********************************************************************");
            System.out.println("Afya-id ->"+afyaId);
            System.out.println("***********************************************************************");
            System.out.println("***********************************************************************");
            Map<String, String> respHeaders = responseEntity.getHeaders().toSingleValueMap();
            String respResult = respHeaders.get("result");
            if(respResult.equals("DUPLICATE_USERNAME")) {
                UtilMessagesAndPopups.showError("Sorry, Mobile number already Registered, Please Register with a different Mobile number");
                return null;
            }
            return afyaId;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, String> checkIfPatientExistInPortalAndCreateIfNotExistWithResult(Patient patient, String tenantId) {
        PatientDto patientDto = new PatientDto();
        patientDto.setPropertiesToPatientDto(patient);
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        String patientJsonString = gson.toJson(patientDto);
        System.out.println(patientJsonString);
        try {
            RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
            HttpHeaders headers = getHttpHeader();
            HttpEntity<String> requestEntity = new HttpEntity<String>(patientJsonString, headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL + "/anon/patient/retrieveAfyaId?tenantId={tenantId}&facilityType={facilityType}", HttpMethod.POST, requestEntity, String.class, tenantId, "CLINIC");
            Map<String, String> result = responseEntity.getHeaders().toSingleValueMap();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static String persistDoctorInPortal(Provider provider, String tenantId, String practiceName) {
        ProviderDto providerDto = new ProviderDto();
        providerDto.setPropertiesToProviderDto(provider, practiceName);
        Gson gson = new GsonBuilder().serializeNulls().setDateFormat("yyyy-MM-dd").create();
        String providerJsonString = gson.toJson(providerDto);
        try {
            RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
            HttpHeaders headers = getHttpHeader();

            restTemplate.getMessageConverters()
                    .add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));

            HttpEntity<String> requestEntity = new HttpEntity<String>(providerJsonString, headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/persistDoctor?tenantId={tenantId}", HttpMethod.POST, requestEntity, String.class, tenantId);
            String providerId = responseEntity.getBody();
            return providerId;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<PharmacyDto> getPharmacies() {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/getAllPharmacies", HttpMethod.GET, requestEntity, String.class);
        String json = responseEntity.getBody();
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        Type typeOfPharmacyDto = new TypeToken<List<PharmacyDto>>() {
        }.getType();
        List<PharmacyDto> pharmacyDtos = gson.fromJson(json, typeOfPharmacyDto);
        return pharmacyDtos;
    }

    public static CivilUserDto fetchUserByCivilId(String civilId) {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL + "/anon/fetchUserByCivilId?civilId={civilId}", HttpMethod.GET, requestEntity, String.class, civilId);
        String repsonseJson = responseEntity.getBody();
        Gson gson = new GsonBuilder().serializeNulls().setDateFormat("yyyy-MM-dd").create();
        CivilUserDto civilUserDto = gson.fromJson(repsonseJson, CivilUserDto.class);
        return civilUserDto;
    }

    public static List<LabDto> getLaboratories() {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/getAllLaboratories", HttpMethod.GET, requestEntity, String.class);
        String json = responseEntity.getBody();
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        Type typeOfPharmacyDto = new TypeToken<List<LabDto>>() {
        }.getType();
        List<LabDto> labDtos = gson.fromJson(json, typeOfPharmacyDto);
        return labDtos;
    }

    public static List<InsuranceCompanyDto> getAllInsuranceCompany() {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/insuranceMaster/allInsuranceCompany", HttpMethod.GET, requestEntity, String.class);
        String json = responseEntity.getBody();
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        Type typeOfInsuranceCompanyDto = new TypeToken<List<InsuranceCompanyDto>>() {
        }.getType();
        List<InsuranceCompanyDto> insuranceCompanyDtos = gson.fromJson(json, typeOfInsuranceCompanyDto);
        return insuranceCompanyDtos;
    }

    public static List<InsurancePlanDto> getInsurancePlanByInsuranceCompany(String insuranceCompanyCode) {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/insuranceMaster/allInsurancePlan?insuranceCompanyCode={insuranceCompanyCode}", HttpMethod.GET, requestEntity, String.class, insuranceCompanyCode);
        String json = responseEntity.getBody();
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        Type typeOfPatientInsurancePlanDto = new TypeToken<List<InsurancePlanDto>>() {
        }.getType();
        List<InsurancePlanDto> patientInsurancePlanDtos = gson.fromJson(json, typeOfPatientInsurancePlanDto);
        return patientInsurancePlanDtos;
    }

    public static List<Map> getGroupsByClinicId(String clinicId) {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL + "/anon/insuranceMaster/fetchListOfGroupNamesByClinicId?clinicId={clinicId}", HttpMethod.GET, requestEntity, String.class, clinicId);
        String json = responseEntity.getBody();

        ObjectMapper mapper = new ObjectMapper();
        List<Map> li = new ArrayList<>();
        try {
            li = mapper.readValue(json, List.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return li;
    }

    public static InsuranceGroupPlanDetailDto getPlanDetailsForGroupId(String groupId, String dependent, String gender) {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/insuranceMaster/getPlanDetailsForGroupId?groupId={groupId}&dependent={dependent}&gender={gender}", HttpMethod.GET, requestEntity, String.class, groupId, dependent, gender);
        String json = responseEntity.getBody();

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
        InsuranceGroupPlanDetailDto insuranceGroupPlanDetailDto = null;
        try {
            insuranceGroupPlanDetailDto = mapper.readValue(json, InsuranceGroupPlanDetailDto.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return insuranceGroupPlanDetailDto;
    }

    public static InsuranceGroupPlanDetailDto getPlanDetailsForPolicyId(String policyId, String dependent, String gender) {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/insuranceMaster/getPlanDetailsForPolicyId?policyId={policyId}&dependent={dependent}&gender={gender}", HttpMethod.GET, requestEntity, String.class, policyId, dependent, gender);
        String json = responseEntity.getBody();

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
        InsuranceGroupPlanDetailDto insuranceGroupPlanDetailDto = null;
        try {
            insuranceGroupPlanDetailDto = mapper.readValue(json, InsuranceGroupPlanDetailDto.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return insuranceGroupPlanDetailDto;
    }

    public static InsuranceGroupPlanDetailDto getPlanDetailsForPolicyIdAndPolicyName(String policyId,String policyName, String dependent, String gender) {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/insuranceMaster/getPlanDetailsForPolicyIdAndPolicyName?policyId={policyId}&policyName={policyName}&dependent={dependent}&gender={gender}", HttpMethod.GET, requestEntity, String.class, policyId,policyName,dependent, gender);
        String json = responseEntity.getBody();

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
        InsuranceGroupPlanDetailDto insuranceGroupPlanDetailDto = null;
        try {
            insuranceGroupPlanDetailDto = mapper.readValue(json, InsuranceGroupPlanDetailDto.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return insuranceGroupPlanDetailDto;
    }

    public static List<ModuleDetailsDto> getModulesByBenefitId(String benefitId) {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/insuranceMaster/getModulesByBenefitId?benefitId={benefitId}", HttpMethod.GET, requestEntity, String.class, benefitId);
        String json = responseEntity.getBody();

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
        List<ModuleDetailsDto> moduleDetailsDtos = null;
        try {
            moduleDetailsDtos = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, ModuleDetailsDto.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return moduleDetailsDtos;
    }

    public static List<HisModuleDto> getHISModules(){
        List<HisModuleDto> hisModuleDtos = null;
        try {
            RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
            HttpHeaders httpHeaders = getHttpHeader();
            HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
            ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/insuranceMaster/getHISModules", HttpMethod.GET, requestEntity, String.class);
            String json = responseEntity.getBody();

            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
            hisModuleDtos = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, HisModuleDto.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hisModuleDtos;
    }

    public static List<TPAPayersDto> allTPAPayers() {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/insuranceMaster/allTPAPayers", HttpMethod.GET, requestEntity, String.class);
        String json = responseEntity.getBody();

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
        List<TPAPayersDto> tpaPayersDtos = null;
        try {
            tpaPayersDtos = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, TPAPayersDto.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tpaPayersDtos;
    }

    public static List<InsuranceDetailsDto> getInsuranceDetailsOfTpa(String payerId) {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
        ResponseEntity<String> responseEntity = null;
        if (UtilValidator.isNotEmpty(payerId))
            responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/insuranceMaster/getInsuranceDetailsOfTpa?payerId={payerId}", HttpMethod.GET, requestEntity, String.class, payerId);
        else
            responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/insuranceMaster/getInsuranceDetailsOfTpa", HttpMethod.GET, requestEntity, String.class);
        String json = responseEntity.getBody();

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
        List<InsuranceDetailsDto> insuranceDetailsDtos = null;
        try {
            insuranceDetailsDtos = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, InsuranceDetailsDto.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return insuranceDetailsDtos;
    }

    public static List<GroupDto> fetchListOfGroupNamesByPayer(String payerId) {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/insuranceMaster/fetchListOfGroupNamesByPayer?payerId={payerId}", HttpMethod.GET, requestEntity, String.class, payerId);
        String json = responseEntity.getBody();

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
        List<GroupDto> groupDtos = null;
        try {
            groupDtos = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, GroupDto.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return groupDtos;
    }

    public static List<GroupDto> fetchListOfGroupNames() {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/insuranceMaster/fetchListOfGroupNames", HttpMethod.GET, requestEntity, String.class);
        String json = responseEntity.getBody();

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
        List<GroupDto> groupDtos = null;
        try {
            groupDtos = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, GroupDto.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return groupDtos;
    }

    public static Map<String, Object> getModulesAndServiceDetailsByBenefitId(String benefitId) {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/insuranceMaster/getServiceOrModuleDataByBenefitId?benefitId={benefitId}", HttpMethod.GET, requestEntity, String.class, benefitId);
        String json = responseEntity.getBody();
        Map<String, Object> result = new HashMap<>();
        Gson gson = new GsonBuilder().serializeNulls().create();
        result = (Map<String, Object>) gson.fromJson(json, result.getClass());
        return furnishResultMap(result);
    }

    private static Map<String, Object> furnishResultMap(Map<String, Object> result) {
        if (result.size() > 0) {
            List<Map<String, Object>> moduleDetails = (List<Map<String, Object>>) result.get("moduleDetails");
            List<Map<String, Object>> associatedServiceDetailsOfTheModule = (List<Map<String, Object>>) result.get("associatedServiceDetailsOfTheModule");
            for (Map<String, Object> detail : moduleDetails) {
                if (detail.get("authorization") != null && (Double) detail.get("authorization") == 1)
                    detail.put("authorization", true);
                if (detail.get("authorizationInclusiveConsultation") != null && (Double) detail.get("authorizationInclusiveConsultation") == 1)
                    detail.put("authorizationInclusiveConsultation", true);
                if (detail.get("authorizationRequiredConsultation") != null && (Double) detail.get("authorizationRequiredConsultation") == 1)
                    detail.put("authorizationRequiredConsultation", true);
            }

            for (Map<String, Object> detail : associatedServiceDetailsOfTheModule) {
                if (detail.get("authorization") != null && (Double) detail.get("authorization") == 1)
                    detail.put("authorization", true);
                if (detail.get("authorizationInclusiveConsultation") != null && (Double) detail.get("authorizationInclusiveConsultation") == 1)
                    detail.put("authorizationInclusiveConsultation", true);
                if (detail.get("authorizationRequiredConsultation") != null && (Double) detail.get("authorizationRequiredConsultation") == 1)
                    detail.put("authorizationRequiredConsultation", true);
            }
            result.put("moduleDetails", moduleDetails);
            result.put("associatedServiceDetailsOfTheModule", associatedServiceDetailsOfTheModule);
        }
        return result;
    }

    public static String getHISModuleNameByBenefitId(String benefitId) {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"//anon/insuranceMaster/getBenefitNameById?benefitId={benefitId}", HttpMethod.GET, requestEntity, String.class, benefitId);
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
        if (result.size() > 0)
            return (String) result.get("benefitName");
        return null;
    }

    public static List<Map<String, Object>> getAllCities() {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        List<Map<String, Object>> result = new ArrayList<>();

        if(PORTAL_AUTHENTICATION.equals("true")){
            ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL + "/anon/getAllCities", HttpMethod.GET, requestEntity, String.class);
            String json = responseEntity.getBody();
            Gson gson = new GsonBuilder().serializeNulls().create();
            result = (List<Map<String, Object>>) gson.fromJson(json, result.getClass());
        } else if (PORTAL_AUTHENTICATION.equals("false")){
            com.nzion.repository.common.CommonCrudRepository commonCrudRepository = com.nzion.util.Infrastructure.getSpringBean("commonCrudRepository");
            List<Enumeration> enumerationList = commonCrudRepository.findByEquality(Enumeration.class, new String[]{"enumType"}, new Object[]{"CITY"});
            for (Enumeration enumeration : enumerationList){
                Map<String, Object> map = new HashMap<>();
                map.put("city", enumeration.description);
                result.add(map);
            }
        }
        return result;
    }

    public static List<Map<String, Object>> getAllStates() {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        List<Map<String, Object>> result = new ArrayList<>();
        if (PORTAL_AUTHENTICATION.equals("true")) {
            ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL + "/anon/getAllStates", HttpMethod.GET, requestEntity, String.class);
            String json = responseEntity.getBody();
            Gson gson = new GsonBuilder().serializeNulls().create();
            result = (List<Map<String, Object>>) gson.fromJson(json, result.getClass());
        } else if (PORTAL_AUTHENTICATION.equals("false")){
            com.nzion.repository.common.CommonCrudRepository commonCrudRepository = com.nzion.util.Infrastructure.getSpringBean("commonCrudRepository");
            List<Enumeration> enumerationList = commonCrudRepository.findByEquality(Enumeration.class, new String[]{"enumType"}, new Object[]{"STATE"});
            for (Enumeration enumeration : enumerationList){
                Map<String, Object> map = new HashMap<>();
                map.put("state", enumeration.description);
                result.add(map);
            }
        }
        return result;
    }

    public static List<Map<String, Object>> getAllCountries() {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/getAllCountries", HttpMethod.GET, requestEntity, String.class);
        String json = responseEntity.getBody();
        List<Map<String, Object>> result = new ArrayList<>();
        Gson gson = new GsonBuilder().serializeNulls().create();
        result = (List<Map<String, Object>>) gson.fromJson(json, result.getClass());
        return result;
    }

    public static List<Map<String, Object>> getAllNationality() {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/getAllNationality", HttpMethod.GET, requestEntity, String.class);
        String json = responseEntity.getBody();
        List<Map<String, Object>> result = new ArrayList<>();
        Gson gson = new GsonBuilder().serializeNulls().create();
        result = (List<Map<String, Object>>) gson.fromJson(json, result.getClass());
        return result;
    }

    public static Map<String, Object> getStateCountryBasedOnCity(String city) {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/getStateCountryBasedOnCity?city={city}", HttpMethod.GET, requestEntity, String.class, city);
        String json = responseEntity.getBody();
        Map<String, Object> result = new HashMap<>();
        Gson gson = new GsonBuilder().serializeNulls().create();
        result = (Map<String, Object>) gson.fromJson(json, result.getClass());
        return result;
    }

    public static Map<String, Object> getClinicDetailsByClinicId(String tenantId) {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        System.out.println("\n\n\ntenant = "+tenantId+"\n\n\n");
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL + "/anon/getClinicDetailsByClinicId?clinicId={tenantId}", HttpMethod.GET, requestEntity, String.class, tenantId);
        String json = responseEntity.getBody();
        Map<String, Object> result = new HashMap<>();
        Gson gson = new GsonBuilder().serializeNulls().create();
        result = (Map<String, Object>) gson.fromJson(json, result.getClass());
        return result;
    }

    public static Map<String, Object> getCorporateMasterByCorporateId(String corporateId) {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/getCorporateMasterByCorporateId?corporateId={corporateId}", HttpMethod.GET, requestEntity, String.class, corporateId);
        String json = responseEntity.getBody();
        Map<String, Object> result = new HashMap<>();
        Gson gson = new GsonBuilder().serializeNulls().create();
        result = (Map<String, Object>) gson.fromJson(json, result.getClass());
        return result;
    }

    public static List<Map<String, Object>> getAllPayers() {

        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/insuranceMaster/getAllPayers", HttpMethod.GET, requestEntity, String.class);
        String json = responseEntity.getBody();
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        Gson gson = new GsonBuilder().serializeNulls().create();
        result = (List<Map<String, Object>>) gson.fromJson(json, result.getClass());
        return result;

    }

    public static List<Map<String, Object>> getAllCorporates() {

        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/insuranceMaster/getAllCorporates", HttpMethod.GET, requestEntity, String.class);
        String json = responseEntity.getBody();
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        Gson gson = new GsonBuilder().serializeNulls().create();
        result = (List<Map<String, Object>>) gson.fromJson(json, result.getClass());
        return result;

    }

    public static List<Map<String,Object>> getAllHealthPolicy() {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/insuranceMaster/getAllHealthPolicy", HttpMethod.GET, requestEntity, String.class);
        String json = responseEntity.getBody();
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        Gson gson = new GsonBuilder().serializeNulls().create();
        result = (List<Map<String, Object>>) gson.fromJson(json, result.getClass());
        return result;
    }

    public static List<GroupDto> getAllGroups() {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/insuranceMaster/getAllGroups", HttpMethod.GET, requestEntity, String.class);
        String json = responseEntity.getBody();
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
        List<GroupDto> groupDtos = null;
        try {
            groupDtos = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, GroupDto.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return groupDtos;
    }

    public static List<InsuranceGroupDto> getPolicyForIndividuals() {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/insuranceMaster/getPolicyForIndividual", HttpMethod.GET, requestEntity, String.class);
        String json = responseEntity.getBody();
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
        List<InsuranceGroupDto> policyDtos = null;
        try {
            policyDtos = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, InsuranceGroupDto.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return policyDtos;
    }

    public static List<InsuranceGroupDto> getPolicyByGroupId(String groupId) {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/insuranceMaster/getPolicyByGroupId?groupId={groupId}", HttpMethod.GET, requestEntity, String.class,groupId);
        String json = responseEntity.getBody();
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
        List<InsuranceGroupDto> policyDtos = null;
        try {
            policyDtos = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, InsuranceGroupDto.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return policyDtos;
    }

    public static InsuranceForTpaDto  getPayerById(String payerId) {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/insuranceMaster/getPayerById?payerId={payerId}", HttpMethod.GET, requestEntity, String.class,payerId);
        String json = responseEntity.getBody();
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
        List<InsuranceForTpaDto> policyDtos = null;
        try {
            policyDtos = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, InsuranceForTpaDto.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (policyDtos != null && policyDtos.size() > 0) {
            return  policyDtos.get(0);
        }
        return null;
    }
    public static HealthPolicyDto getHealthPolicyById(String id) {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/insuranceMaster/getHealthPolicyById?id={id}", HttpMethod.GET, requestEntity, String.class,id);
        String json = responseEntity.getBody();
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
        List<HealthPolicyDto> policyDtos = null;
        try {
            policyDtos = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, HealthPolicyDto.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (policyDtos != null && policyDtos.size() > 0) {
            return policyDtos.get(0);
        }
        return null;
    }

    public static List<Map<String, Object>> getListOfInsuranceForGivenTenant(String tenantId) {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/getListOfInsuranceForGivenTenant?tenantId={tenantId}&facilityType={facilityType}", HttpMethod.GET, requestEntity, String.class, tenantId, "clinic");
        String json = responseEntity.getBody();
        List<Map<String, Object>> result = new ArrayList<>();
        Gson gson = new GsonBuilder().serializeNulls().create();
        result = (List<Map<String, Object>>) gson.fromJson(json, result.getClass());
        return result;
    }

    public static List<Map<String, Object>> getPlansForGivenInsurance(Double payerId) {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/getPlansForGivenInsurance?payerId={payerId}", HttpMethod.GET, requestEntity, String.class, payerId.longValue());
        String json = responseEntity.getBody();
        List<Map<String, Object>> result = new ArrayList<>();
        Gson gson = new GsonBuilder().serializeNulls().create();
        result = (List<Map<String, Object>>) gson.fromJson(json, result.getClass());
        return result;
    }

    public static Map<String, Object> getNationalityByNationalityCode(String nationalityCode) {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/getNationalityByNationalityCode?nationalityCode={nationalityCode}", HttpMethod.GET, requestEntity, String.class, nationalityCode);
        String json = responseEntity.getBody();
        Map<String, Object> result = new HashMap<>();
        Gson gson = new GsonBuilder().serializeNulls().create();
        result = (Map<String, Object>) gson.fromJson(json, result.getClass());
        return result;
    }

    public static  List<ServiceMasterDto> getAllServiceMaster(int serviceMainGroupId) {

        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        List<MediaType> mediaTypes = new ArrayList<MediaType>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(mediaTypes);
        HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/getAllServiceMaster?serviceMainGroupId={serviceMainGroupId}", HttpMethod.GET, requestEntity, String.class,serviceMainGroupId);
        String json = responseEntity.getBody();
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
        List<ServiceMasterDto> dtoList = null;
        try {
            dtoList = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, ServiceMasterDto.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dtoList;
    }

    public static List<Map<String, Object>> getPatientPrivacyPolicyConsents(String afyaId) {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL + "/anon/getPatientConsentForAfyaId?afyaId={afyaId}", HttpMethod.GET, requestEntity, String.class, afyaId);
        String json = responseEntity.getBody();
        List<Map<String, Object>> result = new ArrayList<>();
        Gson gson = new GsonBuilder().serializeNulls().create();
        result = (List<Map<String, Object>>) gson.fromJson(json, result.getClass());
        return result;
    }

    public static Map<String, Object> getSMSSenderNameForGivenTenant(String tenantId) {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL + "/anon/getSMSSenderNameForGivenTenant?tenantId={tenantId}", HttpMethod.GET, requestEntity, String.class, tenantId);
        String json = responseEntity.getBody();
        Gson gson = new GsonBuilder().serializeNulls().create();
        return (Map<String, Object>) gson.fromJson(json, Map.class);
    }

    public static boolean checkIfSmsAvailableForTenant(String tenantId) {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL + "/anon/checkIfSmsAvailableForTenant?tenantId={tenantId}", HttpMethod.GET, requestEntity, String.class, tenantId);
        String result = responseEntity.getBody();
        if(UtilValidator.isEmpty(result))
            return Boolean.FALSE;
        return Boolean.valueOf(result);
    }

    public static boolean updateSMSCountForGivenTenant(String tenantId) {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/updateSMSCountForGivenTenant?tenantId={tenantId}", HttpMethod.POST, requestEntity, String.class, tenantId);
        String result = responseEntity.getBody();
        if(UtilValidator.isEmpty(result))
            return Boolean.FALSE;
        return Boolean.valueOf(result);
    }

    public static Map<String, Object> getSubscribedPackageDetailsForGivenTenant(String tenantId) {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL + "/anon/getSubscribedPackageDetailsForGivenTenant?tenantId={tenantId}", HttpMethod.GET, requestEntity, String.class, tenantId);
        String json = responseEntity.getBody();
        if(UtilValidator.isEmpty(json))
            return Collections.EMPTY_MAP;
        Gson gson = new GsonBuilder().serializeNulls().create();
        return (Map<String, Object>) gson.fromJson(json, Map.class);
    }

    public static boolean clearCookiesOnLogoutInFacility() {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL + "/anon/clearCookiesOnLogoutInFacility", HttpMethod.POST, requestEntity, String.class, Collections.EMPTY_MAP);
        String result = responseEntity.getBody();
        if(UtilValidator.isEmpty(result))
            return Boolean.FALSE;
        return Boolean.valueOf(result);
    }

    public static HttpHeaders getHttpHeader(){
        HttpHeaders headers = new HttpHeaders();
        List<MediaType> mediaTypes = new ArrayList<>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        headers.setAccept(mediaTypes);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

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

    public static void updateDoctorSmartServiceInPortal(VisitTypeSoapModule visitTypeSoapModule) {

        DoctorSmartServiceDto doctorSmartServiceDto = new DoctorSmartServiceDto();
        doctorSmartServiceDto.setPropertiesToDoctorSmartServiceDto(visitTypeSoapModule);
        doctorSmartServiceDto.setTenantId(Infrastructure.getPractice().getTenantId());
        Gson gson = new GsonBuilder().create();
        String doctorSmartServiceJsonString = gson.toJson(doctorSmartServiceDto);

        try {
            RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
            HttpHeaders headers = getHttpHeader();
            HttpEntity<String> requestEntity = new HttpEntity<String>(doctorSmartServiceJsonString, headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/updateDoctorSmartService", HttpMethod.POST, requestEntity, String.class, "CLINIC");
            String responseStatus = responseEntity.getStatusCode().toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Map<String, Object>> getAllServiceMaster() {

        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL+"/anon/getAllService", HttpMethod.GET, requestEntity, String.class);
        String json = responseEntity.getBody();
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        Gson gson = new GsonBuilder().serializeNulls().create();
        result = (List<Map<String, Object>>) gson.fromJson(json, result.getClass());
        return result;

    }
    public static Map<String, Object> getPatientDetailsByAfyaId(String afyaId) {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL + "/anon/getPatientDetailsByAfyaId?afyaId={afyaId}", HttpMethod.GET, requestEntity, String.class, afyaId);
        String json = responseEntity.getBody();
        Gson gson = new GsonBuilder().serializeNulls().create();
        return (Map<String, Object>) gson.fromJson(json, Map.class);
    }

    public static String updateUserDetInPortal(UserLoginDto userLoginDto) {
        Gson gson = new GsonBuilder().serializeNulls().setDateFormat("yyyy-MM-dd").create();
        String userLoginJsonString = gson.toJson(userLoginDto);
        try {
            RestTemplate restTemplate = new RestTemplate(RestServiceConsumer.getHttpComponentsClientHttpRequestFactory());
            HttpHeaders headers = getHttpHeader();
            HttpEntity<String> requestEntity = new HttpEntity<String>(userLoginJsonString, headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL + "/anon/updateUserDetInPortal", HttpMethod.POST, requestEntity, String.class);
            return responseEntity.getBody();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Map<String, Object>> getPatientContactsFromAfyaId(String afyaId) {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL + "/anon/getPatientContactsFromAfyaId?afyaId={afyaId}&filter=|ALTERNATE_CONTACT|", HttpMethod.GET, requestEntity, String.class, afyaId);
        String json = responseEntity.getBody();
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        Gson gson = new GsonBuilder().serializeNulls().create();
        return (List<Map<String, Object>>) gson.fromJson(json, result.getClass());
    }

    public static boolean addPatientAlternateContact(Patient patient, String contactType, String contactValue) {

        int accountNumber = new Double(getPatientDetailsByAfyaId(patient.getAfyaId()).get("accountNumber").toString()).intValue();

        Map<String, Object> map = new HashMap<>();
        map.put("accountNumber", accountNumber);
        map.put("contactType", contactType);
        map.put("contactValue", contactValue);
        map.put("isValid", "true");
        map.put("createdByUser", Infrastructure.getLoggedInPerson().getUserLogin().getId().toString());
        Gson gson = new GsonBuilder().serializeNulls().create();
        String patientJsonString = gson.toJson(map);
        try {
            RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
            HttpHeaders httpHeaders = getHttpHeader();
            HttpEntity<String> requestEntity = new HttpEntity<String>(patientJsonString, httpHeaders);
            ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL + "/anon/addPatientContact", HttpMethod.POST, requestEntity, String.class);
            String json = responseEntity.getBody();
            //List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
            //return (List<Map<String, Object>>) gson.fromJson(json, result.getClass());
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static boolean updatePatientAlternateContact(Map map) {
        int contactId = new Double(map.get("contactId").toString()).intValue();
        Map<String, Object> map1 = new HashMap<>();
        map1.put("contactId", contactId);
        map1.put("contactValue", map.get("contactValue"));
        map1.put("isValid", false);
        map1.put("updatedByUser", Infrastructure.getLoggedInPerson().getUserLogin().getId().toString());
        Gson gson = new GsonBuilder().serializeNulls().create();
        String patientJsonString = gson.toJson(map1);
        try {
            RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
            HttpHeaders httpHeaders = getHttpHeader();
            HttpEntity<String> requestEntity = new HttpEntity<String>(patientJsonString, httpHeaders);
            ResponseEntity<String> responseEntity = restTemplate.exchange(PORTAL_URL + "/anon/updatePatientContact", HttpMethod.POST, requestEntity, String.class);
            String json = responseEntity.getBody();
            //List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
            //return (List<Map<String, Object>>) gson.fromJson(json, result.getClass());
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
    public static List<Map<String, Object>> getPatientContactsFromAfyaIdAndType(String afyaId, String type) {
        RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = null;
        if (type != null) {
            String contactCategory = "|" + type + "|";
            responseEntity = restTemplate.exchange(PORTAL_URL + "/anon/getPatientContactsFromAfyaId?afyaId={afyaId}&filter="+contactCategory, HttpMethod.GET, requestEntity, String.class, afyaId);
        } else {
            responseEntity = restTemplate.exchange(PORTAL_URL + "/anon/getPatientContactsFromAfyaId?afyaId={afyaId}", HttpMethod.GET, requestEntity, String.class, afyaId);
        }
        String json = responseEntity.getBody();
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        Gson gson = new GsonBuilder().serializeNulls().create();
        return (List<Map<String, Object>>) gson.fromJson(json, result.getClass());
    }
}
