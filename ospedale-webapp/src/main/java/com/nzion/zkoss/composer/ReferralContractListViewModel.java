package com.nzion.zkoss.composer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.nzion.repository.notifier.utility.SmsUtil;
import com.nzion.repository.notifier.utility.TemplateNames;
import com.nzion.util.AfyaServiceConsumer;
import com.nzion.util.RestServiceConsumer;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.Window;

import com.nzion.domain.ReferralContract;
import com.nzion.hibernate.ext.multitenant.TenantIdHolder;
import com.nzion.service.common.CommonCrudService;
import com.nzion.service.servlet.ReferralContractApproveReject;
import com.nzion.util.Infrastructure;
import com.nzion.util.UtilMessagesAndPopups;
import com.nzion.zkoss.ext.Navigation;

@VariableResolver(DelegatingVariableResolver.class)
public class ReferralContractListViewModel extends  AutowirableComposer{
	private static final long serialVersionUID = 1L;
	
	@WireVariable
    private CommonCrudService commonCrudService;
	
	
    @Wire("#referralContractList")
    private Window referralContractList;
    
    private List<ReferralContract> initiatedReferralContractList = new ArrayList<>();
    
    private List<ReferralContract> requestedReferralContractList = new ArrayList<>();
    
    
    @AfterCompose
    public void init(@ContextParam(ContextType.VIEW) Component view) {
        Selectors.wireComponents(view, this, true);
        initiatedReferralContractList = commonCrudService.findByEquality(ReferralContract.class, new String[]{"refereeClinicId"}, new Object[]{Infrastructure.getPractice().getTenantId()});
        requestedReferralContractList = commonCrudService.findByEquality(ReferralContract.class, new String[]{"referralClinicId"}, new Object[]{Infrastructure.getPractice().getTenantId()});
        
    }
   
    @Command("approve")
    public void approve(ReferralContract referralContract){
    	/*referralContract.setContractStatus("ACCEPTED");
    	commonCrudService.save(referralContract);
    	
    	TenantIdHolder.setTenantId(referralContract.getRefereeClinicId());
    	ReferralContract refContract = commonCrudService.findUniqueByEquality(ReferralContract.class, new String[]{"referralClinicId","refereeClinicId"},
    			new Object[]{referralContract.getReferralClinicId(),referralContract.getRefereeClinicId()});
    	refContract.setContractStatus("ACCEPTED");
    	commonCrudService.save(refContract);*/
    	
    	ReferralContractApproveReject referralContractApproveReject = new ReferralContractApproveReject();
    	
    	referralContractApproveReject.referralApprove(referralContract.getId().toString(), referralContract.getReferralClinicId(), commonCrudService);
    	
    	TenantIdHolder.setTenantId(referralContract.getReferralClinicId());
    	
    	Navigation.navigate("referralContactList", null, "contentArea");
    	UtilMessagesAndPopups.showSuccess();
    }
    
    @Command("rejected")
    public void rejected(ReferralContract referralContract){
    	/*referralContract.setContractStatus("REJECTED");
    	commonCrudService.save(referralContract);
    	
    	TenantIdHolder.setTenantId(referralContract.getRefereeClinicId());
    	ReferralContract refContract = commonCrudService.findUniqueByEquality(ReferralContract.class, new String[]{"referralClinicId","refereeClinicId"},
    			new Object[]{referralContract.getReferralClinicId(),referralContract.getRefereeClinicId()});
    	refContract.setContractStatus("REJECTED");
    	commonCrudService.save(refContract);*/
    	
    	ReferralContractApproveReject referralContractApproveReject = new ReferralContractApproveReject();
    	
    	referralContractApproveReject.referralReject(referralContract.getId().toString(), referralContract.getReferralClinicId(), commonCrudService);
    	
    	TenantIdHolder.setTenantId(referralContract.getReferralClinicId());
    	
    	
    	Navigation.navigate("referralContactList", null, "contentArea");
    	UtilMessagesAndPopups.showSuccess();
    }


	public List<ReferralContract> getInitiatedReferralContractList() {
        //code start for status label  ( SUBMIT--> CREATED )
        Iterator<ReferralContract> referralContractIterator = initiatedReferralContractList.iterator();
        while (referralContractIterator.hasNext()){
            ReferralContract referralContract = referralContractIterator.next();
            String contractStatus = referralContract.getContractStatus().equals("SUBMIT") ? "VERIFIED" : referralContract.getContractStatus();
            contractStatus = contractStatus.equals("IN-PROGRESS") ? "CREATED" : contractStatus;
            referralContract.setContractStatusNewLabel(contractStatus);
        }
        //code end for status label
		return initiatedReferralContractList;
	}


	public void setInitiatedReferralContractList(List<ReferralContract> initiatedReferralContractList) {
		this.initiatedReferralContractList = initiatedReferralContractList;
	}


	public List<ReferralContract> getRequestedReferralContractList() {
        //code start for status label ( SUBMIT--> CREATED )
        Iterator<ReferralContract> referralContractIterator = requestedReferralContractList.iterator();
        while (referralContractIterator.hasNext()){
            ReferralContract referralContract = referralContractIterator.next();
            String contractStatus = referralContract.getContractStatus().equals("SUBMIT") ? "VERIFIED" : referralContract.getContractStatus();
            contractStatus = contractStatus.equals("IN-PROGRESS") ? "CREATED" : contractStatus;
            referralContract.setContractStatusNewLabel(contractStatus);
        }
        //code end for status label
		return requestedReferralContractList;
	}


	public void setRequestedReferralContractList(List<ReferralContract> requestedReferralContractList) {
		this.requestedReferralContractList = requestedReferralContractList;
	}

    public void view(final ReferralContract referralContract) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> refereeClinicDet = RestServiceConsumer.getClinicDetailsByClinicId(referralContract.getRefereeClinicId());
                Map<String, Object> userLoginMap = AfyaServiceConsumer.getUserLoginByTenantId(referralContract.getRefereeClinicId());
                if((userLoginMap != null) && (userLoginMap.get("languagePreference") != null)) {
                    refereeClinicDet.put("languagePreference", userLoginMap.get("languagePreference").toString());
                }
                Map<String, Object> referralClinicDet = RestServiceConsumer.getClinicDetailsByClinicId(referralContract.getReferralClinicId());
                refereeClinicDet.put("referralName", referralClinicDet.get("admin_first_name")+" "+referralClinicDet.get("admin_last_name"));
                refereeClinicDet.put("key", TemplateNames.REFERRAL_CONTRACT_VIEWED_SMS.name());
                refereeClinicDet.put("referralClinicName", referralClinicDet.get("clinic_name").toString());
                try {
                    SmsUtil.sendSmsForReferral(refereeClinicDet);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }).start();
    }
}
