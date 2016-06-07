package com.nzion.view;

import java.io.Serializable;

import com.nzion.domain.SoapNoteType;

public class SlotNoteVo implements Serializable{

	private static final long serialVersionUID = 1L;
	private SoapNoteType soapNoteType;
	
	public SlotNoteVo() {
	super();
	soapNoteType = new SoapNoteType();
	}
	public SoapNoteType getSoapNoteType() {
	return soapNoteType;
	}
	public void setSoapNoteType(SoapNoteType soapNoteType) {
	this.soapNoteType = soapNoteType;
	}
}
