package com.nzion.zkoss.composer;

import com.nzion.domain.pms.Supplier;
import com.nzion.service.common.CommonCrudService;
import com.nzion.util.UtilMessagesAndPopups;

public class SupplierController extends AutowirableComposer{
	private static final long serialVersionUID = 1L;
	
	private Supplier supplier;
	private CommonCrudService commonCrudService;
	
	public SupplierController(Supplier supplier){
		this.supplier = supplier;
	}
	
	public void Save(){
		commonCrudService.save(supplier);
		UtilMessagesAndPopups.showSuccess();
	}

	public Supplier getSupplier() {
	return supplier;
	}

	public void setSupplier(Supplier supplier) {
	this.supplier = supplier;
	}

	public CommonCrudService getCommonCrudService() {
	return commonCrudService;
	}

	public void setCommonCrudService(CommonCrudService commonCrudService) {
	this.commonCrudService = commonCrudService;
	}

}
