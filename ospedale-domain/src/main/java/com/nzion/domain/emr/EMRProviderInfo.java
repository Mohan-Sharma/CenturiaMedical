package com.nzion.domain.emr;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;

import com.nzion.domain.Provider;
import com.nzion.domain.base.IdGeneratingBaseEntity;

/**
 * @author Sandeep Prusty
 * Apr 14, 2010
 */

@Entity
@Table(name = "EMR_PROVIDER")
@Filters( {
		@Filter(name = "EnabledFilter", condition = "(IS_ACTIVE=1 OR IS_ACTIVE IS NULL)") })
@AttributeOverride(column = @Column(name = "id"), name = "EMR_PROVIDER_CODE")
public class EMRProviderInfo extends IdGeneratingBaseEntity {

	private static final long serialVersionUID = 1L;
	private Provider provider;

	public EMRProviderInfo() {
	}

	public EMRProviderInfo(Provider provider) {
	this.provider = provider;
	}

	/***
	@TODO family illness
	***/
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "PROVIDER_ID")
	public Provider getProvider() {
	return provider;
	}

	public void setProvider(Provider provider) {
	this.provider = provider;
	}
}