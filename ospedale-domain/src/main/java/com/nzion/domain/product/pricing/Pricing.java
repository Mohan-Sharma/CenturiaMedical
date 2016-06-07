package com.nzion.domain.product.pricing;

import java.util.Collection;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.nzion.domain.HistoricalModel;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.product.common.Money;
import com.nzion.util.UtilDateTime;
import com.nzion.util.UtilValidator;

@Entity
@Table
public class Pricing extends IdGeneratingBaseEntity {

	private static final long serialVersionUID = 1L;

	private Money price;
	private Money mrp;
	private HistoricalModel effectivePeriod = new HistoricalModel();

	@Embedded
	@AttributeOverrides( { @AttributeOverride(name = "amount", column = @Column(name = "MRP_AMOUNT")),@AttributeOverride(name = "currency", column = @Column(name = "MRP_CURRENCY")) })
	public Money getMrp() {
	if (mrp == null) 
		mrp = new Money();
	return mrp;
	}

	public void setMrp(Money mrp) {
	this.mrp = mrp;
	}

	@Embedded
	@AttributeOverrides( { @AttributeOverride(name = "amount", column = @Column(name = "PRICE_AMOUNT")),@AttributeOverride(name = "currency", column = @Column(name = "PRICE_CURRENCY")) })
	public Money getPrice() {
	if (price == null) 
		price = new Money();
	return price;
	}

	public void setPrice(Money price) {
	this.price = price;
	}

	@Embedded
	public HistoricalModel getEffectivePeriod() {
	return effectivePeriod;
	}

	public void setEffectivePeriod(HistoricalModel effectivePeriod) {
	this.effectivePeriod = effectivePeriod;
	}

	@Transient
	public boolean isCurrentlyEffective() {
	if (effectivePeriod != null)
		return !effectivePeriod.isExpired(UtilDateTime.nowDate());
	else
		return false;
	}

	public void inValidatePricing(String expiryReason) {
	effectivePeriod.setThruDate(UtilDateTime.addDaysToDate(UtilDateTime.nowDate(), -1));
	effectivePeriod.setExpiryReason(expiryReason);
	}

	public void inValidatePricing() {
	inValidatePricing(null);
	}

	public static Pricing findEffectivePricing(Collection<Pricing> pricings) {
	if (UtilValidator.isNotEmpty(pricings)) {
		for (Pricing pricing : pricings) {
			if (pricing.isCurrentlyEffective()) {
				return pricing;
			}
		}
	}
	return null;
	}
}
