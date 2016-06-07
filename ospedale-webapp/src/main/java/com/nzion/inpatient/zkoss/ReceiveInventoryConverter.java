package com.nzion.inpatient.zkoss;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zkplus.databind.TypeConverter;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;

import com.nzion.domain.product.order.PurchaseOrder;
import com.nzion.util.UtilMisc;
import com.nzion.zkoss.ext.Navigation;

public class ReceiveInventoryConverter implements TypeConverter{
	@Override
	public Object coerceToUi(Object val, Component comp) {
	final PurchaseOrder purchaseOrder = (PurchaseOrder) val;
	if(comp instanceof Button){
	Button linkBtn = (Button) comp;
	linkBtn.setVisible(PurchaseOrder.ORDERSTATUS.ORDER_CREATED.equals(purchaseOrder.getOrderStatus()));
		linkBtn.addEventListener("onClick", new EventListener() {
			@Override
			public void onEvent(Event arg0) throws Exception {
				Navigation.navigate("receivePurchaseItems", UtilMisc.toMap("purchaseOrder",purchaseOrder),"contentArea");
			}
		});
	}else if(comp instanceof Label){
		comp.setVisible(!PurchaseOrder.ORDERSTATUS.ORDER_CREATED.equals(purchaseOrder.getOrderStatus()));
	}
	return null;
	}

	@Override
	public Object coerceToBean(Object val, Component comp) {
	// TODO Auto-generated method stub
	return null;
	}
}
