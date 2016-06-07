package com.nzion.zkoss.composer;

import com.nzion.domain.*;
import com.nzion.domain.Enumeration;
import com.nzion.domain.base.FieldRestriction;
import com.nzion.domain.drug.Drug;
import com.nzion.factory.PatientFactory;
import com.nzion.hibernate.ext.multitenant.TenantIdHolder;
import com.nzion.repository.common.CommonCrudRepository;
import com.nzion.repository.notifier.utility.SmsUtil;
import com.nzion.repository.notifier.utility.TemplateNames;
import com.nzion.service.PatientService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.service.impl.FileBasedServiceImpl;
import com.nzion.util.*;
import com.nzion.view.PatientViewObject;
import com.nzion.zkoss.ext.Navigation;

import org.apache.commons.io.IOUtils;
import org.hibernate.Session;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.annotation.*;
import org.zkoss.image.AImage;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.SimpleConstraint;
import org.zkoss.zul.Span;
import org.zkoss.zul.Window;
import org.zkoss.zul.impl.InputElement;

import javax.annotation.Resource;
import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialException;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.*;

@VariableResolver(DelegatingVariableResolver.class)
public class ReferralContractViewModel extends  AutowirableComposer {

    private static final long serialVersionUID = 1L;
    
    @WireVariable
    private CommonCrudService commonCrudService;
    
    @Wire("#referralContractWin")
    private Window referralContractWin;

    List referrals;

    List<Referral> referralsPharmacy = new ArrayList<Referral>();

    List<Referral> referralsClinic = new ArrayList<Referral>();

    List<Referral> referralsLab = new ArrayList<Referral>();
    
    List referees;

    boolean percentageRequired;

    boolean serviceDetailVisible;
    
    
    private ReferralContract referralContract;

    public CommonCrudService getCommonCrudService() {
        return commonCrudService;
    }

    public void setCommonCrudService(CommonCrudService commonCrudService) {
        this.commonCrudService = commonCrudService;
    }


    @Command("save")
    public void save() {

    	List<ReferralContract> existingRefContract = commonCrudService.findByEquality(ReferralContract.class, 
    			new String[]{"referralClinicId","refereeClinicId"}, new Object[]{referralContract.getReferral().getTenantId(),Infrastructure.getPractice().getTenantId()});
    	for(ReferralContract refContract : existingRefContract){
    		if( !"REJECTED".equals(refContract.getContractStatus()) ){
    			Date contractDate = refContract.getContractDate();
    			Date expiryDate = refContract.getExpiryDate();
    			if(contractDate.compareTo(referralContract.getExpiryDate()) <= 0 && expiryDate.compareTo(referralContract.getContractDate()) >= 0
    					&& referralContract.getId() != null && !referralContract.getId().equals(refContract.getId()) ){
    				UtilMessagesAndPopups.showError("Contract already exists.");
    				return;
    			}else if(contractDate.compareTo(referralContract.getExpiryDate()) <= 0 && expiryDate.compareTo(referralContract.getContractDate()) >= 0
    					&& referralContract.getId() == null){
    				UtilMessagesAndPopups.showError("Contract already exists.");
    				return;
    			}
    		}
    	}
    	
    	referralContract.setContractStatus("IN-PROGRESS");
    	referralContract.setReferralClinicId(referralContract.getReferral().getTenantId());
    	referralContract.setRefereeClinicId(Infrastructure.getPractice().getTenantId());

        Iterator<ReferralContractService> referralContractServiceIterator = referralContract.getReferralContractServices().iterator();
        while (referralContractServiceIterator.hasNext()){
           ReferralContractService referralContractService =  referralContractServiceIterator.next();
            if(referralContract.getPercentageOnBill() != null){
                referralContractService.setPaymentPercentage(referralContract.getPercentageOnBill());
            }
        }
        
        try {
        	commonCrudService.save(referralContract);

            //communication loopback starts
            String adminUserName = Infrastructure.getPractice().getAdminUserLogin().getUsername();
            String userMiddleName = Infrastructure.getUserLogin().getPerson().getMiddleName() != null ? Infrastructure.getUserLogin().getPerson().getMiddleName()+" " : "";
            String user = Infrastructure.getUserLogin().getPerson().getFirstName()+" "+userMiddleName+Infrastructure.getUserLogin().getPerson().getLastName();
            Map<String, Object> details = AfyaServiceConsumer.getUserLoginByUserName(adminUserName);
            details.put("key",TemplateNames.REFERRAL_CONTRACT_SAVED_SMS.name());
            details.put("mobile", details.get("mobile_number"));
            details.put("user",user);
            SmsUtil.sendSmsForReferral(details);
            //communication loopback end

		} catch (Exception e) {
			try {
				commonCrudService.merge(referralContract);
			} catch (Exception e2) {
				// TODO: handle exception
			}
		}
        
        //if(!"PERCENTAGE_OF_BILL".equals(referralContract.getPaymentMode())){
	        //Navigation.navigate("referralContactList", null, "contentArea");
	        //referralContractWin.detach();
    	//}

        Navigation.navigate("referralContactList", null, "contentArea");
        UtilMessagesAndPopups.showSuccess();
    }
    
