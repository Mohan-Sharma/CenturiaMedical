package com.nzion.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * @author Sandeep Prusty
 * May 2, 2010
 */

@Entity
@DiscriminatorValue(value = SoapNoteType.CHILDTYPE)
public class SoapNoteType extends SlotType {

	private static final long serialVersionUID = 1L;
	public static final String CHILDTYPE = "SOAP_NOTE_TYPE";
	@Override
	public String toString() {
	return getName();
	}
}
