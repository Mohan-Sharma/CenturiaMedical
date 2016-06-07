package com.nzion.view.component;

import com.nzion.domain.Schedule;
import com.nzion.util.UtilDateTime;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zk.ui.Component;
import org.zkoss.zkplus.databind.TypeConverter;

/**
 * Created by Mohan Sharma.
 */
public class DateTimeBindConverter implements Converter {

    @Override
    public Object coerceToUi(Object val, Component component, BindContext bindContext) {
        if(val==null)return "";
        Schedule schedule = (Schedule)val;
        return UtilDateTime.format(schedule.getStartDate()) + " " + UtilDateTime.format(schedule.getStartTime(), UtilDateTime.AM_PM_FORMATTER);

    }

    @Override
    public Object coerceToBean(Object o, Component component, BindContext bindContext) {
        return null;
    }
}
