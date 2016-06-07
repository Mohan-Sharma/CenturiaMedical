package com.nzion.domain;

import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.PatientVisit;
import com.nzion.domain.emr.soap.PatientLabOrder;
import com.nzion.util.UtilValidator;

@Entity
@Table(name = "SCHEDULE")

@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "com.nzion.domain")
public class Schedule extends IdGeneratingBaseEntity {

    private static final long serialVersionUID = 1L;
    private STATUS status;
    private Person person;
    private Patient patient;
    private Date startDate;
    private Date startTime;
    private Date endTime;
    private Date signedInTime;
    private Date signedOutTime;
    private Location location;
    private String comments;
    private Integer sequenceNum;
    private String patientContactNumber;
    private String chiefComplaint;
    private Integer length;
    private SlotType slotType;
    private ScheduleType scheduleType;
    private SortedSet<PatientVisit> patientVisits;
    private PatientVisit lastPatientVisit;
    private ScheduleWaitingList waitingList;
    private PatientLabOrder patientLabOrder;
    private Referral referral;
    private String referralDoctorFirstName;
    private String referralDoctorLastName;
    
    private Provider referralClinicDoctorTransient;
    
    private Provider internalReferral;
    private String patientClass;
    private String priority;
    private boolean isPriorityMailSent;
    private String cancelReason;
    private boolean isRescheduled;
    
    private String tentativeStatus = Tentative.Confirmed.toString();
    
    
    private boolean walkinAppointment;
    private boolean mobileOrPatinetPortal;
    
    private boolean patientCancel;
    
    private boolean clinicCancel;
    
    private boolean patientRescheduling;
    
    private boolean clinicRescheduling;
    
    private boolean patientNoShow;
    
    private boolean doctorNoShow;
    
    private boolean consultationInvoiceGenerated;

    private boolean fromMobileApp = Boolean.FALSE;

    private boolean requestForAppointment = Boolean.FALSE;

    private String paymentId;

    private String referralDocMobileNo;

    public Schedule() {
    }

    public Schedule(CalendarSlot slot) {
        startTime = slot.getStartTime();
        endTime = slot.getEndTime();
        sequenceNum = slot.getSequenceNum();
    }

    public Schedule(Schedule existing, CalendarSlot slot) {
        this(slot);
        patient = existing.getPatient();
        comments = existing.getComments();
        patientContactNumber = existing.getPatientContactNumber();
        chiefComplaint = existing.getChiefComplaint();
    }

    public Schedule(Person person, Patient patient, Date startDate, Date startTime) {
        this.person = person;
        this.patient = patient;
        this.startDate = startDate;
        this.startTime = startTime;
        this.status = STATUS.SCHEDULED;
    }

    @Column(name = "PRIORITY")
    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    @ManyToOne(targetEntity = Person.class)
    @JoinColumn(name = "PERSON_ID")
    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    @OneToMany(targetEntity = PatientVisit.class, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "SCHEDULE_ID")
    @Sort(type = SortType.NATURAL)
    @Cascade(value = CascadeType.ALL)
    public SortedSet<PatientVisit> getPatientVisits() {
        if (patientVisits == null) patientVisits = new TreeSet<PatientVisit>();
        return patientVisits;
    }

    public void addPatientVisit(PatientVisit visit) {
        getPatientVisits().add(visit);
        lastPatientVisit = visit;
    }

    public void setPatientVisits(SortedSet<PatientVisit> patientVisits) {
        this.patientVisits = patientVisits;
    }

    @OneToOne(optional = true)
    @JoinColumn(name = "LAST_PATIENT_VISIT_ID")
    public PatientVisit getLastPatientVisit() {
        return lastPatientVisit;
    }

    public void setLastPatientVisit(PatientVisit lastPatientVisit) {
        this.lastPatientVisit = lastPatientVisit;
    }

    @Column(name = "SIGNED_IN_TIME")
    public Date getSignedInTime() {
        return signedInTime;
    }

    public void setSignedInTime(Date signedInTime) {
        this.signedInTime = signedInTime;
    }

    @Column(name = "SIGNED_OUT_TIME")
    public Date getSignedOutTime() {
        return signedOutTime;
    }

