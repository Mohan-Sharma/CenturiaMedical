package com.nzion.domain.emr;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;

import com.nzion.domain.Provider;
import com.nzion.domain.base.IdGeneratingBaseEntity;

@Entity
@Table(name = "PROVIDER_RX_PREFERENCE")
@Filters( {
		@Filter(name = "EnabledFilter", condition = "(IS_ACTIVE=1 OR IS_ACTIVE IS NULL)") })
public class ProviderRxPreference extends IdGeneratingBaseEntity{


	public static enum RxPreferenceEnum {
		PATIENT_ALLERGY_CHECK_REQUIRED, DRUG_CONTRAINDICATION_CHECK_REQUIRED, DRUG_CLASSIFICATION_CHECK_REQUIRED,DRUG_INTERACTION_CHECK_REQUIRED,DRUG_INDICATION_CHECK_REQUIRED,
		DRUG_ADVERSE_REACTION_CHECK_REQUIRED, DRUG_DRUG_INTERACTION_CHECK_REQUIRED, LOW_SEVERITY_CHECK_REQUIRED, MEDIUM_SEVERITY_CHECK_REQUIRED, HIGH_SEVERITY_CHECK_REQUIRED
	}

	Map<String, Boolean> preferences = new HashMap<String, Boolean>();
	
	private Provider provider;
	
	@SuppressWarnings("deprecation")
	@ElementCollection
	@JoinTable(name = "PROVIDER_RX_PREFERENCE_MAP")
	@Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	@org.hibernate.annotations.MapKey(columns = { @Column(name = "RX_PREFERENCE_TYPE") })
	@Column(name = "RX_PREFERENCE_VALUE")
	public Map<String, Boolean> getPreferences() {
	return preferences;
	}

	public void setPreferences(Map<String, Boolean> preferences) {
	this.preferences = preferences;
	}

	@OneToOne
	public Provider getProvider() {
	return provider;
	}

	public void setProvider(Provider provider) {
	this.provider = provider;
	}
	
	@Transient
	public Boolean hasPreference(RxPreferenceEnum rxEnum){
		Object val = preferences.get(rxEnum.name());
		return val==null? null : preferences.get(rxEnum.name());
	}
	
	private static final long serialVersionUID = 1L;
}
