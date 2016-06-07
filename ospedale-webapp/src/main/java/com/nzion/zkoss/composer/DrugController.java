package com.nzion.zkoss.composer;

import com.nzion.domain.drug.Drug;
import com.nzion.service.common.CommonCrudService;
import com.nzion.util.UtilValidator;

public class DrugController extends OspedaleAutowirableComposer{
    private static final long serialVersionUID = 1L;
    private CommonCrudService commonCrudService;

    public void save(Drug drug){
        commonCrudService.save(drug);
        com.nzion.util.UtilMessagesAndPopups.showSuccess();
    }

    public CommonCrudService getCommonCrudService() {
        return commonCrudService;
    }

    public void setCommonCrudService(CommonCrudService commonCrudService) {
        this.commonCrudService = commonCrudService;
    }
}
