package com.nzion.domain.emr.soap;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.beanutils.BeanUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.nzion.domain.Enumeration;
import com.nzion.domain.Patient;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.IcdElement;
import com.nzion.util.UtilValidator;

@Entity
@Table
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "com.nzion.domain")
public class PatientIcd extends IdGeneratingBaseEntity {

	private IcdElement icdElement;
	private Enumeration status;
	private Enumeration severity;
	private Set<PatientCpt> cpts;
	private DiagnosisSection diagnosisSection;
	private PreliminaryDiagnosisSection preliminaryDiagnosisSection;
	private DifferentialDiagnosisSection differentialDiagnosisSection;
	private ProblemListSection problemListSection;
	private Patient patient;
	private Date onSetDate;
	private String description;
	private PatientSoapNote soapNote;
	private Date resolutionDate;
	private String certainity;
	
	private BILLINGSTATUS billingStatus = BILLINGSTATUS.NONINVOICED;

	@Temporal(TemporalType.DATE)
	public Date getResolutionDate() {
	return resolutionDate;
	}

	public void setResolutionDate(Date resolutionDate) {
	this.resolutionDate = resolutionDate;
	}

	@OneToOne(fetch = FetchType.LAZY)
	public PatientSoapNote getSoapNote() {
	return soapNote;
	}

	public void setSoapNote(PatientSoapNote soapNote) {
	this.soapNote = soapNote;
	}

	public String getDescription() {
	return description;
	}

	public void setDescription(String description) {
	this.description = description;
	}

	private Date resolvedOn;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PROBLEM_SECTION_ID")
	public ProblemListSection getProblemListSection() {
	return problemListSection;
	}

	public void setProblemListSection(ProblemListSection problemListSection) {
	this.problemListSection = problemListSection;
	}

	@Temporal(TemporalType.DATE)
	public Date getOnSetDate() {
	return onSetDate;
	}

	public void setOnSetDate(Date onSetDate) {
	this.onSetDate = onSetDate;
	}

	public Date getResolvedOn() {
	return resolvedOn;
	}

	public void setResolvedOn(Date resolvedOn) {
	this.resolvedOn = resolvedOn;
	}

	public PatientIcd() {
	}

	public PatientIcd(IcdElement icdElement) {
	this.icdElement = icdElement;
	}

	@ManyToOne
	@JoinColumn(name = "ICD_ID")
	public IcdElement getIcdElement() {
	return icdElement;
	}

	public void setIcdElement(IcdElement icdElement) {
	this.icdElement = icdElement;
	}

	@Override
	public String toString() {
	return icdElement == null ? description : icdElement.toString();
	}

	private static final long serialVersionUID = 1L;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "PATIENT_ICD_CPT")
	@Fetch(FetchMode.SELECT)
	public Set<PatientCpt> getCpts() {
	return cpts;
	}

	public void setCpts(Set<PatientCpt> cpts) {
	this.cpts = cpts;
	}

	@ManyToOne
	@JoinColumn(name = "DIAGNOSIS_SECTION_ID")
	public DiagnosisSection getDiagnosisSection() {
	return diagnosisSection;
	}

	public void setDiagnosisSection(DiagnosisSection diagnosisSection) {
	this.diagnosisSection = diagnosisSection;
	}
	
	@ManyToOne
	public PreliminaryDiagnosisSection getPreliminaryDiagnosisSection() {
		return preliminaryDiagnosisSection;
	}

	public void setPreliminaryDiagnosisSection(
			PreliminaryDiagnosisSection preliminaryDiagnosisSection) {
		this.preliminaryDiagnosisSection = preliminaryDiagnosisSection;
	}
	
	@ManyToOne
	public DifferentialDiagnosisSection getDifferentialDiagnosisSection() {
		return differentialDiagnosisSection;
	}

	public void setDifferentialDiagnosisSection(
			DifferentialDiagnosisSection differentialDiagnosisSection) {
		this.differentialDiagnosisSection = differentialDiagnosisSection;
	}

	@OneToOne
	public Patient getPatient() {
	return patient;
	}

	public void setPatient(Patient patient) {
	this.patient = patient;
	}

	public PatientIcd createCopy() {
	PatientIcd icd = new PatientIcd();
	try {
		BeanUtils.copyProperties(icd, this);
	} catch (IllegalAccessException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (InvocationTargetException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	icd.setDiagnosisSection(null);
	icd.setId(null);
	icd.setCpts(null);
	return icd;
	}

	@Transient
	public String getCptDescription() {
	if (UtilValidator.isEmpty(cpts)) return "";
	StringBuilder builder = new StringBuilder();
	builder.append('(');
	for (PatientCpt patientCpt : getCpts()) {
		builder.append(patientCpt.getCpt().getId());
		builder.append(',');
	}
	if (builder.length() > 0 && builder.charAt(builder.length() - 1) == ',')
		builder.deleteCharAt(builder.length() - 1);
	builder.append(')');
	return builder.toString();
	}

	@OneToOne
	public Enumeration getStatus() {
	return status;
	}

	public void setStatus(Enumeration status) {
	this.status = status;
	}

	@OneToOne
	public Enumeration getSeverity() {
	return severity;
	}

	public void setSeverity(Enumeration severity) {
	this.severity = severity;
	}

	public String getCertainity() {
	return certainity;
	}

	public void setCertainity(String certainity) {
	this.certainity = certainity;
	}
	
	public static enum BILLINGSTATUS{
		NONINVOICED, INVOICED
	};

	public BILLINGSTATUS getBillingStatus() {
		return billingStatus;
	}

	public void setBillingStatus(BILLINGSTATUS billingStatus) {
		this.billingStatus = billingStatus;
	}
	
}