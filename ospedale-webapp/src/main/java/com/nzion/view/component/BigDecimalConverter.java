package com.nzion.view.component;

import org.zkoss.zk.ui.Component;
import org.zkoss.zkplus.databind.TypeConverter;
import org.zkoss.zul.Label;

import java.math.BigDecimal;

/**
 * Created by Mohan Sharma on 5/6/2015.
 */
public class BigDecimalConverter implements TypeConverter {
    @Override
    public Object coerceToUi(Object val, Component comp) {
        BigDecimal retVal=BigDecimal.ZERO;
        if(val != null){
            retVal=new BigDecimal(val.toString());
        }
        return retVal.setScale(3).toPlainString();
    }

    @Override
    public Object coerceToBean(Object val, Component comp) {
        return null;
    }
}
