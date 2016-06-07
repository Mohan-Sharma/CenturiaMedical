package com.nzion.domain.product.order;

import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.UnitOfMeasurement;
import com.nzion.domain.pms.Product;
import com.nzion.domain.product.common.Money;
import com.nzion.util.UtilValidator;

import javax.persistence.*;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Nafis Sep 30, 2011
 */
@Entity
public class OrderItem extends IdGeneratingBaseEntity {
	private static final long serialVersionUID = 1L;

	private Product product;
	private Integer quantity;
	private Money unitPrice;
	private Money totalPrice;
	private BigDecimal price;
	private UnitOfMeasurement saleUom;
	private String batchNo;
	private Date expiryDate;
	private String brandName;

	// BatchNo
	// ExpiryDate
	// ManufecName

	@OneToOne
	public Product getProduct() {
		return product;
	}

	@OneToOne
	public UnitOfMeasurement getSaleUom() {
		return saleUom;
	}

	public void setSaleUom(UnitOfMeasurement saleUom) {
		this.saleUom = saleUom;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "amount", column = @Column(name = "MRP_AMOUNT")),
			@AttributeOverride(name = "currency", column = @Column(name = "MRP_CURRENCY")) })
	public Money getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(Money unitPrice) {
		this.unitPrice = unitPrice;
	}

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "amount", column = @Column(name = "PRICE_AMOUNT")),
			@AttributeOverride(name = "currency", column = @Column(name = "PRICE_CURRENCY")) })
	public Money getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(Money totalPrice) {
		this.totalPrice = totalPrice;
	}

	@Transient
	public void addTotalPrice() {
		if (quantity != null) {
			totalPrice = new Money(this.unitPrice.getAmount().multiply(
					new BigDecimal(quantity)));
		}
	}

	public BigDecimal getPrice() {
		if (UtilValidator.isNotEmpty(price)) {
			Money money = new Money(price);
			setUnitPrice(money);
		}
		return price;
	}

	public void setPrice(BigDecimal price) {
		if (UtilValidator.isNotEmpty(price)) {
			Money money = new Money(price);
			setUnitPrice(money);
		}
		this.price = price;
	}

	public String getBatchNo() {
		return batchNo;
	}

	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}

	@Temporal(TemporalType.DATE)
	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}

	public String getBrandName() {
		return brandName;
	}

	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}
}
