package com.nzion.service.impl;

import com.nzion.domain.ReferralContract;
import com.nzion.repository.ReferralContractRepository;
import com.nzion.repository.ScheduleRepository;
import com.nzion.repository.common.CommonCrudRepository;
import com.nzion.service.common.CommonCrudService;
import org.springframework.beans.factory.annotation.Required;

import javax.annotation.Resource;

/**
 * Created by Asus on 6/23/2015.
 */
public class ReferralContractServiceImpl implements com.nzion.service.ReferralContractService {
    private ReferralContractRepository referralContractRepository;

    private CommonCrudRepository commonCrudRepository;

    private CommonCrudService commonCrudService;

    @Required
    @Resource
    public void setReferralContractRepository(ReferralContractRepository referralContractRepository) {
        this.referralContractRepository = referralContractRepository;
    }

    @Required
    @Resource
    public void setCommonCrudRepository(CommonCrudRepository commonCrudRepository) {
        this.commonCrudRepository = commonCrudRepository;
    }

    @Required
    @Resource
    public void setCommonCrudService(CommonCrudService commonCrudService) {
        this.commonCrudService = commonCrudService;
    }

    public ReferralContract  getReferralContractByReferralAndReferee(long referralId,long refreeId) {
        return this.referralContractRepository.getReferralContractByReferralAndReferee(referralId,refreeId);
    }

}
