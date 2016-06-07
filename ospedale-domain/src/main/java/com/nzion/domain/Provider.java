/*
 * header file
 */
package com.nzion.domain;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.*;

import com.nzion.domain.emr.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.nzion.domain.annot.AccountNumberField;
import com.nzion.util.UtilValidator;

// TODO: Auto-generated Javadoc
/**
 * The Class Provider.
 */
@Entity
@Table(name = "PROVIDER")
@AccountNumberField
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "com.nzion.domain")
public class Provider extends Employee implements Comparable<Provider>{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4251635378986229662L;

	private ProviderDetail detail;

	private DataResource signatureImage;

	private Map<Long, String> preferenceValues;

	private boolean providerAssistant;

	private Provider reportingProvider;

	private Set<Speciality> specialities;

	private Set<SpokenLanguage> spokenLanguages;

	private Set<Certification> certification;

	private Set<SoapModuleQATemplate> soapModuleTemplates;

	private String comments;

	private String qualifications;
	
	private String regdNo;
	
	private Integer freeVisitDays;
	
	private Integer freeVisits;
	
	private BigDecimal freeVisitCharges;
	
	
	private Integer followUpVisitDays;
	
	private Integer followUpVisits;
	
	private BigDecimal followUpVisitCharges;
	
	private Integer revisitDays;
	
	private Integer revisitVisits;
	
	private BigDecimal revisitCharges;
	
	private Boolean toPrintSignature = true;
	
	public String getRegdNo() {
		return regdNo;
	}

	public void setRegdNo(String regdNo) {
		this.regdNo = regdNo;
	}

	private ReferalLetterTemplate referalLetterTemplate;
	
	private SoapNoteType soapNoteType;
	
	private FixedAsset room;

    private String visitingHours;

    private String specialityToDisplayInPortal;

	private Nationality nationality;

	private String experience;

	private String priceRange;

	private String keyword;

	private String qualificationEng;

	private String qualificationAr;

	private String certificationEng;

	private String certificationAr;

	public String getCertificationAr() {
		return certificationAr;
	}

	public void setCertificationAr(String certificationAr) {
		this.certificationAr = certificationAr;
	}

	private String disease;

	private String diseaseAr;

	private String expertise;

	private String expertiseAr;

	public String getExpertiseAr() {
		return expertiseAr;
	}

	public void setExpertiseAr(String expertiseAr) {
		this.expertiseAr = expertiseAr;
	}

	public String getExpertise() {
		return expertise;
	}

	public void setExpertise(String expertise) {
		this.expertise = expertise;
	}

	public String getDisease() {
		return disease;
	}

	public void setDisease(String disease) {
		this.disease = disease;
	}

	public String getDiseaseAr() {
		return diseaseAr;
	}

	public void setDiseaseAr(String diseaseAr) {
		this.diseaseAr = diseaseAr;
	}

	public String getQualificationEng() {
		return qualificationEng;
	}

	public void setQualificationEng(String qualificationEng) {
		this.qualificationEng = qualificationEng;
	}

	public String getQualificationAr() { return qualificationAr;}

	public void setQualificationAr(String qualificationAr) {
		this.qualificationAr = qualificationAr;
	}

	public String getCertificationEng() {
		return certificationEng;
	}

	public void setCertificationEng(String certificationEng) {
		this.certificationEng = certificationEng;
	}

	public String getQualifications() {
	return qualifications;
	}

	public void setQualifications(String qualifications) {
	this.qualifications = qualifications;
	}

	public Provider() {
	super(PartyType.PROVIDER);
	setSchedulable(true);
	}

	public Provider(Long id, String firstName, String lastName, ContactFields cf) {
	super(id, firstName, lastName, cf);
	setPartyType(PartyType.PROVIDER);
	setSchedulable(true);
	}

	static {
		Party.setPartyMap(Provider.class, PartyType.PROVIDER);
	}

	public Provider(PartyType providerType) {
	super(providerType);
	}

	@ElementCollection
	@MapKeyColumn(name = "PREF_DEF_ID")
	@Column(name = "VALUE")
	@CollectionTable(name = "PROVIDER_PREFERENCES", joinColumns = { @JoinColumn(name = "PROVIDER_ID", nullable = false) })
	@Cascade(CascadeType.ALL)
	public Map<Long, String> getPreferenceValues() {
	return preferenceValues;
	}

	public void setPreferenceValues(Map<Long, String> preferenceValues) {
	this.preferenceValues = preferenceValues;
	}

	@OneToOne(fetch = FetchType.LAZY, targetEntity = DataResource.class)
	@Cascade(value = { CascadeType.ALL })
	@JoinColumn(name = "SIGNATURE_IMAGE")
	public DataResource getSignatureImage() {
	return signatureImage;
	}