    public void setSignedOutTime(Date signedOutTime) {
        this.signedOutTime = signedOutTime;
    }

    @Column(name = "LENGTH")
    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    @OneToOne(targetEntity = SlotType.class)
    @JoinColumn(name = "SOAP_NOTE_TYPE")
    public SlotType getVisitType() {
        return slotType;
    }

    public void setVisitType(SlotType slotType) {
        this.slotType = slotType;
    }

    public String getChiefComplaint() {
        return chiefComplaint;
    }

    public void setChiefComplaint(String chiefComplaint) {
        this.chiefComplaint = chiefComplaint;
    }

    @Column(name = "SEQUENCE_NUMBER")
    public Integer getSequenceNum() {
        return sequenceNum;
    }

    public void setSequenceNum(Integer sequenceNum) {
        this.sequenceNum = sequenceNum;
    }

    @Column(name = "PATIENT_CONTACT_NUMBER")
    public String getPatientContactNumber() {
        return patientContactNumber;
    }

    public void setPatientContactNumber(String patientContactNumber) {
        this.patientContactNumber = patientContactNumber;
    }

    @Column(name = "COMMENTS")
    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    @Column(name = "STATUS")
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

    @Enumerated(EnumType.STRING)
    @Column(name = "SCHEDULE_TYPE")
    public ScheduleType getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(ScheduleType scheduleType) {
        this.scheduleType = scheduleType;
    }

    @ManyToOne(targetEntity = Patient.class)
    @JoinColumn(name = "PATIENT_ID")
    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    @Column(name = "START_DATE")
    @Temporal(TemporalType.DATE)
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @Column(name = "START_TIME")
    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    @OneToOne
    @JoinColumn(name ="INTERNAL_REFERRAL")
    public Provider getInternalReferral() {
        return internalReferral;
    }

    public void setInternalReferral(Provider internalReferral) {
        this.internalReferral = internalReferral;
    }

    @Column(name = "END_TIME")
    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    @ManyToOne(targetEntity = Location.class)
    @JoinColumn(name = "LOCATION_ID", nullable = false)
    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @OneToOne
    @JoinColumn(name = "PATIENT_LAB_ORDER_ID")
    public PatientLabOrder getPatientLabOrder() {
        return patientLabOrder;
    }

    public void setPatientLabOrder(PatientLabOrder patientLabOrder) {
        this.patientLabOrder = patientLabOrder;
    }

    public CalendarSlot buildCalendarSlot() {
        CalendarResourceAssoc association = new CalendarResourceAssoc();
        association.setPerson(person);
        association.setLocation(location);
        association.setFromDate(startDate);
        association.setThruDate(startDate);
        association.setStartTime(startTime);
        association.setEndTime(endTime);
        CalendarSlot slot = new CalendarSlot();
        slot.setAssociation(association);
        slot.setEndTime(getEndTime());
        slot.setStartTime(getStartTime());
        slot.setSequenceNum(getSequenceNum());
        return slot;
    }

    public void populateFromWaitingList(ScheduleWaitingList waitingList) {
        setPatient(waitingList.getPatient());
        setComments(waitingList.getComments());
        setPatientContactNumber(waitingList.getPreferedContactNumber());
        this.waitingList = waitingList;
    }

    public void populateFromSchedule(Schedule schedule) {
        this.patient = schedule.patient;
        this.person = schedule.getPerson();
        this.length = schedule.length;
        this.slotType = schedule.slotType;
        this.chiefComplaint = schedule.chiefComplaint;
        this.patientContactNumber = schedule.patientContactNumber;
        this.status = schedule.getStatus();
        this.comments = schedule.comments;
        this.tentativeStatus = schedule.tentativeStatus;
        this.fromMobileApp = schedule.fromMobileApp;
        this.mobileOrPatinetPortal = schedule.mobileOrPatinetPortal;
        
    }

    public Schedule createCopy() {
        Schedule newSchedule = new Schedule();
        newSchedule.setPatient(patient);
        newSchedule.setChiefComplaint(chiefComplaint);
        newSchedule.setPatientContactNumber(patientContactNumber);
        newSchedule.setComments(comments);
        newSchedule.startTime = startTime;
        newSchedule.endTime = endTime;
        newSchedule.sequenceNum = sequenceNum;
        newSchedule.status = STATUS.SCHEDULED;
        newSchedule.setPerson(person);
        newSchedule.setLocation(location);
        newSchedule.setLength(length);
        newSchedule.setVisitType(slotType);
        return newSchedule;
    }

