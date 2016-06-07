/**
 * @author shwetha
 * Oct 15, 2010 
 */
package com.nzion.service.common.impl;

import com.nzion.domain.Person;
import com.nzion.domain.drug.Drug;
import com.nzion.domain.drug.DrugDosageRoute;
import com.nzion.repository.DrugRepository;
import com.nzion.service.SoapNoteService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.service.common.DrugService;
import com.nzion.util.UtilValidator;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service("drugService")
public class DrugServiceImpl implements DrugService {

    private DrugRepository drugRepository;

    private CommonCrudService commonCrudService;

    private SoapNoteService soapNoteService;

    @Resource
    @Required
    public void setCommonCrudService(CommonCrudService commonCrudService) {
        this.commonCrudService = commonCrudService;
    }

    public void setDrugRepository(DrugRepository drugRepository) {
        this.drugRepository = drugRepository;
    }

    @Override
    public List<Drug> searchDrugs(String fieldName, String searchString) {
        return drugRepository.searchDrugs(fieldName, searchString);
    }

    @Override
    public boolean isDrugPresentWithTradeName(String tradeName) {
        List<Drug> drug = drugRepository.getDrugsByTradeName(tradeName);
        return UtilValidator.isNotEmpty(drug);
    }

    @Override
    public List<Drug> searchDrugs(String searchString) {
        return drugRepository.searchDrugs(searchString);
    }

    public List<DrugDosageRoute> getUnaddedRoutesForDrug(Drug drug) {
        List<DrugDosageRoute> allRoutes = commonCrudService.getAll(DrugDosageRoute.class);
        allRoutes.remove(drug.getRoutes());
        return allRoutes;
    }

    @Override
    public List<Drug> searchDrugBy(String genericName, String tradeName) {
        return drugRepository.searchDrugBy(genericName, tradeName);
    }

    public SoapNoteService getSoapNoteService() {
        return soapNoteService;
    }

    @Resource
    @Required
    public void setSoapNoteService(SoapNoteService soapNoteService) {
        this.soapNoteService = soapNoteService;
    }

    @Override
    public List<Drug> lookupDrug(boolean fromFavourite, String activeOrInactive, String drugGenericName,
                                 String drugTradeName, Person person) {
        List<Drug> drugs = new ArrayList<Drug>();
        drugs = drugRepository.searchDrugBy(drugGenericName, drugTradeName);
        return drugs;
    }

}