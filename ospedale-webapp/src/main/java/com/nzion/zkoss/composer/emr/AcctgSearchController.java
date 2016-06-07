package com.nzion.zkoss.composer.emr;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nzion.domain.Referral;
import com.nzion.repository.common.impl.HibernateCommonCrudRepository;
import com.nzion.service.common.CommonCrudService;
import com.nzion.service.common.impl.CommonCrudServiceImpl;
import com.nzion.util.UtilDateTime;
import org.apache.poi.ss.usermodel.Textbox;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Footer;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Separator;

import com.nzion.domain.Patient;
import com.nzion.domain.Provider;
import com.nzion.domain.Speciality;
import com.nzion.domain.billing.AcctgTransTypeEnum;
import com.nzion.domain.billing.AcctgTransactionEntry;
import com.nzion.domain.billing.DebitCreditEnum;
import com.nzion.service.SoapNoteService;
import com.nzion.util.UtilValidator;

@VariableResolver(DelegatingVariableResolver.class)
public class AcctgSearchController{
	private static final long serialVersionUID = 1L;

    @WireVariable
	private SoapNoteService soapNoteService;

	private Long providerId;
	
	private Long patientId;
	
	private Long encounterId;

    private Speciality speciality;

    private Long invoiceId;

	private AcctgTransTypeEnum acctgTransTypeEnum;
	
	private String ipNumber;

    private List<Speciality> specialityTypeList;

    private List<Referral> referrals;

    private Referral selectedReferral;

    private BigDecimal totalCredits = BigDecimal.ZERO;
    private BigDecimal totalDebits = BigDecimal.ZERO;

    @Wire("#footer")
    private Footer footer;

    @Wire("#acctgListGrid")
    private Grid grid;

    @WireVariable
    private CommonCrudService commonCrudService;

    @Wire
    private HtmlMacroComponent providerLookUpBox;
    
    @Wire
    private HtmlMacroComponent patientLookUpBox;
    
    private List<AcctgTransactionEntry> acctgTransactionEntries;

    @Init
    @AfterCompose
    public void init(@ContextParam(ContextType.VIEW) Component view){
        Selectors.wireComponents(view, this, true);
        specialityTypeList = commonCrudService.getAll(com.nzion.domain.Speciality.class);
        referrals = commonCrudService.getAll(Referral.class);
    }

    @Command("Search")
    @NotifyChange("acctgTransactionEntries")
    public void search(@BindingParam("fromDate")Datebox fromDate,@BindingParam("thruDate")Datebox thruDate){
	acctgTransactionEntries = soapNoteService.getAcctgTransEntryByCriteria(fromDate.getValue(),thruDate.getValue(),providerId, patientId, encounterId, invoiceId, getSpecialityCode(), getReferral(),true);//extPatientCheckbox is always true
		 com.nzion.service.common.CommonCrudService commonCrudService = com.nzion.util.Infrastructure.getSpringBean("commonCrudService");
         totalCredits = BigDecimal.ZERO;
         totalDebits = BigDecimal.ZERO;
        for(AcctgTransactionEntry entry : acctgTransactionEntries){

        	if(entry.getPatientId()!=null){
        	Patient patient = null;//entry.getPatientId();
        	patient = commonCrudService.getById(Patient.class, Long.parseLong(entry.getPatientId()));
                if(patient != null){
                    entry.setPatient(patient);
                }
        	}
            Provider provider = null;
            if(entry.getDoctorId()!=null && entry.getDoctorId()!="")
            {
                try{
                    provider = commonCrudService.getById(Provider.class, Long.parseLong(entry.getDoctorId()));//entry.getProvider();
                    entry.setProvider(provider);
                }catch (Exception e){
                    provider = null;
                }
            }
                /*Set<Speciality> specialities = provider != null ? provider.getSpecialities() : new HashSet<Speciality>();
                if (com.nzion.util.UtilValidator.isNotEmpty(specialities) && specialities.size() == 1) {
                    entry.setSpecialityCode(specialities.iterator().next().toString());
                }//(specialities.iterator().next());*/

            /*entry.setTransactionDateAsString(entry.getTransactionDate().toString().substring(0, 10));*/
            entry.setTransactionDateAsString(UtilDateTime.format(entry.getTransactionDate(),new SimpleDateFormat("dd/MM/yyyy")));

            if(DebitCreditEnum.CREDIT.equals(entry.getDebitOrCredit()))
                totalCredits=totalCredits.add(entry.getAmount());
            else if(DebitCreditEnum.DEBIT.equals(entry.getDebitOrCredit())){
                totalDebits=totalDebits.add(entry.getAmount());
            }
        }
        showCreditDebitAmount();
        /*int colspan = grid.getColumns().getChildren().size();
        footer.getChildren().clear();
        footer.setSpan(colspan);
        Div div = new Div();
        div.setStyle("text-align:right");
        div.setParent(footer);
        Label l1 = new Label(" Total Credits = "+totalCredits );
        l1.setStyle("font-size:14px");
        l1.setParent(div);
        Separator sep = new Separator();
        sep.setParent(div);
        Label l2 = new Label("  Total Debits  = "+totalDebits);
        l2.setStyle("font-size:14px");
        l2.setParent(div);*/
	}



