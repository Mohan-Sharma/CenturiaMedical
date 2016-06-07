package com.nzion.domain.product.inventory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.nzion.domain.Patient;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.billing.InvoiceItem;
import com.nzion.domain.pms.Product;

@Entity
@Table
public class InventoryConsumptionAdjustment extends IdGeneratingBaseEntity{
   
	private static final long serialVersionUID = 1L;

	private Product product;
    
	private String type;
	
	private Integer quantity = 0;
	
	private String quantityAction = "REDUSE_QUANTITY";
	
	private String batchNumber;
	
	private Patient patient;
	
	private Date expiryDate;
	
	private String note;
	
	private List<InventoryItem> inventoryItemsTransient = new ArrayList<InventoryItem>();
	
	@OneToOne
	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	@OneToOne
	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	@Temporal(value = TemporalType.TIMESTAMP)
	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getQuantityAction() {
		if(quantityAction == null)
			quantityAction = "REDUSE_QUANTITY";
		return quantityAction;
	}

	public void setQuantityAction(String quantityAction) {
		this.quantityAction = quantityAction;
	}

	public String getBatchNumber() {
		return batchNumber;
	}

	public void setBatchNumber(String batchNumber) {
		this.batchNumber = batchNumber;
	}

	@Transient
	public List<InventoryItem> getInventoryItemsTransient() {
		return inventoryItemsTransient;
	}

	public void setInventoryItemsTransient(List<InventoryItem> inventoryItemsTransient) {
		this.inventoryItemsTransient = inventoryItemsTransient;
	}

}
