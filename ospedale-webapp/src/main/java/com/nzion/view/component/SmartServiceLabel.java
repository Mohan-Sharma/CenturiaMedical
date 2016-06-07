package com.nzion.view.component;

import org.zkoss.zul.Label;

/**
 * Created by User on 10/13/2015.
 */
public class SmartServiceLabel extends Label {

    public SmartServiceLabel(){
        super();
    }

    public SmartServiceLabel(String value){
        super(value);
    }

    private static final long serialVersionUID = 1L;

    @Override
    public void setValue(String value) {
        if(value.equals("Tele Consultation Visit")) {
            super.setValue("Tele Consultation");
        }else if(value.equals("Premium Visit")){
            super.setValue("Premium Appointment");
        }else {
            super.setValue(value);
        }
    }
}
