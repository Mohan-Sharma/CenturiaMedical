package com.nzion.repository.product.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.nzion.domain.pms.Product;
import com.nzion.domain.pms.Supplier;
import com.nzion.domain.product.inventory.InventoryConsumptionAdjustment;
import com.nzion.domain.product.inventory.InventoryItem;
import com.nzion.domain.product.order.InventoryOrder;
import com.nzion.domain.product.order.OrderItem;
import com.nzion.domain.product.order.PurchaseOrder;
import com.nzion.repository.impl.HibernateBaseRepository;
import com.nzion.repository.product.ProductRepository;
import com.nzion.util.UtilDateTime;
import com.nzion.util.UtilValidator;

public class ProductRepositoryImpl extends HibernateBaseRepository implements ProductRepository{
	
	@Override
	public List<PurchaseOrder> searchPurchaseOrderByCriteria(String poNumber) {
	Criteria criteria = getSession().createCriteria(PurchaseOrder.class);
	if(UtilValidator.isNotEmpty(poNumber))
		criteria.add(Restrictions.like("poNumber", poNumber));
	if(UtilValidator.isNotEmpty(criteria.list())){
		Set<PurchaseOrder> purchaseOrders = new HashSet<PurchaseOrder>(criteria.list());
		return new ArrayList<PurchaseOrder>(purchaseOrders);
	}else
		return null;
	}
	
