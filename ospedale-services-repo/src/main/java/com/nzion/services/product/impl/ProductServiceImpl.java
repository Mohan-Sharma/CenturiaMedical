package com.nzion.services.product.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;

import com.nzion.domain.pms.Product;
import com.nzion.domain.pms.Supplier;
import com.nzion.domain.product.inventory.InventoryConsumptionAdjustment;
import com.nzion.domain.product.inventory.InventoryItem;
import com.nzion.domain.product.order.OrderItem;
import com.nzion.domain.product.order.PurchaseOrder;
import com.nzion.repository.product.ProductRepository;
import com.nzion.service.common.CommonCrudService;
import com.nzion.services.product.ProductService;
import com.nzion.util.UtilValidator;

@Service("productService")
public class ProductServiceImpl implements ProductService{
	
	private CommonCrudService commonCrudService;
    private ProductRepository productRepository;
    
    @Resource
    @Required
	public void setProductRepository(ProductRepository productRepository) {
	this.productRepository = productRepository;
	}

	@Resource
    @Required
    public void setCommonCrudService(CommonCrudService commonCrudService) {
        this.commonCrudService = commonCrudService;
    }
	
	@Override
	public List<PurchaseOrder> searchPurchaseOrderByCriteria(String poNumber) {
	return productRepository.searchPurchaseOrderByCriteria(poNumber);
	}
	
	@Override
	public List<OrderItem> searchOrderItemsFromPurchaseOrder(PurchaseOrder purchaseOrder) {
	return productRepository.searchOrderItemsFromPurchaseOrder(purchaseOrder);
	}
	
	@Override
	public List<PurchaseOrder> getAllPurchaseOrderCreated() {
	return productRepository.getAllPurchaseOrderCreated();
	}
	
	@Override
	public List<PurchaseOrder> getPurchaseOrder(String poNumber, String productName, Supplier supplier) {
	return productRepository.getPurchaseOrder(poNumber, productName, supplier);
	}
	
	@Override
	public List<InventoryItem> getInventoryItemsByCriteria(String productName, Supplier supplier) {
	return productRepository.getInventoryItemsByCriteria(productName, supplier);
	}
	
	@Override
	public BigDecimal getTotalFromInventory(Product product, String coloumName) {
	return productRepository.getTotalFromInventory(product, coloumName);
	}
	
	@Override
	public void reduceProductInventory(Product product, BigDecimal quantity) {
	List<InventoryItem> item = productRepository.reduceProductInventory(product, quantity);
	if(UtilValidator.isNotEmpty(item))
		commonCrudService.save(item);
	}
	
	@Override
	public List<InventoryItem> getInventoryItemsGroupByProductSupplier(String productName, Supplier supplier){
		return productRepository.getInventoryItemsGroupByProductSupplier(productName, supplier);
	}

	@Override
	public List<InventoryConsumptionAdjustment> getInventoryConsumptionAdjustment(String type, Date fromDate, Date endDate, String productName) {
		return productRepository.getInventoryConsumptionAdjustment(type, fromDate, endDate, productName);
	}
}
