package com.nzion.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;

import com.nzion.domain.emr.CVXCPTMapping;
import com.nzion.domain.emr.Cpt;
import com.nzion.domain.emr.Immunization;
import com.nzion.domain.emr.MVXCode;
import com.nzion.domain.emr.VaccineLot;
import com.nzion.repository.VaccineRepository;
import com.nzion.service.VaccineService;

@Service("vaccineService")
public class VaccineServiceImpl implements VaccineService {

	private VaccineRepository vaccineRepository;

	public VaccineRepository getVaccineRepository() {
	return vaccineRepository;
	}

	@Resource
	@Required
	public void setVaccineRepository(VaccineRepository vaccineRepository) {
	this.vaccineRepository = vaccineRepository;
	}

	@Override
	public List<MVXCode> getMvxCodesFor(String cvxCode) {
	return vaccineRepository.getMvxCodesFor(cvxCode);
	}

	@Override
	public List<Cpt> getCptsFor(String cvxCode) {
	List<Cpt> cpts = new ArrayList<Cpt>();
	List<CVXCPTMapping> cvxcptMappings = vaccineRepository.getCvxCptMappingsFor(cvxCode);
	for(CVXCPTMapping cvxcptMapping : cvxcptMappings)
		cpts.add(cvxcptMapping.getCpt());
	return cpts;
	}

	@Override
	public List<VaccineLot> getVaccineLotFor(Immunization immunization) {
	return vaccineRepository.getVaccineLotFor(immunization);
	}
	
	public List<Immunization> searchImmunizationsBy(String shortName,String fullName){
	return vaccineRepository.searchImmunizationsBy(shortName, fullName);
	}
}
