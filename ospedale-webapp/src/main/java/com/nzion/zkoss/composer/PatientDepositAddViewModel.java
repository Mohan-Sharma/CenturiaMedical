package com.nzion.zkoss.composer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.Window;

import com.nzion.domain.Patient;
import com.nzion.domain.PatientDeposit;
import com.nzion.domain.billing.AcctgTransTypeEnum;
import com.nzion.domain.billing.AcctgTransactionEntry;
import com.nzion.domain.billing.DebitCreditEnum;
import com.nzion.service.billing.BillingService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.util.Infrastructure;
import com.nzion.util.UtilMessagesAndPopups;


@VariableResolver(DelegatingVariableResolver.class)
public class PatientDepositAddViewModel {
	
	@WireVariable
	private CommonCrudService commonCrudService;
	
	@Wire("#patientDepositAddWin")
	private Window patientDepositAddWin;
	
	
	private PatientDeposit patientDeposit;
	
	@WireVariable
	private BillingService billingService;
	
	@AfterCompose
	public void init(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, true);
		patientDeposit = new PatientDeposit();
		
	}
	
	@Command("save")
	public void save(){
		if(patientDeposit.getPatient() == null){
			UtilMessagesAndPopups.showError("Patient cannot be empty");
			return;
		}
		if(patientDeposit.getDepositAmount().compareTo(BigDecimal.ZERO) <= 0){
			UtilMessagesAndPopups.showError("Amount must be greater than zero");
			return;
		}
		patientDeposit.setStatus("Deposit");
		patientDeposit.setTotalAvailableAmount( patientDeposit.getDepositAmount().add(calculateAmount(patientDeposit.getPatient())) );
		patientDeposit.setCreatedPerson(Infrastructure.getLoggedInPerson());
		commonCrudService.save(patientDeposit);
		billingService.updatePatientDeposit(patientDeposit.getPatient(),patientDeposit.getDepositAmount());
		if(patientDepositAddWin.getAttribute("patientDepositListBox") != null)
			Events.postEvent("onReloadRequest",(Component)patientDepositAddWin.getAttribute("patientDepositListBox"),null);
		UtilMessagesAndPopups.showSuccess();
		patientDepositAddWin.detach();
	}
	
	private BigDecimal calculateAmount(Patient patient){
		BigDecimal advanceAmount = BigDecimal.ZERO;
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
	    return advanceAmount;
	}

	public CommonCrudService getCommonCrudService() {
		return commonCrudService;
	}

	public void setCommonCrudService(CommonCrudService commonCrudService) {
		this.commonCrudService = commonCrudService;
	}

	public Window getPatientDepositAddWin() {
		return patientDepositAddWin;
	}

	public void setPatientDepositAddWin(Window patientDepositAddWin) {
		this.patientDepositAddWin = patientDepositAddWin;
	}

	public PatientDeposit getPatientDeposit() {
		return patientDeposit;
	}

	public void setPatientDeposit(PatientDeposit patientDeposit) {
		this.patientDeposit = patientDeposit;
	}
	

}
