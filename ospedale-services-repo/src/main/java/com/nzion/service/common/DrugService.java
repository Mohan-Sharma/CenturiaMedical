/**
 * @author shwetha
 * Oct 15, 2010 
 */
package com.nzion.service.common;

import java.util.List;

import com.nzion.domain.Person;
import com.nzion.domain.drug.Drug;
import com.nzion.domain.drug.DrugDosageRoute;

public interface DrugService {
 
	List<Drug> searchDrugs(String fieldName,String searchString);
	
	boolean isDrugPresentWithTradeName(String tradeName);
	
	List<Drug> searchDrugs(String searchField);
	
	List<DrugDosageRoute> getUnaddedRoutesForDrug(Drug drug);
	
	List<Drug> searchDrugBy(String genericName,String tradeName);
	
	List<Drug> lookupDrug(boolean fromFavourite,String activeOrInactive,String drugGenericName,String drugTradeName,Person person);
	
}