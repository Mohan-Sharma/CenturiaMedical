package com.nzion.domain.emr.soap;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.nzion.domain.drug.*;
import org.apache.commons.beanutils.BeanUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.nzion.domain.Patient;
import com.nzion.domain.Provider;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.person.PersonDrug;
import com.nzion.util.UtilDateTime;

/**
 * @author Sandeep Prusty
 * Dec 1, 2010
 */
@Entity
@Table
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "com.nzion.domain")
public class PatientRx extends IdGeneratingBaseEntity {

	private String interactionOccurance;
	private boolean cheked;

	private String providerDirections;
	private String startCondition;
	private Date reviewDate;
	private String substitute = "N";
	private String reasonOfPrescribing;
	private Integer duration;
	private Integer totalSupply;
	private Drug drug;
	private DrugSig quantityQualifier;
	private DrugDosageForm drugDosageForm;
	private DrugDosageRoute drugDosageRoute;
	private DrugSig drugDirection; // frequency
	private DrugSig quantity;
	private String drugStrength;
	private Date startDate;
	private Date endDate;
	private PatientChiefComplaint patientChiefComplaint;
	private Set<PatientIcd> patientIcds;
	private String status = ACTIVE;
	private RxSection rxSection;
	private Provider provider;
	private Frequency frequency;
	private FrequencyQualifier frequencyQualifier;

	private String reasonForOverriding;

	private Set<PatientRxAlert> alerts;

	private Patient patient;

	private Site site;

	private Date discontinueDate;

	private String internalComment;

	private Date prescriptionDate;

	public static final String ACTIVE = "ACTIVE";

	public static final String DISCONTINUED = "DISCONTINUED";

	private MedicationHistorySection medicationHistorySection;

	private DrugSig supplyUom;

	private String drugQuantity;

	private String drugQualifier;

	private String drugFrequency;

	private String drugRoute;

	private String supplyUnit;

	private String drugName;

    private String numberOfDays;

    private String totalCount;
    
    private String totalCountTransient;

    private boolean homeDelivery = false;

    private boolean addToFavourite = false;
    
    private BigDecimal validDays = new BigDecimal("60");

    public boolean isHomeDelivery() {
        return homeDelivery;
    }

    public void setHomeDelivery(boolean homeDelivery) {
        this.homeDelivery = homeDelivery;
    }

    public PatientRx() {
	startDate = new Date();
	}

    public String getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(String totalCount) {
        this.totalCount = totalCount;
    }
    
    @Transient
    public String getTotalCountTransient() {
		return totalCountTransient;
	}

	public void setTotalCountTransient(String totalCountTransient) {
		this.totalCountTransient = totalCountTransient;
	}

	public String getNumberOfDays() {
        return numberOfDays;
    }

    public void setNumberOfDays(String numberOfDays) {
        this.numberOfDays = numberOfDays;
    }

    public Integer getTotalSupply() {
	return totalSupply;
	}

	public void setTotalSupply(Integer totalSupply) {
	this.totalSupply = totalSupply;
	}


	@OneToOne
	@JoinColumn(name = "SUPPLY_UOM")
	public DrugSig getSupplyUom() {
	return supplyUom;
	}

	public void setSupplyUom(DrugSig supplyUom) {
	if(supplyUom!=null)
	supplyUnit = supplyUom.getDescription();
	this.supplyUom = supplyUom;
	}

