package com.nzion.zkoss.composer.emr;

import java.util.Collections;
import java.util.List;

import com.nzion.zkoss.composer.OspedaleAutowirableComposer;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Listbox;

import com.nzion.domain.emr.SoapModule;
import com.nzion.service.common.CommonCrudService;
import com.nzion.util.UtilMessagesAndPopups;
import com.nzion.util.UtilValidator;
import com.nzion.util.ViewUtil;

public class SoapModuleController extends OspedaleAutowirableComposer {

	private static final long serialVersionUID = 1L;
	private SoapModule newSoapModule = new SoapModule();
	private List<SoapModule> soapModules;
	private CommonCrudService commonCrudService;

	@Override
	public void doAfterCompose(Component component) throws Exception {
	super.doAfterCompose(component);
	soapModules = commonCrudService.getAll(SoapModule.class);
	Collections.sort(soapModules, SoapModule.SORTORDERCOMPARATOR);
	component.setAttribute("vo", newSoapModule);
	}

	public void setCommonCrudService(CommonCrudService commonCrudService) {
	this.commonCrudService = commonCrudService;
	}

	public SoapModule getNewSoapModule() {
	return newSoapModule;
	}

	public List<SoapModule> getSoapModules() {
	return soapModules;
	}

	public void onClick$AddSoapModule(Event event) {
	Integer sortOrder = 0;
	if(UtilValidator.isNotEmpty(soapModules)) {
		SoapModule maxSortOrderSoapModule = Collections.max(soapModules, SoapModule.SORTORDERCOMPARATOR);
		sortOrder = maxSortOrderSoapModule.getSortOrder() + 1;
	}
	newSoapModule.setSortOrder(sortOrder);
	commonCrudService.save(newSoapModule);
	soapModules.add(newSoapModule);
	newSoapModule = new SoapModule();
	UtilMessagesAndPopups.showSuccess("Soap Module Added SucessFully");
	}

	public void onClick$UpdateSoapModules(Event event) {
	Listbox listbox = (Listbox)event.getTarget().getFellowIfAny("soapModuleList", true);
	List<SoapModule> selectedSoapModules = ViewUtil.getSelectedItems(listbox);
	commonCrudService.save(selectedSoapModules);
	UtilMessagesAndPopups.showSuccess();
	}
	
	public void onClick$DeleteSoapModules(Event event) {
	Listbox listbox = (Listbox)event.getTarget().getFellowIfAny("soapModuleList", true);
	List<SoapModule> selectedSoapModules = ViewUtil.getSelectedItems(listbox);
	commonCrudService.delete(selectedSoapModules);
	soapModules.removeAll(selectedSoapModules);
	}
}
