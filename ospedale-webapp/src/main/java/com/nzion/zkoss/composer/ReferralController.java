package com.nzion.zkoss.composer;

import java.util.List;
import java.util.Map;

import com.nzion.domain.Referral;
import com.nzion.service.common.CommonCrudService;
import com.nzion.util.AfyaServiceConsumer;
import com.nzion.util.Infrastructure;
import com.nzion.util.UtilValidator;

public class ReferralController extends OspedaleAutowirableComposer{
	
	private CommonCrudService commonCrudService = Infrastructure.getSpringBean("commonCrudService");
	
	public ReferralController(){
		List<Map<String, Object>> listOfSmartClinicAndPharmacy = AfyaServiceConsumer.getAllSmartClinicPharmacy(Infrastructure.getPractice().getTenantId());
		for(Map<String, Object> map : listOfSmartClinicAndPharmacy){
			List<Referral> referrals = null;
			try{
				referrals = commonCrudService.findByEquality(Referral.class, new String[]{"tenantId"}, new Object[]{map.get("tenantId")});
			}catch(Exception e){
				
			}
			if(UtilValidator.isEmpty(referrals)){
				Referral referral = new Referral();
				referral.setFirstName(map.get("adminFirstName") != null ? map.get("adminFirstName").toString() : "");
				referral.setLastName(map.get("adminLastName") != null ? map.get("adminLastName").toString() : "");
				referral.getContacts().setEmail( map.get("email") != null ? map.get("email").toString() : "" );
				referral.getContacts().setMobileNumber(  map.get("mobile") != null ? map.get("mobile").toString() : "" );
				referral.setClinicName(map.get("clinicName") != null ? map.get("clinicName").toString() : "");
				//referral.getContacts().getPostalAddress().setAddress1(address1);
				//referral.getContacts().getPostalAddress().setAddress2(address2);
				referral.getContacts().getPostalAddress().setCity(map.get("city") != null ? map.get("city").toString() : "");
				referral.setTenantId(map.get("tenantId") != null ? map.get("tenantId").toString() : "");
				referral.setAfyaRegistered(true);
				commonCrudService.save(referral);
			}
		}
	}

}
