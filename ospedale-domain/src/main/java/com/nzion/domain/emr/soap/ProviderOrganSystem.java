package com.nzion.domain.emr.soap;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.nzion.domain.Provider;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.OrganSystem;

@Entity
@Table(name = "PROVIDER_ORGANSYTEMS")
@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="com.nzion.domain")
public class ProviderOrganSystem extends IdGeneratingBaseEntity implements Comparable<ProviderOrganSystem>{

	private Provider provider;

	private OrganSystem organSystem;

	private boolean mandatory = false;

	private boolean selected = false;

	private int sortOrder;

	public ProviderOrganSystem() {

	}

	public ProviderOrganSystem(Provider provider, OrganSystem organSystem) {
	this.provider = provider;
	this.organSystem = organSystem;
	this.sortOrder = organSystem.getSortOrder() == null ? 0 : organSystem.getSortOrder();
	}

	@ManyToOne
	@JoinColumn(name = "PROVIDER_ID")
	public Provider getProvider() {
	return provider;
	}

	public void setProvider(Provider provider) {
	this.provider = provider;
	}

	@ManyToOne
	@JoinColumn(name = "ORGANSYSTEM_ID")
	public OrganSystem getOrganSystem() {
	return organSystem;
	}

	public void setOrganSystem(OrganSystem organSystem) {
	this.organSystem = organSystem;
	}

	public boolean isMandatory() {
	return mandatory;
	}

	public void setMandatory(boolean mandatory) {
	this.mandatory = mandatory;
	}

	public boolean isSelected() {
	return selected;
	}

	public void setSelected(boolean selected) {
	this.selected = selected;
	}

	public int getSortOrder() {
	return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
	this.sortOrder = sortOrder;
	}

	public int compareTo(ProviderOrganSystem providerOrganSystem) {
	return sortOrder == providerOrganSystem.sortOrder ? 0 : (sortOrder > providerOrganSystem.sortOrder ? 1 : -1);
	}
	
	private static final long serialVersionUID = 1L;

}
