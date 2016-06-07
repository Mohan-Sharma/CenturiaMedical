package com.nzion.domain.emr.soap;

import java.util.Date;

import javax.persistence.*;

import com.nzion.domain.emr.lab.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;

import com.nzion.domain.Patient;
import com.nzion.domain.base.IdGeneratingBaseEntity;

/**
 * @author Sandeep Prusty
 *         Dec 1, 2010
 */
@Entity
@Table
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "com.nzion.domain")
public class PatientLabOrder extends IdGeneratingBaseEntity {

    private Date startDate;

    private Date endDate;

    private String testNotes;

    private String testCode;

    private String testName;

    private SoapSection soapSection;

    private STATUS status;

    private Patient patient;

    private LabTest labTest;

    private Radiology radiology;

    private LabTestPanel labTestPanel;

    private LabTestProfile labTestProfile;

    private OBRSegment obrSegment;

    private LabOrderRequest labOrderRequest;

    private String reasonForTest;

    private boolean homeService;

    private String typeOfTest;

    private BILLINGSTATUS billingStatus = BILLINGSTATUS.UNINVOICED;

    public PatientLabOrder() {
    }


    public PatientLabOrder(String testCode, String testName) {
        this.testCode = testCode;
        this.testName = testName;
    }

    public boolean isHomeService() {
        return homeService;
    }

    public void setHomeService(boolean homeService) {
        this.homeService = homeService;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }


    @Column(length = 1024)
    public String getReasonForTest() {
        return reasonForTest;
    }

    public void setReasonForTest(String reasonForTest) {
        this.reasonForTest = reasonForTest;
    }

    public String getTestCode() {
        return testCode;
    }

    public void setTestCode(String testCode) {
        this.testCode = testCode;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getTestNotes() {
        return testNotes;
    }

    public void setTestNotes(String testNotes) {
        this.testNotes = testNotes;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @OneToOne
    @JoinColumn(name = "LAB_TEST_ID")
    public LabTest getLabTest() {
        return labTest;
    }

    public void setLabTest(LabTest labTest) {
        this.labTest = labTest;
    }

    @OneToOne
    @JoinColumn(name = "RADIOLOGY_ID")
    public Radiology getRadiology() {
        return radiology;
    }

    public void setRadiology(Radiology radiology) {
        this.radiology = radiology;
    }

    @OneToOne
    @JoinColumn(name = "LAB_TEST_PROFILE_ID")
    public LabTestProfile getLabTestProfile() {
        return labTestProfile;
    }

    public void setLabTestProfile(LabTestProfile labTestProfile) {
        this.labTestProfile = labTestProfile;
    }

    @ManyToOne
    @JoinColumn(name = "LAB_ORDER_SECTION_ID")
    @ForeignKey(name = "FK_LAB_ORDER_SECTION")
    public SoapSection getSoapSection() {
        return soapSection;
    }

    public void setSoapSection(SoapSection soapSection) {
        this.soapSection = soapSection;
    }

    @OneToOne
    @JoinColumn(name = "PATIENT_ID")
    @ForeignKey(name = "FK_PATIENT_LAB_ORDER")
    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    @OneToOne
    @JoinColumn(name = "TEST_PANEL_ID")
    public LabTestPanel getLabTestPanel() {
        return labTestPanel;
    }

    public void setLabTestPanel(LabTestPanel labTestPanel) {
        this.labTestPanel = labTestPanel;
    }


    @ManyToOne
    @JoinColumn(name = "LAB_ORDER_REQUEST")
    public LabOrderRequest getLabOrderRequest() {
        return labOrderRequest;
    }

    public void setLabOrderRequest(LabOrderRequest labOrderRequest) {
        this.labOrderRequest = labOrderRequest;
    }

    public static enum STATUS {
        NEW("Initiated"), SENT("Ordered"), INPROCESS("In Process"), COMPLETED("Completed"), CANCELLED("Cancelled");

        private String description;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }


        STATUS(String description) {
            this.description = description;
        }
    }

    public static enum BILLINGSTATUS {
        UNINVOICED, PAID, INVOICED
    }

    @Enumerated(EnumType.STRING)
    public BILLINGSTATUS getBillingStatus() {
        return billingStatus;
    }

    public void setBillingStatus(BILLINGSTATUS billingStatus) {
        this.billingStatus = billingStatus;
    }

    @OneToOne(mappedBy="patientLabOrder")
    public OBRSegment getObrSegment() {
        return obrSegment;
    }

    public void setObrSegment(OBRSegment obrSegment) {
        this.obrSegment = obrSegment;
    }

    private static final long serialVersionUID = 1L;

    @Transient
    public String getTypeOfTest() {
        return typeOfTest;
    }

    public void setTypeOfTest(String typeOfTest) {
        this.typeOfTest = typeOfTest;
    }
}