	public void setSignatureImage(DataResource signatureImage) {
	this.signatureImage = signatureImage;
	}

	public boolean isProviderAssistant() {
	return providerAssistant;
	}

	public void setProviderAssistant(boolean providerAssistant) {
	this.providerAssistant = providerAssistant;
	setSchedulable(!providerAssistant);
	}

	@OneToOne
	@JoinColumn(name = "REPORTING_PROVIDER_ID")
	public Provider getReportingProvider() {
	return reportingProvider;
	}

	public void setReportingProvider(Provider reportingProvider) {
	this.reportingProvider = reportingProvider;
	}

	public void addSpeciality(Speciality speciality) {
	if (UtilValidator.isEmpty(specialities)) 
		specialities = new HashSet<Speciality>();
	specialities.add(speciality);
	}

	@ManyToMany(targetEntity = Speciality.class,fetch = FetchType.EAGER)
	@JoinTable(name = "PROVIDER_SPECIALITY", joinColumns = { @JoinColumn(name = "PROVIDER_ID") }, inverseJoinColumns = { @JoinColumn(name = "SPECIALITY_ID") })
	@Fetch(FetchMode.SELECT)
	public Set<Speciality> getSpecialities() {
	return specialities;
	}

	public void setSpecialities(Set<Speciality> specialities) {
	this.specialities = specialities;
	}

	@ManyToMany(targetEntity = SpokenLanguage.class,fetch = FetchType.EAGER)
	@JoinTable(name = "PROVIDER_SPOKEN_LANGUAGE", joinColumns = { @JoinColumn(name = "PROVIDER_ID") }, inverseJoinColumns = { @JoinColumn(name = "SPOKEN_LANGUAGE_ID") })
	@Fetch(FetchMode.SELECT)
	public Set<SpokenLanguage> getSpokenLanguages() {
		return spokenLanguages;
	}

	public void setSpokenLanguages(Set<SpokenLanguage> spokenLanguages) {
		this.spokenLanguages = spokenLanguages;
	}

	@ManyToMany(targetEntity = Certification.class,fetch = FetchType.EAGER)
	@JoinTable(name = "PROVIDER_CERTIFICATION", joinColumns = { @JoinColumn(name = "PROVIDER_ID") }, inverseJoinColumns = { @JoinColumn(name = "CERTIFICATION_ID") })
	@Fetch(FetchMode.SELECT)
	public Set<Certification> getCertification() {
		return certification;
	}

	public void setCertification(Set<Certification> certification) {
		this.certification = certification;
	}

	@OneToOne(fetch = FetchType.EAGER, targetEntity = ReferalLetterTemplate.class)
	@Cascade(value = { CascadeType.ALL })
	@JoinColumn(name = "REFERRAL_LETTER_ID")
	public ReferalLetterTemplate getReferalLetterTemplate() {
	return referalLetterTemplate;
	}
	
	

	public void setReferalLetterTemplate(ReferalLetterTemplate referalLetterTemplate) {
	this.referalLetterTemplate = referalLetterTemplate;
	}

	@Embedded
	public ProviderDetail getDetail() {
	if (detail == null) detail = new ProviderDetail();
	return detail;
	}

	public void setDetail(ProviderDetail detail) {
	this.detail = detail;
	}

	@Override
	@Column(length = 1000)
	public String getComments() {
	return comments;
	}

	@Override
	public void setComments(String comments) {
	this.comments = comments;
	}

	@OneToMany(cascade = javax.persistence.CascadeType.ALL,orphanRemoval = true)
	public Set<SoapModuleQATemplate> getSoapModuleTemplates() {
	if (soapModuleTemplates == null) soapModuleTemplates = new HashSet<SoapModuleQATemplate>();
	return soapModuleTemplates;
	}

	public void setSoapModuleTemplates(Set<SoapModuleQATemplate> soapModuleTemplates) {
	this.soapModuleTemplates = soapModuleTemplates;
	}

	public void addSoapModuleTemplates(SoapModuleQATemplate soapModuleTemplates) {
	getSoapModuleTemplates().add(soapModuleTemplates);
	}

	public void clearProviderAssistantship() {
	setProviderAssistant(false);
	setReportingProvider(null);
	}
	
	@OneToOne
	@JoinColumn(name="DEFAULT_VISIT_TYPE")
	public SoapNoteType getSoapNoteType() {
		return soapNoteType;
	}

	public void setSoapNoteType(SoapNoteType soapNoteType) {
		this.soapNoteType = soapNoteType;
	}
	
	@OneToOne
	@JoinColumn(name = "DEFUALT_VISIT_ROOM")
	public FixedAsset getRoom() {
	return room;
	}

	public void setRoom(FixedAsset room) {
	this.room = room;
	}
	
	public Integer getFollowUpVisitDays() {
		if(followUpVisitDays == null)
			followUpVisitDays = 0;
		return followUpVisitDays;
	}

