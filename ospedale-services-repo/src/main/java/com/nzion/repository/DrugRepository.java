/**
 * @author shwetha
 * Oct 15, 2010 
 */
package com.nzion.repository;

import com.nzion.domain.drug.Drug;

import java.util.List;

public interface DrugRepository {

	List<Drug> searchDrugs(String fieldName,String searchString);
	
	List<Drug> searchDrugs(String searchString);

	List<Drug> getDrugsByTradeName(String tradeName);

	List<Drug> searchDrugBy(String genericName,String tradeName);
	
}

