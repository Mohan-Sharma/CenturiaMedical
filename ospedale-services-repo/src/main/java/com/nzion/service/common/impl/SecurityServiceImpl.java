package com.nzion.service.common.impl;

import com.nzion.domain.emr.soap.PatientSoapNote;
import com.nzion.repository.common.CommonCrudRepository;
import com.nzion.service.common.SecurityService;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service("securityService")
public class SecurityServiceImpl implements SecurityService {

    private CommonCrudRepository commonCrudRepository;

    public boolean hasAuthorizationToViewSoapNote(PatientSoapNote patientSoapNote) {
        return true;
    }


    public CommonCrudRepository getCommonCrudRepository() {
        return commonCrudRepository;
    }

    @Resource
    @Required
    public void setCommonCrudRepository(CommonCrudRepository commonCrudRepository) {
        this.commonCrudRepository = commonCrudRepository;
    }

}
