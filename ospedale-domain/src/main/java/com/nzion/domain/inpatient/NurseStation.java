package com.nzion.domain.inpatient;


import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("NURSE_STATION")
public class NurseStation extends EmployeeGroup {
	private static final long serialVersionUID = 1L;
	
}

