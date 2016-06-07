package com.nzion.repository.emr;

import java.util.List;

import com.nzion.domain.Practice;
import com.nzion.domain.emr.CptCodeSet;
import com.nzion.domain.emr.IcdCodeSet;
import com.nzion.domain.emr.IcdElement;

/**
 * @author Sandeep Prusty
 * Jul 30, 2010
 */
public interface IcdRepository {
	
	List<IcdElement> getChildren(IcdElement icdElement);
	
	List<IcdElement> searchIcd(String caption);

	List<IcdElement> searchIcd(IcdElement icdElement);

	IcdElement getRootIcdElement();
	
}
