package com.nzion.service.dto;

import java.util.Date;

/**
 * Created by Mohan Sharma on 7/27/2015.
 */
public class ReschedulePatientAppointmentDto {
    private String scheduleId;
    private Date appointmentStartDate;
    private Date appointmentEndDate;
    private String notes;
    private String visitType;

    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public java.util.Date getAppointmentStartDate() {
        return appointmentStartDate;
    }

    public void setAppointmentStartDate(java.util.Date appointmentStartDate) {
        this.appointmentStartDate = appointmentStartDate;
    }

    public java.util.Date getAppointmentEndDate() {
        return appointmentEndDate;
    }

    public void setAppointmentEndDate(java.util.Date appointmentEndDate) {
        this.appointmentEndDate = appointmentEndDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getVisitType() {
        return visitType;
    }

    public void setVisitType(String visitType) {
        this.visitType = visitType;
    }

    
}
