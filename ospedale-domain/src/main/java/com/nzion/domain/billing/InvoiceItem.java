package com.nzion.domain.billing;

import com.nzion.domain.Provider;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.Cpt;
import com.nzion.domain.pms.Product;
import com.nzion.domain.product.common.Money;
import com.nzion.util.UtilValidator;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

/**
 * @author Sandeep Prusty
 *         <p/>
 *         16-Sep-2011
 */

@Entity
public class InvoiceItem extends IdGeneratingBaseEntity {

    private static final long serialVersionUID = 1L;

    private Invoice invoice;
    private String description;
    private String itemId;
    private InvoiceType itemType;

    private BigDecimal factor;
    private String factorDescription;
    private Money price;

    private BigDecimal netPrice = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
    private BigDecimal unitPrice;
    private Long providerId;
    private Long referralId;

    private String serviceId;
    private String moduleId;
    private BigDecimal copayAmount;
    private BigDecimal maxAmount;
    private BigDecimal actualCopayAmount;
    private BigDecimal copayPercentage;
    private BigDecimal actualCopayPercentage;
    private BigDecimal deductableAmount = BigDecimal.ZERO;
    private BigDecimal deductablePercentage;
    private Boolean authorization = false;
    private boolean preauthorized;
    private BigDecimal authorizationInclusiveConsultation;
    private BigDecimal authorizationAmount;
    private BigDecimal authorizationRequiredConsultation;
    private String computeType;
    private BigDecimal concessionAmount;
    private String concessionType;

    private String authorizationNo;
    private Date authorizationDate;
    private String authorizationNote;

    private String invoiceItemStatus;

    private Cpt cpt;

    private Provider provider;

    private boolean concessionApplied = false;

    private BigDecimal billableAmountMax;

    private BigDecimal billableAmountMin;

    private boolean isMinMaxPriceAvailable;

    private Product product;

    public Long getReferralId() {
		return referralId;
	}

	public void setReferralId(Long referralId) {
		this.referralId = referralId;
	}

	public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    private String taxLedgerName;
    private BigDecimal taxAmount;
    private BigDecimal cessAmount;


    private BigDecimal referral_amountPaid;
    private BigDecimal referral_amountTobePaid;

    private BigDecimal quantity = BigDecimal.ONE;
    private String quanityDesc;
    private Integer itemOrder = 0;
    private Date billedDate;
    private String batchNo;

    private String roomCharge;
    private String source;

    private String concessionAuthoriser;

    private String concessionReason;

    private String cancelAuthoriser;

    private String cancelReason;

    private int invoiceItemCountForReport=1;

    //for report start
    private BigDecimal insurancePayable;
    private BigDecimal patientPayable;
    private String referralClnicName;
    private BigDecimal netIncome;
    private String invoiceStatusForReport;
    //for report end

    public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getRoomCharge() {
        return roomCharge;
    }

