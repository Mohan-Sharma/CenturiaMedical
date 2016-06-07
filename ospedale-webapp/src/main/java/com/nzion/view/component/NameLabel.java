package com.nzion.view.component;

import com.nzion.dto.PatientDto;
import org.zkoss.zul.Label;

import com.nzion.domain.Patient;
import com.nzion.util.ViewUtil;

/**
 * @author Sandeep Prusty
 * Jun 25, 2010
 */
public class NameLabel extends Label {

	public NameLabel() {}

	public NameLabel(Object object) {
		setObject(object);
	}

	public void setObject(Object object) {
		if(object == null)
			return;
		if(object instanceof PatientDto) {
			setValue(((PatientDto) object).getFullName());
			return;
		}
		setValue(ViewUtil.getFormattedName(object));
	}

	public void setPatient(Object object){
		if(object == null)
			return;
		if(object instanceof PatientDto) {
			setValue(((PatientDto) object).getFullName());
			return;
		}
		if(object instanceof Patient){
			setValue( ((Patient)object).getAccountNumber() + " - " +ViewUtil.getFormattedName(object) );
		}else
			setValue(ViewUtil.getFormattedName(object));
	}

	private static final long serialVersionUID = 1L;
}
