package com.nzion.zkoss.dto;

import com.nzion.util.UtilValidator;

/**
 * Created with IntelliJ IDEA.
 * User: USER
 * Date: 4/28/15
 * Time: 9:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class ProviderFavoriteProcedureDto {

    private long providerProcedureId;

    private long procedureGroupId;

    private String name;

    private String css;

    public long getProviderProcedureId() {
        return providerProcedureId;
    }

    public void setProviderProcedureId(long providerProcedureId) {
        this.providerProcedureId = providerProcedureId;
    }

    public long getProcedureGroupId() {
        return procedureGroupId;
    }

    public void setProcedureGroupId(long procedureGroupId) {
        this.procedureGroupId = procedureGroupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCss() {
        if(getProcedureGroupId() != 0)
            return "group";
        else
            return "line";
    }

    public void setCss(String css) {
        this.css = css;
    }
}
