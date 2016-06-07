package com.nzion.view.component;

import com.nzion.domain.drug.Drug;
import com.nzion.util.UtilDateTime;
import org.zkoss.zkplus.databind.TypeConverter;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Nth
 * Date: 1/30/13
 * Time: 10:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class DrugDisplayConverter  implements TypeConverter {


    public Object coerceToBean(java.lang.Object val, org.zkoss.zk.ui.Component comp) {
        return null;
    }

    public Object coerceToUi(java.lang.Object val, final org.zkoss.zk.ui.Component comp) {
        if(val==null)return null;
        Drug drug=(Drug)val;
        return drug.getGenericName()+" - "+drug.getTradeName();
    }
}
