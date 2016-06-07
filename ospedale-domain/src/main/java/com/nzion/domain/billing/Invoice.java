package com.nzion.domain.billing;

import com.nzion.domain.*;
import com.nzion.domain.annot.AccountNumberField;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.base.LocationAware;
import com.nzion.domain.product.common.Money;
import com.nzion.util.UtilValidator;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * @author Sandeep Prusty
 *         <p/>
 *         16-Sep-2011
 */

@Entity
@Filters({@Filter(name = "EnabledFilter", condition = "(IS_ACTIVE=1 OR IS_ACTIVE IS NULL)"),
        @Filter(name = "LocationFilter", condition = "( :locationId=LOCATION_ID OR LOCATION_ID IS NULL )")
})
@AccountNumberField("invoiceNumber")
public class Invoice extends IdGeneratingBaseEntity implements LocationAware,Comparable<Invoice> {

    private Date invoiceDate;

    private Money totalAmount;

    private Money amountToBePaid = new Money();

    private Money collectedAmount = new Money();

    private String invoiceStatus;

    private String itemId;

    private String itemType;

    private List<InvoiceItem> invoiceItems;

    private Employee consultant;

    private Long referralConsultantId;

    private Patient patient;

    private BigDecimal totalCopayAmount;

    private BigDecimal totalDeductableAmount;

    private BigDecimal totalReferralAmountTobePaid;
    
    private BigDecimal totalReferralAmountPaid;
    
    //will remove this totalReferralAmountPayable in future
    private BigDecimal totalReferralAmountPayable;
    
    private ReferralContract referralContract;
    
    private ReferralContractStatus referralContractStatus;



    private BigDecimal totalAuthorizationAmount;

    private String concessionAuthoriser;

    private String concessionReason;

    private BigDecimal concessionAmount;

    private String concessionType;

    private String cancelAuthoriser;

    private String cancelReason;

    private INSURANCESTATUS insuranceStatus;

    private BigDecimal insuranceRejectedAmount = BigDecimal.ZERO;

    private boolean mobileOrPatinetPortal;
    
    private boolean amountRefundedToPatient;
    
    private String referralDoctorFirstName;
    
    private String referralDoctorLastName;

    private String tariffCategory;

    private String paymentId;

    private BigDecimal patientPayable;

    private BigDecimal insurancePayable;

    private boolean referral_payment_acknowledged;

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

    @Column(precision = 19, scale = 3 ,columnDefinition="DECIMAL(19,3)")
    public BigDecimal getConcessionAmount() {
        return concessionAmount;
    }

    public void setConcessionAmount(BigDecimal concessionAmount) {
        this.concessionAmount = concessionAmount;
    }

    public String getConcessionType() {
        if(concessionType == null)
            return "AMOUNT";
        return concessionType;
    }

    public void setConcessionType(String concessionType) {
        this.concessionType = concessionType;
    }

    public Long getReferralConsultantId() {
        return referralConsultantId;
    }

    public void setReferralConsultantId(Long referralConsultantId) {
        this.referralConsultantId = referralConsultantId;
    }


    private String ipNumber;

    private Location location;

    private Contract contract;

    private Set<InvoicePayment> invoicePayments;

    private InvoiceType invoiceType;

    private String invoiceNumber;

    private String externalPatient;

    private String externalProvider;

    private Date billDate;

    private String collectedByUser;

    private Date generatedOn;

    private String selectedHisModuleId;

    private Long patientInsuranceId;

    @Temporal(TemporalType.DATE)
    public Date getGeneratedOn() {
        return generatedOn;
    }

    public void setGeneratedOn(Date generatedOn) {
        this.generatedOn = generatedOn;
    }

    public String getCollectedByUser() {
        return collectedByUser;
    }

    public void setCollectedByUser(String collectedByUser) {
        this.collectedByUser = collectedByUser;
    }

    public String getExternalProvider() {
        return externalProvider;
    }

    public void setExternalProvider(String externalProvider) {
        this.externalProvider = externalProvider;
    }

    public String getExternalPatient() {
        return externalPatient;
    }

    public void setExternalPatient(String externalPatient) {
        this.externalPatient = externalPatient;
    }


    /**
     * This represents the userLoginId which updated this entity.
     */
    private String writeOffBy;

    private Date writeOffDate;

    private Money writtenOffAmount;

    private String writtenOffReason;

    /* If this is true then It will shows that
      * payment received as Advance for in-patient while admission.
      */
    private Boolean isAdvance = false;

