package com.nzion.domain.emr.soap;

import com.nzion.domain.*;
import com.nzion.domain.Schedule.STATUS;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.SoapModule;
import com.nzion.util.UtilDateTime;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Sandeep Prusty Dec 1, 2010
 */

@Entity
@Table
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "com.nzion.domain")
public class PatientSoapNote extends IdGeneratingBaseEntity {
	
	private Set<SoapNoteActor> actors;

	private SoapNoteType encounterType;

	private Schedule schedule;

	private Date date;

	private Provider provider;

	private Patient patient;

	private String newCorpNurseXml;

	private Boolean ccdExported = Boolean.FALSE;

	private Boolean ccdImported = Boolean.FALSE;

	private Boolean vaccineExported = Boolean.FALSE;

	private Boolean cdssAlerted = Boolean.FALSE;

	private Date exportedOn;

	private Date importedOn;

	private Date printedByPatientOn;

	private Date printedByProviderOn;

	private Boolean printedByPatient = Boolean.FALSE;

	private Boolean printedByProvider = Boolean.FALSE;

	private Date vaccineExportedOn;

	private Date cdssAlertedOn;

	private Boolean soapNoteReleased = Boolean.FALSE;

	private Boolean releasedWithIn4Days = Boolean.FALSE;

	private boolean surveillanceEligility;

	private Boolean surveillanceExported = Boolean.FALSE;

	private Date surveillanceExportedOn;

	private Speciality speciality;

    private String selectedHisModuleId;

    private PatientInsurance patientInsurance;

	/** Medical Legal Case **/
	private Boolean isMlc = Boolean.FALSE;

	@Column(name = "DATE", updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getDate() {
	return date;
	}

	public void setDate(Date date) {
	this.date = date;
	}

	@OneToOne(targetEntity = Schedule.class)
	@JoinColumn(name = "SCHEDULE_ID", updatable = false)
	@Cascade(CascadeType.SAVE_UPDATE)
	public Schedule getSchedule() {
	return schedule;
	}

	public void setSchedule(Schedule schedule) {
	this.schedule = schedule;
	this.provider = (Provider) schedule.getPerson();
	this.patient = schedule.getPatient();
	}

	@OneToMany(targetEntity = SoapNoteActor.class, fetch = FetchType.EAGER)
	@JoinColumn(name = "SOAPNOTE_ID")
	@Cascade(CascadeType.ALL)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "com.nzion.domain")
	public Set<SoapNoteActor> getActors() {
	return actors;
	}

	public void setActors(Set<SoapNoteActor> actors) {
	this.actors = actors;
	}

	@OneToOne(targetEntity = SoapNoteType.class)
	@JoinColumn(name = "SOAP_NOTE_TYPE_ID")
	public SoapNoteType getEncounterType() {
	return encounterType;
	}

	public void setEncounterType(SoapNoteType encounterType) {
	this.encounterType = encounterType;
	}

	public void addActor(SoapNoteActor actor) {
	if (actors == null) actors = new HashSet<SoapNoteActor>();
	actors.add(actor);
	}

	public void addActor(Person person, long actingRole) {
	addActor(new SoapNoteActor(person, actingRole));
	}

	@OneToOne
	@JoinColumn(name = "PROVIDER_ID", updatable = false)
	public Provider getProvider() {
	return provider;
	}

	public void setProvider(Provider provider) {
	this.provider = provider;
	}

	@OneToOne
	@JoinColumn(name = "PATIENT_ID", updatable = false)
	public Patient getPatient() {
	return patient;
	}

	public void setPatient(Patient patient) {
	this.patient = patient;
	}

	@Transient
	public String getEncounterDate() {
	return UtilDateTime.format(this.date, UtilDateTime.DEFAULT_DATE_FORMATTER);
	}

	public ChiefComplainSection createChiefComplaintSection(SoapModule hpiModule) {
	ChiefComplainSection chiefComplainSection = new ChiefComplainSection();
	chiefComplainSection.initialize(this, null, null, null);
	chiefComplainSection.setSoapModule(hpiModule);
	return chiefComplainSection;
	}

	@Transient
	public STATUS getStatus() {
	return schedule.getStatus();
	}

	@Column(length = 2048)
	public String getNewCorpNurseXml() {
	return newCorpNurseXml;
	}

	public void setNewCorpNurseXml(String newCorpNurseXml) {
	this.newCorpNurseXml = newCorpNurseXml;
	}

	public Boolean getCcdExported() {
	return ccdExported;
	}

