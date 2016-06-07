package com.nzion.repository.product;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.nzion.domain.pms.Product;
import com.nzion.domain.pms.Supplier;
import com.nzion.domain.product.inventory.InventoryConsumptionAdjustment;
import com.nzion.domain.product.inventory.InventoryItem;
import com.nzion.domain.product.order.OrderItem;
import com.nzion.domain.product.order.PurchaseOrder;
import com.nzion.repository.BaseRepository;

public interface ProductRepository extends BaseRepository{
	
	List<PurchaseOrder> searchPurchaseOrderByCriteria(String poNumber);
	
	List<OrderItem> searchOrderItemsFromPurchaseOrder(PurchaseOrder purchaseOrder);
	
	List<PurchaseOrder> getAllPurchaseOrderCreated();
	
	List<PurchaseOrder> getPurchaseOrder(String poNumber , String productName,Supplier supplier);
	
	List<InventoryItem> getInventoryItemsByCriteria(String productName, Supplier supplier);
	
	BigDecimal getTotalFromInventory(Product product,String columnName);
	
	List<InventoryItem> reduceProductInventory(Product product, BigDecimal quantity);
	
	Integer getTotalQuantityFromInventory(Product product);
	
	List<InventoryItem> getInventoryItemsGroupByProductSupplier(String productName, Supplier supplier);
	
	 List<InventoryConsumptionAdjustment> getInventoryConsumptionAdjustment(String type,Date fromDate, Date endDate, String productName);

}
