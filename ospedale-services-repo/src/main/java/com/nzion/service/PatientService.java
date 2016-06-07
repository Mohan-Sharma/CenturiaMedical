package com.nzion.service;

import com.nzion.domain.*;
import com.nzion.domain.emr.EMRPatientInfo;
import com.nzion.domain.emr.FamilyMember;
import com.nzion.domain.emr.soap.PatientRx;
import com.nzion.domain.emr.soap.PatientSoapNote;
import com.nzion.domain.pms.PMSPatientInfo;
import com.nzion.exception.TransactionException;
import com.nzion.repository.PatientRepository;
import com.nzion.view.PatientViewObject;

import java.util.Date;
import java.util.List;
import java.util.Map;
public interface PatientService {

	public void saveOrUpdate(PatientViewObject patient);

    public Long savePatient(Patient patient);

	public void saveOrUpdate(Patient patient,String eventMessage);
	@Deprecated
	/**
	 * Use commonCrudService.getByAccountNumber instead
	 * **/
	public Patient getPatient(String accountNumber);

	public Patient getPatientById(Long patientId);

	public void setPatientRepository(PatientRepository repository);

	public List<Patient> getAllPatients();

	void deletePatient(Patient patient);

	void deleteAllPatient();

	public void saveOrUpdate(PMSPatientInfo patient);

	public void saveOrUpdate(EMRPatientInfo patient);

	public EMRPatientInfo getEmrPatient(Long patientId);

	public PMSPatientInfo getPmsPatient(Long patientId);

	List<PatientFamilyMember> getFamilyMembers(Patient patient);

	public void createPatient(PatientViewObject patientVO)throws TransactionException;
	
	List<Patient> searchPatientsBy(String accountNumber,String firstName,String lastName,Enumeration gender,Integer age);

    List<Patient> searchPatient(String accountNumber, String firstName, String lastName, Long genderId, Integer age,String mobileNumber, Integer lowerLimit, Integer upperLimit);
	
	List<PatientFamilyMember> getFamilyMembersFor(Patient patient,FamilyMember familyMember);

    List<PatientRx> getActivePatientRxs(Patient patient);
	
	PatientOtherContactDetail getPatientOtherContactDetailFor(Patient patient);
	
	List<File> getFilesForDocumentType(Patient patient, String documentType);

	void storeActiveMedications(PatientSoapNote soapNote, List<PatientRx> allMedications);
	
	List<PatientRemainder> getPatientRemainders(Patient patient);
	
	void createUserLoginForPatient(Patient patient);
	
	List<Map<String, Object>> getAllInsurenceMaster(); 
	
	List<Map<String, Object>> getSelectedInsurancePlan(String insuranceCode);
	
	List<Map<String, Object>> getSelectedCorporatePlan(String corporateCode);

    void createAppointmentReqFromMobile(boolean isUrgent, Date sendOn, String patientId, String doctorId, String patientFirstName,
                                        String lastName, String mobileNumber, String email, String messageText, String visitType);

    boolean cancelAppointmentReqFromMobile(boolean isUrgent, Date sendOn, String patientId, String doctorId, String patientFirstName,
                                        String lastName, String mobileNumber, String email, String messageText, String visitType);
    
    List<Map<String, Object>> getTariffCategoryByPatientCategory(String patientCategory);
    
    Map<String, Object> getTariffCategoryByTariffCode(String teriffCode);
    
    List<PatientDeposit> getPatientDepositsByCriteria(Patient patient, Date fromDate, Date thruDate);
    
    List<PatientWithDraw> getPatientWithdrawByCriteria(Patient patient, Date fromDate, Date thruDate);
	
}