    public void setRoomCharge(String roomCharge) {
        this.roomCharge = roomCharge;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public InvoiceItem() {
    }

    public InvoiceItem(Invoice invoice, String itemId, InvoiceType itemType, String description, Integer quantity, String quantityDescription,String source) {
        this.invoice = invoice;
        this.referralId = invoice.getReferralConsultantId();
        this.itemId = itemId;
        this.itemType = itemType;
        this.description = description;
        if (quantity != null)
            this.quantity = new BigDecimal(quantity);
        this.quanityDesc = quantityDescription;
        this.source=source;
    }

    public void init(BigDecimal factor, String factorDesc, Money price, Money netPrice, Integer itemOrder) {
        this.factor = factor;
        this.factorDescription = factorDesc;
        this.price = price;
        this.netPrice = netPrice.getAmount();
        this.itemOrder = itemOrder;
    }

    @Column(precision = 19, scale = 3 ,columnDefinition="DECIMAL(19,3)")
    public BigDecimal getCessAmount() {
        return cessAmount;
    }

    public void setCessAmount(BigDecimal cessAmount) {
        this.cessAmount = cessAmount;
    }

    public String getTaxLedgerName() {
        return taxLedgerName;
    }

    public void setTaxLedgerName(String taxLedgerName) {
        this.taxLedgerName = taxLedgerName;
    }

    @Column(precision = 19, scale = 3 ,columnDefinition="DECIMAL(19,3)")
    public BigDecimal getFactor() {
        return factor;
    }

    public void setFactor(BigDecimal factor) {
        this.factor = factor;
    }

    public String getFactorDescription() {
        return factorDescription;
    }

    public void setFactorDescription(String factorDescription) {
        this.factorDescription = factorDescription;
    }

    @ManyToOne
    @JoinColumn(name = "INVOICE_ID")
    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    @Transient
    public BigDecimal getPriceValue() {
        if (netPrice == null)
            netPrice = BigDecimal.ZERO;
        return netPrice;
    }

    public void setPriceValue(BigDecimal priceValue) {
        this.netPrice = priceValue;
    }

    @Transient
    public BigDecimal getGrossAmount() {
        if(this.factor == null)
            this.factor = BigDecimal.ZERO;
        return this.factor.multiply(this.quantity).setScale(3,RoundingMode.HALF_UP);
    }

    @Transient
    public void setGrossAmount(BigDecimal grossAmount) {
    	if(billableAmountMin != null && billableAmountMin.compareTo(grossAmount) > 0)
    		return;
    	if(billableAmountMax != null && billableAmountMax.compareTo(grossAmount) < 0)
    		return;
    	this.factor = grossAmount.divide(this.quantity).setScale(3,RoundingMode.HALF_UP);
    	Money money = new Money(this.factor.multiply(this.quantity),getPrice().getCurrency());
    	setPrice(money);
    }

    @Transient
    public BigDecimal getInsuranceAmount() {
        if(getCopayCalculatedAmount() == null)
            copayAmount = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
        if(deductableAmount == null)
            copayAmount = BigDecimal.ZERO.setScale(3,RoundingMode.HALF_UP);
        boolean isCorporate = "CORPORATE".equals( this.getInvoice().getPatient().getPatientType() ) ? true : false;
        if(this.serviceId != null || isCorporate){

        	BigDecimal actualNetAmount = this.getNetPrice();
        	BigDecimal maxAmount = this.getMaxAmount();
        	if( maxAmount.compareTo(BigDecimal.ZERO) != 0 && maxAmount.compareTo(actualNetAmount) <= 0){
        		actualNetAmount = maxAmount;
        	}

            BigDecimal insAmount = actualNetAmount.subtract(getCopayAmountOrPercent().add(getDeductableAmountOrPercent()));
            if(insAmount.compareTo(BigDecimal.ZERO) < 0)
                return BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
            return insAmount.setScale(3, RoundingMode.HALF_UP);
        }else
            return BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
    }

    @Transient
    public BigDecimal getCopayCalculatedAmount() {
    	if("Cancel".equals(this.getInvoiceItemStatus()))
			return BigDecimal.ZERO;

    	if(getNetPrice() == null)
    		setNetPrice(BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP));
        if(authorization){
            if(getAuthorizationAmount() != null && getAuthorizationAmount().compareTo(BigDecimal.ZERO) >= 0)
                setCopayAmount((getNetPrice().subtract(getAuthorizationAmount())).subtract(getDeductableAmountOrPercent()));
        }else{
            if(getAuthorizationAmount() == null)
                setAuthorizationAmount(BigDecimal.ZERO);
            setCopayAmount((getNetPrice().subtract(getAuthorizationAmount())).subtract(getDeductableAmountOrPercent()));
        }
        if(copayAmount.compareTo(BigDecimal.ZERO) <= 0)
            copayAmount = BigDecimal.ZERO;
        return copayAmount;
    }

    @Transient
    public BigDecimal getUnitPrice() {
        return netPrice.divide(quantity);
    }

