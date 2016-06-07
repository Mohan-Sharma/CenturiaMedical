package com.nzion.service.impl;

import com.nzion.domain.*;
import com.nzion.domain.Enumeration;
import com.nzion.domain.emr.EMRPatientInfo;
import com.nzion.domain.emr.FamilyMember;
import com.nzion.domain.emr.soap.PatientRx;
import com.nzion.domain.emr.soap.PatientSoapNote;
import com.nzion.domain.emr.soap.RxSection;
import com.nzion.domain.messaging.Message;
import com.nzion.domain.pms.PMSPatientInfo;
import com.nzion.dto.PatientDto;
import com.nzion.enums.EventType;
import com.nzion.exception.TransactionException;
import com.nzion.repository.PatientRepository;
import com.nzion.service.PatientService;
import com.nzion.service.SoapNoteService;
import com.nzion.service.UserLoginService;
import com.nzion.service.billing.BillingService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.service.common.impl.ApplicationEvents;
import com.nzion.util.Infrastructure;
import com.nzion.util.UtilMisc;
import com.nzion.util.UtilValidator;
import com.nzion.view.PatientViewObject;
import com.nzion.view.RolesValueObject;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.axonframework.domain.GenericEventMessage;
import org.axonframework.eventhandling.EventBus;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
@Service("patientService")
public class PatientServiceImpl implements PatientService {

    @Autowired
    private EventBus eventBus;
    private PatientRepository patientRepository;

    @Autowired(required = true)
    private CommonCrudService commonCrudService;

    @Autowired(required = true)
    private SoapNoteService soapNoteService;
    private UserLoginService userLoginService;

    @Autowired(required = true)
    private BillingService billingService;

    @Resource
    @Required
    public void setSoapNoteService(SoapNoteService soapNoteService) {
        this.soapNoteService = soapNoteService;
    }

    public Patient getPatient(String accountNumber) {
        return commonCrudService.getByAccountNumber(Patient.class, accountNumber);
    }