    @Command("submit")
    public void submit(){
    	save();
    	ReferralContract refcon = commonCrudService.getById(ReferralContract.class, referralContract.getId());
    	refcon.setContractStatus("SUBMIT");
    	commonCrudService.save(refcon);
    	updateReferralDb();
    	
    	if("PERCENTAGE_OF_BILL".equals(referralContract.getPaymentMode())){
	        Navigation.navigate("referralContactList", null, "contentArea");
	        referralContractWin.detach();
    	}
    	
    	UtilMessagesAndPopups.showSuccess();

        /*//code start for communication loop
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> referralClinicDet = RestServiceConsumer.getClinicDetailsByClinicId(referralContract.getReferralClinicId());
                String referralUserName = referralClinicDet.get("first_name")+" "+referralClinicDet.get("last_name");

                String languagePreference = AfyaServiceConsumer.getUserLoginByTenantId(referralContract.getRefereeClinicId()).get("languagePreference").toString();
                Map<String, Object> refereeClinicDet = RestServiceConsumer.getClinicDetailsByClinicId(referralContract.getRefereeClinicId());

                refereeClinicDet.put("referralUserName", referralUserName);
                refereeClinicDet.put("languagePreference", languagePreference);
                refereeClinicDet.put("key", TemplateNames.REFERRAL_CONTRACT_SUBMITTED.name());

                SmsUtil.sendSmsForReferral(refereeClinicDet);
            }
        }).start();
        //code end for communication loop*/
    }
    
    
    private void updateReferralDb(){
   	 TenantIdHolder.setTenantId(referralContract.getReferralClinicId());

   	 ReferralContract refContract =  commonCrudService.findUniqueByEquality(ReferralContract.class, new String[]{"referralClinicId","refereeClinicId","expiryDate","contractDate"}, 
   			 new Object[]{referralContract.getReferralClinicId(),referralContract.getRefereeClinicId(),referralContract.getExpiryDate(),referralContract.getContractDate()});
   	 Referral referral = commonCrudService.findUniqueByEquality(Referral.class, new String[]{"tenantId"}, new Object[]{referralContract.getRefereeClinicId()});
   	 if(refContract == null)
         refContract = new ReferralContract();
   	 refContract.setReferral(referral);
        refContract.setReferralType(referralContract.getReferralType());
        refContract.setExpiryDate(referralContract.getExpiryDate());
        refContract.setContractDate(referralContract.getContractDate());
        refContract.setPaymentMode(referralContract.getPaymentMode());
        refContract.setContractStatus(referralContract.getContractStatus());
        refContract.setPaypoint(referralContract.getPaypoint());
        refContract.setPercentageOnBill(referralContract.getPercentageOnBill());
        refContract.setDocument(referralContract.getDocument());
        refContract.setDocumentName(referralContract.getDocumentName());
        refContract.setReferralClinicId(referralContract.getReferralClinicId());
        refContract.setRefereeClinicId(referralContract.getRefereeClinicId());
        Infrastructure.getSessionFactory().getCurrentSession().clear();
        commonCrudService.save(refContract);
        if(UtilValidator.isEmpty(refContract.getReferralContractServices())){
	         for(ReferralContractService reffContractService : referralContract.getReferralContractServices()){
	         	ReferralContractService contractService = new ReferralContractService(reffContractService.getPaymentPercentage(), 
	         			reffContractService.getPaymentAmount(),reffContractService.getServiceCode(),reffContractService.getServiceName(),
	         			reffContractService.getServiceSubGroupDescription(), 
	         			reffContractService.getServiceSubGroupId(),reffContractService.getServiceMainGroup(),
	         			reffContractService.getServiceMainGroupId(), refContract);
	         	commonCrudService.save(contractService);
	         }
        }else{
       	 for(ReferralContractService reffContractService : refContract.getReferralContractServices()){
	         	commonCrudService.save(reffContractService);
	         } 
        }
        
        TenantIdHolder.setTenantId(referralContract.getRefereeClinicId());
   }

