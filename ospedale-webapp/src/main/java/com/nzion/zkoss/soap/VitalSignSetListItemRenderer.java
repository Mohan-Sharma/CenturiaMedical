package com.nzion.zkoss.soap;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Window;

import com.nzion.domain.emr.UnitOfMeasurement;
import com.nzion.domain.emr.VitalSign;
import com.nzion.domain.emr.soap.PatientVitalSign;
import com.nzion.domain.emr.soap.PatientVitalSignSet;
import com.nzion.util.Constants;
import com.nzion.util.UtilDateTime;
import com.nzion.util.UtilValidator;

/**
 * @author Sandeep Prusty
 * Feb 7, 2011
 */
public class VitalSignSetListItemRenderer implements ListitemRenderer {

	private final Map<VitalSign, Integer> map = new HashMap<VitalSign, Integer>();
	
	private final List<VitalSign> preferedVitalSigns; 
	
	private Listbox listbox;

	public void setListbox(Listbox listbox) {
	this.listbox = listbox;
	}

	public VitalSignSetListItemRenderer(List<VitalSign> preferedVitalSigns) {
	this.preferedVitalSigns = preferedVitalSigns;
	Collections.sort(this.preferedVitalSigns, new Comparator<VitalSign>() {
		@Override
		public int compare(VitalSign o1, VitalSign o2) {
		return o1.getSortOrder().compareTo(o2.getSortOrder());
		}
	});
	int index = 1;
	for(VitalSign personVitalSign : preferedVitalSigns)
		map.put(personVitalSign, index++);
	}
	
	public List<VitalSign> getPreferedVitalSigns() {
	return preferedVitalSigns;
	}

	@Override
	public void render(Listitem item, Object object,int index) throws Exception {
	PatientVitalSignSet patientVitalSignSet = (PatientVitalSignSet)object;
	item.setValue(patientVitalSignSet);
	item.addEventListener("onDoubleClick", DOUBLE_CLICK_LISTENER);
	if(patientVitalSignSet.getVitalSignSection()!=null){
	Listcell dateCell = new Listcell(UtilDateTime.format(patientVitalSignSet.getRecordedOn()));
	dateCell.setParent(item);
	}
	Listcell timeCell = new Listcell(UtilDateTime.format(patientVitalSignSet.getRecordedOn(), UtilDateTime.AM_PM_FORMATTER));
	timeCell.setParent(item);
	for(int i = 0; i < map.size() + 1 ; ++i){
		Listcell cell = new Listcell();
		cell.setParent(item);
	}
    NumberFormat formatter = NumberFormat.getInstance();
    formatter.setMinimumFractionDigits(2);
    formatter.setMaximumFractionDigits(2);
    formatter.setRoundingMode(RoundingMode.HALF_EVEN);
	for(PatientVitalSign patientVitalSign : patientVitalSignSet.getVitalSigns()){
		if(UtilValidator.isEmpty(patientVitalSign.getValue()))
			continue;
		Listcell cell = (Listcell)item.getChildren().get(map.get(patientVitalSign.getVitalSign()));
		UnitOfMeasurement uom = patientVitalSign.getUom();
		Object value = UtilValidator.isEmpty(patientVitalSign.getValue()) ? "" : formatter.format(formatter.parseObject(patientVitalSign.getValue()));  
		cell.setLabel(value + Constants.BLANK + (uom == null ? "" : uom.getCode()));
	}
	}
	
	public final EventListener DOUBLE_CLICK_LISTENER = new EventListener() {
		@Override
		public void onEvent(Event event) throws Exception {
		Window window = (Window) Executions.createComponents("/soap/soapPatientVitalSign.zul", null, com.nzion.util.UtilMisc.toMap("entity", ((Listitem)event.getTarget()).getValue()));
		window.addForward("onDetach", listbox, "onReloadRequest");
		}
	};
}