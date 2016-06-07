package com.nzion.zkoss.composer.appointment;

import com.nzion.domain.CalendarSlot;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zk.ui.Component;

import java.text.SimpleDateFormat;

/**
 * Created with IntelliJ IDEA.
 * User: USER
 * Date: 4/26/15
 * Time: 11:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class PatientSlotConverter implements Converter{

    @Override
    public Object coerceToUi(Object val, Component component, BindContext ctx) {
        if(val==null)return null;
        StringBuilder buffer = new StringBuilder();
        if(val instanceof CalendarSlot) {
            CalendarSlot slot = (CalendarSlot)val;
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm a");
            buffer.append(sdf.format(slot.getStartTime()));
            buffer.append(" - ");
            buffer.append(sdf.format(slot.getEndTime()));
        }
        return buffer.toString();
    }

    @Override
    public Object coerceToBean(Object compAttr, Component component, BindContext ctx) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
