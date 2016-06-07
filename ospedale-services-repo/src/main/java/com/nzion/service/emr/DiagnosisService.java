package com.nzion.service.emr;

import java.util.List;

import com.nzion.domain.Practice;
import com.nzion.domain.emr.Cpt;
import com.nzion.domain.emr.CptCodeSet;
import com.nzion.domain.emr.IcdElement;

/**
 * @author Sandeep Prusty
 * Aug 2, 2010
 */
public interface DiagnosisService {

	IcdElement getRootIcdElement();

	List<IcdElement> getChildren(IcdElement icdElement);

	List<IcdElement> searchIcd(String caption);

	List<IcdElement> searchIcd(IcdElement icdElement);

	List<Cpt> getAllCpts();

	List<Cpt> serachCpt(String searchText);
	
	List<Cpt> searchCptBy(String code,String description);
}