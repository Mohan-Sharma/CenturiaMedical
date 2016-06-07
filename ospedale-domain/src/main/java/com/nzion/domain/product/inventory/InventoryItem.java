package com.nzion.domain.product.inventory;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.nzion.domain.Location;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.UnitOfMeasurement;
import com.nzion.domain.pms.Product;
import com.nzion.domain.pms.Supplier;
import com.nzion.domain.product.common.Money;

@Entity
@Table
public class InventoryItem extends IdGeneratingBaseEntity {
	private static final long serialVersionUID = 1L;

	private Product product;
    private STATUS status;
    private Integer qoh;
    private Integer freeQty;
    private Integer atp = 1;
    private String serialNumber;
    private Money unitCost;
    private String comments;
    private UnitOfMeasurement purchaseUom;
    private String batchNumber;
    private Location location;
    private Supplier supplier;
    private Boolean isSerialized = false;
    private POSTATUS poStatus;
    private String drugStrength;
    private Date manufactureDate;
    private Date expiryDate;
    private Integer rejectedQuantity = 0;
    private String rejectionReason;
    
    public InventoryItem(){}
    
    public InventoryItem(InventoryItem item){
    	status = item.getStatus();
    	qoh = item.getQoh();
    	atp = item.getAtp();
    	unitCost = item.getUnitCost();
    	purchaseUom = item.getPurchaseUom();
    	batchNumber = item.getBatchNumber();
    	location = item.getLocation();
    	supplier = item.getSupplier();
    	isSerialized = item.getSerialized();
    	poStatus = item.getPoStatus();
    	drugStrength = item.getDrugStrength();
    	manufactureDate = item.getManufactureDate();
    	expiryDate = item.getExpiryDate();
    	rejectedQuantity = item.getRejectedQuantity();
    	rejectionReason = item.getRejectionReason();
    }
    
	public String getRejectionReason() {
	return rejectionReason;
	}

	public void setRejectionReason(String rejectionReason) {
	this.rejectionReason = rejectionReason;
	}

	public Integer getRejectedQuantity() {
	return rejectedQuantity;
	}

	public void setRejectedQuantity(Integer rejectedQuantity) {
	this.rejectedQuantity = rejectedQuantity;
	}

	public Boolean getSerialized() {
        return isSerialized;
    }

    public void setSerialized(Boolean serialized) {
        isSerialized = serialized;
    }
    
    @Temporal(value = TemporalType.TIMESTAMP)
    public Date getManufactureDate() {
	return manufactureDate;
	}

	public void setManufactureDate(Date manufactureDate) {
	this.manufactureDate = manufactureDate;
	}

	@Temporal(value = TemporalType.TIMESTAMP)
	public Date getExpiryDate() {
	return expiryDate;
	}

	public void setExpiryDate(Date expiryDate) {
	this.expiryDate = expiryDate;
	}

	@ManyToOne
    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @ManyToOne
    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    @Column
    @Enumerated(EnumType.STRING)
    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }
    
    @Column
    @Enumerated(EnumType.STRING)
    public POSTATUS getPoStatus() {
	return poStatus;
	}

	public void setPoStatus(POSTATUS poStatus) {
	this.poStatus = poStatus;
	}

	public Integer getQoh() {
        return qoh;
    }

    public void setQoh(Integer qoh) {
        this.qoh = qoh;
    }

    public Integer getAtp() {
        return atp;
    }

    public void setAtp(Integer atp) {
        this.atp = atp;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    @Embedded
    public Money getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(Money unitCost) {
        this.unitCost = unitCost;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
    
    public String getBatchNumber() {
        return batchNumber;
    }
    
    @OneToOne
    public UnitOfMeasurement getPurchaseUom() {
	return purchaseUom;
	}

	public void setPurchaseUom(UnitOfMeasurement purchaseUom) {
	this.purchaseUom = purchaseUom;
	}

	public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getDrugStrength() {
	return drugStrength;
	}

	public void setDrugStrength(String drugStrength) {
	this.drugStrength = drugStrength;
	}
	
	@OneToOne
	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public Integer getFreeQty() {
		return freeQty;
	}

	public void setFreeQty(Integer freeQty) {
		this.freeQty = freeQty;
	}



	public static enum STATUS {
        READY, IN_USE, IN_PREPARATION, INACTIVE
    }
    
    public static enum POSTATUS{
    	PO_CREATED,PO_RECEIVED,PO_REJECTED
    }
}
