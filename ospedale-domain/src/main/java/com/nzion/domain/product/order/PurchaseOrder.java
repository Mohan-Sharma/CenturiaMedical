/**
 * 
 */
package com.nzion.domain.product.order;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.nzion.domain.product.inventory.InventoryItem;

/**
 * @author Nafis
 *
 */
@Entity
@DiscriminatorValue("PURCHASE")
public class PurchaseOrder extends InventoryOrder{
	private static final long serialVersionUID = 1L;
	
	private String poNumber;
	
	private List<InventoryItem> inventoryItems;
	
	private ORDERSTATUS orderStatus;
	
	public String getPoNumber() {
	return poNumber;
	}

	public void setPoNumber(String poNumber) {
	this.poNumber = poNumber;
	}
	
	@OneToMany
	@Cascade (value=CascadeType.SAVE_UPDATE)
	@JoinTable(name="PURCHASE_ORDER_INVENTORY_ITEM", joinColumns=@JoinColumn(name="PO_ID"),inverseJoinColumns=@JoinColumn(name="INVENTORY_ID"))
	public List<InventoryItem> getInventoryItems() {
	return inventoryItems;
	}

	public void setInventoryItems(List<InventoryItem> inventoryItems) {
	this.inventoryItems = inventoryItems;
	}
	
	@Column
    @Enumerated(EnumType.STRING)
	public ORDERSTATUS getOrderStatus() {
	return orderStatus;
	}


	public void setOrderStatus(ORDERSTATUS orderStatus) {
	this.orderStatus = orderStatus;
	}



	public static enum ORDERSTATUS{
    	ORDER_CREATED,ORDER_RECEIVED
    }
	
	
}