    @AfterCompose
    public void init(@ContextParam(ContextType.VIEW) Component view) {
        Selectors.wireComponents(view, this, true);
        referralContract = (ReferralContract) referralContractWin.getAttribute("referralContract");
        percentageRequired = false;
        if (ReferralContract.PAYMENT_MODE_ENUM.PERCENTAGE_OF_BILL.toString().equals(referralContract.getPaymentMode())) {
            percentageRequired = true;
        }
        referrals = commonCrudService.getAll(com.nzion.domain.Referral.class, "clinicName");
        for (Object referralObject : referrals){
            Referral referral = (Referral)referralObject;
            if ((referral.getReferralType() != null) && (referral.getReferralType().equals("CLINIC"))){
                referralsClinic.add(referral);
            } else if ((referral.getReferralType() != null) && (referral.getReferralType().equals("PHARMACY"))){
                referralsPharmacy.add(referral);
            } else if ((referral.getReferralType() != null) && (referral.getReferralType().equals("LAB"))){
                referralsLab.add(referral);
            }
        }
        referees = commonCrudService.getAll(com.nzion.domain.Employee.class);
    }
    
    @Command("uploadFile")
    @NotifyChange("referralContract")
    public void uploadFile(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) throws SerialException, SQLException, IOException{
		UploadEvent upEvent = null;
		Object objUploadEvent = ctx.getTriggerEvent();
		if (objUploadEvent != null && (objUploadEvent instanceof UploadEvent)) {
	         upEvent = (UploadEvent) objUploadEvent;
	    }
		if (upEvent != null) {
		    Media media = upEvent.getMedia();
		    Blob blob = null;
		    if("pdf".equals(media.getFormat())){
		    	byte[] content = IOUtils.toByteArray(media.getStreamData());
			    blob = new SerialBlob(content);
			    referralContract.setDocumentName(media.getName());
			    referralContract.setDocument(blob);
		    }else{
		    	UtilMessagesAndPopups.showError("Please upload valid document");
		    }
		 }
    }
    
    @Command("downloadFile")
    public void downloadFile() throws SerialException, SQLException, IOException{
    	if(referralContract == null)
    		return;
    	if(referralContract.getDocument() == null)
    		return;
    	Blob blob = referralContract.getDocument();
    	if(blob != null)
    		Filedownload.save(blob.getBytes(1, (int) blob.length()), "", referralContract.getDocumentName());
    	
    }

    public ReferralContract getReferralContract() {
        return referralContract;
    }

    public void setReferralContract(ReferralContract referralContract) {
        this.referralContract = referralContract;
    }

    public List getReferrals() {
        return referrals;
    }

    public void setReferrals(List referrals) {
        this.referrals = referrals;
    }

    public List getReferees() {
        return referees;
    }

    public void setReferees(List referees) {
        this.referees = referees;
    }

    public boolean isServiceDetailVisible() {
        return serviceDetailVisible;
    }

    public void setServiceDetailVisible(boolean serviceDetailVisible) {
        this.serviceDetailVisible = serviceDetailVisible;
    }

    public boolean isPercentageRequired() {
        return percentageRequired;
    }

    public void setPercentageRequired(boolean percentageRequired) {
        this.percentageRequired = percentageRequired;
    }

    public List getReferralsPharmacy() {
        return referralsPharmacy;
    }

    public void setReferralsPharmacy(List referralsPharmacy) {
        this.referralsPharmacy = referralsPharmacy;
    }

    public List getReferralsClinic() {
        return referralsClinic;
    }

    public void setReferralsClinic(List referralsClinic) {
        this.referralsClinic = referralsClinic;
    }

    public List getReferralsLab() {
        return referralsLab;
    }

    public void setReferralsLab(List referralsLab) {
        this.referralsLab = referralsLab;
    }
}