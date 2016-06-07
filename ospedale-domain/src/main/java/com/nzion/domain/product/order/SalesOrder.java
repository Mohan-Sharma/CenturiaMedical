/**
 * 
 */
package com.nzion.domain.product.order;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import com.nzion.domain.Patient;
import com.nzion.domain.Provider;

/**
 * @author Nafis
 *
 */
@Entity
@DiscriminatorValue("SALES")
public class SalesOrder extends InventoryOrder{
	private static final long serialVersionUID = 1L;
	
	private String admissionNumber;
	
	private Boolean billGenerated = Boolean.FALSE;
	
	private Provider provider;
	
	private String outSideProvider;
	
	private Patient patient;
	
	private String outSidePatient;
	
	private boolean isOutPatient = true;

	private STATUS status;
	
	public STATUS getStatus() {
		return status;
	}

	public void setStatus(STATUS status) {
		this.status = status;
	}

	public boolean isOutPatient() {
		return isOutPatient;
	}

	public void setOutPatient(boolean isOutPatient) {
		this.isOutPatient = isOutPatient;
	}

	@OneToOne
	public Provider getProvider() {
		return provider;
	}

	public void setProvider(Provider provider) {
		this.provider = provider;
	}
    
	public String getOutSideProvider() {
		return outSideProvider;
	}

	public void setOutSideProvider(String outSideProvider) {
		this.outSideProvider = outSideProvider;
	}

	@OneToOne
	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public String getOutSidePatient() {
		return outSidePatient;
	}

	public void setOutSidePatient(String outSidePatient) {
		this.outSidePatient = outSidePatient;
	}

	public String getAdmissionNumber() {
	return admissionNumber;
	}

	public void setAdmissionNumber(String admissionNumber) {
	this.admissionNumber = admissionNumber;
	}

	public Boolean getBillGenerated() {
	return billGenerated;
	}

	public void setBillGenerated(Boolean billGenerated) {
	this.billGenerated = billGenerated;
	}

	
	public static enum STATUS{
		NEW{
			@Override
			public STATUS[] getAllowedModifications() {
				return new STATUS[] {NEW,INPROGRESS};
			}
		},
		INPROGRESS{
			@Override
			public STATUS[] getAllowedModifications() {
				return new STATUS[] {INPROGRESS,INVOICED};
			}
		},
		INVOICED{
			@Override
			public STATUS[] getAllowedModifications() {
				return new STATUS[] {INVOICED};
			}
			
		},
		PAID{
			@Override
			public STATUS[] getAllowedModifications() {
				return new STATUS[] {PAID};
			}
		},
		DELIVERED{
			@Override
			public STATUS[] getAllowedModifications() {
				return new STATUS[] {DELIVERED};
			}
		};
		
		public abstract STATUS[] getAllowedModifications();
	}
	
	
}