    @OneToOne
    @JoinColumn(name = "FREQUENCY_ID")
    public Frequency getFrequency() {
        return frequency;
    }

    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }

    @OneToOne
    @JoinColumn(name = "FREQUENCY_QUALIFIER_ID")
    public FrequencyQualifier getFrequencyQualifier() {
        return frequencyQualifier;
    }

    public void setFrequencyQualifier(FrequencyQualifier frequencyQualifier) {
        this.frequencyQualifier = frequencyQualifier;
    }

    @ManyToOne
	@JoinColumn(name = "MEDICATION_HISTORY_ID")
	public MedicationHistorySection getMedicationHistorySection() {
	return medicationHistorySection;
	}

	public void setMedicationHistorySection(MedicationHistorySection medicationHistorySection) {
	this.medicationHistorySection = medicationHistorySection;
	}

	public String getStatus() {
	return status;
	}

	public void setStatus(String status) {
	this.status = status;
	}

	@ManyToOne
	@JoinColumn(name = "PATIENT_CHIEF_COMPLAINT_ID")
	public PatientChiefComplaint getPatientChiefComplaint() {
	return patientChiefComplaint;
	}

	public void setPatientChiefComplaint(PatientChiefComplaint patientChiefComplaint) {
	this.patientChiefComplaint = patientChiefComplaint;
	}

	@ManyToMany(targetEntity = PatientIcd.class, fetch = FetchType.EAGER)
	@JoinTable(name = "PATIENT_RX_ICD", joinColumns = { @JoinColumn(name = "PATIENT_RX_ID", nullable = false) }, inverseJoinColumns = { @JoinColumn(name = "ICD_ID") })
	public Set<PatientIcd> getPatientIcds() {
	return patientIcds;
	}

	public void setPatientIcds(Set<PatientIcd> patientIcds) {
	this.patientIcds = patientIcds;
	}

	public Date getEndDate() {
	return endDate;
	}

	public void setEndDate(Date endDate) {
	this.endDate = endDate;
	}

	public Date getStartDate() {
	return startDate;
	}

	public void setStartDate(Date startDate) {
	this.startDate = startDate;
	}

	public String getProviderDirections() {
	return providerDirections;
	}

	public void setProviderDirections(String providerDirections) {
	this.providerDirections = providerDirections;
	}

	public String getStartCondition() {
	return startCondition;
	}

	public void setStartCondition(String startCondition) {
	this.startCondition = startCondition;
	}

	public Date getReviewDate() {
	return reviewDate;
	}

	public void setReviewDate(Date reviewDate) {
	this.reviewDate = reviewDate;
	}

	public String getReasonOfPrescribing() {
	return reasonOfPrescribing;
	}

	public void setReasonOfPrescribing(String reasonOfPrescribing) {
	this.reasonOfPrescribing = reasonOfPrescribing;
	}

	public void populateDrugAttributes(PersonDrug personDrug) {
	if (personDrug == null) return;
	this.drugDosageRoute = personDrug.getDrugDosageRoute() == null ? null : personDrug.getDrugDosageRoute();
	this.drugStrength = personDrug.getStrength() == null ? null : personDrug.getStrength().toString();
	this.drugDirection = personDrug.getDrugDirection() == null ? null : personDrug.getDrugDirection();
	}

	@Transient
	public String getDetails() {
	StringBuilder builder = new StringBuilder();
	builder.append(this.drug == null ? " " : this.drug.getTradeName() + "  ");
	builder.append(this.drugDosageForm == null ? " " : this.drugDosageForm.getCode() + "  ");
	builder.append(this.drugStrength == null ? " " : this.drugStrength + "  ");
	builder.append(this.drugDosageRoute == null ? " " : this.drugDosageRoute.getCode() + "  ");

	builder.append(this.quantity == null ? " " : this.quantity + "  ");
	builder.append(this.quantityQualifier == null ? " " : this.quantityQualifier.getCode() + "  ");
	builder.append(this.drugDirection == null ? " " : this.drugDirection + "  ");
	builder.append(this.providerDirections == null ? " " : this.providerDirections + "  ");
	return builder.toString();
	}

	private static final long serialVersionUID = 1L;

	@Transient
	public String getFormattedStartDate() {
	if (startDate != null) return UtilDateTime.format(startDate, UtilDateTime.HIPHEN_DATE_FORMATTER);
	return null;
	}

	@Transient
	public String getFormattedEndDate() {
	if (endDate != null) return UtilDateTime.format(endDate, UtilDateTime.HIPHEN_DATE_FORMATTER);
	return null;
	}

	public PatientRx createCopy() {

	PatientRx patientRx = new PatientRx();
	try {
		BeanUtils.copyProperties(patientRx, this);
		patientRx.setAlerts(null);
		patientRx.setPatientIcds(null);
		patientRx.setRxSection(null);
	} catch (Exception e) {
		throw new RuntimeException("coping data from previous soapnote failed");
	}
	patientRx.setId(null);
	return patientRx;
	}

	@OneToOne
	@JoinColumn(name = "DRUG_ID")
	public Drug getDrug() {
	return drug;
	}

	public void setDrug(Drug drug) {
	this.drug = drug;
	}

	@OneToOne
	@JoinColumn(name = "DRUG_QQ")
	public DrugSig getQuantityQualifier() {
	return quantityQualifier;
	}

	public void setQuantityQualifier(DrugSig quantityQualifier) {
	if(quantityQualifier!=null)
	drugQualifier = quantityQualifier.getDescription();
	this.quantityQualifier = quantityQualifier;
	}

	@OneToOne
	@JoinColumn(name = "DRUG_DDF")
	public DrugDosageForm getDrugDosageForm() {
	return drugDosageForm;
	}

	public void setDrugDosageForm(DrugDosageForm drugDosageForm) {
	this.drugDosageForm = drugDosageForm;
	}

	@OneToOne
	@JoinColumn(name = "DRUG_DDR")
	public DrugDosageRoute getDrugDosageRoute() {
	return drugDosageRoute;
	}

	public void setDrugDosageRoute(DrugDosageRoute drugDosageRoute) {
	if(drugDosageRoute!=null)
	drugRoute = drugDosageRoute.getDescription();
	this.drugDosageRoute = drugDosageRoute;
	}

	@OneToOne
	@JoinColumn(name = "DRUG_DIRECTION")
	public DrugSig getDrugDirection() {
	return drugDirection;
	}

	public void setDrugDirection(DrugSig drugDirection) {
	if(drugDirection!=null)
	drugFrequency = drugDirection.getDescription();
	this.drugDirection = drugDirection;
	}

	@OneToOne
	@JoinColumn(name = "DRUG_QUANTITY")
	public DrugSig getQuantity() {
	return quantity;
	}

	public void setQuantity(DrugSig quantity) {
	if(quantity!=null)
	drugQuantity = quantity.getDescription();
	this.quantity = quantity;
	}

	public String getDrugStrength() {
	return drugStrength;
	}

	public void setDrugStrength(String drugStrength) {
	this.drugStrength = drugStrength;
	}

	public Integer getDuration() {
	if (duration == null && (startDate != null && endDate != null)) {
		duration = UtilDateTime.getIntervalInDays(startDate, endDate);
	}
	return duration;
	}

	public void setDuration(Integer duration) {
	this.duration = duration;
	if (startDate != null && duration != null) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(startDate);
		cal.add(Calendar.DAY_OF_YEAR, duration);
		endDate = cal.getTime();
		if (reviewDate == null) reviewDate = cal.getTime();
	}
	}

	public String getSubstitute() {
	return substitute;
	}

	public void setSubstitute(String substitute) {
	this.substitute = substitute;
	}

	@OneToOne
	@JoinColumn(name = "PATIENT_ID", nullable = false)
	public Patient getPatient() {
	return patient;
	}

	public void setPatient(Patient patient) {
	this.patient = patient;
	}

	@OneToOne
	@JoinColumn(name = "PROVIDER_ID")
	public Provider getProvider() {
	return provider;
	}

	public void setProvider(Provider provider) {
	this.provider = provider;
	}

	public String getReasonForOverriding() {
	return reasonForOverriding;
	}

	public void setReasonForOverriding(String reasonForOverriding) {
	this.reasonForOverriding = reasonForOverriding;
	}

	@OneToMany(targetEntity = PatientRxAlert.class, fetch = FetchType.EAGER)
	@JoinColumn(name = "PATIENT_RX_ID")
	public Set<PatientRxAlert> getAlerts() {
	return alerts;
	}

	public void setAlerts(Set<PatientRxAlert> alerts) {
	this.alerts = alerts;
	}

	public void addAlert(PatientRxAlert alert) {
	if (getAlerts() == null) {
		setAlerts(new HashSet<PatientRxAlert>());
	}
	alerts.add(alert);
	}

	@OneToOne
	@JoinColumn(name = "DRUG_SITE_ID")
	public Site getSite() {
	return site;
	}

	public void setSite(Site site) {
	this.site = site;
	}

	public Date getDiscontinueDate() {
	return discontinueDate;
	}

	public void setDiscontinueDate(Date discontinueDate) {
	this.discontinueDate = discontinueDate;
	}

	public String getInternalComment() {
	return internalComment;
	}

	public void setInternalComment(String internalComment) {
	this.internalComment = internalComment;
	}

	public Date getPrescriptionDate() {
	return prescriptionDate;
	}

	public void setPrescriptionDate(Date prescriptionDate) {
		if(prescriptionDate == null)
			prescriptionDate = new Date();
		
		this.prescriptionDate = prescriptionDate;
	}

	@Transient
	public String getIcdCode() {
	StringBuilder builder = new StringBuilder();
	for (PatientIcd patientIcd : getPatientIcds()) {
		builder.append(patientIcd.getIcdElement().getCode());
		builder.append(',');
	}
	if (builder.length() > 0 && builder.charAt(builder.length() - 1) == ',')
		builder.deleteCharAt(builder.length() - 1);
	return builder.toString();
	}

	@ManyToOne
	@JoinColumn(name = "RX_SECTION_ID")
	public RxSection getRxSection() {
	return rxSection;
	}

	public void setRxSection(RxSection rxSection) {
	this.rxSection = rxSection;
	}

	@Column(name = "QUANTITY")
	public String getDrugQuantity() {
	return drugQuantity;
	}

	public void setDrugQuantity(String drugQuantity) {
	this.drugQuantity = drugQuantity;
	}

	@Column(name = "QUALIFIER")
	public String getDrugQualifier() {
	return drugQualifier;
	}

	public void setDrugQualifier(String drugQualifier) {
	this.drugQualifier = drugQualifier;
	}

	public String getDrugFrequency() {
	return drugFrequency;
	}

	public void setDrugFrequency(String drugFrequency) {
	this.drugFrequency = drugFrequency;
	}

	@Column(name = "ROUTE")
	public String getDrugRoute() {
	return drugRoute;
	}

	public void setDrugRoute(String drugRoute) {
	this.drugRoute = drugRoute;
	}

	@Column(name = "SUPPLY_UNIT")
	public String getSupplyUnit() {
	return supplyUnit;
	}

	public void setSupplyUnit(String supplyUnit) {
	this.supplyUnit = supplyUnit;
	}

	@Column(name = "DRUG_NAME")
	public String getDrugName() {
	return drugName;
	}

	public void setDrugName(String drugName) {
	this.drugName = drugName;
	}

    public boolean isAddToFavourite() {
        return addToFavourite;
    }

    public void setAddToFavourite(boolean addToFavourite) {
        this.addToFavourite = addToFavourite;
    }

	public BigDecimal getValidDays() {
		return validDays;
	}

	public void setValidDays(BigDecimal validDays) {
		this.validDays = validDays;
	}
    
    
}