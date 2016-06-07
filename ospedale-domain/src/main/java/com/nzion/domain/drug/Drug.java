package com.nzion.domain.drug;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Filter;

import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.billing.TaxItem;
import com.nzion.domain.emr.UnitOfMeasurement;

/**
 * @author Sandeep Prusty Dec 22, 2010
 */

// As per drug.com the second line is generic name
@Entity
@Table
@Filter(name = "EnabledFilter", condition = "(IS_ACTIVE=1 OR IS_ACTIVE IS NULL)")
public class Drug extends IdGeneratingBaseEntity{

	private String tradeName;

	private DrugGenericName genericName;

    private DrugForm drugForm;

	private String brandName;

	private Set<String> strengths;

	private DrugDosageRoute routes;

	private DrugDosageForm dosageForm;
	
	private TaxItem taxItem;
	
	private BigDecimal amount;
	
	private UnitOfMeasurement baseSaleUom;
	
	private Integer availableQty;
	
	private UnitOfMeasurement purchaseUom;

	@Transient
	public UnitOfMeasurement getPurchaseUom() {
		return purchaseUom;
	}

	public void setPurchaseUom(UnitOfMeasurement purchaseUom) {
		this.purchaseUom = purchaseUom;
	}

	@Transient
	public Integer getAvailableQty() {
		return availableQty;
	}

	public void setAvailableQty(Integer availableQty) {
		this.availableQty = availableQty;
	}

	@Transient
	public UnitOfMeasurement getBaseSaleUom() {
		return baseSaleUom;
	}

	public void setBaseSaleUom(UnitOfMeasurement baseSaleUom) {
		this.baseSaleUom = baseSaleUom;
	}

	@Transient
	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getTradeName() {
		return tradeName;
	}

	public void setTradeName(String tradeName) {
		this.tradeName = tradeName;
	}

    @OneToOne
    @JoinColumn(name = "DRUG_GENERIC_NAME_ID")
    @Fetch(FetchMode.SELECT)
	public DrugGenericName getGenericName() {
		return genericName;
	}

    @OneToOne
    @JoinColumn(name = "DRUG_FORM_ID")
    @Fetch(FetchMode.SELECT)
    public DrugForm getDrugForm() {
        return drugForm;
    }

    public void setDrugForm(DrugForm drugForm) {
        this.drugForm = drugForm;
    }

    public void setGenericName(DrugGenericName genericName) {
		this.genericName = genericName;
	}

	@Column(length = 1024)
	public String getBrandName() {
		return brandName;
	}

	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}

	@ElementCollection(fetch=FetchType.EAGER)
	@CollectionTable(name = "DRUG_STRENGTH")
	@Fetch(FetchMode.SELECT)
	public Set<String> getStrengths() {
	if(strengths == null)
		strengths = new HashSet<String>(); 
	return strengths;
	}

	public void setStrengths(Set<String> strengths) {
		this.strengths = strengths;
	}

	@OneToOne(targetEntity = DrugDosageRoute.class)
	@Fetch(FetchMode.SELECT)
    @JoinColumn(name = "DRUG_DOSAGE_ROUTE_ID")
	public DrugDosageRoute getRoutes() {
	if(routes == null)
		routes = new DrugDosageRoute();
	return routes;
	}

	public void setRoutes(DrugDosageRoute routes) {
		this.routes = routes;
	}

	@OneToOne
	@JoinColumn(name = "DOSAGE_ID")
    @Fetch(FetchMode.SELECT)
	public DrugDosageForm getDosageForm() {
		return dosageForm;
	}

	public void setDosageForm(DrugDosageForm dosageForm) {
		this.dosageForm = dosageForm;
	}
	
	@OneToOne
	public TaxItem getTaxItem() {
		return taxItem;
	}

	public void setTaxItem(TaxItem taxItem) {
		this.taxItem = taxItem;
	}


	private static final long serialVersionUID = 1L;

}