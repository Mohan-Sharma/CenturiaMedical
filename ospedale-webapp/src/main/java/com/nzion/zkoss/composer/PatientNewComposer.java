package com.nzion.zkoss.composer;

import com.nzion.domain.Enumeration;
import com.nzion.domain.Patient;
import com.nzion.domain.Practice;
import com.nzion.domain.base.FieldRestriction;
import com.nzion.factory.PatientFactory;
import com.nzion.hibernate.ext.multitenant.TenantIdHolder;
import com.nzion.repository.common.CommonCrudRepository;
import com.nzion.repository.notifier.utility.NotificationTask;
import com.nzion.service.PatientService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.service.impl.FileBasedServiceImpl;
import com.nzion.util.*;
import com.nzion.view.PatientViewObject;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.SimpleConstraint;
import org.zkoss.zul.Span;
import org.zkoss.zul.Window;
import org.zkoss.zul.impl.InputElement;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class PatientNewComposer extends OspedaleAutowirableComposer {
    static String PORTAL_AUTHENTICATION = null;
    static {
        Properties properties = new Properties();
        try {
            String profileName = System.getProperty("profile.name") != null ? System.getProperty("profile.name") : "dev";
            properties.load(RestServiceConsumer.class.getClassLoader().getResourceAsStream("application-"+profileName+".properties"));
            PORTAL_AUTHENTICATION = (String)properties.get("PORTAL_AUTHENTICATION");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static final long serialVersionUID = 1L;
    @Wire
    Window NewPatient;

    private NotificationTask notificationTask;
    private PatientViewObject patientVO;
    private PatientService patientService;
    private CommonCrudRepository commonCrudRepository;
    private CommonCrudService commonCrudService;

    private FileBasedServiceImpl fileBasedServiceImpl;

    public void setCommonCrudRepository(CommonCrudRepository commonCrudRepository) {
        this.commonCrudRepository = commonCrudRepository;
    }

    public void onClick$Save(final Event evt) throws InterruptedException, IOException {
        System.out.println(patientVO.getPatient().getPatientType());
        if(commonCrudService == null)
        	commonCrudService = Infrastructure.getSpringBean("commonCrudService");
        
        Practice practice = commonCrudService.getAll(Practice.class) != null ? commonCrudService.getAll(Practice.class).get(0) : null;
        patientVO.getPatient().setRegisteredFrom("CLINIC_MANUAL_ENTRY");    // Kannan 2015-12-28
        String afyaId = "";
        if (PORTAL_AUTHENTICATION.equals("true")) {
            afyaId = RestServiceConsumer.checkIfPatientExistInPortalAndCreateIfNotExist(patientVO.getPatient(), practice != null ? practice.getTenantId() : null);
        } else if (PORTAL_AUTHENTICATION.equals("false")){
            Connection connection = null;
            try {

            DataSource dataSource = (DataSource)Infrastructure.getSpringBean("tenantDataSource");
            connection = dataSource.getConnection();
            Statement statement = connection.createStatement();




                /*long afyaSeqId = this.getNextAfyaId();
                String afyaId = "KWT" + afyaSeqId;
                return afyaId;*/
                String str1 = "select sequence_cur_value from entity_sequence where sequence_name='patient_sequence'";
                /*String str2 = "update entity_sequence set sequence_cur_value= " + id + " where sequence_name='patient_sequence'";*/
                String s = "";
            ResultSet resultSet = statement.executeQuery(str1);
            while (resultSet.next()) {
                s = resultSet.getString(1);
                System.out.println("**************************"+s);
                /*resultSet.beforeFirst();
                resultSet.last();
                int size = resultSet.getRow();
                System.out.println("**************************"+size);*/
            }
                Long id = Long.parseLong(s);
                id = id+1;
                String str2 = "update entity_sequence set sequence_cur_value= " + id + " where sequence_name='patient_sequence'";
                afyaId = "KWT"+id;
                statement.executeUpdate(str2);

                String str3 = "insert into patient(afya_id, account_number, first_name, last_name, mobile_number)values('"+afyaId+"',null,'"+patientVO.getPatient().getFirstName()+"','"+patientVO.getPatient().getLastName()+"','"+patientVO.getPatient().getContacts().getMobileNumber()+"')";
                int i = statement.executeUpdate(str3);

            } catch (Exception e){
                e.printStackTrace();
            } finally {
                if (connection != null){
                    try {
                        connection.close();
                    }catch (Exception e){}
                }
            }
        }
        if (afyaId != null) {
            patientVO.getPatient().setAfyaId(afyaId);
            if (UtilValidator.isEmpty(patientVO.getPatient().getPatientType())) {
                patientVO.getPatient().setPatientType("CASH PAYING");
            }
            List<Patient> oldPatientList = commonCrudRepository.findByEquality(Patient.class, new String[]{"afyaId"}, new Object[]{afyaId});
            if (UtilValidator.isEmpty(oldPatientList)) {
                oldPatientList = commonCrudRepository.findByEquality(Patient.class, new String[]{"firstName",
                        "lastName", "dateOfBirth", "contacts.mobileNumber"}, new Object[]{patientVO.getPatient().getFirstName(),
                        patientVO.getPatient().getLastName(), patientVO.getPatient().getDateOfBirth(), patientVO.getPatient().getContacts().getMobileNumber()});
            }
            if (UtilValidator.isNotEmpty(oldPatientList)) {
                patientVO.setPropertiesToExistingPatient(oldPatientList.get(0), patientVO.getPatient());
            }
            newPatientCreate(evt);
            UtilMessagesAndPopups.showSuccess();

            //new code start for sending sms
            //notificationTask.sendRegistrationMailToPatient(patientVO);
        }
    }

    private void populateExistingPatient(Event evt, List<Patient> oldPatientList) {
        Patient existingPatient = oldPatientList.get(0);
        patientVO.setPatient(existingPatient);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("entity", patientVO.getPatient());
        Executions.createComponents("/patient/patientView.zul", (Component) desktopScope.get("contentArea"), args);
        evt.getTarget().getFellow("NewPatient").detach();
    }

    private void newPatientCreate(Event evt){
        try {
            patientService.saveOrUpdate(patientVO);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        fileBasedServiceImpl.createDefaultFolderStructure(patientVO.getPatient());
        UtilMessagesAndPopups.displaySuccess();
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("entity", patientVO.getPatient());
        Executions.createComponents("/patient/patientView.zul", (Component) desktopScope.get("contentArea"), args);
        evt.getTarget().getFellow("NewPatient").detach();
    }

    public PatientViewObject getPatientVO() {
        return patientVO;
    }

    public void setPatientVO(PatientViewObject patientVO) {
        this.patientVO = patientVO;
    }

    public void setPatientService(PatientService patientService) {
        this.patientService = patientService;
    }

    @Override
    public void doAfterCompose(Component component) throws Exception {
        super.doAfterCompose(component);
        if(commonCrudService == null)
        	commonCrudService = Infrastructure.getSpringBean("commonCrudService");
        patientVO = PatientFactory.createPatientViewObject();
        if(patientVO.getPatient().getLanguage() == null) {
            Enumeration engEnum = commonCrudService.getById(Enumeration.class, Long.valueOf("10338"));
            patientVO.getPatient().setLanguage(engEnum);
        }
        component.setAttribute("vo", patientVO);
        Patient patientEditObject = (Patient) component.getAttribute("patientEditObject");
        if(patientEditObject != null)
            patientVO.setPatient(patientEditObject);
        setConstraints(component);
    }

    private void setConstraints(Component component) {
        List<FieldRestriction> mandatoryRestrictions = commonCrudRepository.findByEquality(FieldRestriction.class, new String[] { "entityName", "restrictionType"},
                new Object[] {com.nzion.domain.Patient.class.getSimpleName(), FieldRestriction.RESTRICTION_TYPE.MANDATORY});
        for (FieldRestriction restriction : mandatoryRestrictions) {
            if ("true".equalsIgnoreCase(restriction.getValue())) {
                System.out.println(restriction.getValue() + "------------------" + restriction.getField() + "\n\n\n\n\n\n\n\n");
                String componentName = UtilDisplay.uiStringToCamelCase(restriction.getDisplayName());
                Component comp = component.getFellowIfAny(componentName, true);
                if (comp instanceof InputElement) {
                    ((InputElement) comp).setConstraint(new SimpleConstraint(SimpleConstraint.NO_EMPTY));
                    Span mandatoryMarker = (Span) component.getFellowIfAny(Constants.LABEL_PREFIX + componentName + "Req", true);
                    mandatoryMarker.setVisible(true);
                }
            }
        }
    }

    public FileBasedServiceImpl getFileBasedServiceImpl() {
        return fileBasedServiceImpl;
    }

    @Resource
    public void setFileBasedServiceImpl(FileBasedServiceImpl fileBasedServiceImpl) {
        this.fileBasedServiceImpl = fileBasedServiceImpl;
    }

    public List<String> getBloodGroup(){
        List<String> bloodGroups = new ArrayList<String>(){{
            add("A");
            add("B");
            add("AB");
            add("O");
        }};
        return bloodGroups;
    }

    public List<String> getRh(){
        List<String> rhs = new ArrayList<String>(){{
            add("+");
            add("-");
        }};
        return rhs;
    }

    public Patient savePatientWhenTriggeredFromLookup(Event evt, boolean isQuickBook){
        try {
        	if(commonCrudService == null)
            	commonCrudService = Infrastructure.getSpringBean("commonCrudService");
        	
            Practice practice = commonCrudService.getAll(Practice.class) != null ? commonCrudService.getAll(Practice.class).get(0) : null;
            patientVO.getPatient().setRegisteredFrom("CLINIC_MANUAL_ENTRY");    // Kannan 2015-12-28
            String afyaId = RestServiceConsumer.checkIfPatientExistInPortalAndCreateIfNotExist(patientVO.getPatient(), practice != null ? practice.getTenantId() : null);
            if (afyaId != null) {
                patientVO.getPatient().setAfyaId(afyaId);
                final List<Patient> oldPatientListByAfyaId = commonCrudRepository.findByEquality(Patient.class, new String[]{"afyaId"}, new Object[]{afyaId});
                boolean isNewPatient = true;
                if (UtilValidator.isNotEmpty(patientVO.getPatient().getId())) {
                    isNewPatient = false;
                }
                if (UtilValidator.isEmpty(oldPatientListByAfyaId)) {
                    final List<Patient> oldPatientList = commonCrudRepository.findByEquality(Patient.class, new String[]{"firstName",
                            "lastName", "dateOfBirth"}, new Object[]{patientVO.getPatient().getFirstName(),
                            patientVO.getPatient().getLastName(), patientVO.getPatient().getDateOfBirth()});
                    if (UtilValidator.isNotEmpty(oldPatientList)) {
                        patientVO.setPropertiesToExistingPatient(oldPatientList.get(0), patientVO.getPatient());
                    }
                } else {
                    patientVO.setPropertiesToExistingPatient(oldPatientListByAfyaId.get(0), patientVO.getPatient());
                }
                patientService.saveOrUpdate(patientVO);

                if (isNewPatient)
                    fileBasedServiceImpl.createDefaultFolderStructure(patientVO.getPatient());

                return patientVO.getPatient() != null ? patientVO.getPatient() : new Patient();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return patientVO.getPatient() != null? patientVO.getPatient(): new Patient();
    }

    public void sendMessageForHighPriorityPatient(Patient patient){}

    public Patient quickRegisterPatient(){
        try {
            registerPatient("firstName","lastName", "contacts.mobileNumber", patientVO.getPatient().getFirstName(), patientVO.getPatient().getLastName(), patientVO.getPatient().getContacts().getMobileNumber());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return patientVO.getPatient() != null? patientVO.getPatient(): new Patient();
    }

    public void registerPatient(String column1, String column2, String column3, Object value1, Object value2, Object value3){
        if(UtilValidator.isEmpty(patientVO.getPatient().getPatientType())){
            patientVO.getPatient().setPatientType("CASH PAYING");
        }
        final List<Patient> oldPatientList = commonCrudRepository.findByEquality(Patient.class, new String[] { column1,column2, column3 }, new Object[] { value1, value2, value3 });
        if(oldPatientList.size() != 0) {
            //patientVO.setPatient(oldPatientList.get(0));
            patientVO.setPropertiesToExistingPatient(oldPatientList.get(0), patientVO.getPatient());
        }
        patientService.saveOrUpdate(patientVO);
        fileBasedServiceImpl.createDefaultFolderStructure(patientVO.getPatient());
    }

}