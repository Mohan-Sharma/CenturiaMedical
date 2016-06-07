package com.nzion.view.component;

import java.math.BigDecimal;
import java.util.Map.Entry;
import java.util.Set;

import org.zkoss.zkplus.databind.TypeConverter;
import org.zkoss.zul.Row;

import com.nzion.domain.product.inventory.InventoryItem;

public class AtpTotalDisplayConverter implements TypeConverter{
	
	public Object coerceToBean(java.lang.Object val, org.zkoss.zk.ui.Component comp) {
		 return null;
		 }
	
		 public Object coerceToUi(java.lang.Object val,  final org.zkoss.zk.ui.Component comp) {
			String d = (String)val;
			BigDecimal totalVal = BigDecimal.ZERO;
			Object obj =  ((Row)comp.getParent()).getValue();
			if(obj != null){
			  if(obj instanceof Entry){
				  	@SuppressWarnings("unchecked")
					Set<InventoryItem>  invItems =  (Set<InventoryItem>)((Entry<String, InventoryItem>)obj).getValue();				  	
					  	for(InventoryItem invItem :  invItems)
					  		totalVal = totalVal.add(new BigDecimal(invItem.getAtp()==null?0:invItem.getAtp()));	
				  return totalVal.toString();
			  }					
					
			}
			
			return "";
		}	
		 
}
