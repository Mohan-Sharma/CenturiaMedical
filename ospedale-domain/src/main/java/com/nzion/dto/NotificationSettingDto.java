package com.nzion.dto;

import com.nzion.domain.NotificationSetup;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

/**
 * Created by Mohan Sharma on 6/3/2015.
 */
public class NotificationSettingDto {

    private boolean notificationRequired;
    private boolean bySMS;
    private boolean byEmail;
    private boolean inApp;
    private int triggetPointValue;
    private boolean patientRole;
    private boolean doctorRole;
    private boolean receptionistRole;

    public void setPropertiesToNotificationDto(NotificationSetup notificationSetup) {
        this.setNotificationRequired(notificationSetup.isNotificationRequired());
        this.setBySMS(notificationSetup.isBySMS());
        this.setByEmail(notificationSetup.isByEmail());
        this.setInApp(notificationSetup.isInApp());
        this.setTriggetPointValue(notificationSetup.getTriggetPointValue());
        this.setPatientRole(notificationSetup.isPatientRole());
        this.setDoctorRole(notificationSetup.isDoctorRole());
        this.setReceptionistRole(notificationSetup.isReceptionistRole());
    }

    public boolean isBySMS() {
        return bySMS;
    }

    public void setBySMS(boolean bySMS) {
        this.bySMS = bySMS;
    }

    public boolean isByEmail() {
        return byEmail;
    }

    public void setByEmail(boolean byEmail) {
        this.byEmail = byEmail;
    }

    public boolean isInApp() {
        return inApp;
    }

    public void setInApp(boolean inApp) {
        this.inApp = inApp;
    }

    public int getTriggetPointValue() {
        return triggetPointValue;
    }

    public void setTriggetPointValue(int triggetPointValue) {
        this.triggetPointValue = triggetPointValue;
    }

    public boolean isPatientRole() {
        return patientRole;
    }

    public void setPatientRole(boolean patientRole) {
        this.patientRole = patientRole;
    }

    public boolean isDoctorRole() {
        return doctorRole;
    }

    public void setDoctorRole(boolean doctorRole) {
        this.doctorRole = doctorRole;
    }

    public boolean isReceptionistRole() {
        return receptionistRole;
    }

    public void setReceptionistRole(boolean receptionistRole) {
        this.receptionistRole = receptionistRole;
    }

    public boolean isNotificationRequired() {
        return notificationRequired;
    }

    public void setNotificationRequired(boolean notificationRequired) {
        this.notificationRequired = notificationRequired;
    }

}