    @Resource(name = "patientRepo")
    @Required
    public void setPatientRepository(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public void saveOrUpdate(Patient patient, String eventMessage) {
        patientRepository.saveOrUpdate(patient);
        if (UtilValidator.isNotEmpty(eventMessage))
            ApplicationEvents.postEvent(EventType.PATIENT_RECORD, patient, Infrastructure.getUserLogin(), eventMessage);
    }

    public void saveOrUpdate(PatientViewObject patientVO) {
        Patient patient = patientVO.getPatient();
        patientRepository.saveOrUpdate(patient);
    }

    public void createPatient(PatientViewObject patientVO) throws TransactionException {
        Patient patient = patientVO.getPatient();
        patientRepository.save(patient);
    }

    public void createPatientForSubscriptionType(PatientViewObject patientVO) {
        PatientDto patientDto = new PatientDto();
        patientDto.setPropertiesToPatientDto(patientVO.getPatient());
        saveOrUpdate(patientVO.getEmrPatientInfo());
        saveOrUpdate(patientVO.getPmsPatientInfo());
        GenericEventMessage<PatientDto> genericEventMessage = new GenericEventMessage<PatientDto>(UUID.randomUUID().toString(), new DateTime(), patientDto, null);
        eventBus.publish(genericEventMessage);
    }

    public void saveOrUpdate(PMSPatientInfo patient) {
        patientRepository.saveOrUpdate(patient);
    }

    public void saveOrUpdate(EMRPatientInfo patient) {
        patientRepository.saveOrUpdate(patient);
    }

    public List<Patient> getAllPatients() {
        return patientRepository.getAllPatients();
    }

    public void deleteAllPatient() {
        patientRepository.deleteAllPatient();
    }

    public void deletePatient(Patient patient) {
        patientRepository.deletePatient(patient);

    }

    public EMRPatientInfo getEmrPatient(Long patientId) {
        return patientRepository.getEMRPatientInfo(patientId);
    }

    public PMSPatientInfo getPmsPatient(Long patientId) {
        return patientRepository.getPMSPatientInfo(patientId);
    }

    public Patient getPatientById(Long patientId) {
        return patientRepository.getPatientById(patientId);
    }

    @Resource
    @Required
    public void setCommonCrudService(CommonCrudService commonCrudService) {
        this.commonCrudService = commonCrudService;
    }

    @Override
    public List<PatientFamilyMember> getFamilyMembers(Patient patient) {
        return patientRepository.getFamilyMembers(patient);
    }

    @Override
    public List<Patient> searchPatientsBy(String accountNumber, String firstName, String lastName, Enumeration gender,
                                          Integer age) {
        return patientRepository.searchPatientsBy(accountNumber, firstName, lastName, gender, age);
    }

    @Override
    public List<Patient> searchPatient(String accountNumber, String firstName, String lastName, Long genderId, Integer age, String mobileNumber, Integer lowerLimit, Integer upperLimit) {
        if (genderId != null) {
            Enumeration gender = commonCrudService.getById(Enumeration.class, genderId);
            return patientRepository.searchPatient(accountNumber, firstName, lastName, gender, age, mobileNumber, lowerLimit, upperLimit);
        } else {
            return patientRepository.searchPatient(accountNumber, firstName, lastName, null, age, mobileNumber, lowerLimit, upperLimit);
        }
    }

    @Override
    public List<PatientFamilyMember> getFamilyMembersFor(Patient patient, FamilyMember familyMember) {
        return patientRepository.getFamilyMembersFor(patient, familyMember);
    }

    @SuppressWarnings("unchecked")
    public List<PatientRx> getActivePatientRxs(Patient patient) {
        List<RxSection> rxSections = (List<RxSection>) soapNoteService.getSoapSections(RxSection.class, patient);
        List<PatientRx> activePatientRxs = new ArrayList<PatientRx>();
        for (RxSection rxSection : rxSections) {
            List<PatientRx> patientRxs = new ArrayList<PatientRx>(rxSection.getPatientRxs());
            for (PatientRx patientRx : patientRxs)
                if ("Active".equalsIgnoreCase(patientRx.getStatus())) activePatientRxs.add(patientRx);
        }
        return activePatientRxs;
    }

    @Override
    public PatientOtherContactDetail getPatientOtherContactDetailFor(Patient patient) {
        List<PatientOtherContactDetail> patientOtherContactDetails = patientRepository.getpatientOtherContactDetailFor(patient);
        if (UtilValidator.isNotEmpty(patientOtherContactDetails)) return patientOtherContactDetails.get(0);
        return new PatientOtherContactDetail(patient);
    }

    @Override
    public List<File> getFilesForDocumentType(Patient patient, String documentType) {
        return patientRepository.getFilesForDocumentType(patient, new String[]{documentType});
    }

    @Override
    public void storeActiveMedications(PatientSoapNote soapNote, List<PatientRx> allMedications) {
        patientRepository.storeActiveMedications(soapNote, allMedications);
    }

    public UserLoginService getUserLoginService() {
        return userLoginService;
    }

    @Resource
    public void setUserLoginService(UserLoginService userLoginService) {
        this.userLoginService = userLoginService;
    }

    @Override
    public List<PatientRemainder> getPatientRemainders(Patient patient) {
        return patientRepository.getPatientRemainders(patient);
    }

    @Override
    public void createUserLoginForPatient(Patient patient) {
        patient = patientRepository.getPatientById(patient.getId());
        if (StringUtils.isNotBlank(patient.getContacts().getEmail())) {
            UserLogin userLogin = new UserLogin();
            String username = null;
            String firstName = patient.getFirstName();
            String lastName = patient.getLastName();
            if (firstName.length() >= 2 && lastName.length() >= 2)
                username = firstName.substring(0, 2) + lastName.substring(0, 2);
            else {
                username = firstName.substring(0, 1) + lastName.substring(0, 1);
                username = RandomStringUtils.random(6, username);
            }
            boolean userLoginExists = true;
            int i = 1;
            Infrastructure.getSessionFactory().getCurrentSession().disableFilter("PracticeFilter");
            while (userLoginExists) {
                try {
                    userLoginService.getUserByUsername(username);
                    username = username + "00" + i;
                    i++;
                } catch (UsernameNotFoundException unfe) {
                    userLoginExists = false;
                }
            }
            username = username.toUpperCase();
            userLogin.setUsername(username);
            userLogin.setPerson(patient);
            userLogin.addRole(Roles.PATIENT);
            userLoginService.createUserLogin(userLogin);
            patientRepository.save(patient);
        }

    }

    public Long savePatient(Patient patient) {
        patient = commonCrudService.save(patient);
        return patient.getId();
    }

    @Override
    public List<Map<String, Object>> getAllInsurenceMaster() {
        return patientRepository.getAllInsurenceMaster();
    }

    @Override
    public List<Map<String, Object>> getSelectedInsurancePlan(
            String insuranceCode) {
        return patientRepository.getSelectedInsurancePlan(insuranceCode);
    }

    @Override
    public List<Map<String, Object>> getSelectedCorporatePlan(
            String corporateCode) {
        return patientRepository.getSelectedCorporatePlan(corporateCode);
    }

    public void createAppointmentReqFromMobile(boolean isUrgent, Date sendOn, String patientId, String doctorId, String patientFirstName,
                                               String lastName, String mobileNumber, String email, String messageText, String visitType){
        Message message = new Message();
        RolesValueObject rolesVo = new RolesValueObject(message.getAuthorization());
        message.setTask(false);
        message.setUrgent(isUrgent);
        rolesVo.setReception(true);
        message.setSentOn(new Date());
        if(UtilValidator.isNotEmpty(patientId)){
            Patient patient = commonCrudService.getById(Patient.class,Long.valueOf(patientId) );
            message.setSentBy(patient);
        }
        if(UtilValidator.isNotEmpty(doctorId)){
            Provider provider = commonCrudService.getById(Provider.class, Long.valueOf(doctorId));
            message.setPersons(UtilMisc.toSet((Person) provider));
        }
        StringBuffer reader = new StringBuffer();
        reader.append("Name - " + patientFirstName + " " + lastName + "\n");
        reader.append("Mobile - " + mobileNumber + " Email - " + email + "\n \n");
        reader.append("Visit Type - " + visitType + ". \n \n \t \t       ");
        reader.append(messageText);
        message.setMessageText(reader.toString());
        commonCrudService.save(message);
    }

    @Transactional
    public boolean cancelAppointmentReqFromMobile(boolean isUrgent, Date sendOn, String patientId, String doctorId, String patientFirstName, String patientLastName, String mobileNumber, String email, String messageText, String visitType){
        Message message = new Message();
        RolesValueObject rolesVo = new RolesValueObject(message.getAuthorization());
        message.setTask(false);
        message.setUrgent(isUrgent);
        rolesVo.setReception(true);
        message.setSentOn(sendOn);
        if(UtilValidator.isNotEmpty(patientId)){
            Patient patient = commonCrudService.getById(Patient.class,Long.valueOf(patientId) );
            message.setSentBy(patient);
        }
        Provider provider = commonCrudService.getById(Provider.class, Long.valueOf(doctorId));
        message.setPersons(UtilMisc.toSet((Person) provider));
        StringBuffer reader = new StringBuffer();
        reader.append("Cancel appointment request for the given details :\n");
        reader.append("Name - " + patientFirstName + " " + patientLastName + "\n");
        reader.append("Mobile - " + mobileNumber + "\n");
        reader.append("Email - " + email + "\n");
        reader.append("Visit Type - " + visitType + "\n");
        reader.append("Doctor - " + provider.getProviderName() + "\n");
        if(UtilValidator.isNotEmpty(messageText))
            reader.append("Patient's message - "+messageText);
        message.setMessageText(reader.toString());
        commonCrudService.save(message);
        return true;
    }

    public CommonCrudService getCommonCrudService() {
        return commonCrudService;
    }

    @Override
    public List<Map<String, Object>> getTariffCategoryByPatientCategory(String patientCategory) {
        return patientRepository.getTariffCategoryByPatientCategory(patientCategory);
    }

    @Override
    public Map<String, Object> getTariffCategoryByTariffCode(String teriffCode) {
        return patientRepository.getTariffCategoryByTariffCode(teriffCode);
    }

    @Override
    public List<PatientDeposit> getPatientDepositsByCriteria(Patient patient,
                                                             Date fromDate, Date thruDate) {
        return patientRepository.getPatientDepositsByCriteria(patient, fromDate, thruDate);
    }

    @Override
    public List<PatientWithDraw> getPatientWithdrawByCriteria(Patient patient,Date fromDate, Date thruDate) {
        return patientRepository.getPatientWithdrawByCriteria(patient, fromDate, thruDate);
    }


}
