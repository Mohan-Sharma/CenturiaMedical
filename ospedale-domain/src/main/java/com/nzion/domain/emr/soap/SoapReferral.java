package com.nzion.domain.emr.soap;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.nzion.domain.Provider;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.nzion.domain.Referral;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.SoapModule;

@Entity
@Table
@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="com.nzion.domain")
public class SoapReferral extends IdGeneratingBaseEntity {

	private static final long serialVersionUID = 1L;
	
	private Referral referral;

    private Provider provider;
	
	private String notes;
	
	private Set<SoapModule> modules;
	
	private ReferralSection referralSection;

    private boolean internalReferral;
    
    private String referralDoctorFirstName;
    
    private String referralDoctorLastName;
    
    private Provider referralClinicDoctorTransient;

	private Long doctorIdFromPortal;

	private String status;

	@OneToOne
	@JoinColumn(name = "REFERRAL_ID")
	public Referral getReferral() {
	return referral;
	}

	public void setReferral(Referral referral) {
	this.referral = referral;
	}

    @OneToOne
    @JoinColumn(name = "PROVIDER_ID")
    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    @Column(length = 1024)
	public String getNotes() {
	return notes;
	}

	public void setNotes(String notes) {
	this.notes = notes;
	}

	@ManyToMany
	@JoinTable(name = "SOAPREFERRAL_MODULES", joinColumns = { @JoinColumn(name = "SOAPREFERRAL_ID") }, inverseJoinColumns = { @JoinColumn(name = "MODULE_ID") })
	public Set<SoapModule> getModules() {
	return modules;
	}

	public void setModules(Set<SoapModule> modules) {
	this.modules = modules;
	}

	@OneToOne
	@JoinColumn(name = "REFERRAL_SECTION_ID")
	public ReferralSection getReferralSection() {
	return referralSection;
	}

	public void setReferralSection(ReferralSection referralSection) {
	this.referralSection = referralSection;
	}

    public boolean isInternalReferral() {
        return internalReferral;
    }

    public void setInternalReferral(boolean internalReferral) {
        this.internalReferral = internalReferral;
    }
    
    public String getReferralDoctorFirstName() {
		return referralDoctorFirstName;
	}

	public void setReferralDoctorFirstName(String referralDoctorFirstName) {
		this.referralDoctorFirstName = referralDoctorFirstName;
	}

	public String getReferralDoctorLastName() {
		return referralDoctorLastName;
	}

	public void setReferralDoctorLastName(String referralDoctorLastName) {
		this.referralDoctorLastName = referralDoctorLastName;
	}
	
	@Transient
	public Provider getReferralClinicDoctorTransient() {
		return referralClinicDoctorTransient;
	}

	public void setReferralClinicDoctorTransient(Provider referralClinicDoctorTransient) {
		this.referralDoctorFirstName = referralClinicDoctorTransient.getFirstName();
		this.referralDoctorLastName = referralClinicDoctorTransient.getLastName();
		this.referralClinicDoctorTransient = referralClinicDoctorTransient;
	}

	public Long getDoctorIdFromPortal() {
		return doctorIdFromPortal;
	}

	public void setDoctorIdFromPortal(Long doctorIdFromPortal) {
		this.doctorIdFromPortal = doctorIdFromPortal;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
