package com.nzion.zkoss.composer.appointment;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nzion.zkoss.composer.OspedaleAutowirableComposer;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;

import com.nzion.domain.Person;
import com.nzion.domain.screen.ScheduleCustomView;
import com.nzion.service.common.CommonCrudService;
import com.nzion.util.UtilValidator;

/**
 * @author Sandeep Prusty
 * Jun 9, 2010
 */

@SuppressWarnings("unchecked")
public class ScheduleCustomViewController extends OspedaleAutowirableComposer {

	private CommonCrudService commonCrudService;
	
	private ScheduleCustomView selectedView = new ScheduleCustomView();
	
	public ScheduleCustomViewController(ScheduleCustomView choosenView) {
	if(choosenView != null)
		this.selectedView = choosenView;
	}
	
	public void save(){
	populatePersonsToVo();
	if(UtilValidator.isEmpty(selectedView.getPersons()))
		throw new RuntimeException("Choose at least one of the providers or resources");
	commonCrudService.save(selectedView);
	}

	private void populatePersonsToVo() {
	Set<Listitem> selectedProviderListItems = ((Listbox)(root.getFellow("providerList"))).getSelectedItems();
	Set<Person> persons = new HashSet<Person>();
	for(Listitem item : selectedProviderListItems)
		persons.add((Person)item.getAttribute("person"));
	selectedView.clearAndAddPersons(persons);
	}

	@Override
	public void doAfterCompose(Component component) throws Exception {
	super.doAfterCompose(component);
	if(selectedView.getId() == null)
		return;
	populateProviderSelectionsToView(component);
	}
	
	private void populateProviderSelectionsToView(Component window){
	List<Component> providerListboxChildren = window.getFellow("providerList").getChildren();
	for(int i = 1 ; i < providerListboxChildren.size() ; ++i){
		Listitem currentItem = (Listitem)providerListboxChildren.get(i);
		if(selectedView.getPersons().contains(currentItem.getAttribute("person")))
			currentItem.setSelected(true);
	}
	}

	public void setCommonCrudService(CommonCrudService commonCrudService) {
	this.commonCrudService = commonCrudService;
	}

	public ScheduleCustomView getSelectedView() {
	return selectedView;
	}
	
	public void setSelectedView(ScheduleCustomView selectedView) {
	this.selectedView = selectedView;
	}

	private static final long serialVersionUID = 1L;
}