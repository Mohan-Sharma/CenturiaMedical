package com.nzion.domain;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Mohan Sharma on 6/10/2015.
 */
@Entity
public class ProviderAvailability {

    private Long id;
    private Provider provider;
    private Date statusChangeTime;
    public STATUS status;
    private Date appointmentDate;
    public static enum STATUS{
        IN{
            @Override
            public String toString() {
                return "IN";
            }
        }, OUT{
            @Override
            public String toString() {
                return "OUT";
            }
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @OneToOne
    @JoinColumn(name = "PROVIDER_ID")
    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    @Temporal(value = TemporalType.TIMESTAMP)
    public Date getStatusChangeTime() {
        return statusChangeTime;
    }

    public void setStatusChangeTime(Date statusChangeTime) {
        this.statusChangeTime = statusChangeTime;
    }

    @Enumerated(EnumType.STRING)
    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public void setStatus(String status) {
        STATUS.valueOf(STATUS.class, status);
    }

    @Temporal(value = TemporalType.DATE)
    public Date getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(Date appointmentDate) {
        this.appointmentDate = appointmentDate;
    }
}
