package com.nzion.service.patient;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nzion.util.UtilValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.nzion.domain.ContactFields;
import com.nzion.domain.Enumeration;
import com.nzion.domain.Patient;
import com.nzion.domain.Person;
import com.nzion.hibernate.ext.multitenant.TenantIdHolder;
import com.nzion.service.common.CommonCrudService;
import com.nzion.service.common.impl.EnumerationServiceImpl;
import com.nzion.service.dto.BookAppointmentDto;
import com.nzion.service.impl.FileBasedServiceImpl;
import com.nzion.util.RestServiceConsumer;

public class CreatePatientServlet extends HttpServlet{
	
	@Autowired
    private FileBasedServiceImpl fileBasedServiceImpl;
	
    @Autowired
    CommonCrudService commonCrudService;
    
    @Autowired
    EnumerationServiceImpl enumerationServiceImpl;
    
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
    }
    
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String tenantId = request.getParameter("clinicId");
        if(tenantId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ClinicId cannot be null");
            return;
        }
        TenantIdHolder.setTenantId(tenantId);
        
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        objectMapper.setDateFormat(df);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        BookAppointmentDto bookAppointmentDto = objectMapper.readValue(request.getInputStream(), BookAppointmentDto.class);
        
        if(bookAppointmentDto.getFirstName() == null || bookAppointmentDto.getLastName() == null || bookAppointmentDto.getMobileNumber() == null ||  bookAppointmentDto.getDateOfBirth() == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Patient details cannot be null");
            return;
        }
        
        Enumeration gender = getGenderEnumerationForPatient(bookAppointmentDto.getGender());
        if(gender == null){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "gender cannot be null");
            return;
        }
        
        Patient patient = new Patient();
        patient.setFirstName(bookAppointmentDto.getFirstName());
        patient.setLastName(bookAppointmentDto.getLastName());
        patient.setMiddleName(bookAppointmentDto.getMiddleName());
        patient.setNationality(bookAppointmentDto.getNationality());
        Enumeration langEnumeration = commonCrudService.findUniqueByEquality(Enumeration.class, new String[]{"enumType", "enumCode"}, new Object[]{"LANGUAGE", bookAppointmentDto.getPreferredLanguage()});
        patient.setLanguage(langEnumeration);
        patient.setPatientType("CASH PAYING");
        patient.setDateOfBirth(bookAppointmentDto.getDateOfBirth());
        ContactFields contactFields = new ContactFields();
        contactFields.setMobileNumber(bookAppointmentDto.getMobileNumber());
        contactFields.setEmail(bookAppointmentDto.getEmailId());
        patient.setContacts(contactFields);
        patient.setGender(gender);
        patient.setCivilId(bookAppointmentDto.getCivilId());    //kannan 2015-12-13 - moved it here from below
        patient.setFileNo(bookAppointmentDto.getFileNo());    //kannan 2015-12-13 - moved it here from below
        patient.setRegisteredFrom(bookAppointmentDto.getRegisteredFrom());
        Map<String,String> result = RestServiceConsumer.checkIfPatientExistInPortalAndCreateIfNotExistWithResult(patient, tenantId);
        String afyaId = result.get("afyaId");
        patient.setAfyaId(afyaId);
        //patient.setCivilId(bookAppointmentDto.getCivilId());
        List<Patient> patientList = commonCrudService.findByEquality(Patient.class, new String[]{"afyaId"}, new Object[]{afyaId});
        if(UtilValidator.isEmpty(patientList)) {
            patient = commonCrudService.save(patient);
            fileBasedServiceImpl.createDefaultFolderStructure(patient);
        }

        // pass on the results from the Portal service back to the caller
        for(Map.Entry<String,String> entry: result.entrySet())
            response.setHeader(entry.getKey(), entry.getValue());

        response.setStatus(HttpServletResponse.SC_OK, afyaId );
    }
    
    private Enumeration getGenderEnumerationForPatient(String gender) {
        List<Enumeration> emEnumerations = enumerationServiceImpl.getGeneralEnumerationsByType("GENDER");
        for(Enumeration enumeration : emEnumerations){
            if(enumeration.getDescription().equals(gender))
                return enumeration;
        }
        return null;
    }
    
    private Enumeration getLanguageEnumerationForPatient(String language) {
        List<Enumeration> emEnumerations = enumerationServiceImpl.getGeneralEnumerationsByType("LANGUAGE");
        for(Enumeration enumeration : emEnumerations){
            if(enumeration.getDescription().equals(language))
                return enumeration;
        }
        return null;
    }

}
