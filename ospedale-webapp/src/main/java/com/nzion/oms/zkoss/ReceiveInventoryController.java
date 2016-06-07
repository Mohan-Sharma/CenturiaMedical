/**
 * 
 */
package com.nzion.oms.zkoss;

import java.math.BigDecimal;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zkplus.databind.TypeConverter;
import org.zkoss.zul.Listcell;

import com.nzion.domain.pms.Supplier;
import com.nzion.domain.product.inventory.InventoryItem;
import com.nzion.domain.product.inventory.InventoryItem.POSTATUS;
import com.nzion.domain.product.order.PurchaseOrder;
import com.nzion.repository.common.CommonCrudRepository;
import com.nzion.service.common.CommonCrudService;
import com.nzion.services.product.ProductService;
import com.nzion.util.UtilMessagesAndPopups;
import com.nzion.util.UtilValidator;
import com.nzion.zkoss.composer.AutowirableComposer;

/**
 * @author Nafis
 *
 * Sep 8, 2011
 */
public class ReceiveInventoryController extends AutowirableComposer implements TypeConverter{
	private static final long serialVersionUID = 1L;
	
	private CommonCrudService commonCrudService;
	private CommonCrudRepository commonCrudRepository;
	private List<InventoryItem> inventoryItems;
	private List<PurchaseOrder> purchaseOrders;
	private ProductService productService;
	private List<Supplier> suppliers;
	private Supplier supplier;
	private String productName;
	private String poCode;
	
	@Override
	public void doAfterCompose(Component component) throws Exception {
	super.doAfterCompose(component);
		purchaseOrders = productService.getAllPurchaseOrderCreated();
		suppliers = commonCrudService.getAll(Supplier.class);
	}
	
	public void searchByCriteria(){
		purchaseOrders = null;
		if(supplier == null && UtilValidator.isEmpty(productName) && UtilValidator.isEmpty(poCode)){
			return;
		}
		purchaseOrders = productService.getPurchaseOrder(poCode, productName, supplier);
	}
	
	public void receiveProduct(InventoryItem inventoryItem){
	inventoryItem.setQoh(inventoryItem.getAtp());
	inventoryItem.setPoStatus(POSTATUS.PO_RECEIVED);
	commonCrudService.save(inventoryItem);
	UtilMessagesAndPopups.showSuccess();
	}
	
	public CommonCrudService getCommonCrudService() {
	return commonCrudService;
	}
	public void setCommonCrudService(CommonCrudService commonCrudService) {
	this.commonCrudService = commonCrudService;
	}
	public List<InventoryItem> getInventoryItems() {
	return inventoryItems;
	}
	public void setInventoryItems(List<InventoryItem> inventoryItems) {
	this.inventoryItems = inventoryItems;
	}
	public CommonCrudRepository getCommonCrudRepository() {
	return commonCrudRepository;
	}
	public void setCommonCrudRepository(CommonCrudRepository commonCrudRepository) {
	this.commonCrudRepository = commonCrudRepository;
	}
	public ProductService getProductService() {
	return productService;
	}
	public void setProductService(ProductService productService) {
	this.productService = productService;
	}
	public List<PurchaseOrder> getPurchaseOrders() {
	return purchaseOrders;
	}
	public void setPurchaseOrders(List<PurchaseOrder> purchaseOrders) {
	this.purchaseOrders = purchaseOrders;
	}
	public List<Supplier> getSuppliers() {
	return suppliers;
	}
	public void setSuppliers(List<Supplier> suppliers) {
	this.suppliers = suppliers;
	}
	public Supplier getSupplier() {
	return supplier;
	}
	public void setSupplier(Supplier supplier) {
	this.supplier = supplier;
	}
	public String getProductName() {
	return productName;
	}
	public void setProductName(String productName) {
	this.productName = productName;
	}
	public String getPoCode() {
	return poCode;
	}
	public void setPoCode(String poCode) {
	this.poCode = poCode;
	}

	@Override
	public Object coerceToUi(Object val, Component comp) {
	InventoryItem invItem = (InventoryItem) val;
	if(invItem.getUnitCost() != null && invItem.getQoh() != null)
	((Listcell)comp).setLabel(invItem.getUnitCost().getAmount().multiply(BigDecimal.valueOf(invItem.getQoh())).toPlainString() + " " + invItem.getUnitCost().getCurrency());
	return null;
	}

	@Override
	public Object coerceToBean(Object val, Component comp) {
	return null;
	}
	
	
}
