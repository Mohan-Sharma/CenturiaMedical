package com.nzion.zkoss.composer.appointment;

import java.math.BigDecimal;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zk.ui.Component;

public class InvoiceCollectionConverter implements Converter{
	
	@Override
    public Object coerceToUi(Object val, Component component, BindContext ctx) {
        if(val==null)return null;
        StringBuilder buffer = new StringBuilder();
        if(val instanceof BigDecimal) {
            if(((BigDecimal) val).compareTo(BigDecimal.ZERO) == 0){
            	buffer.toString();
            }else{
            	buffer.append(val);
            	buffer.append(" KD ");
            }
        }
        return buffer.toString();
    }

    @Override
    public Object coerceToBean(Object compAttr, Component component, BindContext ctx) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
