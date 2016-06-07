package com.nzion.view.component;

import org.zkoss.zul.Button;

/**
 * Created with IntelliJ IDEA.
 * User: USER
 * Date: 4/13/15
 * Time: 6:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class ButtonCssConfig extends Button{

    public ButtonCssConfig(){
        super();
    }

    @Override
    public void setSclass(String sclass) {
        super.setSclass(sclass);
    }

    @Override
    public void setLabel(String label) {
        super.setLabel(label);    //To change body of overridden methods use File | Settings | File Templates.
        if("Save".equals(this.getLabel())){
            this.setSclass("btn btn-success");
            this.setZclass("btn btn-success");
        }else if("Cancel".equals(this.getLabel()) || "Close".equals(this.getLabel()) ){
            this.setSclass("btn btn-danger");
            this.setZclass("btn btn-danger");
        }else if("Add".equals(this.getLabel())){
            this.setSclass("btn btn-default");
            this.setZclass("btn btn-default");
        }
    }
}
