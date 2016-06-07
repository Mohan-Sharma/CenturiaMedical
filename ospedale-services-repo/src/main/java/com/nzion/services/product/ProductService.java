package com.nzion.services.product;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.nzion.domain.pms.Product;
import com.nzion.domain.pms.Supplier;
import com.nzion.domain.product.inventory.InventoryConsumptionAdjustment;
import com.nzion.domain.product.inventory.InventoryItem;
import com.nzion.domain.product.order.OrderItem;
import com.nzion.domain.product.order.PurchaseOrder;

public interface ProductService {
	
	List<PurchaseOrder> searchPurchaseOrderByCriteria(String poNumber);
	
	List<OrderItem> searchOrderItemsFromPurchaseOrder(PurchaseOrder purchaseOrder);
	
	List<PurchaseOrder> getAllPurchaseOrderCreated();
	
    List<PurchaseOrder> getPurchaseOrder(String poNumber , String productName,Supplier supplier);
    
    List<InventoryItem> getInventoryItemsByCriteria(String productName, Supplier supplier);
    
    BigDecimal getTotalFromInventory(Product product,String columnName);
    
    void reduceProductInventory(Product product, BigDecimal quantity);
    
    List<InventoryItem> getInventoryItemsGroupByProductSupplier(String productName, Supplier supplier);
    
    List<InventoryConsumptionAdjustment> getInventoryConsumptionAdjustment(String type,Date fromDate, Date endDate, String productName);

}
