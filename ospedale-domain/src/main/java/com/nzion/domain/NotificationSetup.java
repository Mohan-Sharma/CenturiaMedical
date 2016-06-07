package com.nzion.domain;

import com.nzion.dto.NotificationSettingDto;

import javax.persistence.*;

/**
 * Created by Mohan Sharma on 6/3/2015.
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"status"})})
public class NotificationSetup {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Enumerated(EnumType.STRING)
    private STATUS status;
    private boolean bySMS;
    private boolean byEmail;
    private boolean inApp;
    private int triggetPointValue;
    private boolean patientRole;
    private boolean doctorRole;
    private boolean receptionistRole;
    private boolean notificationRequired;

    public void setPropertiesToEntity(NotificationSettingDto notificationSettingDto) {
        this.notificationRequired = notificationSettingDto.isNotificationRequired();
        this.bySMS = notificationSettingDto.isBySMS();
        this.byEmail = notificationSettingDto.isByEmail();
        this.inApp = notificationSettingDto.isInApp();
        this.triggetPointValue = notificationSettingDto.getTriggetPointValue();
        this.patientRole = notificationSettingDto.isPatientRole();
        this.doctorRole = notificationSettingDto.isDoctorRole();
        this.receptionistRole = notificationSettingDto.isReceptionistRole();
    }

    public enum STATUS{
        SCHEDULED{
            @Override
            public String toString(){
                return "When appointment is given";
            }
        },
        RESCHEDULED{
            @Override
            public String toString(){
                return "When appointment is rescheduled";
            }
        },
        CANCELLED{
            @Override
            public String toString(){
                return "When appointment is cancelled";
            }
        },
        FIRST_REMINDER{
            @Override
            public String toString(){
                return "First reminder regarding appointment";
            }
        },
        SECOND_REMINDER{
            @Override
            public String toString() {
                return "Second reminder regarding appointment";
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
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
