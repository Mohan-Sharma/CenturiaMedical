package com.nzion.oms.zkoss;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nzion.domain.billing.InvoiceItem;
import com.nzion.domain.pms.Product;
import com.nzion.domain.pms.Supplier;
import com.nzion.domain.product.inventory.InventoryItem;
import com.nzion.repository.common.CommonCrudRepository;
import com.nzion.service.common.CommonCrudService;
import com.nzion.services.product.ProductService;
import com.nzion.util.UtilValidator;
import com.nzion.zkoss.composer.AutowirableComposer;

public class InventoryStockReportController extends AutowirableComposer{
private static final long serialVersionUID = 1L;
	
	private CommonCrudService commonCrudService;
	private InventoryItem inventoryItem;
	private List<InventoryItem> inventoryItems;
	private CommonCrudRepository commonCrudRepository;
	private Set<Product> products;
	private ProductService productService;
	private List<Map<String, Object>> groupInvItemList;
	private List<Supplier> suppliers;
	private String productName;
	private Supplier supplier;
	private Map<String, Set<InventoryItem>> inventoryItemsMap;
	
	private List<InventoryItem> invItemForReports = new ArrayList<InventoryItem>();
	

	public InventoryStockReportController(){
		suppliers = commonCrudRepository.getAll(Supplier.class); 
	}
	
	public void searchByCriteria(){
		inventoryItems = null;
		inventoryItemsMap = new HashMap<String, Set<InventoryItem>>();
		if(UtilValidator.isEmpty(productName) && supplier == null){
			return;
		}
		inventoryItems = productService.getInventoryItemsByCriteria(productName, supplier);
	    
		if(UtilValidator.isNotEmpty(inventoryItems)){		
			for(InventoryItem   invItm : inventoryItems){
				Product pd = invItm.getProduct();
				if(pd == null)
					continue;
				Set<InventoryItem> invItmTmp =  inventoryItemsMap.get(pd.getId().toString()+" - "+ pd.getTradeName());
				if(UtilValidator.isEmpty(invItmTmp))  invItmTmp = new HashSet<InventoryItem>();
				invItmTmp.add(invItm);
				inventoryItemsMap.put(pd.getId().toString()+" - "+ pd.getTradeName(),invItmTmp);
			}
			
		}
		
		BigDecimal totalVal = BigDecimal.ZERO;
		
	  	for(String key : inventoryItemsMap.keySet()){
	  	   Set<InventoryItem> invItemSet = inventoryItemsMap.get(key);
	  	   InventoryItem invItem = new InventoryItem();
	  	   for( InventoryItem inv : invItemSet){
	  		   	invItem.setProduct(inv.getProduct());
		  		totalVal = totalVal.add(new BigDecimal(inv.getQoh()==null?0:inv.getQoh()));
	  	   }
	  	   invItem.setManufactureDate(new Date());
	  	   invItem.setQoh(totalVal.intValue());
	  	   invItemForReports.add(invItem);
	  	}
	}
	
	public void searchByGroup(){
	if(products == null)
		products = new HashSet<Product>();
		inventoryItems = commonCrudRepository.getAll(InventoryItem.class);
		for(InventoryItem invItem : inventoryItems){
			products.add(invItem.getProduct());
		}
	}
	
	public List<Map<String, Object>> getTotalInventory(Product product){
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		Map<String, Object> gropuInvItemSet = new HashMap<String, Object>();
		gropuInvItemSet.put("qoh", productService.getTotalFromInventory(product, "qoh"));
		gropuInvItemSet.put("atp", productService.getTotalFromInventory(product, "atp"));
		list.add(gropuInvItemSet);
		return list;
		
	}
	
	public List<InventoryItem> getInventoryItems() {
	return inventoryItems;
	}
	public void setInventoryItems(List<InventoryItem> inventoryItems) {
	this.inventoryItems = inventoryItems;
	}
	public InventoryItem getInventoryItem() {
	return inventoryItem;
	}
	public void setInventoryItem(InventoryItem inventoryItem) {
	this.inventoryItem = inventoryItem;
	}
	public CommonCrudService getCommonCrudService() {
	return commonCrudService;
	}
	public void setCommonCrudService(CommonCrudService commonCrudService) {
	this.commonCrudService = commonCrudService;
	}
	public CommonCrudRepository getCommonCrudRepository() {
	return commonCrudRepository;
	}
	public void setCommonCrudRepository(CommonCrudRepository commonCrudRepository) {
	this.commonCrudRepository = commonCrudRepository;
	}
	public Set<Product> getProducts() {
		return products;
	}
	public void setProducts(Set<Product> products) {
		this.products = products;
	}
	public ProductService getProductService() {
	return productService;
	}
	public void setProductService(ProductService productService) {
	this.productService = productService;
	}
	public List<Map<String, Object>> getGroupInvItemList() {
	return groupInvItemList;
	}
	public void setGroupInvItemList(List<Map<String, Object>> groupInvItemList) {
	this.groupInvItemList = groupInvItemList;
	}
	public List<Supplier> getSuppliers() {
	return suppliers;
	}
	public void setSuppliers(List<Supplier> suppliers) {
	this.suppliers = suppliers;
	}

	public String getProductName() {
	return productName;
	}

	public void setProductName(String productName) {
	this.productName = productName;
	}

	public Supplier getSupplier() {
	return supplier;
	}

	public void setSupplier(Supplier supplier) {
	this.supplier = supplier;
	}
	public Map<String, Set<InventoryItem>> getInventoryItemsMap() {
		return inventoryItemsMap;
	}

	public List<InventoryItem> getInvItemForReports() {
		return invItemForReports;
	}
	
	
}
