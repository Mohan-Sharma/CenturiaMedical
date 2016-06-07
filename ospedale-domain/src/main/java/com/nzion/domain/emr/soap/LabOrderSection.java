package com.nzion.domain.emr.soap;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.nzion.domain.emr.SoapModule;
import com.nzion.util.UtilValidator;

/**
 * @author Sandeep Prusty
 * Dec 1, 2010
 */
@Entity
@DiscriminatorValue("LABORDER")
@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="com.nzion.domain")
public class LabOrderSection extends SoapSection {

    private Set<PatientLabOrder> labOrder;

    private String laboratoryTenantId;

    public String getLaboratoryTenantId() {
        return laboratoryTenantId;
    }

    public void setLaboratoryTenantId(String laboratoryTenantId) {
        this.laboratoryTenantId = laboratoryTenantId;
    }

    @OneToMany(targetEntity=PatientLabOrder.class,mappedBy="soapSection",orphanRemoval=true)
    @Cascade(CascadeType.ALL)
    public Set<PatientLabOrder> getLabOrder() {
        return labOrder;
    }

    public void setLabOrder(Set<PatientLabOrder> labOrder) {
        this.labOrder = labOrder;
    }

    public void addPatientLabOrder(PatientLabOrder patientLabOrder){
        labOrder = labOrder == null ? new HashSet<PatientLabOrder>() :  labOrder;
        patientLabOrder.setSoapSection(this);
        labOrder.add(patientLabOrder);
    }

    @Override
    public void init(PatientSoapNote soapNote, SoapModule module, PatientSoapNote lastEncounter, Map<String, SoapSection> previousSoapSections) {

    }

    private static final long serialVersionUID = 1L;

    public static final String MODULE_NAME = "Lab Orders" ;

    @Override
    public boolean edited() {
        return getEdited();
        //return UtilValidator.isNotEmpty(labOrder);
    }
}