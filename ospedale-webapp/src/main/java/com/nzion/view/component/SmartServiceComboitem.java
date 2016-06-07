package com.nzion.view.component;

import org.zkoss.zul.Comboitem;

/**
 * Created by User on 10/13/2015.
 */
public class SmartServiceComboitem extends Comboitem{

    private static final long serialVersionUID = 1L;

    @Override
    public void setLabel(String label) {
        if(label.equals("Tele Consultation Visit")) {
            super.setLabel("Tele Consultation");
        }else if(label.equals("Premium Visit")){
            super.setLabel("Premium Appointment");
        }else{
            super.setLabel(label);
        }
    }
}