	@Override
	public List<OrderItem> searchOrderItemsFromPurchaseOrder(PurchaseOrder purchaseOrder) {
	Criteria criteria = getSession().createCriteria(InventoryOrder.class);
	if(purchaseOrder != null)
		criteria.add(Restrictions.eq("poNumber", purchaseOrder.getPoNumber()));
	if(UtilValidator.isNotEmpty(criteria.list()))
		return (List<OrderItem>) new ArrayList<OrderItem>(((PurchaseOrder)criteria.list().get(0)).getOrderItems());
	else
		return (List<OrderItem>) new ArrayList<OrderItem>();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<PurchaseOrder> getAllPurchaseOrderCreated() {
	Criteria criteria = getSession().createCriteria(PurchaseOrder.class);
	criteria.add(Restrictions.eq("orderStatus", PurchaseOrder.ORDERSTATUS.ORDER_CREATED));
	criteria.addOrder(Order.desc("createdTxTimestamp"));
	if(UtilValidator.isNotEmpty(criteria.list())){
		Set<PurchaseOrder> purchaseOrders = new HashSet<PurchaseOrder>(criteria.list());
		return new ArrayList<PurchaseOrder>(purchaseOrders);
	}else
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<PurchaseOrder> getPurchaseOrder(String poNumber, String productName, Supplier supplier) {
	Criteria criteria = getSession().createCriteria(PurchaseOrder.class);
	if(UtilValidator.isNotEmpty(poNumber))
		criteria.add(Restrictions.eq("poNumber", poNumber));
	if(UtilValidator.isNotEmpty(productName))
		criteria.createCriteria("inventoryItems").createCriteria("product").add(Restrictions.like("tradeName", productName,MatchMode.START));
	if(supplier != null)
		criteria.createCriteria("inventoryItems").add(Restrictions.eq("supplier", supplier));
	if(UtilValidator.isNotEmpty(criteria.list())){
		Set<PurchaseOrder> purchaseOrders = new HashSet<PurchaseOrder>(criteria.list());
		return new ArrayList<PurchaseOrder>(purchaseOrders);
	}else
		return null;
	}
	
	@Override
	public List<InventoryItem> getInventoryItemsByCriteria(String productName, Supplier supplier) {
	Criteria criteria = getSession().createCriteria(InventoryItem.class);
	
	if(supplier != null)
		criteria.add(Restrictions.eq("supplier", supplier));
	if(UtilValidator.isNotEmpty(productName))
		criteria.createCriteria("product").add(Restrictions.like("tradeName", productName,MatchMode.START));
	return criteria.list();
	}
	
	@Override
	public BigDecimal getTotalFromInventory(Product product,String columnName){
	Criteria criteria = getSession().createCriteria(InventoryItem.class);
	if(product == null && UtilValidator.isEmpty(columnName))
		return BigDecimal.ZERO;
	criteria.add(Restrictions.eq("product", product));
	//criteria.add(Restrictions.or(Restrictions.eq("expiryDate", null),Restrictions.ge("expiryDate", new Date())));
	criteria.setProjection(Projections.sum(columnName));
	BigDecimal totalInventory = BigDecimal.ZERO ;
		if(UtilValidator.isNotEmpty(criteria.list())){
			Object value = criteria.list().get(0) == null ? 0 : criteria.list().get(0);
			if(value instanceof Long)
				totalInventory = BigDecimal.valueOf(((Long)value));
			else if(value instanceof BigDecimal)
				totalInventory = ((BigDecimal)value);
		}
	return totalInventory;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<InventoryItem> reduceProductInventory(Product product, BigDecimal qty) {
		int quantity = - qty.intValue();
		Criteria criteria = getSession().createCriteria(InventoryItem.class);
		List<InventoryItem> inventoryItems = new ArrayList<InventoryItem>();
		if(product != null){
			criteria.add(Restrictions.eq("product", product));
			criteria.add(Restrictions.gt("expiryDate", UtilDateTime.getDateOnly(new Date())));
			criteria.addOrder(Order.asc("expiryDate"));
			List<InventoryItem> inveItems = criteria.list();
			Integer quantityToReduce = 0;
			int listSize = inveItems.size();
			for(int i=0; i<listSize ; i++){
				InventoryItem invItem = inveItems.get(i);
				Integer invQuantity = invItem.getQoh();
				quantity = quantity + invQuantity;
				if(quantity >= 0){
					quantityToReduce = quantity;
					quantity = 0;
				}else if(i == listSize-1){
					quantityToReduce = quantity;
				}else
					quantityToReduce = 0;
				invItem.setQoh((quantityToReduce));
				invItem.setAtp((quantityToReduce));

				inventoryItems.add(invItem);
				
				if(quantity == 0) break;
			}
		}
		return inventoryItems;
	}
	
	@Override
	public Integer getTotalQuantityFromInventory(Product product) {
		Criteria criteria = getSession().createCriteria(InventoryItem.class);
		if(product == null)
			return null;
		criteria.add(Restrictions.eq("product", product));
		criteria.add(Restrictions.gt("expiryDate", UtilDateTime.getDateOnly(new Date())));
		criteria.setProjection(Projections.sum("qoh"));
		if(UtilValidator.isNotEmpty(criteria.list()) && criteria.list().get(0) != null)
		 return ((Long) criteria.list().get(0)).intValue();
		else
		 return new Integer(0);
	}

	@Override
	public List<InventoryItem> getInventoryItemsGroupByProductSupplier(String productName, Supplier supplier) {
		Criteria criteria = getSession().createCriteria(InventoryItem.class);
		if(supplier != null)
			criteria.add(Restrictions.eq("supplier", supplier));
		if(UtilValidator.isNotEmpty(productName))
			criteria.createCriteria("product").add(Restrictions.like("tradeName", productName,MatchMode.START));
		criteria.setProjection(Projections.projectionList().add(Projections.groupProperty("supplier")).add(Projections.groupProperty("product")));
		return criteria.list();
	}

	@Override
	public List<InventoryConsumptionAdjustment> getInventoryConsumptionAdjustment(String type, Date fromDate, Date endDate, String productName) {
		Criteria criteria = getSession().createCriteria(InventoryConsumptionAdjustment.class);
		if(UtilValidator.isNotEmpty(type))
			criteria.add(Restrictions.eq("type", type));
		if(UtilValidator.isNotEmpty(fromDate))
			//criteria.add(Restrictions.ge("createTxTimestamp", fromDate));
		if(UtilValidator.isNotEmpty(endDate))
			//criteria.add(Restrictions.le("createTxTimestamp", endDate));
		
		if(UtilValidator.isNotEmpty(productName))
			criteria.createCriteria("product").add(Restrictions.like("tradeName", productName,MatchMode.START));
		
		return criteria.list();
	}
	
}
