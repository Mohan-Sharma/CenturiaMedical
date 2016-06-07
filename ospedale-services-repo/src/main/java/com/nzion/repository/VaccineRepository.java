package com.nzion.repository;

import java.util.List;

import com.nzion.domain.emr.CVXCPTMapping;
import com.nzion.domain.emr.Immunization;
import com.nzion.domain.emr.MVXCode;
import com.nzion.domain.emr.VaccineLot;

public interface VaccineRepository {

	<T> List<T> getAllIncludingInactivesPageWise(Class<T> klass, int pageSize, int firstRecord,	String... lazyLoadAssocPaths);

	Long getCountForAllIncludingInactives(Class<?> klass);

	<T> List<T> search(String searchString, Class<?> entityClass, String... fields);

	List<MVXCode> getMvxCodesFor(String cvxCode);

	List<CVXCPTMapping> getCvxCptMappingsFor(String cvxCode);
	
	List<VaccineLot> getVaccineLotFor(Immunization immunization);
	
	List<Immunization> searchImmunizationsBy(String shortName,String fullName);
}
