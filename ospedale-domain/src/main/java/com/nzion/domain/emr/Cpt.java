package com.nzion.domain.emr;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.Index;

import com.nzion.domain.base.BaseEntity;
import com.nzion.domain.pms.Product;
import com.nzion.domain.pms.ProductBillOfMaterial;
import com.nzion.util.UtilReflection;

/**
 * @author Sandeep Prusty
 * Aug 2, 2010
 */

@Entity
@Table(name = "CPT")
@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="com.nzion.domain")
@Filters( {
		@Filter(name = "EnabledFilter", condition = "(IS_ACTIVE=1 OR IS_ACTIVE IS NULL)")})
public class Cpt extends BaseEntity implements Comparable<Cpt>{

	private static final long serialVersionUID = 1L;

	private String id;

	private String description;

	private String cptVersion;

	private String shortDescription;

	private String longDescription;

	private BigDecimal price;
	
	private Set<ProductBillOfMaterial> productBillOfMaterials;
	
	@Index(name = "CPT_SHORT_DESC_IDX")
	public String getShortDescription() {
	return shortDescription;
	}

	public void setShortDescription(String shortDescription) {
	this.shortDescription = shortDescription;
	}

	@Column(name="LONG_DESCRIPTION", length = 2000)
	public String getLongDescription() {
	return longDescription;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price != null ? price.setScale(3, BigDecimal.ROUND_HALF_UP) : price;
	}

	public void setLongDescription(String longDescription) {
	this.longDescription = longDescription;
	}

	@Id
	@Column(name = "CPT_CODE")
	public String getId() {
	return id;
	}

	public void setId(String id) {
	this.id = (String) id;
	}

	@Column(name = "CPT_VERSION")
	public String getCptVersion() {
	return cptVersion;
	}

	public void setCptVersion(String cptVersion) {
	this.cptVersion = cptVersion;
	}

	@Column
	@Index(name = "CPT_DESC_IDX")
	public String getDescription() {
	return description;
	}

	public void setDescription(String description) {
	this.description = description;
	}
	
	@ManyToMany(targetEntity = ProductBillOfMaterial.class, fetch = FetchType.LAZY)
	@Cascade(CascadeType.SAVE_UPDATE)
	public Set<ProductBillOfMaterial> getProductBillOfMaterials() {
		if(productBillOfMaterials == null)
			productBillOfMaterials = new HashSet<ProductBillOfMaterial>();
		return productBillOfMaterials;
	}

	public void setProductBillOfMaterials(Set<ProductBillOfMaterial> productBillOfMaterials) {
		this.productBillOfMaterials = productBillOfMaterials;
	}

	@Transient
	public void addProduct(ProductBillOfMaterial productBillOfMaterial){
		getProductBillOfMaterials().add(productBillOfMaterial);
	}
	
	@Transient
	public void removeProduct(ProductBillOfMaterial productBillOfMaterial){
		getProductBillOfMaterials().remove(productBillOfMaterial);
	}

	@Override
	public boolean equals(Object obj) {
	return UtilReflection.areEqual(obj, this, "id");
	}

	@Transient
	private transient Integer hash = null;

	@Override
	public int hashCode() {
	if (hash != null) return hash;
	if (getId() == null) {
		hash = 0;
		return hash;
	}
	return id.hashCode();
	}

	@Override
	public String toString() {
	return description;
	}
	
	public int compareTo(Cpt cpt) {
		if(cpt.getId()==null || this.getId()==null)
			return 0;
		return this.getId().compareTo(cpt.getId());
		}
		
	}