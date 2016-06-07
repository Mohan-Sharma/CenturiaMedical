package com.nzion.domain.person;

import com.nzion.domain.Person;
import com.nzion.domain.emr.lab.LabTest;
import com.nzion.domain.emr.lab.LabTestPanel;
import com.nzion.domain.emr.lab.LabTestProfile;

import javax.persistence.*;

/**
 * Created by Mohan Sharma on 4/24/2015.
 */
@Entity
public class PersonLab {
    private long id;
    private LabGroup labGroup;
    private LabTest labTest;
    private LabTestPanel labTestPanel;
    private LabTestProfile labTestProfile;
    private Person person;
    private String testName;
    private String testType;

    public void setTestType(String testType) {
        this.testType = testType;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @OneToOne
    @JoinColumn(name = "LAB_GROUP_ID")
    public LabGroup getLabGroup() {
        return labGroup;
    }

    public void setLabGroup(LabGroup labGroup) {
        this.labGroup = labGroup;
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
    @JoinColumn(name = "LAB_TEST_PANEL_ID")
    public LabTestPanel getLabTestPanel() {
        return labTestPanel;
    }

    public void setLabTestPanel(LabTestPanel labTestPanel) {
        this.labTestPanel = labTestPanel;
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
    @JoinColumn(name = "PERSON_ID")
    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getTestType(){
        return testType;
    }
}
