package com.nzion.view.component;

import com.nzion.domain.Schedule;
import com.nzion.util.UtilDateTime;
import org.zkoss.zkplus.databind.TypeConverter;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: USER
 * Date: 4/30/15
 * Time: 5:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class DateTimeConverter implements TypeConverter {

    public Object coerceToBean(java.lang.Object val, org.zkoss.zk.ui.Component comp) {
        return null;
    }

    public Object coerceToUi(java.lang.Object val, final org.zkoss.zk.ui.Component comp) {
        if(val==null)return "";
        Schedule schedule = (Schedule)val;
        return UtilDateTime.format(schedule.getStartDate()) + " " + UtilDateTime.format(schedule.getStartTime(), UtilDateTime.AM_PM_FORMATTER);
    }
}
