package com.nzion.external;

import com.nzion.domain.emr.soap.PatientRx;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by pradyumna on 04-04-2015.
 */
public class PrescriptionLineItem {
    private String tradeName;
    private BigDecimal quantity;
    private String details;
    private boolean homeService;
    private String genericName;
    private String drugCategory;

    public PrescriptionLineItem() {
    }

    public String getDrugCategory() {
        return drugCategory;
    }

    public void setDrugCategory(String drugCategory) {
        this.drugCategory = drugCategory;
    }

    public String getGenericName() {
        return genericName;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public String getTradeName() {
        return tradeName;
    }

    public void setTradeName(String tradeName) {
        this.tradeName = tradeName;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public boolean isHomeService() {
        return homeService;
    }

    public void setHomeService(boolean homeService) {
        this.homeService = homeService;
    }

    public void setPropertiesFromPatientRx(PatientRx patientRx) {
        this.setTradeName(patientRx.getDrug().getTradeName());
        try{
            this.setQuantity(new BigDecimal(patientRx.getTotalCountTransient()));
        }catch(Exception e){
            this.setQuantity(new BigDecimal(patientRx.getTotalCount()));
        }
        this.setDetails(patientRx.getFrequency().getDescription()+" "+patientRx.getFrequencyQualifier().getDescription());
        this.setHomeService(patientRx.isHomeDelivery());
    }
}
