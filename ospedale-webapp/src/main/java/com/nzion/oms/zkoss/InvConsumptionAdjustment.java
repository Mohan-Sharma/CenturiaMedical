package com.nzion.oms.zkoss;

import com.nzion.domain.Patient;
import com.nzion.domain.pms.Product;
import com.nzion.domain.product.inventory.InventoryConsumptionAdjustment;
import com.nzion.domain.product.inventory.InventoryItem;
import com.nzion.service.common.CommonCrudService;
import com.nzion.services.product.ProductService;
import com.nzion.util.Infrastructure;
import com.nzion.util.UtilMessagesAndPopups;
import com.nzion.util.UtilValidator;
import com.nzion.zkoss.composer.AutowirableComposer;
import com.nzion.zkoss.ext.Navigation;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class InvConsumptionAdjustment extends AutowirableComposer{
	private static final long serialVersionUID = 1L;
	
	private List<Product> products;
	
	private List<InventoryConsumptionAdjustment> inventoryConsumptionAdjustments = new ArrayList<InventoryConsumptionAdjustment>();
	
	private CommonCrudService commonCrudService;
	
	private List<Patient> patients;
	private String batchNumber;
	
	private ProductService productService;
	
	@Override
	public void doAfterCompose(Component component) throws Exception {
	super.doAfterCompose(component);

	products = commonCrudService.getAll(Product.class);
	for(Product prd : products){
		InventoryConsumptionAdjustment inventoryConsumptionAdjustment = new InventoryConsumptionAdjustment();
		inventoryConsumptionAdjustment.setProduct(prd);
		inventoryConsumptionAdjustments.add(inventoryConsumptionAdjustment);
	}
	patients = commonCrudService.getAll(Patient.class);
	
	productService = Infrastructure.getSpringBean("productService");
	
	}
	
	public void changeStatus(Listitem item){
		List<Component> listCellComps = item.getChildren();
		InventoryConsumptionAdjustment inventoryConsumptionAdjustment = item.getValue();
		int count = 1;
		for(Component listCellComp : listCellComps){
			Listcell listCell = (Listcell) listCellComp;
			inventoryConsumptionAdjustment.setInventoryItemsTransient(commonCrudService.findByEquality(InventoryItem.class, new String[]{"product"}, new Object[]{inventoryConsumptionAdjustment.getProduct()}));
			
			if("Consumption".equals(inventoryConsumptionAdjustment.getType())){
				Component comp = listCell.getFirstChild();
				
				if(comp instanceof Intbox){
					((Intbox)listCell.getFirstChild()).setConstraint("no negative");
				}
				if(comp instanceof Combobox  && count == 3){
					((Combobox) comp).setSelectedIndex(0);
					((Combobox) comp).setDisabled(true);
				}
				
				if(comp instanceof Combobox && count == 5 ){
					((Combobox) comp).setValue(" ");
					((Combobox) comp).setConstraint("");
					((Combobox) comp).setDisabled(true);
				}
				
				if(comp instanceof Combobox && count == 6){
					((Combobox) comp).setConstraint("");
					if(UtilValidator.isNotEmpty( ((Combobox) comp).getModel() ) ){
						((Combobox) comp).setDisabled(false);
						((Combobox) comp).setSelectedItem(null);
					}
				}
				
			}
			if("Adjustment".equals(inventoryConsumptionAdjustment.getType())){
				Component comp = listCell.getFirstChild();
				if(comp instanceof Intbox){
					((Intbox)listCell.getFirstChild()).setConstraint("no negative");
				}
				if(comp instanceof Combobox && count == 3 ){
					((Combobox) comp).setSelectedIndex(0);
					((Combobox) comp).setDisabled(false);
				}
				
				if(comp instanceof Combobox && count == 5 ){
					((Combobox) comp).setValue(" ");
					//((Combobox) comp).setConstraint("no empty:Batch No. Required");
					((Combobox) comp).setDisabled(false);
					Events.postEvent("onReload",((Combobox) comp),null);
				}
				
				if(comp instanceof Combobox && count == 6){
					((Combobox) comp).setConstraint("");
					if(UtilValidator.isNotEmpty( ((Combobox) comp).getModel() ) ){
						((Combobox) comp).setDisabled(true);
						((Combobox) comp).setSelectedItem(null);
					}
				}

			}
			
			count ++;
		}
		
	}
	
	public void saveItems(){
		for(InventoryConsumptionAdjustment inventoryConsumptionAdjustment : inventoryConsumptionAdjustments ){
			if(UtilValidator.isNotEmpty(inventoryConsumptionAdjustment.getQuantity())){
				commonCrudService.save(inventoryConsumptionAdjustment);
				if("Consumption".equals(inventoryConsumptionAdjustment.getType())){
					productService.reduceProductInventory(inventoryConsumptionAdjustment.getProduct(),new BigDecimal(inventoryConsumptionAdjustment.getQuantity()));
					Navigation.navigate("invConsumptionAdjustment", null,"contentArea");
					UtilMessagesAndPopups.showSuccess();
				} else if("Adjustment".equals(inventoryConsumptionAdjustment.getType())){
					try {
						List<InventoryItem> invItems = commonCrudService.findByEquality(InventoryItem.class, new String[]{"batchNumber"}, new Object[]{inventoryConsumptionAdjustment.getBatchNumber()});
						if (!invItems.isEmpty()) {
							if ("REDUSE_QUANTITY".equals(inventoryConsumptionAdjustment.getQuantityAction())) {
								InventoryItem invItem = invItems.get(0);
								invItem.setAtp(invItem.getAtp() - inventoryConsumptionAdjustment.getQuantity());
								invItem.setQoh(invItem.getQoh() - inventoryConsumptionAdjustment.getQuantity());
								invItem.setComments(inventoryConsumptionAdjustment.getNote());
								commonCrudService.save(invItem);
							} else if (inventoryConsumptionAdjustment.getQuantity() > 0) {
								InventoryItem invItem = invItems.get(0);
								invItem.setAtp(invItem.getAtp() + inventoryConsumptionAdjustment.getQuantity());
								invItem.setQoh(invItem.getQoh() + inventoryConsumptionAdjustment.getQuantity());
								invItem.setComments(inventoryConsumptionAdjustment.getNote());
								commonCrudService.save(invItem);
							}
							UtilMessagesAndPopups.showSuccess();
						}
					} catch (Exception e) {
						Navigation.navigate("invConsumptionAdjustment", null,"contentArea");
						UtilMessagesAndPopups.showError("Please select Batch Number");
					}
				} else {
					Navigation.navigate("invConsumptionAdjustment", null, "contentArea");
					UtilMessagesAndPopups.showSuccess();
				}
			}
		}
	}

	public List<Product> getProducts() {
		return products;
	}

	public void setProducts(List<Product> products) {
		this.products = products;
	}

	public CommonCrudService getCommonCrudService() {
		return commonCrudService;
	}

	public void setCommonCrudService(CommonCrudService commonCrudService) {
		this.commonCrudService = commonCrudService;
	}

	public List<InventoryConsumptionAdjustment> getInventoryConsumptionAdjustments() {
		return inventoryConsumptionAdjustments;
	}

	public void setInventoryConsumptionAdjustments(List<InventoryConsumptionAdjustment> inventoryConsumptionAdjustments) {
		this.inventoryConsumptionAdjustments = inventoryConsumptionAdjustments;
	}

	public List<Patient> getPatients() {
		return patients;
	}

	public void setPatients(List<Patient> patients) {
		this.patients = patients;
	}

	public String getBatchNumber() {
		return batchNumber;
	}

	public void setBatchNumber(String batchNumber) {
		this.batchNumber = batchNumber;
	}
}
