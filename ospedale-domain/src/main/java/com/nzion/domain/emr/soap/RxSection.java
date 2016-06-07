package com.nzion.domain.emr.soap;

import java.util.Map;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Proxy;

import com.nzion.domain.emr.SoapModule;
import com.nzion.util.UtilValidator;

/**
 * @author Sandeep Prusty
 * Dec 1, 2010
 */
@Entity
@DiscriminatorValue("RX_SECTION")
@Proxy(lazy=false)
public class RxSection extends SoapSection  {

    private Set<PatientRx> patientRxs;

    private String comment;

    //	@OneToMany(targetEntity = PatientRx.class, mappedBy="rxSection", fetch=FetchType.EAGER,orphanRemoval=true)
//	@Cascade(CascadeType.ALL)
//	public Set<PatientRx> getExtPatientRxs() {
//	if(extPatientRxs == null)
//		extPatientRxs = new HashSet<PatientRx>();
//	return extPatientRxs;
//	}

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @OneToMany(targetEntity=PatientRx.class,mappedBy="rxSection",cascade=javax.persistence.CascadeType.ALL,orphanRemoval = true)
    public Set<PatientRx> getPatientRxs() {
        return patientRxs;
    }

    public void setPatientRxs(Set<PatientRx> rxs) {
        this.patientRxs = rxs;
    }

    public void addPatientRx(PatientRx patientRx){
        patientRx.setPatient(this.getSoapNote().getPatient());
        patientRx.setProvider(this.getSoapNote().getProvider());
        patientRx.setRxSection(this);
        if(!checkIfPatientRxAlreadyExist(getPatientRxs(), patientRx))
            getPatientRxs().add(patientRx);
    }

    private boolean checkIfPatientRxAlreadyExist(Set<PatientRx> patientRxs, PatientRx patientRx) {
        if(UtilValidator.isEmpty(patientRx))
            return Boolean.FALSE;
        else {
            for(PatientRx existingPatientRx : patientRxs){
                if(existingPatientRx.getDrug().getId().equals(patientRx.getDrug().getId()))
                    return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public void init(PatientSoapNote soapNote, SoapModule module, PatientSoapNote lastEncounter, Map<String, SoapSection> previousSoapSections) {

    }

    private static final long serialVersionUID = 1L;

    public static final String MODULE_NAME = "Rx" ;

    @Override
    public boolean edited() {
        return UtilValidator.isNotEmpty(patientRxs);
    }
}