	@Command("Reset")
    @NotifyChange({"acctgTransactionEntries"})
    public void reset(@BindingParam("fromDate")Datebox fromDate,@BindingParam("thruDate")Datebox thruDate,@BindingParam("referralCombo")Combobox referralCombo,@BindingParam("specialityCombo")Combobox specialityCombo){
    	fromDate.setValue(new Date());
    	thruDate.setValue(new Date());
        if( referralCombo != null){
            referralCombo.setValue(null);
            this.setSelectedReferral(null);
        }
        if(specialityCombo != null){
            specialityCombo.setValue(null);
            this.setSpeciality(null);
        }
    	providerLookUpBox.recreate();
    	patientLookUpBox.recreate();
        totalCredits = BigDecimal.ZERO;
        totalDebits = BigDecimal.ZERO;
    	this.acctgTransactionEntries = null;
        showCreditDebitAmount();
	}


	public SoapNoteService getSoapNoteService() {
		return soapNoteService;
	}

	public void setSoapNoteService(SoapNoteService soapNoteService) {
		this.soapNoteService = soapNoteService;
	}

	public Long getProviderId() {
		return providerId;
	}

	public void setProviderId(Long providerId) {
		this.providerId = providerId;
	}

	public Long getPatientId() {
		return patientId;
	}

	public void setPatientId(Long patientId) {
		this.patientId = patientId;
	}

	public Long getEncounterId() {
		return encounterId;
	}

	public void setEncounterId(Long encounterId) {
		this.encounterId = encounterId;
	}

	public Long getInvoiceId() {
		return invoiceId;
	}

	public void setInvoiceId(Long invoiceId) {
		this.invoiceId = invoiceId;
	}

	
	public AcctgTransTypeEnum getAcctgTransTypeEnum() {
		return acctgTransTypeEnum;
	}

	public void setAcctgTransTypeEnum(AcctgTransTypeEnum acctgTransTypeEnum) {
		this.acctgTransTypeEnum = acctgTransTypeEnum;
	}

    public Speciality getSpeciality() {
        return speciality;
    }

    public void setSpeciality(Speciality speciality) {
        this.speciality = speciality;
    }

	public String getIpNumber() {
		return ipNumber;
	}

	public void setIpNumber(String ipNumber) {
		this.ipNumber = ipNumber;
	}

	public List<AcctgTransactionEntry> getAcctgTransactionEntries() {
		return acctgTransactionEntries;
	}

	public void setAcctgTransactionEntries(
			List<AcctgTransactionEntry> acctgTransactionEntries) {
		this.acctgTransactionEntries = acctgTransactionEntries;
	}

    public List<Speciality> getSpecialityTypeList() {
        return specialityTypeList;
    }

    public void setSpecialityTypeList(List<Speciality> specialityTypeList) {
        this.specialityTypeList = specialityTypeList;
    }

    public List<Referral> getReferrals() {
        return referrals;
    }

    public void setReferrals(List<Referral> referrals) {
        this.referrals = referrals;
    }

    public Referral getSelectedReferral() {
        return selectedReferral;
    }

    public void setSelectedReferral(Referral selectedReferral) {
        this.selectedReferral = selectedReferral;
    }

    public String getSpecialityCode(){
        if (getSpeciality() != null){
            return getSpeciality().getDescription();
        }else{
            return null;
        }
    }

    public String getReferral(){
        if (getSelectedReferral() != null){
            return getSelectedReferral().getId().toString();
        }else{
            return null;
        }
    }

    public void showCreditDebitAmount(){
        int colspan = grid.getColumns().getChildren().size();
        footer.getChildren().clear();
        footer.setSpan(colspan);
        Div div = new Div();
        div.setStyle("text-align:right");
        div.setParent(footer);
        Label l1 = new Label(" Total Credits = "+totalCredits );
        l1.setStyle("font-size:14px");
        l1.setParent(div);
        Separator sep = new Separator();
        sep.setParent(div);
        Label l2 = new Label("  Total Debits  = "+totalDebits);
        l2.setStyle("font-size:14px");
        l2.setParent(div);
    }
}