    @Lob
    public String getDescription() {
        if(description == null)
            description = new String();
        return description.toUpperCase();
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    @Enumerated(EnumType.STRING)
    public InvoiceType getItemType() {
        return itemType;
    }

    public void setItemType(InvoiceType itemType) {
        this.itemType = itemType;
    }

    public Integer getItemOrder() {
        return itemOrder;
    }

    public void setItemOrder(Integer itemOrder) {
        this.itemOrder = itemOrder;
    }

    @Embedded
    public Money getPrice() {
        return price;
    }

    public void setPrice(Money price) {
        this.price = price;
    }

    @Column(precision = 19, scale = 3 ,columnDefinition="DECIMAL(19,3)")
    public BigDecimal getNetPrice() {
    	if(netPrice == null)
    		netPrice = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
        return netPrice;
    }

    public void setNetPrice(BigDecimal netPrice) {
        this.netPrice = netPrice;
    }

    /*public Integer getQuantity() {
    return quantity;
	}

	public void setQuantity(Integer quantity) {
	this.quantity = quantity;
	}
*/

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getQuanityDesc() {
        return quanityDesc;
    }

    public void setQuanityDesc(String quanityDesc) {
        this.quanityDesc = quanityDesc;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getBilledDate() {
        return billedDate;
    }

    public void setBilledDate(Date billedDate) {
        this.billedDate = billedDate;
    }

    @Column(precision = 19, scale = 3 ,columnDefinition="DECIMAL(19,3)")
    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    @Column(precision = 19, scale = 3 ,columnDefinition="DECIMAL(19,3)")
    public BigDecimal getCopayAmount() {
        return copayAmount;
    }

    public void setCopayAmount(BigDecimal copayAmount) {
        this.copayAmount = copayAmount;
    }

    @Column(precision = 19, scale = 3 ,columnDefinition="DECIMAL(19,3)")
    public BigDecimal getActualCopayAmount() {
		return actualCopayAmount;
	}

	public void setActualCopayAmount(BigDecimal actualCopayAmount) {
		this.actualCopayAmount = actualCopayAmount;
	}

	public BigDecimal getCopayPercentage() {
        return copayPercentage;
    }

    public void setCopayPercentage(BigDecimal copayPercentage) {
        this.copayPercentage = copayPercentage;
    }

    public BigDecimal getActualCopayPercentage() {
		return actualCopayPercentage;
	}

	public void setActualCopayPercentage(BigDecimal actualCopayPercentage) {
		this.actualCopayPercentage = actualCopayPercentage;
	}

	@Column(precision = 19, scale = 3 ,columnDefinition="DECIMAL(19,3)")
    public BigDecimal getDeductableAmount() {
        if(deductableAmount == null)
            return BigDecimal.ZERO.setScale(3,RoundingMode.HALF_UP);
        return deductableAmount;
    }

    public void setDeductableAmount(BigDecimal deductableAmount) {
        this.deductableAmount = deductableAmount;
    }

    public BigDecimal getDeductablePercentage() {
        return deductablePercentage;
    }

    public void setDeductablePercentage(BigDecimal deductablePercentage) {
        this.deductablePercentage = deductablePercentage;
    }

    public Boolean getAuthorization() {
        if(authorization == null)
        	authorization = Boolean.FALSE;
        if("OPD_REGISTRATION".equals(getItemType().toString()))
            return Boolean.TRUE;
        return authorization;
    }

    public void setAuthorization(Boolean authorization) {
        this.authorization = authorization;
    }

    @Column(precision = 19, scale = 3 ,columnDefinition="DECIMAL(19,3)")
    public BigDecimal getAuthorizationInclusiveConsultation() {
        return authorizationInclusiveConsultation;
    }

    public void setAuthorizationInclusiveConsultation(BigDecimal authorizationInclusiveConsultation) {
        this.authorizationInclusiveConsultation = authorizationInclusiveConsultation;
    }

    @Column(precision = 19, scale = 3 ,columnDefinition="DECIMAL(19,3)")
    public BigDecimal getAuthorizationAmount() {
        return authorizationAmount;
    }

    public void setAuthorizationAmount(BigDecimal authorizationAmount) {
        this.authorizationAmount = authorizationAmount;
    }

    @Column(precision = 19, scale = 3 ,columnDefinition="DECIMAL(19,3)")
    public BigDecimal getAuthorizationRequiredConsultation() {
        return authorizationRequiredConsultation;
    }

    public void setAuthorizationRequiredConsultation(BigDecimal authorizationRequiredConsultation) {
        this.authorizationRequiredConsultation = authorizationRequiredConsultation;
    }

    public String getComputeType() {
        return computeType;
    }

    public void setComputeType(String computeType) {
        this.computeType = computeType;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    @Column(precision = 19, scale = 3 ,columnDefinition="DECIMAL(19,3)")
    public BigDecimal getConcessionAmount() {
        if(UtilValidator.isEmpty(concessionAmount))
            concessionAmount=BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
        return concessionAmount;
    }

    public void setConcessionAmount(BigDecimal concessionAmount) {
    	this.concessionAmount = concessionAmount;
        if(concessionAmount != null)
        	this.concessionAmount.setScale(3, RoundingMode.HALF_UP);
    }

    public String getConcessionType() {
        if(UtilValidator.isEmpty(concessionType))
            return "AMOUNT";
        return concessionType;
    }

    public void setConcessionType(String concessionType) {
        this.concessionType = concessionType;
    }

    public String getAuthorizationNo() {
        return authorizationNo;
    }

    public void setAuthorizationNo(String authorizationNo) {
        this.authorizationNo = authorizationNo;
    }

    public Date getAuthorizationDate() {
        return authorizationDate;
    }

    public void setAuthorizationDate(Date authorizationDate) {
        this.authorizationDate = authorizationDate;
    }

    public String getAuthorizationNote() {
        return authorizationNote;
    }

    public void setAuthorizationNote(String authorizationNote) {
        this.authorizationNote = authorizationNote;
    }

    public String getInvoiceItemStatus() {
        return invoiceItemStatus;
    }

    public void setInvoiceItemStatus(String invoiceItemStatus) {
        this.invoiceItemStatus = invoiceItemStatus;
    }

    @Transient
    public Boolean getIsStatusCancel() {
        if("Cancel".equals(getInvoiceItemStatus()))
            return Boolean.TRUE;
        else
            return Boolean.FALSE;
    }

    @Transient
    public Boolean getIsStatusNotCancel() {
        if("Cancel".equals(getInvoiceItemStatus()))
            return Boolean.FALSE;
        else
            return Boolean.TRUE;
    }

    @Transient
    public  Boolean getIsAuthorized(){
        return getAuthorization();
    }

    @Transient
    public Boolean isInsurancePatient(){
        if("INSURANCE".equals(invoice.getPatient().getPatientType()) && UtilValidator.isNotEmpty(this.getServiceId()) )
            return true;
        setAuthorization(true);
        return false;
    }

    public void setIsAuthorized(Boolean bool){
        setAuthorization(!bool);
    }

    @OneToOne(fetch = FetchType.EAGER)
    public Cpt getCpt() {
        return cpt;
    }

    public void setCpt(Cpt cpt) {
        this.cpt = cpt;
    }

    @OneToOne(fetch = FetchType.EAGER)
    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public boolean isConcessionApplied() {
        return concessionApplied;
    }

    public void setConcessionApplied(boolean concessionApplied) {
        this.concessionApplied = concessionApplied;
    }

    public String getConcessionAuthoriser() {
        return concessionAuthoriser;
    }

    public void setConcessionAuthoriser(String concessionAuthoriser) {
        this.concessionAuthoriser = concessionAuthoriser;
    }

    public String getConcessionReason() {
        return concessionReason;
    }

    public void setConcessionReason(String concessionReason) {
        this.concessionReason = concessionReason;
    }

    public boolean isPreauthorized() {
        return preauthorized;
    }

    public void setPreauthorized(boolean preauthorized) {
        this.preauthorized = preauthorized;
    }

	public BigDecimal getBillableAmountMax() {
		return billableAmountMax;
	}

	public void setBillableAmountMax(BigDecimal billableAmountMax) {
		this.billableAmountMax = billableAmountMax;
	}

	public BigDecimal getBillableAmountMin() {
		return billableAmountMin;
	}

	public void setBillableAmountMin(BigDecimal billableAmountMin) {
		this.billableAmountMin = billableAmountMin;
	}

	public boolean isMinMaxPriceAvailable() {
		return isMinMaxPriceAvailable;
	}

	public void setMinMaxPriceAvailable(boolean isMinMaxPriceAvailable) {
		this.isMinMaxPriceAvailable = isMinMaxPriceAvailable;
	}

	@Transient
	public boolean isNotMinMax(){
		return !isMinMaxPriceAvailable;
	}

	@Transient
    public BigDecimal getCopayAmountOrPercent(){
		if("Cancel".equals(this.getInvoiceItemStatus()) || getNetPrice().compareTo(BigDecimal.ZERO) == 0)
			return BigDecimal.ZERO.setScale(3,RoundingMode.HALF_UP);
    	BigDecimal lineCoPay = this.getCopayCalculatedAmount() != null ? this.getCopayCalculatedAmount() : BigDecimal.ZERO;
        if(lineCoPay.compareTo(BigDecimal.ZERO) == 0 && this.getCopayPercentage() != null){
        	BigDecimal actualNetAmount = this.getGrossAmount();
        	BigDecimal maxAmount = this.getMaxAmount();
        	if( maxAmount.compareTo(BigDecimal.ZERO) != 0 && maxAmount.compareTo(actualNetAmount) <= 0){
        		actualNetAmount = maxAmount;
        	}

        	BigDecimal netPrice = actualNetAmount.subtract(getDeductableAmountOrPercent());
        	lineCoPay = percentage(netPrice,this.getCopayPercentage());
            BigDecimal insAmount = actualNetAmount.subtract(lineCoPay.add(getDeductableAmountOrPercent()));
            if(getConcessionAmount() != null && getConcessionAmount().compareTo(BigDecimal.ZERO) > 0)
                lineCoPay = this.getGrossAmount().subtract(insAmount).subtract(getConcessionAmount());

        }
        if(lineCoPay.compareTo(BigDecimal.ZERO) <= 0)
            lineCoPay = BigDecimal.ZERO;
        return lineCoPay.setScale(3,RoundingMode.HALF_UP);
    }

	@Transient
	public String getMaxAmountDif(){
		if("Cancel".equals(this.getInvoiceItemStatus()))
			return "";
		String str = "";
		BigDecimal actualNetAmount = this.getNetPrice();
    	BigDecimal maxAmount = this.getMaxAmount();
    	BigDecimal diffAmount = BigDecimal.ZERO;
    	if( maxAmount.compareTo(BigDecimal.ZERO) != 0 && maxAmount.compareTo(actualNetAmount) <= 0){
    		diffAmount = actualNetAmount.subtract(maxAmount);
    		if(diffAmount != null && diffAmount.compareTo(BigDecimal.ZERO) > 0)
    		str = "(+" + diffAmount.setScale(3,RoundingMode.HALF_UP) + ")";
    	}
        return str;
    }

	@Transient
	public BigDecimal getTotalMaxCopayAmount(){
		if("Cancel".equals(this.getInvoiceItemStatus()))
			return BigDecimal.ZERO;
		BigDecimal amount = BigDecimal.ZERO;
		BigDecimal actualNetAmount = this.getNetPrice();
    	BigDecimal maxAmount = this.getMaxAmount();
    	BigDecimal diffAmount = BigDecimal.ZERO;
    	if( maxAmount.compareTo(BigDecimal.ZERO) != 0 && maxAmount.compareTo(actualNetAmount) <= 0){
    		diffAmount = actualNetAmount.subtract(maxAmount);
    		amount = diffAmount.setScale(3,RoundingMode.HALF_UP);
    	}
        return amount;
    }

    @Transient
    public BigDecimal getDeductableAmountOrPercent() {
    	if("Cancel".equals(this.getInvoiceItemStatus()))
			return BigDecimal.ZERO;
    	BigDecimal deductableAmount = this.getDeductableAmount() != null ? this.getDeductableAmount() : BigDecimal.ZERO;
        if(deductableAmount.compareTo(BigDecimal.ZERO) == 0 && this.getDeductablePercentage() != null){
        	BigDecimal actualNetAmount = this.getNetPrice();
        	BigDecimal maxAmount = this.getMaxAmount();
        	if( maxAmount.compareTo(BigDecimal.ZERO) != 0 && maxAmount.compareTo(actualNetAmount) <= 0){
        		actualNetAmount = maxAmount;
        	}
        	deductableAmount = percentage(actualNetAmount,this.getDeductablePercentage());
        }
        return deductableAmount.setScale(3,RoundingMode.HALF_UP);
    }

    @Transient
    public static BigDecimal percentage(BigDecimal base, BigDecimal pct){
        if(UtilValidator.isEmpty(base) || UtilValidator.isEmpty(pct))
            return BigDecimal.ZERO;
        return base.multiply(pct).divide(new BigDecimal(100));
    }

    public BigDecimal getReferral_amountPaid() {
        return referral_amountPaid;
    }

    public void setReferral_amountPaid(BigDecimal referral_amountPaid) {
        this.referral_amountPaid = referral_amountPaid;
    }

    public BigDecimal getReferral_amountTobePaid() {
        if(referral_amountTobePaid==null)
            referral_amountTobePaid=BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);

        return referral_amountTobePaid;
    }

    public void setReferral_amountTobePaid(BigDecimal referral_amountTobePaid) {
        this.referral_amountTobePaid = referral_amountTobePaid;
    }

    public String getCancelAuthoriser() {
		return cancelAuthoriser;
	}

	public void setCancelAuthoriser(String cancelAuthoriser) {
		this.cancelAuthoriser = cancelAuthoriser;
	}

	public String getCancelReason() {
		return cancelReason;
	}

	public void setCancelReason(String cancelReason) {
		this.cancelReason = cancelReason;
	}

	public BigDecimal getMaxAmount() {
		if(maxAmount == null)
			maxAmount = BigDecimal.ZERO;
		return maxAmount;
	}

	public void setMaxAmount(BigDecimal maxAmount) {
		this.maxAmount = maxAmount;
	}

    @OneToOne(fetch = FetchType.EAGER)
    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    @Transient
    public int getInvoiceItemCountForReport() {
        return invoiceItemCountForReport;
    }

    public void setInvoiceItemCountForReport(int invoiceItemCountForReport) {
        this.invoiceItemCountForReport = invoiceItemCountForReport;
    }

    @Transient
    public BigDecimal getPatientPayable() {
        return patientPayable;
    }

    public void setPatientPayable(BigDecimal patientPayable) {
        this.patientPayable = patientPayable;
    }

    @Transient
    public BigDecimal getInsurancePayable() {
        return insurancePayable;
    }

    public void setInsurancePayable(BigDecimal insurancePayable) {
        this.insurancePayable = insurancePayable;
    }
    @Transient
    public String getReferralClnicName() {
        return referralClnicName;
    }

    public void setReferralClnicName(String referralClnicName) {
        this.referralClnicName = referralClnicName;
    }
    @Transient
    public BigDecimal getNetIncome() {
        return netIncome;
    }

    public void setNetIncome(BigDecimal netIncome) {
        this.netIncome = netIncome;
    }
    @Transient
    public String getInvoiceStatusForReport() {
        return invoiceStatusForReport;
    }

    public void setInvoiceStatusForReport(String invoiceStatusForReport) {
        this.invoiceStatusForReport = invoiceStatusForReport;
    }
}