package com.nzion.zkoss.composer.emr;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nzion.zkoss.composer.OspedaleAutowirableComposer;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Window;

import com.nzion.domain.emr.IcdElement;
import com.nzion.domain.emr.IcdGroup;
import com.nzion.service.common.CommonCrudService;
import com.nzion.service.emr.IcdService;
import com.nzion.util.UtilMessagesAndPopups;
import com.nzion.util.UtilMisc;
import com.nzion.util.UtilValidator;

public class IcdGroupController extends OspedaleAutowirableComposer {

	private IcdService icdService;

	private IcdGroup icdGroup;

	private Set<IcdElement> icdElements = new HashSet<IcdElement>();

	private Set<IcdElement> selectedIcdElements;

	private CommonCrudService commonCrudService;

	Listbox icdElementListbox;

	public void doAfterCompose(Component component) throws Exception {
	super.doAfterCompose(component);
	icdGroup = (IcdGroup) Executions.getCurrent().getArg().get("entity");
	if (icdGroup == null) {
		icdGroup = new IcdGroup();
		return;
	}
	icdGroup = commonCrudService.refreshEntity(icdGroup);
	icdElements = icdGroup.getIcdElements();
	}

	public List<IcdElement> searchIcd(String code, String description) {
	return icdService.searchIcdBy(code, description);
	}

	public void saveIcdGroup() {
	icdGroup.setIcdElements(icdElements);
	commonCrudService.save(icdGroup);
	UtilMessagesAndPopups.showSuccess();
	}

	public void openAddIcdWindow() {
	Executions.createComponents("/emr/icdLookUp.zul", null, UtilMisc.toMap("controller", this));
	}

	public void addIcd(Window parent) {
	if(UtilValidator.isEmpty(selectedIcdElements)){
		UtilMessagesAndPopups.displayError("Select atleast one ICD");
		return;
	}
	icdElements.addAll(selectedIcdElements);
	Events.postEvent("onReloadRequest", icdElementListbox, null);
	parent.detach();
	}

	@SuppressWarnings("unchecked")
	public void removeIcd() {
	Set<Listitem> selectedItems = (Set<Listitem>)icdElementListbox.getSelectedItems();
	Set<IcdElement> icds = new HashSet<IcdElement>();
	for(Listitem listitem : selectedItems)
		icds.add((IcdElement)listitem.getValue());
	icdElements.removeAll(icds);
	Events.postEvent("onReloadRequest", icdElementListbox, null);
	}

	public CommonCrudService getCommonCrudService() {
	return commonCrudService;
	}

	public void setCommonCrudService(CommonCrudService commonCrudService) {
	this.commonCrudService = commonCrudService;
	}

	public IcdGroup getIcdGroup() {
	return icdGroup;
	}

	public void setIcdGroup(IcdGroup icdGroup) {
	this.icdGroup = icdGroup;
	}

	public Set<IcdElement> getIcdElements() {
	return icdElements;
	}

	public void setIcdElements(Set<IcdElement> icdElements) {
	this.icdElements = icdElements;
	}

	public IcdService getIcdService() {
	return icdService;
	}

	public void setIcdService(IcdService icdService) {
	this.icdService = icdService;
	}

	public Set<IcdElement> getSelectedIcdElements() {
	return selectedIcdElements;
	}

	public void setSelectedIcdElements(Set<IcdElement> selectedIcdElements) {
	this.selectedIcdElements = selectedIcdElements;
	}

	private static final long serialVersionUID = 1L;
}
