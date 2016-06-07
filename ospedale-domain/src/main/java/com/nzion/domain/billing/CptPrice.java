package com.nzion.domain.billing;

import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.Cpt;
import com.nzion.domain.product.common.Money;

import org.hibernate.annotations.Filter;

import javax.persistence.*;

/**
 * @author Dharanesha.K
 * 
 *         15-Nov-2011
 */

@Entity
@Filter(name = "EnabledFilter", condition = "(IS_ACTIVE=1 OR IS_ACTIVE IS NULL)")
public class CptPrice extends IdGeneratingBaseEntity {

	private Cpt cpt;
	
	private Money price;
	
	private Money allowedPrice;
	
	private Money standardPrice;
	
	private Contract contract;
	
	public CptPrice(){}
	
	public CptPrice(Cpt cpt, Money price) {
	this.cpt = cpt;
	this.price = price;
	}

	@OneToOne
	@JoinColumn(name = "CPT_ID")
	public Cpt getCpt() {
		return cpt;
	}

	public void setCpt(Cpt cpt) {
		this.cpt = cpt;
	}

	@Embedded
	@AttributeOverride(name = "amount", column = @Column(name = "PRICE"))
	@AssociationOverride(name="currency", joinColumns=@JoinColumn(name="CURRENCY_ID"))
	public Money getPrice() {
		return price = (price == null ? new Money() : price);
	}

	public void setPrice(Money price) {
		this.price = price;
	}

	@Embedded
	@AttributeOverride(name = "amount", column = @Column(name = "ALLOWED_PRICE"))
	@AssociationOverride(name="currency", joinColumns=@JoinColumn(name="ALLOWED_CURRENCY"))
	public Money getAllowedPrice() {
	return allowedPrice = (allowedPrice == null ? new Money() : allowedPrice);
	}

	public void setAllowedPrice(Money allowedPrice) {
	this.allowedPrice = allowedPrice;
	}

	@Embedded
	@AttributeOverride(name = "amount", column = @Column(name = "STANDARD_PRICE"))
	@AssociationOverride(name="currency", joinColumns=@JoinColumn(name="STANDARD_CURRENCY"))
	public Money getStandardPrice() {
	return this.standardPrice = (this.standardPrice == null ? new Money() : this.standardPrice);
	}

	public void setStandardPrice(Money standardPrice) {
	this.standardPrice = standardPrice;
	}
	
	@ManyToOne
	@JoinColumn(name="CONTRACT_ID")
	public Contract getContract() {
	return contract;
	}
	
	public void setContract(Contract contract) {
	this.contract = contract;
	}


	private static final long serialVersionUID = 1L;
}
