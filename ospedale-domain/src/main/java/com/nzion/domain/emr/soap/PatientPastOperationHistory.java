package com.nzion.domain.emr.soap;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.nzion.domain.Patient;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.Cpt;

@Entity
@Table
@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="com.nzion.domain")
public class PatientPastOperationHistory extends IdGeneratingBaseEntity {

	private static final long serialVersionUID = 1L;
	private String operationName;
	private Date occuranceDate;
	private String locationName;
	private String operatedBy;
	private Boolean currentVisit;
	private Set<Cpt> cpts;
	private String site;
	private Patient patient;
	private PastHistorySection soapSection;
	
	@OneToOne
	@JoinColumn(name="PATIENT_ID",nullable=false)
	public Patient getPatient() {
	return patient;
	}

	public void setPatient(Patient patient) {
	this.patient = patient;
	}
	
	@ManyToOne
	@JoinColumn(name="PAST_HX_SECTION_ID",nullable=false)
	public PastHistorySection getSoapSection() {
	return soapSection;
	}

	public void setSoapSection(PastHistorySection section) {
	this.soapSection = section;
	}

	public String getOperationName() {
	return operationName;
	}

	public void setOperationName(String operationName) {
	this.operationName = operationName;
	}

	public Date getOccuranceDate() {
	return occuranceDate;
	}

	public void setOccuranceDate(Date occuranceDate) {
	this.occuranceDate = occuranceDate;
	}

	public String getLocationName() {
	return locationName;
	}

	public void setLocationName(String locationName) {
	this.locationName = locationName;
	}

	public String getOperatedBy() {
	return operatedBy;
	}

	public void setOperatedBy(String operatedBy) {
	this.operatedBy = operatedBy;
	}

	public Boolean getCurrentVisit() {
	return currentVisit;
	}

	public void setCurrentVisit(Boolean currentVisit) {
	this.currentVisit = currentVisit;
	}

	@ManyToMany(targetEntity = Cpt.class,fetch=FetchType.EAGER)
	@JoinTable(name = "PATIENT_SURGERY_CPT", joinColumns = { @JoinColumn(name = "SURGERY_ID", nullable = false) }, inverseJoinColumns = { @JoinColumn(name = "CPT_ID") })
	public Set<Cpt> getCpts() {
	if(cpts == null)
		cpts = new HashSet<Cpt>();
	return cpts;
	}

	public void setCpts(Set<Cpt> cpts) {
	this.cpts = cpts;
	}

	@Transient
	public String getCptCodes() {
	StringBuilder builder = new StringBuilder();
	for (Cpt cpt : getCpts()) {
		builder.append(cpt.getId());
		builder.append(',');
	}
	if (builder.length() > 0 && builder.charAt(builder.length() - 1) == ',')
		builder.deleteCharAt(builder.length() - 1);
	return builder.toString();
	}
	
	@Transient
	public String getCptDescription(){
	StringBuilder builder = new StringBuilder();
	for (Cpt cpt : getCpts()) {
		builder.append(cpt.getDescription());
		builder.append(',');
	}
	if (builder.length() > 0 && builder.charAt(builder.length() - 1) == ',')
		builder.deleteCharAt(builder.length() - 1);
	return builder.toString();
	}
	
	public String getSite() {
	return site;
	}

	public void setSite(String site) {
	this.site = site;
	}
	
	public void addCpts(Collection<Cpt> cpts){
	for(Cpt cpt : cpts)
		this.cpts.add(cpt);
	}

	
}
