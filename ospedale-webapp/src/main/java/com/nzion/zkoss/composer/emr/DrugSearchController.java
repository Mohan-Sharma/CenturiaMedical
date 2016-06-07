package com.nzion.zkoss.composer.emr;

import java.util.Arrays;
import java.util.List;

import com.nzion.domain.Person;
import com.nzion.domain.drug.Drug;
import com.nzion.service.common.DrugService;
import com.nzion.util.Infrastructure;
import com.nzion.zkoss.composer.OspedaleAutowirableComposer;

public class DrugSearchController extends OspedaleAutowirableComposer {

	private String searchCriteria;

	private DrugService drugService;

	private List<String> searchCriterias = Arrays.asList("Favourite", "PatientRx");

	private boolean inactive;

	private String genericName;

	private String tradeName;

	private Person person = Infrastructure.getUserLogin().getPerson();

	public List<Drug> lookUpDrug() {
	if (searchCriterias.contains(searchCriteria)) {
		boolean fromFavourite = false;
		String activeOrInactive = "Active";
		if (inactive) activeOrInactive = "InActive";
		if ("Favourite".equalsIgnoreCase(searchCriteria)) {
			fromFavourite = true;
			return drugService.lookupDrug(fromFavourite, activeOrInactive, genericName, tradeName, person);
		}
		return drugService.lookupDrug(fromFavourite, activeOrInactive, genericName, tradeName, person);
	}
	return drugService.searchDrugBy(genericName, tradeName);
	}

	public String getSearchCriteria() {
	return searchCriteria;
	}

	public void setSearchCriteria(String searchCriteria) {
	this.searchCriteria = searchCriteria;
	}

	public DrugService getDrugService() {
	return drugService;
	}

	public void setDrugService(DrugService drugService) {
	this.drugService = drugService;
	}

	public boolean isInactive() {
	return inactive;
	}

	public void setInactive(boolean inactive) {
	this.inactive = inactive;
	}

	public String getGenericName() {
	return genericName;
	}

	public void setGenericName(String genericName) {
	this.genericName = genericName;
	}

	public String getTradeName() {
	return tradeName;
	}

	public void setTradeName(String tradeName) {
	this.tradeName = tradeName;
	}

	private static final long serialVersionUID = 1L;

}
