package com.nzion.zkoss.composer;

import java.util.List;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
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
import org.zkoss.zul.Listbox;

import com.nzion.domain.Patient;
import com.nzion.domain.PharmacyOrder;
import com.nzion.domain.PharmacyOrder.PharmacyOrderStatus;
import com.nzion.service.SoapNoteService;
import com.nzion.service.common.CommonCrudService;

@VariableResolver(DelegatingVariableResolver.class)
public class PharmacyOrderViewModel {
	
	@WireVariable
    private CommonCrudService commonCrudService;

    @WireVariable
    private SoapNoteService soapNoteService;

    private Patient patient;
    
    private List<PharmacyOrder> pharmacyOrders;
    
    @Wire("#pharmacyOrderListBox")
    private Listbox pharmacyOrderListBox;
    
    @AfterCompose
    public void init(@ContextParam(ContextType.VIEW) Component view, @BindingParam("patient") Patient patient) {
        Selectors.wireComponents(view, this, true);
        this.patient = patient;
        pharmacyOrders = commonCrudService.findByEquality(PharmacyOrder.class, new String[]{"patient"}, new Object[]{patient});
    }
    
    @Command("update")
    public void update(PharmacyOrder pharmacyOrder){
    	pharmacyOrder.setPharOrderStatus(PharmacyOrderStatus.COMPLETED);
    	soapNoteService.completeOrder(pharmacyOrder.getOrderId(), pharmacyOrder.getPharmacyTennantId());
    	commonCrudService.save(pharmacyOrder);
    	Events.postEvent("onReload",pharmacyOrderListBox,null);
    }

	public CommonCrudService getCommonCrudService() {
		return commonCrudService;
	}

	public void setCommonCrudService(CommonCrudService commonCrudService) {
		this.commonCrudService = commonCrudService;
	}

	public SoapNoteService getSoapNoteService() {
		return soapNoteService;
	}

	public void setSoapNoteService(SoapNoteService soapNoteService) {
		this.soapNoteService = soapNoteService;
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public List<PharmacyOrder> getPharmacyOrders() {
		return pharmacyOrders;
	}

	public void setPharmacyOrders(List<PharmacyOrder> pharmacyOrders) {
		this.pharmacyOrders = pharmacyOrders;
	}
    
    

}
