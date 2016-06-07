package com.nzion.zkoss.dto;

import com.nzion.util.UtilValidator;

/**
 * Created with IntelliJ IDEA.
 * User: USER
 * Date: 4/27/15
 * Time: 1:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProviderFavoriteDrugDto {

    private long providerDrugId;

    private long drugGroupId;

    private String name;

    private String css;

    public long getProviderDrugId() {
        return providerDrugId;
    }

    public void setProviderDrugId(long providerDrugId) {
        this.providerDrugId = providerDrugId;
    }

    public long getDrugGroupId() {
        return drugGroupId;
    }

    public void setDrugGroupId(long drugGroupId) {
        this.drugGroupId = drugGroupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCss() {
        if(getDrugGroupId() != 0)
            return "group";
        else
            return "line";
    }

    public void setCss(String css) {
        this.css = css;
    }
}
