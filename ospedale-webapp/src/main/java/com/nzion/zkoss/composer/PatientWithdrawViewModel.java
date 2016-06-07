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
import com.nzion.domain.PatientWithDraw;
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
public class PatientWithdrawViewModel {
	
	@WireVariable
	private CommonCrudService commonCrudService;
	
	@WireVariable
	private PatientService patientService;
	
	@Wire("#patientRefundWin")
	private Window patientRefundWin;
	
	@WireVariable
	private BillingService billingService;
	
	private PatientWithDraw patientWithDraw;
	
	private List<PatientWithDraw> patientWithDraws;
	
	private Patient patient;
	
	private Date fromDate;
	
	private Date thruDate;
	

	@AfterCompose
	public void init(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, true);
		//patientWithDraws = commonCrudService.getAll(PatientWithDraw.class);
	}
	
	public void searchPatientWithdraw(){
		if(fromDate == null)
			fromDate = new Date();
		if(thruDate == null)
			thruDate = new Date();
		if(UtilDateTime.getIntervalInDays(fromDate,thruDate) > 30){
			UtilMessagesAndPopups.showError("Search Date range cannot be greater than 30 days");
			return;
		}
		patientWithDraws = patientService.getPatientWithdrawByCriteria(patient, fromDate, thruDate);
	}
	
	@Command("cancel")
	public void cancel(PatientWithDraw patientWithDraw){
		BigDecimal totalAvailableAmount = calculateAmount(patientWithDraw.getPatient());
		patientWithDraw.setTotalAvailableAmount(patientWithDraw.getWithdrawAmount().add(totalAvailableAmount) );
		patientWithDraw.setStatus("Cancelled");
		commonCrudService.save(patientWithDraw);
		billingService.updatePatientDeposit(patientWithDraw.getPatient(),patientWithDraw.getWithdrawAmount());
	    Events.postEvent("onReloadRequest",patientRefundWin.getFellow("patientWithdrawListBox"),null);
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

	public CommonCrudService getCommonCrudService() {
		return commonCrudService;
	}

	public void setCommonCrudService(CommonCrudService commonCrudService) {
		this.commonCrudService = commonCrudService;
	}

	public PatientService getPatientService() {
		return patientService;
	}

	public void setPatientService(PatientService patientService) {
		this.patientService = patientService;
	}

	public Window getPatientRefundWin() {
		return patientRefundWin;
	}

	public void setPatientRefundWin(Window patientRefundWin) {
		this.patientRefundWin = patientRefundWin;
	}

	public PatientWithDraw getPatientWithDraw() {
		return patientWithDraw;
	}

	public void setPatientWithDraw(PatientWithDraw patientWithDraw) {
		this.patientWithDraw = patientWithDraw;
	}

	public List<PatientWithDraw> getPatientWithDraws() {
		if(patientWithDraws == null)
			patientWithDraws = new ArrayList<PatientWithDraw>();
		return patientWithDraws;
	}

	public void setPatientWithDraws(List<PatientWithDraw> patientWithDraws) {
		this.patientWithDraws = patientWithDraws;
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
