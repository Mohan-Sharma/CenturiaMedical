package com.nzion.service.emr.lab.impl;

import com.nzion.domain.Location;
import com.nzion.domain.Patient;
import com.nzion.domain.Person;
import com.nzion.domain.Provider;
import com.nzion.domain.billing.Invoice;
import com.nzion.domain.emr.lab.*;
import com.nzion.domain.emr.soap.LabOrderSection;
import com.nzion.domain.emr.soap.PatientLabOrder;
import com.nzion.domain.emr.soap.PatientSoapNote;
import com.nzion.repository.emr.lab.LabRepository;
import com.nzion.service.billing.BillingService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.service.emr.lab.LabService;
import com.nzion.util.UtilValidator;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Transactional
@Service("labService")
public class LabServiceImpl implements LabService {

    private final Logger logger = Logger.getLogger(LabServiceImpl.class);

    @Autowired(required = true)
    private LabRepository labRepository;
    private BillingService billingService;
    @Autowired
    private CommonCrudService commonCrudService;

    public void setLabRepository(LabRepository labRepository) {
        this.labRepository = labRepository;
    }

    public LabRepository getLabRepository() {
        return labRepository;
    }

    public BillingService getBillingService() {
        return billingService;
    }

    public CommonCrudService getCommonCrudService() {
        return commonCrudService;
    }

    public void setCommonCrudService(CommonCrudService commonCrudService) {
        this.commonCrudService = commonCrudService;
    }

    @Resource
    public void setBillingService(BillingService billingService) {
        this.billingService = billingService;
    }

    @Override
    public void saveLabTestPanel(LabTestPanel labTestPanel) {
//	for (PanelTechnician panelTechnician : labTestPanel.getPanelTechnicians()) {
//		for (Person person : panelTechnician.getTechnicians())
//			person.setSchedulable(true);
//	}
        labRepository.save(labTestPanel);
    }

    @Override
    public List<LabTest> findLabTestByTestCode(String testCode) {
        return labRepository.findLabTestByTestCode(testCode);
    }

    @Override
    public LabTestPanel findLabPanelByPanelCode(String panelCode) {
        List<LabTestPanel> labTestPanels = labRepository.findLabPanelByPanelCode(panelCode);
        return UtilValidator.isNotEmpty(labTestPanels) ? labTestPanels.get(0) : null;
    }

    @Override
    public void saveLabTestResult(LabTestResult labTestResult) {
        labRepository.save(labTestResult);
    }

    @Override
    public LabTestResultItem getLabTestResultItem(LabTestResult labTestResult, String labTestName) {
        List<LabTestResultItem> labTestResultItems = labRepository.getLabTestResultItem(labTestResult, labTestName);
        if (UtilValidator.isNotEmpty(labTestResultItems)) return labTestResultItems.get(0);
        return null;
    }

    @Override
    public List<PatientLabOrder> getPatientLabOrderByLoggedInUserLocation(Set<Location> loggedinUserLocations) {
        return labRepository.getAllRequestedPatientLaborder(loggedinUserLocations);
    }


    @Override
    public List<Person> getPersonsForTestName(String testName, Collection<Location> locations) {
        Set<LabTestPanel> labTestPanels = new HashSet<LabTestPanel>();
        labTestPanels.addAll(labRepository.getLabTestPanelsForTest(testName));
        labTestPanels.addAll(labRepository.getLabTestPanelFor(testName));
        List<Person> persons = new ArrayList<Person>();
        List<PanelTechnician> panelTechnicians = new ArrayList<PanelTechnician>();
        /*for (LabTestPanel labTestPanel : labTestPanels)
            panelTechnicians.addAll(labTestPanel.getPanelTechnicians());*/
        for (PanelTechnician panelTechnician : panelTechnicians)
            if (UtilValidator.isEmpty(locations) || locations.contains(panelTechnician.getLocation()))
                persons.addAll(panelTechnician.getTechnicians());
        return persons;
    }

    @Override
    public List<PatientLabOrder> getPatientLabOrdersFor(Patient patient) {
        return labRepository.getPatientLabOrdersFor(patient);
    }

    @Override
    public LabTest getLabTestFor(String testName) {
        List<LabTest> labTests = labRepository.getLabTestsFor(testName);
        if (UtilValidator.isNotEmpty(labTests)) return labTests.get(0);
        return null;
    }

    @Override
    public List<LabTest> searchLabTestsBy(String testCode, String testName) {
        return labRepository.searchLabTestsBy(testCode, testName);
    }

    @Override
    public List<LabTestPanel> searchLabTestPanelsBy(String panelName) {
        return labRepository.searchLabTestPanelsBy(panelName);
    }

    @Override
    public List<LabOrderRequest> getLabRequestBy(Patient patient, Provider provider, PatientLabOrder.STATUS status,
                                                 String panelName, String panelCode) {
        return labRepository.getLabRequestBy(patient, provider, status, panelName, panelCode);

    }

    @Override
    public List<LabOrderRequest> getLabRequestsForProcessing() {
        return labRepository.getLabRequestsForProcessing();

    }

