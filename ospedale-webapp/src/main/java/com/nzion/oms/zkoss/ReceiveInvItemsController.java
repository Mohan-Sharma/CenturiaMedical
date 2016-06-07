package com.nzion.oms.zkoss;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zkplus.databind.TypeConverter;
import org.zkoss.zul.Datebox;

import com.nzion.domain.product.inventory.InventoryItem;
import com.nzion.domain.product.order.PurchaseOrder;
import com.nzion.service.common.CommonCrudService;
import com.nzion.services.product.ProductService;
import com.nzion.util.UtilMessagesAndPopups;
import com.nzion.util.UtilValidator;
import com.nzion.zkoss.composer.AutowirableComposer;
import com.nzion.zkoss.ext.Navigation;

/**
 * @author Nafis
 * Oct 17, 2011
 */
public class ReceiveInvItemsController extends AutowirableComposer implements TypeConverter{
	private static final long serialVersionUID = 1L;
	
	private PurchaseOrder purchaseOrder;
	private List<InventoryItem> inventoryItems;
	private CommonCrudService commonCrudService;
	private ProductService productService;
	
	public ProductService getProductService() {
		return productService;
	}

	public void setProductService(ProductService productService) {
		this.productService = productService;
	}

	public ReceiveInvItemsController(){}
	
	public ReceiveInvItemsController(PurchaseOrder poOrder){
		if(poOrder != null){
			purchaseOrder = poOrder;
			commonCrudService.refreshEntity(purchaseOrder);
			inventoryItems = purchaseOrder.getInventoryItems();
		}
	}
	
	@Override
	public void doAfterCompose(Component component) throws Exception {
	super.doAfterCompose(component);
	}
	
	public void calculateDischargeQuantity(InventoryItem invItem){
		 if(UtilValidator.isEmpty(invItem.getQoh()))
			invItem.setQoh(new Integer(0));
		 if(UtilValidator.isEmpty(invItem.getAtp()))
			invItem.setAtp(new Integer(0));
		 if(invItem.getQoh().compareTo(invItem.getAtp()) < 0){
			 invItem.setRejectedQuantity(invItem.getAtp() - invItem.getQoh());
			 purchaseOrder.setOrderStatus(PurchaseOrder.ORDERSTATUS.ORDER_RECEIVED);
			 invItem.setPoStatus(InventoryItem.POSTATUS.PO_REJECTED);
		 }else{
			 invItem.setRejectedQuantity(new Integer(0));
			 purchaseOrder.setOrderStatus(PurchaseOrder.ORDERSTATUS.ORDER_RECEIVED);
			 invItem.setPoStatus(InventoryItem.POSTATUS.PO_RECEIVED);
		 }
	}
	
	public void saveInventoryItem(){
		List<InventoryItem> inventoryItemList = new ArrayList<InventoryItem>();
		for(InventoryItem invItem : inventoryItems){
			if(invItem.getFreeQty() != null)
				invItem.setQoh(invItem.getQoh() + invItem.getFreeQty());
			if(!invItem.getSerialized()){
				invItem.setAtp(invItem.getQoh());
				inventoryItemList.add(invItem);
			}else{
				Integer totalInvCreate = invItem.getAtp();
				Integer rejectedQTY = invItem.getRejectedQuantity();
				
				invItem.setAtp(1);
				invItem.setQoh(1);
				if(rejectedQTY > 0)
					invItem.setRejectedQuantity(1);
				inventoryItemList.add(invItem);
				
				if(invItem.getRejectedQuantity() > 0){
					totalInvCreate = totalInvCreate - rejectedQTY;
					/*for(int i = 1; i <= rejectedQTY; i++){
						InventoryItem rejectedInventoryItem = new InventoryItem(invItem);
						rejectedInventoryItem.setAtp(1);
						rejectedInventoryItem.setQoh(1);
						inventoryItemList.add(rejectedInventoryItem);
					}*/
				}
				
				if(totalInvCreate > 0){
					invItem.setAtp(invItem.getQoh());
					invItem.setRejectedQuantity(0);
					invItem.setRejectionReason(null);
					invItem.setPoStatus(InventoryItem.POSTATUS.PO_RECEIVED);
					for(int i = 1; i < totalInvCreate; i++){
						InventoryItem inItem = new InventoryItem(invItem);
						inItem.setAtp(1);
						inItem.setQoh(1);
						inventoryItemList.add(inItem);
					}
				}
				
			}
		}
		purchaseOrder.setInventoryItems(inventoryItemList);
		purchaseOrder.setOrderStatus(PurchaseOrder.ORDERSTATUS.ORDER_RECEIVED);
		commonCrudService.save(purchaseOrder);
		UtilMessagesAndPopups.showSuccess();
		Navigation.navigate("receiveInventory", null,"contentArea");
	}
	
	public PurchaseOrder getPurchaseOrder() {
	return purchaseOrder;
	}
	public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
	this.purchaseOrder = purchaseOrder;
	}
	public List<InventoryItem> getInventoryItems() {
	return inventoryItems;
	}
	public void setInventoryItems(List<InventoryItem> inventoryItems) {
	this.inventoryItems = inventoryItems;
	}
	public void setCommonCrudService(CommonCrudService commonCrudService) {
	this.commonCrudService = commonCrudService;
	}

	@Override
	public Object coerceToUi(Object val, Component comp) {
	if(comp instanceof Datebox)
		setDateboxEnableDisable((InventoryItem)val,(Datebox)comp);
	return null;
	}

	private void setDateboxEnableDisable(InventoryItem val,Datebox comp) {
		//comp.setDisabled(!val.getProductDetails().getPerishable());
		//if(val.getProductDetails().getPerishable())
			comp.setConstraint("no empty,no past");
	}

	@Override
	public Object coerceToBean(Object val, Component comp) {
	return null;
	}
	
	
	

}
