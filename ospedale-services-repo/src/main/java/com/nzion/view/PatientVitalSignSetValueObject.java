package com.nzion.view;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nzion.domain.emr.UnitOfMeasurement;
import com.nzion.domain.emr.VitalSign;
import com.nzion.domain.emr.soap.PatientVitalSign;
import com.nzion.domain.emr.soap.PatientVitalSignSet;
import com.nzion.util.Constants;
import com.nzion.util.UtilDateTime;
import com.nzion.util.UtilValidator;

public class PatientVitalSignSetValueObject {

	private List<PatientVitalSignSet> patientVitalSignSets;
	
	private final Map<VitalSign, Integer> map = new HashMap<VitalSign, Integer>();
	
	private List<VitalSign> vitalSigns;
	
	private String[][] patientVitalSignSetView;
	
	
	public List<PatientVitalSignSet> getPatientVitalSignSets() {
	return patientVitalSignSets;
	}

	public PatientVitalSignSetValueObject(Set<PatientVitalSignSet> patientVitalSignSets,List<VitalSign> vitalSigns){
	this.patientVitalSignSets = new ArrayList<PatientVitalSignSet>(patientVitalSignSets);
	this.vitalSigns = vitalSigns; 
	Collections.sort(this.vitalSigns, new Comparator<VitalSign>() { 

	@Override
	public int compare(VitalSign o1, VitalSign o2) {
	return o1.getSortOrder().compareTo(o2.getSortOrder());
	}
	});
	int index = 1;
	for(VitalSign personVitalSign : vitalSigns)
		map.put(personVitalSign, index++);
	patientVitalSignSetView = new String[vitalSigns.size()+2][patientVitalSignSets.size() + 1];
	patientVitalSignSetView[0][0] = patientVitalSignSetView[1][0] = "";
	int i = 2; 
	for(VitalSign vitalSign : this.vitalSigns)
		patientVitalSignSetView[i++][0] = vitalSign.getName();
	}

	public List<VitalSign> getVitalSigns() {
	return vitalSigns;
	}
	
	
	public String[][] populatePatientVitalSignSet() throws ParseException{
	    NumberFormat formatter = NumberFormat.getInstance();
	    formatter.setMinimumFractionDigits(2); 
	    formatter.setMaximumFractionDigits(2); 
	    formatter.setRoundingMode(RoundingMode.HALF_EVEN); 
		for(int j = 0 ; j < patientVitalSignSets.size();j++){
			PatientVitalSignSet patientVitalSignSet = patientVitalSignSets.get(j);
			for(PatientVitalSign patientVitalSign : patientVitalSignSet.getVitalSigns()){
				UnitOfMeasurement uom = patientVitalSign.getUom();
				int	i = map.get(patientVitalSign.getVitalSign());
				patientVitalSignSetView[i + 1][j + 1] = new String(UtilValidator.isEmpty(patientVitalSign.getValue()) ? "" : formatter.format(formatter.parseObject(patientVitalSign.getValue())))+ Constants.BLANK + (UtilValidator.isNotEmpty(patientVitalSign.getValue()) ? (uom == null ? "" : uom.getCode()) : "");
			}	
			patientVitalSignSetView[0][j + 1] = UtilDateTime.format(patientVitalSignSet.getRecordedOn());
			patientVitalSignSetView[1][j + 1] = UtilDateTime.format(patientVitalSignSet.getRecordedOn(), UtilDateTime.AM_PM_FORMATTER);
		}
	return patientVitalSignSetView;
	}
}
