package com.nzion.repository.emr;

import java.util.List;

import com.nzion.domain.emr.Cpt;
import com.nzion.domain.emr.IcdElement;
import com.nzion.repository.BaseRepository;


/**
 * @author Sandeep Prusty
 * Aug 2, 2010
 */
public interface DiagnosisRepository extends BaseRepository, IcdRepository {

	List<Cpt> getAllCpts();
	
	IcdElement getIcdByCode(String icdCode);
	
	List<Cpt> searchCpts(String searchText);
	
	Cpt getCptByCode(String cptCode);
	
	List<IcdElement> searchIcdBy(String code,String description);
	
	List<Cpt> searchCptBy(String code,String description);
}