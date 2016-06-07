package com.nzion.dto;

import com.nzion.domain.Provider;
import com.nzion.domain.SoapNoteType;
import com.nzion.domain.Speciality;
import com.nzion.domain.emr.Cpt;
import com.nzion.domain.pms.Product;

import java.math.BigDecimal;

/**
 * Created with IntelliJ IDEA.
 * User: USER
 * Date: 5/13/15
 * Time: 6:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class PatientInvoiceItem {

    private Provider provider;

    private Cpt cpt;

    private String invoiceType;

    private BigDecimal unitPrice = BigDecimal.ZERO;

    private BigDecimal quantity = BigDecimal.ONE;

    private BigDecimal grossAmount = BigDecimal.ZERO;

    private Boolean doctor;

    private Boolean cptVisible;

    private SoapNoteType soapNoteType;

    private Speciality speciality;

    private String formattedServiceName;

    private Product product;

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public Cpt getCpt() {
        if(cpt == null)
            cpt = new Cpt();
        return cpt;
    }

    public void setCpt(Cpt cpt) {
        this.cpt = cpt;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getGrossAmount() {
        grossAmount = unitPrice.multiply(quantity);
        return grossAmount;
    }

    public void setGrossAmount(BigDecimal grossAmount) {
        this.grossAmount = grossAmount;
    }

    public String getInvoiceType() {
        return invoiceType;
    }

    public void setInvoiceType(String invoiceType) {
        provider = null;
        cpt = null;
        this.invoiceType = invoiceType;
    }

    public Boolean getDoctor() {
        if("OPD_CONSULTATION".equals(invoiceType))
            return true;
        return doctor;
    }

    public void setDoctor(Boolean doctor) {
        this.doctor = doctor;
    }

    public Boolean getCptVisible() {
        if("OPD_PROCEDURE".equals(invoiceType))
            return true;
        return cptVisible;
    }

    public void setCptVisible(Boolean cptVisible) {
        this.cptVisible = cptVisible;
    }

    public SoapNoteType getSoapNoteType() {
        return soapNoteType;
    }

    public void setSoapNoteType(SoapNoteType soapNoteType) {
        this.soapNoteType = soapNoteType;
    }

    public Speciality getSpeciality() {
        return speciality;
    }

    public void setSpeciality(Speciality speciality) {
        this.speciality = speciality;
    }

    public String getFormattedServiceName(){
        if("OPD_CONSULTATION".equals(invoiceType)){
             return "Consultation " + " - " + getSoapNoteType().getName();
        }
        if("OPD_PROCEDURE".equals(invoiceType)){
            return "Procedure " + " - " + cpt.getShortDescription();
        }
        if("OPD_PRODUCT".equals(invoiceType)){
            return "Product " + " - " + getProduct().getTradeName();
        }
        return "";
    }

    public void setFormattedServiceName(String formattedServiceName) {
        this.formattedServiceName = formattedServiceName;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
