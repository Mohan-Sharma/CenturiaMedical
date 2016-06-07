package com.nzion.service.emr.impl;

import java.util.Collections;
import java.util.List;

import com.nzion.domain.Practice;
import com.nzion.domain.emr.Cpt;
import com.nzion.domain.emr.CptCodeSet;
import com.nzion.domain.emr.IcdElement;
import com.nzion.repository.emr.DiagnosisRepository;
import com.nzion.service.emr.DiagnosisService;
import com.nzion.util.UtilValidator;

/**
 * @author Sandeep Prusty
 * Aug 2, 2010
 */
public class DiagnosisServiceImpl implements DiagnosisService {

	private DiagnosisRepository diagnosisRepository;

	public void setDiagnosisRepository(DiagnosisRepository icdRepository) {
	this.diagnosisRepository = icdRepository;
	}

	public List<IcdElement> getChildren(IcdElement icdElement) {
	return diagnosisRepository.getChildren(icdElement);
	}

	public List<IcdElement> searchIcd(String caption) {
	return diagnosisRepository.searchIcd(caption);
	}

	public List<IcdElement> searchIcd(IcdElement icdElement) {
	return diagnosisRepository.searchIcd(icdElement);
	}

	public IcdElement getRootIcdElement() {
	return diagnosisRepository.getRootIcdElement();
	}

	@Override
	public List<Cpt> getAllCpts() {
	return diagnosisRepository.getAllCpts();
	}

	@Override
	public List<Cpt> serachCpt(String searchText) {
	return diagnosisRepository.searchCpts(searchText);
	}

	@Override
	public List<Cpt> searchCptBy(String code, String description) {
	//if(UtilValidator.isEmpty(code)&& UtilValidator.isEmpty(description))
	//	return Collections.emptyList();
	return diagnosisRepository.searchCptBy(code, description);
	}

}