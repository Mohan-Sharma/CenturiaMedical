package com.nzion.dto;

import com.nzion.domain.emr.VisitTypeSoapModule;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DoctorSmartServiceDto implements Serializable {
    private Long doctorId;
    private String tenantId;
    private Long visitType;
    private Boolean smartServiceEnabled;

    public void setPropertiesToDoctorSmartServiceDto(VisitTypeSoapModule visitTypeSoapModule) {
        if(visitTypeSoapModule.getProvider() != null)
            this.doctorId = visitTypeSoapModule.getProvider().getId();
        if(visitTypeSoapModule.getSlotType() != null)
            this.visitType = visitTypeSoapModule.getSlotType().getId();
        if(visitTypeSoapModule.isSmartServiceDisplayInPortal()) {
            this.smartServiceEnabled = visitTypeSoapModule.isSmartServiceDisplayInPortal();
        } else {
            this.smartServiceEnabled = false;
        }
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Long getVisitType() {
        return visitType;
    }

    public void setVisitType(Long visitType) {
        this.visitType = visitType;
    }

    public Boolean getSmartServiceEnabled() {
        return smartServiceEnabled;
    }

    public void setSmartServiceEnabled(Boolean smartServiceEnabled) {
        this.smartServiceEnabled = smartServiceEnabled;
    }
}
