package com.nzion.service.emr;

import java.util.List;

import com.nzion.domain.Patient;
import com.nzion.domain.Person;
import com.nzion.domain.Practice;
import com.nzion.domain.emr.IcdCodeSet;
import com.nzion.domain.emr.IcdElement;

/**
 * @author Sandeep Prusty
 * Jul 29, 2010
 */
public interface IcdService {
	
	IcdElement getRootIcdElement();
	
	List<IcdElement> getChildren(IcdElement icdElement);
	
	List<IcdElement> search(String caption);
	
	List<IcdElement> search(IcdElement icdElement);
	
	List<IcdElement> searchIcdBy(String code,String description);
	
	List<IcdElement> lookUpIcd(boolean fromFavourite,Person person, String code,String description,Patient patient);
	
}