	public void setCcdExported(Boolean ccdExported) {
	this.ccdExported = ccdExported;
	}

	public Boolean getCcdImported() {
	return ccdImported;
	}

	public void setCcdImported(Boolean ccdImported) {
	this.ccdImported = ccdImported;
	}

	public Date getExportedOn() {
	return exportedOn;
	}

	public void setExportedOn(Date exportedOn) {
	this.exportedOn = exportedOn;
	}

	public Date getImportedOn() {
	return importedOn;
	}

	public void setImportedOn(Date importedOn) {
	this.importedOn = importedOn;
	}

	public Boolean getVaccineExported() {
	return vaccineExported;
	}

	public void setVaccineExported(Boolean vaccineExported) {
	this.vaccineExported = vaccineExported;
	}

	public Date getVaccineExportedOn() {
	return vaccineExportedOn;
	}

	public void setVaccineExportedOn(Date vaccineExportedOn) {
	this.vaccineExportedOn = vaccineExportedOn;
	}

	public Boolean getCdssAlerted() {
	return cdssAlerted;
	}

	public void setCdssAlerted(Boolean cdssAlerted) {
	this.cdssAlerted = cdssAlerted;
	}

	@Temporal(TemporalType.DATE)
	public Date getCdssAlertedOn() {
	return cdssAlertedOn;
	}

	public void setCdssAlertedOn(Date cdssAlertedOn) {
	this.cdssAlertedOn = cdssAlertedOn;
	}

	@Temporal(TemporalType.DATE)
	public Date getPrintedByPatientOn() {
	return printedByPatientOn;
	}

	public void setPrintedByPatientOn(Date printedByPatientOn) {
	this.printedByPatientOn = printedByPatientOn;
	}

	@Temporal(TemporalType.DATE)
	public Date getPrintedByProviderOn() {
	return printedByProviderOn;
	}

	public void setPrintedByProviderOn(Date printedByProviderOn) {
	this.printedByProviderOn = printedByProviderOn;
	}

	public Boolean getPrintedByPatient() {
	return printedByPatient;
	}

	public void setPrintedByPatient(Boolean printedByPatient) {
	this.printedByPatient = printedByPatient;
	}

	public Boolean getPrintedByProvider() {
	return printedByProvider;
	}

	public void setPrintedByProvider(Boolean printedByProvider) {
	this.printedByProvider = printedByProvider;
	}

	public Boolean getReleasedWithIn4Days() {
	return releasedWithIn4Days;
	}

	public void setReleasedWithIn4Days(Boolean releasedWithIn4Days) {
	this.releasedWithIn4Days = releasedWithIn4Days;
	}

	public Boolean getSoapNoteReleased() {
	return soapNoteReleased;
	}

	public void setSoapNoteReleased(Boolean soapNoteReleased) {
	this.soapNoteReleased = soapNoteReleased;
	}

	public boolean isSurveillanceEligility() {
	return surveillanceEligility;
	}

	public void setSurveillanceEligility(boolean surveillanceEligility) {
	this.surveillanceEligility = surveillanceEligility;
	}

	public Boolean getSurveillanceExported() {
	return surveillanceExported;
	}

	public void setSurveillanceExported(Boolean surveillanceExported) {
	this.surveillanceExported = surveillanceExported;
	}

	public Date getSurveillanceExportedOn() {
	return surveillanceExportedOn;
	}

	public void setSurveillanceExportedOn(Date surveillanceExportedOn) {
	this.surveillanceExportedOn = surveillanceExportedOn;
	}

	@OneToOne
	@JoinColumn(name = "SPECIALITY_ID")
	public Speciality getSpeciality() {
	return speciality;
	}

	public void setSpeciality(Speciality speciality) {
	this.speciality = speciality;
	}

    @OneToOne(fetch = FetchType.EAGER)
    public PatientInsurance getPatientInsurance() {
        return patientInsurance;
    }

    public void setPatientInsurance(PatientInsurance patientInsurance) {
        this.patientInsurance = patientInsurance;
    }

    public Boolean getIsMlc() {
		return isMlc;
	}

	public void setIsMlc(Boolean isMlc) {
		this.isMlc = isMlc;
	}

    public String getSelectedHisModuleId() {
        return selectedHisModuleId;
    }

    public void setSelectedHisModuleId(String selectedHisModuleId) {
        this.selectedHisModuleId = selectedHisModuleId;
    }

    private static final long serialVersionUID = 1L;

	public static final String BILLING_GENERATOR_NAME = "";
}