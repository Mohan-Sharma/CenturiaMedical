package com.nzion.domain.drug;

import javax.persistence.Entity;

import com.nzion.domain.emr.MasterEntity;

@Entity
public class DrugSig extends MasterEntity{
	
	private static final long serialVersionUID = 1L;
	private String type;
	
	public DrugSig(){
	
	}
	
	public DrugSig(String type){
	this.type=type;
	}
	
	public void setType(String type){
	this.type=type;
	}
	
	public String getType(){
	return this.type;
	}
}
