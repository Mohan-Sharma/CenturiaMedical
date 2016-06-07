package com.nzion.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * @author Sandeep Prusty
 * May 2, 2010
 */

@Entity
@DiscriminatorValue(value = ResourceNoteType.CHILDTYPE)
public class ResourceNoteType extends SlotType {

	private static final long serialVersionUID = 1L;
	public static final String CHILDTYPE = "RESOURCE_NOTE_TYPE";
}