    @Transient
    public ScheduleWaitingList getWaitingList() {
        return waitingList;
    }

    @Transient
    public boolean isForSameSlot(Schedule schedule) {
        boolean cmp = true;
        if (this.person != null) cmp = cmp && person.equals(schedule.getPerson());
        CalendarSlot thisSlot = new CalendarSlot(getStartTime(), schedule.getEndTime(), schedule.getSequenceNum());
        CalendarSlot thatSlot = new CalendarSlot(schedule.getStartTime(), schedule.getEndTime(), schedule.getSequenceNum());
        cmp = cmp && (thisSlot.compareTo(thatSlot) == 0);
        return cmp;
    }

    public static enum ScheduleType {
        NORMAL, FORCEINSERTED, CREATEDFROM_SIGNIN, BLOCKED;
    }

	/*
	 * *
	 * @author Sandeep Prusty Jul 15, 2010 Statuses used for scheduling. The getAllowedModifications() gives the
	 * statuses to which a given status can be changed to.
	 */

    public static enum STATUS {

        SCHEDULED("SCHEDULED") {
            @Override
            public STATUS[] getAllowedModifications() {
                return new STATUS[] { SCHEDULED, CHECKEDIN,EXAMINING, NOSHOW, CANCELLED };
            }
        },
        CHECKEDIN("ASSIGNED TO NURSE") {
            @Override
            public STATUS[] getAllowedModifications() {
                return new STATUS[] { CHECKEDIN, CHECKEDOUT };
            }
        },
        PROCEDUREPENDING("PROCEDURE PENDING") {
            @Override
            public STATUS[] getAllowedModifications() {
                return new STATUS[] { CHECKEDIN, CHECKEDOUT };
            }
        },
        READY_FOR_BILLING("READY FOR BILLING"){
            @Override
            public STATUS[] getAllowedModifications() {
                return new STATUS[] {CHECKEDOUT};
            }
        },
        /*ROOMED("ROOMED") {
            @Override
            public STATUS[] getAllowedModifications() {
                return new STATUS[] { ROOMED, READY_FOR_BILLING,CHECKEDOUT };
            }
        },*/
        EXAMINING("ASSIGNED TO DOCTOR") {
            @Override
            public STATUS[] getAllowedModifications() {
                return new STATUS[] { EXAMINING, CHECKEDOUT };
            }
        },
        SOAPSIGNEDOUT("SIGNEDOUT") {
            @Override
            public STATUS[] getAllowedModifications() {
                return new STATUS[] { SOAPSIGNEDOUT,CHECKEDOUT };
            }
        },
        CHECKEDOUT("CHECKEDOUT") {
            @Override
            public STATUS[] getAllowedModifications() {
                return new STATUS[] { CHECKEDOUT };
            }
        },
        CANCELLED("CANCELLED") {
            @Override
            public STATUS[] getAllowedModifications() {
                return new STATUS[] { CANCELLED, SCHEDULED };
            }
        },
        NOSHOW("NOSHOW") {
            @Override
            public STATUS[] getAllowedModifications() {
                return new STATUS[] { NOSHOW, SCHEDULED };
            }
        };

        private String description;

        public String getDescription() {
            return description;
        }
        public void setDescription(String description) {
            this.description = description;
        }

        STATUS(String description){
            this.description = description;
        }
        public abstract STATUS[] getAllowedModifications();
    }

    @OneToOne
    public Referral getReferral() {
        return referral;
    }

    public void setReferral(Referral referral) {
        this.referral = referral;
    }

    public String getPatientClass() {
        return patientClass;
    }

    public void setPatientClass(String patientClass) {
        this.patientClass = patientClass;
    }

    public void assignInternalReferral(boolean isInternalReferral){
        if(isInternalReferral)
            setReferral(null);
        else
            setInternalReferral(null);
    }

    public boolean isPriorityMailSent() {
        return isPriorityMailSent;
    }