	public void setFollowUpVisitDays(Integer followUpVisitDays) {
		this.followUpVisitDays = followUpVisitDays;
	}

	public Integer getFollowUpVisits() {
		if(followUpVisits == null)
			followUpVisits = 0;
		return followUpVisits;
	}

	public void setFollowUpVisits(Integer followUpVisits) {
		this.followUpVisits = followUpVisits;
	}

	public BigDecimal getFollowUpVisitCharges() {
		if(followUpVisitCharges == null)
			followUpVisitCharges = BigDecimal.ZERO;
		return followUpVisitCharges;
	}

	public void setFollowUpVisitCharges(BigDecimal followUpVisitCharges) {
		this.followUpVisitCharges = followUpVisitCharges;
	}
	
	public Integer getRevisitDays() {
		if(revisitDays == null)
			revisitDays = 0;
		return revisitDays;
	}

	public void setRevisitDays(Integer revisitDays) {
		this.revisitDays = revisitDays;
	}

	public Integer getRevisitVisits() {
		if(revisitVisits == null)
			revisitVisits = 0;
		return revisitVisits;
	}

	public void setRevisitVisits(Integer revisitVisits) {
		this.revisitVisits = revisitVisits;
	}

	public BigDecimal getRevisitCharges() {
		if(revisitCharges == null)
			revisitCharges = BigDecimal.ZERO;
		return revisitCharges;
	}

	public void setRevisitCharges(BigDecimal revisitCharges) {
		this.revisitCharges = revisitCharges;
	}
	
	public Integer getFreeVisitDays() {
		if(freeVisitDays == null)
			freeVisitDays = 0;
		return freeVisitDays;
	}

	public void setFreeVisitDays(Integer freeVisitDays) {
		this.freeVisitDays = freeVisitDays;
	}

	public Integer getFreeVisits() {
		if(freeVisits == null)
			freeVisits = 0;
		return freeVisits;
	}

	public void setFreeVisits(Integer freeVisits) {
		this.freeVisits = freeVisits;
	}

	public BigDecimal getFreeVisitCharges() {
		if(freeVisitCharges == null)
			freeVisitCharges = BigDecimal.ZERO;
		return freeVisitCharges;
	}

	public void setFreeVisitCharges(BigDecimal freeVisitCharges) {
		this.freeVisitCharges = freeVisitCharges;
	}

    public String getVisitingHours() {
        return visitingHours;
    }

    public void setVisitingHours(String visitingHours) {
        this.visitingHours = visitingHours;
    }

    public String getSpecialityToDisplayInPortal() {
        return specialityToDisplayInPortal;
    }

    public void setSpecialityToDisplayInPortal(String specialityToDisplayInPortal) {
        this.specialityToDisplayInPortal = specialityToDisplayInPortal;
    }

	@OneToOne(fetch = FetchType.EAGER, targetEntity = Nationality.class)
	@Cascade(value = { CascadeType.ALL })
	public Nationality getNationality() {
		return nationality;
	}

	public void setNationality(Nationality nationality) {
		this.nationality = nationality;
	}

	public Boolean getToPrintSignature() {
    	if(toPrintSignature == null)
    		toPrintSignature = true;
		return toPrintSignature;
	}

	public String getExperience() {
		return experience;
	}

	public void setExperience(String experience) {
		this.experience = experience;
	}

	public String getPriceRange() {
		return priceRange;
	}

	public void setPriceRange(String priceRange) {
		this.priceRange = priceRange;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public void setToPrintSignature(Boolean toPrintSignature) {
		this.toPrintSignature = toPrintSignature;
	}

	@Override
	public String toString() {
		StringBuilder br = new StringBuilder();
		if(super.getSalutation() != null)
			br.append(this.getSalutation()+".");
		if(super.getFirstName() != null)
			br.append(this.getFirstName());
		if(super.getMiddleName() != null)
			br.append(" "+this.getMiddleName());
		if(super.getLastName() != null)
			br.append(" "+this.getLastName());
		if(super.getEndMostName() != null)
			br.append(" "+this.getEndMostName());
		return br.toString();
	}

	@Override
	public int compareTo(Provider o) {
	return	Long.valueOf(this.id.toString()).compareTo(Long.valueOf(o.id.toString()));
	}

    @Transient
    public String getProviderName(){
        StringBuilder br = new StringBuilder();
        if(this.getSalutation() != null)
            br.append(this.getSalutation()+".");
        if(this.getFirstName() != null)
            br.append(this.getFirstName());
        if(this.getMiddleName() != null)
            br.append(" "+this.getMiddleName());
        if(this.getLastName() != null)
            br.append(" "+this.getLastName());
        if(this.getEndMostName() != null)
            br.append(" "+this.getEndMostName());
        return br.toString();
    }
}