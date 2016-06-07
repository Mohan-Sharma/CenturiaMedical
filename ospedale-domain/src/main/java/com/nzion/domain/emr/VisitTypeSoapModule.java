package com.nzion.domain.emr;

import com.nzion.domain.Provider;
import com.nzion.domain.SlotType;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Filters( { @Filter(name = "EnabledFilter",condition="(IS_ACTIVE=1 OR IS_ACTIVE IS NULL)") })
public class VisitTypeSoapModule extends IdGeneratingBaseEntity {

	private static final long serialVersionUID = 1L;
	
	private SlotType slotType;
	
	private Provider provider;
	
	private boolean smartService;

	private boolean smartServiceDisplayInPortal;
	
	private boolean visitPolicy;
	
	private Set<SoapModule> modules;

	@ManyToOne(targetEntity = SlotType.class)
	@JoinColumn(name="SLOT_TYPE_ID")
	public SlotType getSlotType() {
	return slotType;
	}

	public void setSlotType(SlotType slotType) {
	this.slotType = slotType;
	}

	@ManyToOne(targetEntity = Provider.class)
	@JoinColumn(name = "PROVIDER_ID")
	public Provider getProvider() {
	return provider;
	}

	public void setProvider(Provider provider) {
	this.provider = provider;
	}

	@ManyToMany
	@JoinTable(name = "VISIT_TYPE_MODULE_SOAP_MODULE", joinColumns = { @JoinColumn(name = "VISIT_TYPE_MODULE_ID") }, inverseJoinColumns = { @JoinColumn(name = "SOAP_MODULE_ID") })
	public Set<SoapModule> getModules() {
	return modules;
	}

	public void setModules(Set<SoapModule> modules) {
	this.modules = modules;
	}
	
	
	public void addOrRemoveSoapModule(boolean add,SoapModule soapModule){
		if(modules == null)
			modules = new HashSet<SoapModule>();
		boolean b  =  add ? modules.add(soapModule) : modules.remove(soapModule);
	}

  public boolean isSmartService() {
		return smartService;
	}

	public void setSmartService(boolean smartService) {
		this.smartService = smartService;
	}

	public boolean isSmartServiceDisplayInPortal() {
		return smartServiceDisplayInPortal;
	}

	public void setSmartServiceDisplayInPortal(boolean smartServiceDisplayInPortal) {
		this.smartServiceDisplayInPortal = smartServiceDisplayInPortal;
	}

	public boolean isVisitPolicy() {
		return visitPolicy;
	}

	public void setVisitPolicy(boolean visitPolicy) {
		this.visitPolicy = visitPolicy;
	}
	
}