    public void setPriorityMailSent(boolean isPriorityMailSent) {
        this.isPriorityMailSent = isPriorityMailSent;
    }

	public boolean isWalkinAppointment() {
		return walkinAppointment;
	}

	public void setWalkinAppointment(boolean walkinAppointment) {
		this.walkinAppointment = walkinAppointment;
	}

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public boolean isRescheduled() {
        return isRescheduled;
    }

    public void setRescheduled(boolean isRescheduled) {
        this.isRescheduled = isRescheduled;
    }

	public boolean isMobileOrPatinetPortal() {
		return mobileOrPatinetPortal;
	}
	
	public String getTentativeStatus() {
		if(UtilValidator.isEmpty(tentativeStatus)){
			return Tentative.Confirmed.toString();
		}
		return tentativeStatus;
	}

	public void setTentativeStatus(String tentativeStatus) {
		this.tentativeStatus = tentativeStatus;
	}

	public void setMobileOrPatinetPortal(boolean mobileOrPatinetPortal) {
		this.mobileOrPatinetPortal = mobileOrPatinetPortal;
	}

	public boolean isPatientCancel() {
		return patientCancel;
	}

	public void setPatientCancel(boolean patientCancel) {
		this.patientCancel = patientCancel;
	}

	public boolean isClinicCancel() {
		return clinicCancel;
	}

	public void setClinicCancel(boolean clinicCancel) {
		this.clinicCancel = clinicCancel;
	}

	public boolean isPatientRescheduling() {
		return patientRescheduling;
	}

	public void setPatientRescheduling(boolean patientRescheduling) {
		this.patientRescheduling = patientRescheduling;
	}

	public boolean isClinicRescheduling() {
		return clinicRescheduling;
	}

	public void setClinicRescheduling(boolean clinicRescheduling) {
		this.clinicRescheduling = clinicRescheduling;
	}

	public boolean isPatientNoShow() {
		return patientNoShow;
	}

	public void setPatientNoShow(boolean patientNoShow) {
		this.patientNoShow = patientNoShow;
	}

	public boolean isDoctorNoShow() {
		return doctorNoShow;
	}

	public void setDoctorNoShow(boolean doctorNoShow) {
		this.doctorNoShow = doctorNoShow;
	}

	public boolean isConsultationInvoiceGenerated() {
		return consultationInvoiceGenerated;
	}

	public void setConsultationInvoiceGenerated(boolean consultationInvoiceGenerated) {
		this.consultationInvoiceGenerated = consultationInvoiceGenerated;
	}

    public boolean isFromMobileApp() {
        return fromMobileApp;
    }

    public void setFromMobileApp(boolean fromMobileApp) {
        this.fromMobileApp = fromMobileApp;
    }

    public boolean isRequestForAppointment() {
        return requestForAppointment;
    }

    public void setRequestForAppointment(boolean requestForAppointment) {
        this.requestForAppointment = requestForAppointment;
    }
    
    public String getReferralDoctorFirstName() {
		return referralDoctorFirstName;
	}

	public void setReferralDoctorFirstName(String referralDoctorFirstName) {
		this.referralDoctorFirstName = referralDoctorFirstName;
	}

	public String getReferralDoctorLastName() {
		return referralDoctorLastName;
	}

	public void setReferralDoctorLastName(String referralDoctorLastName) {
		this.referralDoctorLastName = referralDoctorLastName;
	}
	
	@Transient
	public Provider getReferralClinicDoctorTransient() {
		return referralClinicDoctorTransient;
	}

	public void setReferralClinicDoctorTransient(Provider referralClinicDoctorTransient) {
		this.referralDoctorFirstName = referralClinicDoctorTransient.getFirstName();
		this.referralDoctorLastName = referralClinicDoctorTransient.getLastName();
		this.referralClinicDoctorTransient = referralClinicDoctorTransient;
	}

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    @Transient
    public String getReferralDocMobileNo() {
        return referralDocMobileNo;
    }

    public void setReferralDocMobileNo(String referralDocMobileNo) {
        this.referralDocMobileNo = referralDocMobileNo;
    }

    public enum Tentative{
    	Tentative,Confirmed,Paid;
    }
}