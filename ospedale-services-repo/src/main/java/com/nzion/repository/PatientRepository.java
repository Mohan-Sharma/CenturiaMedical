package com.nzion.repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.nzion.domain.Enumeration;
import com.nzion.domain.File;
import com.nzion.domain.Patient;
import com.nzion.domain.PatientDeposit;
import com.nzion.domain.PatientFamilyMember;
import com.nzion.domain.PatientOtherContactDetail;
import com.nzion.domain.PatientRemainder;
import com.nzion.domain.PatientWithDraw;
import com.nzion.domain.emr.EMRPatientInfo;
import com.nzion.domain.emr.FamilyMember;
import com.nzion.domain.emr.soap.PatientRx;
import com.nzion.domain.emr.soap.PatientSoapNote;
import com.nzion.domain.pms.PMSPatientInfo;
import com.nzion.domain.pms.Policy;

public interface PatientRepository extends BaseRepository {
	
	public void saveOrUpdate(Patient patient);

	public void saveOrUpdate(PMSPatientInfo patient);

	public void saveOrUpdate(EMRPatientInfo patient);

	public List<Patient> getAllPatients();

	public void deleteAllPatient();

	public void deletePatient(Patient patient);

	public Patient getPatientByAccountNumber(String accountNumber);

	public EMRPatientInfo getEMRPatientInfo(long patientId);

	public PMSPatientInfo getPMSPatientInfo(long patientId);

	public Patient getPatientById(Long patientId);
	
	public List<Policy> getPoliciesForPatientId(Long patientId);

	public void savePolicy(Policy policy);
	
	public <T> List<T> getEntityInfo(Class<T> klass, Map<String, String> params);
	
	List<Policy> getPrimaryPolicyAndValidPolicies(Long patientId);
	
	List<PatientFamilyMember> getFamilyMembers(Patient patient);
	
	List<Patient> searchPatientsBy(String accountNumber,String firstName,String lastName,Enumeration gender,Integer age);

    List<Patient> searchPatient(String accountNumber, String firstName, String lastName, Enumeration gender, Integer age,String mobileNumber, Integer startIndex, Integer noOfRecordsPerPage);

    Policy getPrimaryPolicy(Patient patient);
	
	List<PatientFamilyMember> getFamilyMembersFor(Patient patient,FamilyMember familyMember);
	
	List<PatientOtherContactDetail> getpatientOtherContactDetailFor(Patient patient);

	List<File> getFilesForDocumentType(Patient patient,String[] documentTypes);

	void storeActiveMedications(PatientSoapNote soapNote, List<PatientRx> allMedications);

	List<PatientRemainder> getPatientRemainders(Patient patient);
	
	List<Map<String, Object>> getAllInsurenceMaster();
	
	List<Map<String, Object>> getSelectedInsurancePlan(String insuranceCode);
	
	List<Map<String, Object>> getSelectedCorporatePlan(String corporateCode);

    Patient getPatientByAfyaId(String afyaId);
    
    List<Map<String, Object>> getTariffCategoryByPatientCategory(String patientCategory);
    
    Map<String, Object> getTariffCategoryByTariffCode(String teriffCode);
    
    List<PatientDeposit> getPatientDepositsByCriteria(Patient patient, Date fromDate, Date thruDate);
	
    List<PatientWithDraw> getPatientWithdrawByCriteria(Patient patient, Date fromDate, Date thruDate);
}
