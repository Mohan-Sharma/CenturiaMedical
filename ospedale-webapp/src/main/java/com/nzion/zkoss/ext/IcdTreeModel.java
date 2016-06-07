package com.nzion.zkoss.ext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.zul.AbstractTreeModel;

import com.nzion.domain.emr.IcdElement;
import com.nzion.repository.emr.IcdRepository;

/**
 * @author Sandeep Prusty
 * Jul 29, 2010
 */
public class IcdTreeModel extends AbstractTreeModel {
	
	private IcdRepository icdRepository;
	
	private Map<IcdElement, List<IcdElement>> cache = new HashMap<IcdElement, List<IcdElement>>();
	
	private static final IcdElement DUMMYROOT = new IcdElement();
	
	public IcdTreeModel(IcdElement root, IcdRepository icdRepository) {
	super(DUMMYROOT);
	List<IcdElement> actualRootContainer = new ArrayList<IcdElement>(1);
	actualRootContainer.add(root);
	cache.put(DUMMYROOT, actualRootContainer);
	this.icdRepository = icdRepository;
	}

	public IcdTreeModel(IcdRepository icdRepository) {
	super(icdRepository.getRootIcdElement());
	this.icdRepository = icdRepository;
	}

	public Object getChild(Object parent, int index) {
	return getChildren((IcdElement)parent).get(index);
	}

	public int getChildCount(Object parent) {
	return getChildren((IcdElement)parent).size();
	}

	public boolean isLeaf(Object node) {
	return IcdElement.Type.DISEASECODE.equals(((IcdElement)node).getType());
	}
	
	private List<IcdElement> getChildren(IcdElement parent){
	List<IcdElement> elements = cache.get(parent);
	if(elements == null){
		elements = icdRepository.getChildren(parent);;
		elements = elements == null ? new ArrayList<IcdElement>() : elements;
		cache.put(parent, elements);
	}
	return elements;
	}
	
	private static final long serialVersionUID = 1L;
}