    @Override
    public List<LabOrderRequest> getOutPatientLabRequests(Patient patient, PatientSoapNote patientSoapNote) {
        return labRepository.getOutPatientLabRequests(patient, patientSoapNote);
    }

    @Override
    public List<LabOrderRequest> getNonInPatientLabOrders() {
        return labRepository.getNonInPatientLabOrders();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientLabOrder> getPatientLabOrderBy(Patient patient, Provider provider, PatientLabOrder.STATUS status,
                                                      String panelName, String panelCode) {
        return labRepository.getPatientLabOrderBy(patient, provider, status, panelName, panelCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabRequisition> getLabRequisitionForProcessing(Boolean inPatient, Date fromDate, Date thruDate, Patient patient, String ipNumber) {
        return labRepository.getLabRequisitionForProcessing(inPatient, fromDate, thruDate, patient, ipNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabRequisition> getCompletedLabRequisition(Boolean inPatient, Date fromDate, Date thruDate, Patient patient, String specimenLabel, String ipNumber) {
        return labRepository.getCompletedLabRequisition(inPatient, fromDate, thruDate, patient, specimenLabel, ipNumber);
    }

    /**
     * If Invoice is null its signifies that the Lab Test
     * is being done for the IN PATIENT.
     *
     * @param labOrderRequest
     * @param invoice
     */
    @Override
    public LabRequisition createLabRequisition(LabOrderRequest labOrderRequest, Invoice invoice) {
        int nextToken = 1;
        String token = "";
        if (invoice != null) {
            LabRequisition latestLabRequisition = labRepository.getLatestOPDLabRequisition(invoice.getInvoiceDate());
            if (latestLabRequisition != null)
                nextToken = Integer.valueOf(latestLabRequisition.getToken()) + 1;
            token = "" + nextToken;
        }
        LabRequisition labRequisition = LabRequisition.createLabRequisition(labOrderRequest, invoice != null ? invoice.getInvoiceNumber() : "", token);
        labRepository.save(labRequisition);
        return labRequisition;
    }

    @Override
    public OBRSegment createOBRSegment(LabRequisition labRequisition, SpecimenModel specimenModel, LabTestPanel labTestPanel) {
        OBRSegment obrSegment = null;
        obrSegment = commonCrudService.findUniqueByEquality(OBRSegment.class, new String[]{"labTestPanel", "specimen"}, new Object[]{labTestPanel, specimenModel});
        if (obrSegment != null) {
            return obrSegment;
        }
        obrSegment = new OBRSegment();
        PatientSoapNote patientSoapNote = null;
        if (labRequisition.getPatientSoapNoteId() != null) {
            patientSoapNote = commonCrudService.getById(PatientSoapNote.class, labRequisition.getPatientSoapNoteId());
            obrSegment.setSoapNote(patientSoapNote);
            LabOrderSection labOrderSection = new LabOrderSection();
            labOrderSection.setSoapNote(patientSoapNote);
            PatientLabOrder searchVO = new PatientLabOrder();
            searchVO.setPatient(labRequisition.getPatient());
            searchVO.setLabOrderRequest(labRequisition.getLabOrderRequest());
            searchVO.setLabTestPanel(labTestPanel);
            searchVO.setSoapSection(labOrderSection);
            List<PatientLabOrder> patientSoapNoteList = (List<PatientLabOrder>) commonCrudService.searchByExample(searchVO);
            if (patientSoapNoteList.size() > 0)
                obrSegment.setPatientLabOrder(patientSoapNoteList.get(0));
            obrSegment.setProvider(patientSoapNote.getProvider());
        }
        obrSegment.setLabTestPanel(labTestPanel);
        obrSegment.setPatient(labRequisition.getPatient());
        labTestPanel = commonCrudService.getById(LabTestPanel.class, labTestPanel.getId());
        /*for (LabTest labTest : labTestPanel.getLabTests()) {
            OBXSegment obxSegment = new OBXSegment(labTest);
            obxSegment.setSoapNote(patientSoapNote);
            obrSegment.addOBX(obxSegment);
        }*/
        obrSegment.setObservationDateTime(new Date());
        obrSegment.setSpecimen(specimenModel);
        return obrSegment;
    }

    @Override
    public List<SpecimenModel> getSpecimenList(LabRequisition labRequisition, LabTestPanel selectedLabTest) {
        return labRepository.getSpecimenList(labRequisition, selectedLabTest);
    }

    @Override
    public List<OBRSegment> getLabResultForReviewByProvider(Provider provider) {
        return labRepository.getLabResultForReviewByProvider(provider);
    }

    @Override
    public List<OBXSegment> getLabResultForPatient(Patient patient) {
        return labRepository.getLabResultForPatient(patient);
    }

    @Override
    public List<OBXSegment> getLabResultFor(Patient patient, String testName, LabCategory labCategory, Integer numbOfDays) {
        return labRepository.getLabResultFor(patient, testName, labCategory, numbOfDays);
    }

    @Override
    public List<OBRSegment> getLabResultForReviewByProviderOrPatient(Provider provider, Patient patient) {
        return labRepository.getLabResultForReviewByProviderOrPatient(provider, patient);
    }

}
