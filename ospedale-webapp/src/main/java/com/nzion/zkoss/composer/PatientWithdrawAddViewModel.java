package com.nzion.zkoss.composer;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import com.nzion.domain.PatientWithDraw;
import com.nzion.domain.billing.AcctgTransTypeEnum;
import com.nzion.domain.billing.AcctgTransactionEntry;
import com.nzion.domain.billing.DebitCreditEnum;
import com.nzion.service.billing.BillingService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.util.Infrastructure;
import com.nzion.util.UtilMessagesAndPopups;
import com.nzion.util.UtilValidator;

@VariableResolver(DelegatingVariableResolver.class)
public class PatientWithdrawAddViewModel {
	
	@WireVariable
	private CommonCrudService commonCrudService;
	
	@Wire("#patientWithDrawAddWin")
	private Window patientWithDrawAddWin;
	
	private PatientWithDraw patientWithDraw;
	
	@WireVariable
	private BillingService billingService;
	
	@AfterCompose
	public void init(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, true);
		patientWithDraw = new PatientWithDraw();
	}
	
	@Command("save")
	public void save(){
		if(patientWithDraw.getPatient() == null){
			UtilMessagesAndPopups.showError("Patient cannot be empty");
			return;
		}
		if(patientWithDraw.getWithdrawAmount().compareTo(BigDecimal.ZERO) <= 0){
			UtilMessagesAndPopups.showError("Amount must be greater than zero");
			return;
		}
		BigDecimal totalAvailableAmount = calculateAmount(patientWithDraw.getPatient());
		totalAvailableAmount = totalAvailableAmount.subtract(patientWithDraw.getWithdrawAmount());
		if(UtilValidator.isEmpty(totalAvailableAmount) || totalAvailableAmount.compareTo(BigDecimal.ZERO) < 0){
			UtilMessagesAndPopups.showError("No sufficient amount available for refund");
			return;
		}
		patientWithDraw.setTotalAvailableAmount(totalAvailableAmount);
		patientWithDraw.setStatus("Refunded");
		patientWithDraw.setCreatedPerson(Infrastructure.getLoggedInPerson());
		patientWithDraw.setWithdrawDate(new Date());
		commonCrudService.save(patientWithDraw);
		billingService.updatePatientWithdraw(patientWithDraw.getPatient(), patientWithDraw.getWithdrawAmount());
		if(patientWithDrawAddWin.getAttribute("patientWithdrawListBox") != null)
			Events.postEvent("onReloadRequest",(Component)patientWithDrawAddWin.getAttribute("patientWithdrawListBox"),null);
		UtilMessagesAndPopups.showSuccess();
		patientWithDrawAddWin.detach();
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

	public Window getPatientWithDrawAddWin() {
		return patientWithDrawAddWin;
	}

	public void setPatientWithDrawAddWin(Window patientWithDrawAddWin) {
		this.patientWithDrawAddWin = patientWithDrawAddWin;
	}

	public PatientWithDraw getPatientWithDraw() {
		return patientWithDraw;
	}

	public void setPatientWithDraw(PatientWithDraw patientWithDraw) {
		this.patientWithDraw = patientWithDraw;
	}
	
}
