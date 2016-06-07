package com.nzion.zkoss.dto;


import java.util.Date;

public class SlotsDetDto {
    private int premiumVisitTotal;
    private int premiumVisitBooked;
    private int premiumVisitAvailable;

    private int homeVisitTotal;
    private int homeVisitBooked;
    private int homeVisitAvailable;

    private int teleConsultationTotal;
    private int teleConsultationBooked;
    private int teleConsultationAvailable;

    private int appointmentRequestTotal;
    private int appointmentRequestBooked;
    private int appointmentRequestAvailable;

    private int clinicBooked;
    private int clinicOffered;

    private String date;

    private long doctorId;

    public int getPremiumVisitTotal() {
        return premiumVisitTotal;
    }

    public void setPremiumVisitTotal(int premiumVisitTotal) {
        this.premiumVisitTotal = premiumVisitTotal;
    }

    public int getPremiumVisitBooked() {
        return premiumVisitBooked;
    }

    public void setPremiumVisitBooked(int premiumVisitBooked) {
        this.premiumVisitBooked = premiumVisitBooked;
    }

    public int getPremiumVisitAvailable() {
        return premiumVisitAvailable;
    }

    public void setPremiumVisitAvailable(int premiumVisitAvailable) {
        this.premiumVisitAvailable = premiumVisitAvailable;
    }

    public int getHomeVisitTotal() {
        return homeVisitTotal;
    }

    public void setHomeVisitTotal(int homeVisitTotal) {
        this.homeVisitTotal = homeVisitTotal;
    }

    public int getHomeVisitBooked() {
        return homeVisitBooked;
    }

    public void setHomeVisitBooked(int homeVisitBooked) {
        this.homeVisitBooked = homeVisitBooked;
    }

    public int getHomeVisitAvailable() {
        return homeVisitAvailable;
    }

    public void setHomeVisitAvailable(int homeVisitAvailable) {
        this.homeVisitAvailable = homeVisitAvailable;
    }

    public int getTeleConsultationTotal() {
        return teleConsultationTotal;
    }

    public void setTeleConsultationTotal(int teleConsultationTotal) {
        this.teleConsultationTotal = teleConsultationTotal;
    }

    public int getTeleConsultationBooked() {
        return teleConsultationBooked;
    }

    public void setTeleConsultationBooked(int teleConsultationBooked) {
        this.teleConsultationBooked = teleConsultationBooked;
    }

    public int getTeleConsultationAvailable() {
        return teleConsultationAvailable;
    }

    public void setTeleConsultationAvailable(int teleConsultationAvailable) {
        this.teleConsultationAvailable = teleConsultationAvailable;
    }

    public int getAppointmentRequestTotal() {
        return appointmentRequestTotal;
    }

    public void setAppointmentRequestTotal(int appointmentRequestTotal) {
        this.appointmentRequestTotal = appointmentRequestTotal;
    }

    public int getAppointmentRequestBooked() {
        return appointmentRequestBooked;
    }

    public void setAppointmentRequestBooked(int appointmentRequestBooked) {
        this.appointmentRequestBooked = appointmentRequestBooked;
    }

    public int getAppointmentRequestAvailable() {
        return appointmentRequestAvailable;
    }

    public void setAppointmentRequestAvailable(int appointmentRequestAvailable) {
        this.appointmentRequestAvailable = appointmentRequestAvailable;
    }

    public int getClinicOffered() {
        return clinicOffered;
    }

    public void setClinicOffered(int clinicOffered) {
        this.clinicOffered = clinicOffered;
    }

    public long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(long doctorId) {
        this.doctorId = doctorId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getClinicBooked() {
        return clinicBooked;
    }

    public void setClinicBooked(int clinicBooked) {
        this.clinicBooked = clinicBooked;
    }
}
