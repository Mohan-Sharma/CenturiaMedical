package com.nzion.zkoss.composer.emr;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.nzion.domain.Roles;
import com.nzion.util.Infrastructure;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.Tab;

import com.nzion.domain.Patient;
import com.nzion.domain.billing.AcctgTransTypeEnum;
import com.nzion.domain.billing.AcctgTransactionEntry;
import com.nzion.domain.billing.DebitCreditEnum;
import com.nzion.domain.screen.BillingDisplayConfig;
import com.nzion.service.billing.BillingService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.util.UtilMessagesAndPopups;
import com.nzion.util.UtilValidator;

@VariableResolver(DelegatingVariableResolver.class)
public class PatientAdvanceAmountController{

	private static final long serialVersionUID = 1L;

	@WireVariable
	private BillingService billingService;
	
	@WireVariable
	private CommonCrudService commonCrudService;
	
	BillingDisplayConfig billingDisplayConfig = null; 
	
	private Patient patient;
	
	private BigDecimal depositAmount;
	
	private BigDecimal withdrowAmount;
	
	private BigDecimal advanceAmount;
	
	private Tab patientBalanceTab;
	
	
	@Init
    public void init(@ContextParam(ContextType.VIEW) Component view, @BindingParam("arg1") String patientId){
		this.depositAmount = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
		this.withdrowAmount = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
	    this.advanceAmount = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
	    if(UtilValidator.isNotEmpty(patientId))
	    	this.patient = (Patient) commonCrudService.getById(Patient.class, Long.valueOf(patientId));
	    calculateAmount();
	    patientBalanceTab = (Tab) view;
	    patientBalanceTab.setLabel("Patient Balance : " + this.advanceAmount.setScale(3, RoundingMode.HALF_UP) + " " + "KD");
        if(Infrastructure.getUserLogin().hasRole(Roles.PATIENT))
            patientBalanceTab.setDisabled(true);
        Selectors.wireComponents(view, this, true);
    }
	
	public void depositOrwithdraw(){
		if(depositAmount.compareTo(BigDecimal.ZERO) > 0)
			billingService.updatePatientDeposit(patient,depositAmount);
		if(withdrowAmount.compareTo(BigDecimal.ZERO) > 0)
			billingService.updatePatientWithdraw(patient,withdrowAmount);
		
		calculateAmount();
		patientBalanceTab.setLabel("Patient Balance : " + this.advanceAmount.setScale(3, RoundingMode.HALF_UP) + " " + "KD");
		UtilMessagesAndPopups.showSuccess();
		this.depositAmount = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
		this.withdrowAmount = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
	}
	
	public void clear(){
		this.depositAmount = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
		this.withdrowAmount = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
	}
	
	private void calculateAmount(){
		List<AcctgTransactionEntry> accTransDebit = commonCrudService.findByEquality(AcctgTransactionEntry.class, new String[]{"patientId","transactionType","debitOrCredit"}, new Object[]{patient.getId().toString(),AcctgTransTypeEnum.PATIENT_DEPOSIT,DebitCreditEnum.DEBIT});
	    List<AcctgTransactionEntry> accTransCredit = commonCrudService.findByEquality(AcctgTransactionEntry.class, new String[]{"patientId","transactionType","debitOrCredit"}, new Object[]{patient.getId().toString(),AcctgTransTypeEnum.PATIENT_WITHDRAW,DebitCreditEnum.CREDIT});
	    BigDecimal debitAmount = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
	    BigDecimal creditAmount = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
	    for(AcctgTransactionEntry acc : accTransDebit){
	    	if(acc.getAmount() != null)
	    	debitAmount = debitAmount.add(acc.getAmount());
	    }
	    for(AcctgTransactionEntry acc : accTransCredit){
	    	if(acc.getAmount() != null)
	    		creditAmount = creditAmount.add(acc.getAmount());
	    }
	    if(debitAmount.compareTo(BigDecimal.ZERO) > 0)
	    	advanceAmount = debitAmount.subtract(creditAmount);
	}
	
	public BillingService getBillingService() {
		return billingService;
	}

	public void setBillingService(BillingService billingService) {
		this.billingService = billingService;
	}

	public CommonCrudService getCommonCrudService() {
		return commonCrudService;
	}

	public void setCommonCrudService(CommonCrudService commonCrudService) {
		this.commonCrudService = commonCrudService;
	}


	public Patient getPatient() {
		return patient;
	}


	public void setPatient(Patient patient) {
		this.patient = patient;
	}


	public BigDecimal getAdvanceAmount() {
		return advanceAmount;
	}


	public void setAdvanceAmount(BigDecimal advanceAmount) {
		this.advanceAmount = advanceAmount;
	}

	public Tab getPatientBalanceTab() {
		return patientBalanceTab;
	}

	public void setPatientBalanceTab(Tab patientBalanceTab) {
		this.patientBalanceTab = patientBalanceTab;
	}

	public BigDecimal getDepositAmount() {
		return depositAmount;
	}

	public void setDepositAmount(BigDecimal depositAmount) {
		this.withdrowAmount = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
		this.depositAmount = depositAmount;
	}

	public BigDecimal getWithdrowAmount() {
		return withdrowAmount;
	}

	public void setWithdrowAmount(BigDecimal withdrowAmount) {
		this.depositAmount = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
		this.withdrowAmount = withdrowAmount;
	}

	public BillingDisplayConfig getBillingDisplayConfig() {
		if(billingDisplayConfig == null)
			billingDisplayConfig = commonCrudService.getByPractice(BillingDisplayConfig.class);
		return billingDisplayConfig;
	}

	
}
