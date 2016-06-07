package com.nzion.zkoss.dto;

import com.nzion.util.UtilValidator;

/**
 * Created with IntelliJ IDEA.
 * User: USER
 * Date: 4/27/15
 * Time: 11:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProviderFavoriteLabDto {

    private long providerLabId;

    private long labGroupId;

    private String name;

    private String css;

    public long getProviderLabId() {
        return providerLabId;
    }

    public void setProviderLabId(long providerLabId) {
        this.providerLabId = providerLabId;
    }

    public long getLabGroupId() {
        return labGroupId;
    }

    public void setLabGroupId(long labGroupId) {
        this.labGroupId = labGroupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCss() {
        if(getLabGroupId() != 0)
            return "group";
        else
            return "line";
    }

    public void setCss(String css) {
        this.css = css;
    }

}
