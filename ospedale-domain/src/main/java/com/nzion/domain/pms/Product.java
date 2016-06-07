package com.nzion.domain.pms;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;

import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.UnitOfMeasurement;

@Entity
@Filters( { @Filter(name = "EnabledFilter",condition="(IS_ACTIVE=1 OR IS_ACTIVE IS NULL)") })
public class Product extends IdGeneratingBaseEntity{

	private static final long serialVersionUID = 1L;
	
	private String tradeName;

	private String genericName;

	private String brandName;
	
	private UnitOfMeasurement baseUom;

    private String salesPrice;

	public String getTradeName() {
		return tradeName;
	}

	public void setTradeName(String tradeName) {
		this.tradeName = tradeName;
	}

	public String getGenericName() {
		return genericName;
	}

	public void setGenericName(String genericName) {
		this.genericName = genericName;
	}

	public String getBrandName() {
		return brandName;
	}

	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}
	
	@OneToOne
	public UnitOfMeasurement getBaseUom() {
		return baseUom;
	}

	public void setBaseUom(UnitOfMeasurement baseUom) {
		this.baseUom = baseUom;
	}

    public String getSalesPrice() {
        return salesPrice;
    }

    public void setSalesPrice(String salesPrice) {
        this.salesPrice = salesPrice;
    }
}