    /*
      * This will set true when received Advance for a particular in-patient is deducted while billing for that in-patient.
      * Or If the bill balance amount is added to next bill
      */
    private Boolean isDeducted = false;

    /*
    * Modified by Mohan Sharma
    * Added schedule's reference to invoice
    * */

    private Schedule schedule;

     public Invoice() {
    }

    public Invoice(String itemId, String itemType, Employee consulatnt, Patient patient, Location location) {
        this.itemId = itemId;
        this.itemType = itemType;
        this.invoiceDate = new Date();
        //this.billDate = new Date();
        this.consultant = consulatnt;
        this.patient = patient;
        this.location = location;
        this.setInvoiceStatus(InvoiceStatusItem.READY.toString());
    }
    @Temporal(value = TemporalType.TIMESTAMP)
    public Date getBillDate() {
        return billDate;
    }

    public void setBillDate(Date billDate) {
        this.billDate = billDate;
    }

    public String getInvoiceStatus() {
        return invoiceStatus;
    }

    public void setInvoiceStatus(String invoiceStatus) {
        this.invoiceStatus = invoiceStatus;
    }

    @Embedded
    public Money getTotalAmount() {
        return totalAmount = (totalAmount == null ? new Money() : totalAmount);
    }

    public void setTotalAmount(Money totalAmount) {
        this.totalAmount = totalAmount;
    }

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "COLLECTED_PRICE"))
    @AssociationOverride(name = "currency", joinColumns = {@JoinColumn(name = "COLLECTED_CURRENCY_ID")})
    public Money getCollectedAmount() {
        if (collectedAmount != null) {
            if (collectedAmount.getAmount() == null) {
                collectedAmount.setAmount(BigDecimal.ZERO);
            }
        } else {
            collectedAmount = new Money();
        }
        return collectedAmount;
    }

    public void setCollectedAmount(Money collectedAmount) {
        this.collectedAmount = collectedAmount;
    }

    @Temporal(TemporalType.DATE)
    public Date getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(Date invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    @OneToMany(targetEntity = InvoiceItem.class, mappedBy = "invoice", fetch = FetchType.EAGER)
    @Cascade(CascadeType.ALL)
    @OrderBy("itemOrder")
    public List<InvoiceItem> getInvoiceItems() {
        if (invoiceItems == null) return new ArrayList<InvoiceItem>();
        return invoiceItems;
    }

    public void setInvoiceItems(List<InvoiceItem> invoiceItems) {
        this.invoiceItems = invoiceItems;
    }

    @Enumerated(EnumType.STRING)
    public INSURANCESTATUS getInsuranceStatus() {
        return insuranceStatus;
    }

    public void setInsuranceStatus(INSURANCESTATUS insuranceStatus) {
        this.insuranceStatus = insuranceStatus;
    }

    @Transient
    public void addInvoiceItem(Collection<InvoiceItem> invoiceItems) {
        for (InvoiceItem invoiceItem : invoiceItems)
            addInvoiceItem(invoiceItem);
    }

    @Transient
    public void addInvoiceItem(InvoiceItem invoiceItem) {
        invoiceItem.setInvoice(this);
        if (UtilValidator.isEmpty(getInvoiceItems()))
            this.invoiceItems = new ArrayList<InvoiceItem>();
        this.invoiceItems.add(invoiceItem);
    }

    @ManyToOne
    public Employee getConsultant() {
        return consultant;
    }

    public void setConsultant(Employee consultant) {
        this.consultant = consultant;
    }

    @ManyToOne
    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    @ManyToOne(targetEntity = Location.class)
    @JoinColumn(name = "LOCATION_ID", nullable = false, updatable = false)
    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @OneToMany(targetEntity = InvoicePayment.class, mappedBy = "invoice", orphanRemoval = false)
    @Cascade(CascadeType.ALL)
    @OrderBy("paymentDate")
    public Set<InvoicePayment> getInvoicePayments() {
        return invoicePayments;
    }

    public void setInvoicePayments(Set<InvoicePayment> invoicePayments) {
        this.invoicePayments = invoicePayments;
    }

    public void addInvoicePayment(InvoicePayment invoicePayment) {
        if (UtilValidator.isEmpty(getInvoicePayments()))
            invoicePayments = new HashSet<InvoicePayment>();
        invoicePayment.setInvoice(this);
        invoicePayments.add(invoicePayment);
    }


    @Transient
    public BigDecimal getBalanceAmount() {
        if (collectedAmount.getAmount() == null)
            return BigDecimal.ZERO;
        return totalAmount.getAmount().subtract(collectedAmount.getAmount());
    }

    public BigDecimal percent(BigDecimal arg) {
        if (totalAmount == null)
            return BigDecimal.ZERO;
        return totalAmount.getAmount().multiply(arg).divide(new BigDecimal(100));
    }

    @ManyToOne
    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public String getIpNumber() {
        return ipNumber;
    }

    public void setIpNumber(String ipNumber) {
        this.ipNumber = ipNumber;
    }

    public String getWriteOffBy() {
        return writeOffBy;
    }

    public void setWriteOffBy(String writeOffBy) {
        this.writeOffBy = writeOffBy;
    }

    @Temporal(value = TemporalType.TIMESTAMP)
    public Date getWriteOffDate() {
        return writeOffDate;
    }

    public void setWriteOffDate(Date writeOffDate) {
        this.writeOffDate = writeOffDate;
    }

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "WRITTENOFF_PRICE"))
    @AssociationOverride(name = "currency", joinColumns = {@JoinColumn(name = "WRITTENOFF_CURRENCY_ID")})
    public Money getWrittenOffAmount() {
        return writtenOffAmount = (writtenOffAmount == null ? new Money() : writtenOffAmount);
    }

    public void setWrittenOffAmount(Money writtenOffAmount) {
        this.writtenOffAmount = writtenOffAmount;
    }

    @Column(length = 1042)
    public String getWrittenOffReason() {
        return writtenOffReason;
    }

    public void setWrittenOffReason(String writtenOffReason) {
        this.writtenOffReason = writtenOffReason;
    }

    public Boolean getIsAdvance() {
        return isAdvance;
    }

    public void setIsAdvance(Boolean isAdvance) {
        this.isAdvance = isAdvance;
    }

    public Boolean getIsDeducted() {
        return isDeducted;
    }

    public void setIsDeducted(Boolean isDeducted) {
        this.isDeducted = isDeducted;
    }

    @Column
    @Enumerated(EnumType.STRING)
    public InvoiceType getInvoiceType() {
        return invoiceType;
    }

    public void setInvoiceType(InvoiceType invoiceType) {
        this.invoiceType = invoiceType;
    }

    @Column(name = "INVOICE_NUMBER")
    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }


    private static final long serialVersionUID = 1L;

    @Override
    public int compareTo(Invoice other) {
        return other.invoiceDate.compareTo(this.invoiceDate);
    }

    @Column(precision = 19, scale = 3 ,columnDefinition="DECIMAL(19,3)")
    public BigDecimal getTotalCopayAmount() {
        return totalCopayAmount;
    }

    public void setTotalCopayAmount(BigDecimal totalCopayAmount) {
        this.totalCopayAmount = totalCopayAmount;
    }

    @Column(precision = 19, scale = 3 ,columnDefinition="DECIMAL(19,3)")
    public BigDecimal getTotalDeductableAmount() {
        return totalDeductableAmount;
    }

    public void setTotalDeductableAmount(BigDecimal totalDeductableAmount) {
        this.totalDeductableAmount = totalDeductableAmount;
    }

    @Column(precision = 19, scale = 3 ,columnDefinition="DECIMAL(19,3)")
    public BigDecimal getTotalAuthorizationAmount() {
        return totalAuthorizationAmount;
    }

    public void setTotalAuthorizationAmount(BigDecimal totalAuthorizationAmount) {
        this.totalAuthorizationAmount = totalAuthorizationAmount;
    }

    public String getSelectedHisModuleId() {
        return selectedHisModuleId;
    }

    public void setSelectedHisModuleId(String selectedHisModuleId) {
        this.selectedHisModuleId = selectedHisModuleId;
    }

    public Long getPatientInsuranceId() {
        return patientInsuranceId;
    }

    public void setPatientInsuranceId(Long patientInsuranceId) {
        this.patientInsuranceId = patientInsuranceId;
    }

    @Column(precision = 19, scale = 3 ,columnDefinition="DECIMAL(19,3)")
    public BigDecimal getTotalReferralAmountTobePaid() {

        if(UtilValidator.isEmpty(totalReferralAmountTobePaid))
            totalReferralAmountTobePaid=BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
        return totalReferralAmountTobePaid;
    }

    public void setTotalReferralAmountTobePaid(BigDecimal totalReferralAmountTobePaid) {
        this.totalReferralAmountTobePaid = totalReferralAmountTobePaid;
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

    @Column(precision = 19, scale = 3 ,columnDefinition="DECIMAL(19,3)")
    public BigDecimal getTotalReferralAmountPayable() {
        return totalReferralAmountPayable;
    }

    public void setTotalReferralAmountPayable(BigDecimal totalReferralAmountPayable) {
        this.totalReferralAmountPayable = totalReferralAmountPayable;
    }
    
    @Column(precision = 19, scale = 3 ,columnDefinition="DECIMAL(19,3)")
    public BigDecimal getTotalReferralAmountPaid() {
    	if(totalReferralAmountPaid == null){
    		totalReferralAmountPaid = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
    	}
		return totalReferralAmountPaid;
	}

	public void setTotalReferralAmountPaid(BigDecimal totalReferralAmountPaid) {
		this.totalReferralAmountPaid = totalReferralAmountPaid;
	}

	@Column(precision = 19, scale = 3 ,columnDefinition="DECIMAL(19,3)")
    public BigDecimal getInsuranceRejectedAmount() {
        return insuranceRejectedAmount;
    }

    public void setInsuranceRejectedAmount(BigDecimal insuranceRejectedAmount) {
        this.insuranceRejectedAmount = insuranceRejectedAmount;
    }

    public boolean isMobileOrPatinetPortal() {
        return mobileOrPatinetPortal;
    }

    public void setMobileOrPatinetPortal(boolean mobileOrPatinetPortal) {
        this.mobileOrPatinetPortal = mobileOrPatinetPortal;
    }
    
    public boolean isAmountRefundedToPatient() {
		return amountRefundedToPatient;
	}

	public void setAmountRefundedToPatient(boolean amountRefundedToPatient) {
		this.amountRefundedToPatient = amountRefundedToPatient;
	}


	public static enum INSURANCESTATUS {
        PENDING_APPROVAL("PENDING APPROVAL"),PRE_APPROVED("PRE APPROVED"),APPROVED("APPROVED"),PART_APPROVED("PART APPROVED"),SENT_FOR_CLAIM("SENT FOR CLAIM"),CLOSED("CLOSED");

        private String description;

        private INSURANCESTATUS(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    @OneToOne(targetEntity = Schedule.class, cascade = javax.persistence.CascadeType.ALL)
    @JoinColumn(name = "schedule_id")
    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    @OneToOne(fetch=FetchType.EAGER)
	public ReferralContract getReferralContract() {
		return referralContract;
	}

	public void setReferralContract(ReferralContract referralContract) {
		this.referralContract = referralContract;
	}

	@Enumerated(EnumType.STRING)
    public ReferralContractStatus getReferralContractStatus() {
		return referralContractStatus;
	}

	public void setReferralContractStatus(
			ReferralContractStatus referralContractStatus) {
		this.referralContractStatus = referralContractStatus;
	}
	
	public String getReferralDoctorFirstName() {
		return referralDoctorFirstName;
	}

	public void setReferralDoctorFirstName(String referralDoctorFirstName) {
		this.referralDoctorFirstName = referralDoctorFirstName;
	}

	public String getReferralDoctorLastName() {
		return referralDoctorLastName;
	}

	public void setReferralDoctorLastName(String referralDoctorLastName) {
		this.referralDoctorLastName = referralDoctorLastName;
	}

    @Column(precision = 19, scale = 3 ,columnDefinition="DECIMAL(19,3)")
    public BigDecimal getPatientPayable() {
        return patientPayable;
    }

    public void setPatientPayable(BigDecimal patientPayable) {
        this.patientPayable = patientPayable;
    }

    @Column(precision = 19, scale = 3 ,columnDefinition="DECIMAL(19,3)")
    public BigDecimal getInsurancePayable() {
        return insurancePayable;
    }

    public void setInsurancePayable(BigDecimal insurancePayable) {
        this.insurancePayable = insurancePayable;
    }

    public enum ReferralContractStatus{
    	INPROCESS("In Process"),COMPLETED("Completed");
    	private String name;
    	ReferralContractStatus(String name){
    		this.name = name;
    	}
    	public String getName(){
    		return name;
    	}
    }

    public String getTariffCategory() {
        return tariffCategory;
    }

    public void setTariffCategory(String tariffCategory) {
        this.tariffCategory = tariffCategory;
    }

    public boolean isReferral_payment_acknowledged() {
        return referral_payment_acknowledged;
    }

    public void setReferral_payment_acknowledged(boolean referral_payment_acknowledged) {
        this.referral_payment_acknowledged = referral_payment_acknowledged;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }
}