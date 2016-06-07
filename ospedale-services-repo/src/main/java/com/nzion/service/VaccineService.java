package com.nzion.service;

import java.util.List;

import com.nzion.domain.emr.Cpt;
import com.nzion.domain.emr.Immunization;
import com.nzion.domain.emr.MVXCode;
import com.nzion.domain.emr.VaccineLot;

public interface VaccineService{

	List<MVXCode> getMvxCodesFor(String cvxCode);

	List<Cpt> getCptsFor(String cvxCode);
	
	List<VaccineLot> getVaccineLotFor(Immunization immunization);
	
	List<Immunization> searchImmunizationsBy(String shortName,String fullName);
}