/**
 * 
 */
package com.nzion.oms.zkoss;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.zkoss.zk.ui.Component;
import org.zkoss.zkplus.databind.TypeConverter;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Include;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;

import com.nzion.domain.emr.UnitOfMeasurement;
import com.nzion.domain.pms.Product;
import com.nzion.domain.pms.Supplier;
import com.nzion.domain.product.common.Money;
import com.nzion.domain.product.inventory.InventoryItem;
import com.nzion.domain.product.inventory.InventoryItem.POSTATUS;
import com.nzion.domain.product.inventory.InventoryItem.STATUS;
import com.nzion.domain.product.order.OrderItem;
import com.nzion.domain.product.order.PurchaseOrder;
import com.nzion.repository.common.CommonCrudRepository;
import com.nzion.service.common.CommonCrudService;
import com.nzion.util.Infrastructure;
import com.nzion.util.UtilMessagesAndPopups;
import com.nzion.util.UtilValidator;
import com.nzion.zkoss.composer.AutowirableComposer;
import com.nzion.zkoss.ext.Navigation;

/**
 * @author Nafis
 *
 * Sep 7, 2011
 */
public class PurchaseOrderController extends AutowirableComposer implements TypeConverter{

	private static final long serialVersionUID = 1L;

	private CommonCrudService commonCrudService;
	private InventoryItem inventoryItem;
	private List<Supplier> suppliers;
	private List<Product> products;
	private CommonCrudRepository commonCrudRepository;
	private List<InventoryItem> inventoryItems;
	private PurchaseOrder purchaseOrder;
	private List<UnitOfMeasurement> unitOfMeasurements;

	@Override
	public void doAfterCompose(Component component) throws Exception {
	inventoryItem = new InventoryItem();
	inventoryItem.setUnitCost(new Money(BigDecimal.ZERO));
	suppliers = commonCrudService.getAll(Supplier.class);
	unitOfMeasurements = commonCrudRepository.findByEquality(UnitOfMeasurement.class,new String[]{"uomType"},new Object[]{"PRODUCT"});
	purchaseOrder = new PurchaseOrder();
	super.doAfterCompose(component);
	}
	
	@SuppressWarnings("unchecked")
	public void save(Listbox listboxPODetails) {
		Set<Listitem> listItem = listboxPODetails.getSelectedItems();
		if (UtilValidator.isEmpty(listItem)) {
			UtilMessagesAndPopups.showError("Please select atleast 1 Product.");
			return;
		}
		List<InventoryItem> invItems = new ArrayList<InventoryItem>();
		Set<OrderItem> orderItems = new HashSet<OrderItem>();
		setInventoryItemsAndOrderItems(invItems, listItem, orderItems);
		setPurchaseOrderAndSave(invItems, orderItems);
		UtilMessagesAndPopups.showMessage("Purchase Order created successfully;needs to receive.");
		Navigation.navigate("receiveInventory", null, "contentArea");
	}
	
	public void setInventoryItemsAndOrderItems(List<InventoryItem> invItems , Set<Listitem> listItem , Set<OrderItem> orderItems){
		for(Listitem item : listItem){
			InventoryItem invItem = (InventoryItem) item.getValue();
			checkListItemChildren(item);
			invItem.setLocation(Infrastructure.getSelectedLocation());
			invItems.add(invItem);
			OrderItem orderItem = new OrderItem();
			orderItem.setProduct(invItem.getProduct());
			orderItem.setQuantity(invItem.getAtp());
			orderItem.setUnitPrice(invItem.getUnitCost());
			orderItem.addTotalPrice();
			orderItems.add(orderItem);
		}
	}
	
	private void checkListItemChildren(Listitem item){
	
		for(Object lc : item.getChildren()){
			for(Object comp : ((Listcell) lc).getChildren() ){
				if(comp instanceof Intbox){
					((Intbox) comp).getValue();
				}else if(comp instanceof Decimalbox){
					((Decimalbox) comp).getValue();
				}else if(comp instanceof Combobox){
					((Combobox) comp).setConstraint("no empty");
					((Combobox) comp).getValue();
				}
			}
		}
		
	}
	
	public void setPurchaseOrderAndSave(List<InventoryItem> invItems , Set<OrderItem> orderItems){
		purchaseOrder.setOrderItems(orderItems);
		purchaseOrder.setLocation(Infrastructure.getSelectedLocation());
		purchaseOrder.setUserLogin(Infrastructure.getUserLogin());
		purchaseOrder.setInventoryItems(invItems);
		purchaseOrder.setOrderStatus(PurchaseOrder.ORDERSTATUS.ORDER_CREATED);
		commonCrudService.save(purchaseOrder);
	}
	

	public void getProductsAssociateSupplier(Include productDetailsInc) {
	products = commonCrudService.getAll(Product.class);//productService.getProductsFromProductSupplier(inventoryItem.getSupplier());
	setInventoryItems();
	productDetailsInc.invalidate();
	productDetailsInc.setDynamicProperty("poController", this);
	productDetailsInc.setSrc("/oms/product/purchase-order-product-details.zul");
	}
	
	public void setInventoryItems(){
		if(inventoryItems == null)
			inventoryItems = new ArrayList<InventoryItem>();
		inventoryItems.clear();
		for(Product product : products){
			InventoryItem item = new InventoryItem();
			item.setProduct(product);
			item.setStatus(STATUS.READY);
			item.setPoStatus(POSTATUS.PO_CREATED);
			item.setSupplier(inventoryItem.getSupplier());
			Money amount = new Money(BigDecimal.ZERO);
			item.setUnitCost(amount);
			item.setPurchaseUom(product.getBaseUom());
			inventoryItems.add(item);
		}
	}
	
	public List<UnitOfMeasurement> getUnitOfMeasurements() {
	return unitOfMeasurements;
	}

	public void setUnitOfMeasurements(List<UnitOfMeasurement> unitOfMeasurements) {
	this.unitOfMeasurements = unitOfMeasurements;
	}

	public PurchaseOrder getPurchaseOrder() {
	return purchaseOrder;
	}

	public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
	this.purchaseOrder = purchaseOrder;
	}

	public List<InventoryItem> getInventoryItems() {
	if (inventoryItems == null) inventoryItems = new ArrayList<InventoryItem>();
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

	public CommonCrudService getCommonCrudService() {
	return commonCrudService;
	}

	public void setCommonCrudService(CommonCrudService commonCrudService) {
	this.commonCrudService = commonCrudService;
	}

	public InventoryItem getInventoryItem() {
	return inventoryItem;
	}

	public void setInventoryItem(InventoryItem inventoryItem) {
	this.inventoryItem = inventoryItem;
	}

	public List<Supplier> getSuppliers() {
	return suppliers;
	}

	public void setSuppliers(List<Supplier> suppliers) {
	this.suppliers = suppliers;
	}

	public List<Product> getProducts() {
	return products;
	}

	public void setProducts(List<Product> products) {
	this.products = products;
	}
	
	@Override
	public Object coerceToUi(Object arg0, Component arg1) {
	InventoryItem invItem = (InventoryItem) arg0;
	Combobox combobox = (Combobox) arg1;
	/*if(invItem.getProduct() != null && UtilValidator.isNotEmpty(invItem.getProduct().getBaseUom()))
	combobox.setModel(new BindingListModelList(new ArrayList<UnitOfMeasurement>(invItem.getProduct().getBaseUom()), false));*/
	return null;
	}
	
	@Override
	public Object coerceToBean(Object arg0, Component arg1) {
	// TODO Auto-generated method stub
	return null;
	}
	
}