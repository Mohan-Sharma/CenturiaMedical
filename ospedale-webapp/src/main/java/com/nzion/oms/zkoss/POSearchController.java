package com.nzion.oms.zkoss;

import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Label;

import com.nzion.domain.pms.Product;
import com.nzion.domain.product.order.PurchaseOrder;
import com.nzion.service.common.CommonCrudService;
import com.nzion.services.product.ProductService;
import com.nzion.util.UtilMessagesAndPopups;
import com.nzion.util.UtilValidator;
import com.nzion.zkoss.composer.AutowirableComposer;

/**
 * @author Nafis
 * Oct 7, 2011
 */
public class POSearchController extends AutowirableComposer {
	private static final long serialVersionUID = 1L;
	
	private CommonCrudService commonCrudService;
	private List<PurchaseOrder> purchaseOrders;
	private List<Product> products;
	private String poNumber;
	private String productCode;
	private ProductService productService;
	
	@Override
	public void doAfterCompose(Component component) throws Exception {
	super.doAfterCompose(component);
	products = commonCrudService.getAll(Product.class);
	}
	
	public void searchPurchaseOrders(Label noRecordFoundLabel){
		try{
			purchaseOrders = commonCrudService.findByEquality(PurchaseOrder.class, new String[]{"poNumber"}, new Object[]{poNumber}); 
		}catch(Exception e){
			UtilMessagesAndPopups.showError("Please provide exact PO Number");
		}
		noRecordFoundLabel.setVisible(UtilValidator.isEmpty(purchaseOrders));
	}
	
	public CommonCrudService getCommonCrudService() {
	return commonCrudService;
	}
	public List<PurchaseOrder> getPurchaseOrders() {
	return purchaseOrders;
	}

	public List<Product> getProducts() {
		return products;
	}

	public void setProducts(List<Product> products) {
		this.products = products;
	}

	public String getPoNumber() {
		return poNumber;
	}

	public void setPoNumber(String poNumber) {
		this.poNumber = poNumber;
	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public void setCommonCrudService(CommonCrudService commonCrudService) {
		this.commonCrudService = commonCrudService;
	}

	public void setPurchaseOrders(List<PurchaseOrder> purchaseOrders) {
		this.purchaseOrders = purchaseOrders;
	}

	public ProductService getProductService() {
		return productService;
	}

	public void setProductService(ProductService productService) {
		this.productService = productService;
	}
	
}
