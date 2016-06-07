package com.nzion.view.component;

import org.zkoss.zul.A;

/**
 * Created with IntelliJ IDEA.
 * User: USER
 * Date: 4/14/15
 * Time: 11:49 AM
 * To change this template use File | Settings | File Templates.
 */
public class AnchorTagCssConfig extends A {

    @Override
    public void setLabel(String label) {
        super.setLabel(label);    //To change body of overridden methods use File | Settings | File Templates.
        if("Save".equals(this.getLabel())){
            this.setSclass("btn btn-success");
            this.setZclass("btn btn-success");
        }else if( "Cancel".equals(this.getLabel()) || "Close".equals(this.getLabel()) ){
            this.setSclass("btn btn-danger");
            this.setZclass("btn btn-danger");
        }else if("Add".equals(this.getLabel())){
            this.setSclass("btn btn-default");
            this.setZclass("btn btn-default");
        }
    }
}
