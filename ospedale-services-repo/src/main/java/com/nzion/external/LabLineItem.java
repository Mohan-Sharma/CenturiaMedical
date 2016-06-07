package com.nzion.external;

import com.nzion.domain.emr.soap.PatientLabOrder;

/**
 * Created by Mohan Sharma on 4/7/2015.
 */
public class LabLineItem {
    private String testName;
    private String testCode;
    private String details;
    private boolean homeService;
    private String typeOfTest;

    public void setPropertiesFromlabLineItem(PatientLabOrder patientLabOrder) {
        this.testName = patientLabOrder.getTestName();
        this.testCode = patientLabOrder.getTestCode();
        this.homeService = patientLabOrder.isHomeService();
        this.details = patientLabOrder.getReasonForTest()+" "+patientLabOrder.getTestNotes();
        if(patientLabOrder.getLabTest() != null)
            typeOfTest = "LabTest";
        if(patientLabOrder.getLabTestPanel() != null)
            typeOfTest = "LabPanel";
        if(patientLabOrder.getLabTestProfile() != null)
            typeOfTest = "LabProfile";
    }
}
