package com.nzion.zkoss.composer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.zk.ui.Component;
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
import com.nzion.service.PatientService;
import com.nzion.service.billing.BillingService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.util.UtilDateTime;
import com.nzion.util.UtilMessagesAndPopups;
import com.nzion.util.UtilValidator;

@VariableResolver(DelegatingVariableResolver.class)
public class PatientDepositViewModel {

	@WireVariable
	private CommonCrudService commonCrudService;

	@WireVariable
	private PatientService patientService;

	@WireVariable
	private BillingService billingService;

	@Wire("#patientDepositWin")
	private Window patientDepositWin;

	private PatientDeposit patientDeposit;

	private List<PatientDeposit> patientDeposits;

	private Patient patient;

	private Date fromDate;

	private Date thruDate;


	@AfterCompose
	public void init(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, true);
		//patientDeposits = commonCrudService.getAll(PatientDeposit.class);
	}

	@Command("cancel")
	public void cancel(PatientDeposit patientDeposit){
		BigDecimal totalAvailableAmount = calculateAmount(patientDeposit.getPatient());
		totalAvailableAmount = totalAvailableAmount.subtract(patientDeposit.getDepositAmount());
		/*if(UtilValidator.isEmpty(totalAvailableAmount) || totalAvailableAmount.compareTo(BigDecimal.ZERO) <= 0){
			UtilMessagesAndPopups.showError("No sufficient amount available for refund");
			return;
		}*/
		patientDeposit.setTotalAvailableAmount(totalAvailableAmount);
		patientDeposit.setStatus("Cancelled");
		commonCrudService.save(patientDeposit);
		billingService.updatePatientWithdraw(patientDeposit.getPatient(), patientDeposit.getDepositAmount());
		Events.postEvent("onReloadRequest",patientDepositWin.getFellow("patientDepositListBox"),null);
		UtilMessagesAndPopups.showSuccess();
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


	public void searchPatientDeposits(){
		if(fromDate == null)
			fromDate = new Date();
		if(thruDate == null)
			thruDate = new Date();
		if(UtilDateTime.getIntervalInDays(fromDate,thruDate) > 30){
			UtilMessagesAndPopups.showError("Search Date range cannot be greater than 30 days");
			return;
		}
		patientDeposits = patientService.getPatientDepositsByCriteria(patient, fromDate, thruDate);
	}


	public CommonCrudService getCommonCrudService() {
		return commonCrudService;
	}


	public void setCommonCrudService(CommonCrudService commonCrudService) {
		this.commonCrudService = commonCrudService;
	}


	public Window getPatientDepositWin() {
		return patientDepositWin;
	}


	public void setPatientDepositWin(Window patientDepositWin) {
		this.patientDepositWin = patientDepositWin;
	}


	public PatientDeposit getPatientDeposit() {
		return patientDeposit;
	}


	public void setPatientDeposit(PatientDeposit patientDeposit) {
		this.patientDeposit = patientDeposit;
	}


	public List<PatientDeposit> getPatientDeposits() {
		if(patientDeposits == null)
			patientDeposits = new ArrayList<PatientDeposit>();
		return patientDeposits;
	}


	public void setPatientDeposits(List<PatientDeposit> patientDeposits) {
		this.patientDeposits = patientDeposits;
	}


	public Patient getPatient() {
		return patient;
	}


	public void setPatient(Patient patient) {
		this.patient = patient;
	}


	public Date getFromDate() {
		return fromDate;
	}


	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}


	public Date getThruDate() {
		return thruDate;
	}


	public void setThruDate(Date thruDate) {
		this.thruDate = thruDate;
	}


}
