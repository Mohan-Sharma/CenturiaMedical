package com.nzion.oms.zkoss;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.nzion.domain.product.inventory.InventoryConsumptionAdjustment;
import com.nzion.service.common.CommonCrudService;
import com.nzion.services.product.ProductService;
import com.nzion.util.UtilValidator;
import com.nzion.zkoss.composer.AutowirableComposer;

public class AdjConsumptionReportController extends AutowirableComposer{
	
	private CommonCrudService commonCrudService;
	
	private ProductService productService;
	
	private String type;
	
	private String productName;
	
	private Date fromDate = new Date();
	
	private Date thruDate = new Date();
	
	private List<InventoryConsumptionAdjustment> inventoryConsumptionAdjustments = new ArrayList<>();
	
	public AdjConsumptionReportController(){
		
	}
	
	public void searchByCriteria(){
		if(UtilValidator.isEmpty(type) && UtilValidator.isEmpty(productName) && UtilValidator.isEmpty(fromDate) && UtilValidator.isEmpty(thruDate)){
			return;
		}
		
		inventoryConsumptionAdjustments = productService.getInventoryConsumptionAdjustment(type, fromDate, thruDate, productName);
		
		
	}

	public CommonCrudService getCommonCrudService() {
		return commonCrudService;
	}

	public void setCommonCrudService(CommonCrudService commonCrudService) {
		this.commonCrudService = commonCrudService;
	}

	public ProductService getProductService() {
		return productService;
	}

	public void setProductService(ProductService productService) {
		this.productService = productService;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public Date getFromDate() {
		return fromDate;
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	public Date getThruDate() {
		return thruDate;
	}

	public void setThruDate(Date thruDate) {
		this.thruDate = thruDate;
	}

	public List<InventoryConsumptionAdjustment> getInventoryConsumptionAdjustments() {
		return inventoryConsumptionAdjustments;
	}

	public void setInventoryConsumptionAdjustments(List<InventoryConsumptionAdjustment> inventoryConsumptionAdjustments) {
		this.inventoryConsumptionAdjustments = inventoryConsumptionAdjustments;
	}
	
	
	
	
	

}
