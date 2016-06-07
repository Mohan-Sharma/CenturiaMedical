package com.nzion.service.patient;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.nzion.domain.Patient;
import com.nzion.domain.Person;
import com.nzion.hibernate.ext.multitenant.TenantIdHolder;
import com.nzion.repository.PatientRepository;
import com.nzion.repository.PersonRepository;
import com.nzion.repository.PracticeRepository;
import com.nzion.repository.notifier.utility.NotificationTaskExecutor;
import com.nzion.service.PatientService;
import com.nzion.service.ScheduleService;
import com.nzion.service.billing.BillingService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.service.common.impl.EnumerationServiceImpl;
import com.nzion.service.dto.BookAppointmentDifferentSlotsDto;
import com.nzion.service.dto.BookAppointmentDto;
import com.nzion.service.impl.FileBasedServiceImpl;
import com.nzion.util.RestServiceConsumer;


public class RequestAppointmentServlet extends HttpServlet {

    @Autowired
    private PatientService patientService;
    
    @Autowired
    PersonRepository personRepository;
   
    @Autowired
    ScheduleService scheduleService;
    
    @Autowired
    PatientRepository patientRepository;
    
    @Autowired
    PracticeRepository practiceRepository;
    
    @Autowired
    CommonCrudService commonCrudService;
    
    @Autowired
    NotificationTaskExecutor notificationTaskExecutor;
    
    @Autowired
    EnumerationServiceImpl enumerationServiceImpl;
    
    @Autowired
    private BillingService billingService;
    
    @Autowired
    private FileBasedServiceImpl fileBasedServiceImpl;
    

    public void init(ServletConfig config) throws ServletException{
        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String tenantId = (String)request.getParameter("clinicId");
        TenantIdHolder.setTenantId(tenantId);
        

        Map<String, Object> clinicDetails = RestServiceConsumer.getClinicDetailsByClinicId(tenantId);
        
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        objectMapper.setDateFormat(df);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        
        BookAppointmentDifferentSlotsDto bookAppointmentDifferentSlotsDto = objectMapper.readValue(request.getInputStream(), BookAppointmentDifferentSlotsDto.class);
        
        Person provider = personRepository.getPersonById(Long.valueOf(bookAppointmentDifferentSlotsDto.getProviderId()));
        
        BookAppointmentDto bookAppointmentDto = new BookAppointmentDto();
        bookAppointmentDto.setCivilId(bookAppointmentDifferentSlotsDto.getCivilId());
        bookAppointmentDto.setDateOfBirth(bookAppointmentDifferentSlotsDto.getDateOfBirth());
        bookAppointmentDto.setEmailId(bookAppointmentDifferentSlotsDto.getEmailId());
        bookAppointmentDto.setFirstName(bookAppointmentDifferentSlotsDto.getFirstName());
        bookAppointmentDto.setLastName(bookAppointmentDifferentSlotsDto.getLastName());
        bookAppointmentDto.setFromMobileApp(true);
        bookAppointmentDto.setGender(bookAppointmentDifferentSlotsDto.getGender());
        bookAppointmentDto.setNotes(bookAppointmentDifferentSlotsDto.getNotes());
        bookAppointmentDto.setMobileNumber(bookAppointmentDifferentSlotsDto.getMobileNumber());
        bookAppointmentDto.setProviderId(Long.valueOf(bookAppointmentDifferentSlotsDto.getProviderId()));
        bookAppointmentDto.setVisitType(bookAppointmentDifferentSlotsDto.getVisitType());
        bookAppointmentDto.setLocation(bookAppointmentDifferentSlotsDto.getLocation());
        
        bookAppointmentDto.setAppointmentEndDate(bookAppointmentDifferentSlotsDto.getFirstAppointmentEndDate());
        bookAppointmentDto.setAppointmentStartDate(bookAppointmentDifferentSlotsDto.getFirstAppointmentStartDate());
        bookAppointmentDto.setPreferredLanguage(bookAppointmentDifferentSlotsDto.getPreferredLanguage());
        
        
        PatientBookAppointmentServlet patientBookAppointmentServlet =  new PatientBookAppointmentServlet();
        patientBookAppointmentServlet.setPersonRepository(personRepository);
        patientBookAppointmentServlet.setScheduleService(scheduleService);
        patientBookAppointmentServlet.setPatientRepository(patientRepository);
        patientBookAppointmentServlet.setPracticeRepository(practiceRepository);
        patientBookAppointmentServlet.setCommonCrudService(commonCrudService);
        patientBookAppointmentServlet.setNotificationTaskExecutor(notificationTaskExecutor);
        patientBookAppointmentServlet.setEnumerationServiceImpl(enumerationServiceImpl);
        patientBookAppointmentServlet.setBillingService(billingService);
        patientBookAppointmentServlet.setFileBasedServiceImpl(fileBasedServiceImpl);
        
        Patient patient = patientBookAppointmentServlet.checkIfPatientAlreadyExistOrPersist(bookAppointmentDto, tenantId, response);
        if(patient == null){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "either patient does not exist with the given afya_id or gender is null");
            return;
        }
    	
        Map<String, Object> result = patientBookAppointmentServlet.bookAppointment(response, patient, provider, bookAppointmentDto, clinicDetails);
        
        String status = (String)result.get("status");
        
        if(status.equals("created")){
        	response.setStatus(HttpServletResponse.SC_OK, "Appointment booked on " + bookAppointmentDifferentSlotsDto.getFirstAppointmentStartDate());
        	return;
        }
        
        bookAppointmentDto.setAppointmentEndDate(bookAppointmentDifferentSlotsDto.getSecondAppointmentEndDate());
        bookAppointmentDto.setAppointmentStartDate(bookAppointmentDifferentSlotsDto.getSecondAppointmentStartDate());
        result = patientBookAppointmentServlet.bookAppointment(response, patient, provider, bookAppointmentDto, clinicDetails);
        status = (String)result.get("status");
        
        if(status.equals("created")){
        	response.setStatus(HttpServletResponse.SC_OK, "Appointment booked on " + bookAppointmentDifferentSlotsDto.getSecondAppointmentStartDate());
        	return;
        }
        
        bookAppointmentDto.setAppointmentEndDate(bookAppointmentDifferentSlotsDto.getThirdAppointmentEndDate());
        bookAppointmentDto.setAppointmentStartDate(bookAppointmentDifferentSlotsDto.getThirdAppointmentStartDate());
        result = patientBookAppointmentServlet.bookAppointment(response, patient, provider, bookAppointmentDto, clinicDetails);
        status = (String)result.get("status");
        
        if(status.equals("created")){
        	response.setStatus(HttpServletResponse.SC_OK, "Appointment booked on " + bookAppointmentDifferentSlotsDto.getThirdAppointmentStartDate());
        	return;
        }
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Appointment not book");
        
    }
    

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
       
    }

	public PatientService getPatientService() {
		return patientService;
	}

	public void setPatientService(PatientService patientService) {
		this.patientService = patientService;
	}

	public PersonRepository getPersonRepository() {
		return personRepository;
	}

	public void setPersonRepository(PersonRepository personRepository) {
		this.personRepository = personRepository;
	}

	public ScheduleService getScheduleService() {
		return scheduleService;
	}

	public void setScheduleService(ScheduleService scheduleService) {
		this.scheduleService = scheduleService;
	}

	public PatientRepository getPatientRepository() {
		return patientRepository;
	}

	public void setPatientRepository(PatientRepository patientRepository) {
		this.patientRepository = patientRepository;
	}

	public PracticeRepository getPracticeRepository() {
		return practiceRepository;
	}

	public void setPracticeRepository(PracticeRepository practiceRepository) {
		this.practiceRepository = practiceRepository;
	}

	public CommonCrudService getCommonCrudService() {
		return commonCrudService;
	}

	public void setCommonCrudService(CommonCrudService commonCrudService) {
		this.commonCrudService = commonCrudService;
	}

	public NotificationTaskExecutor getNotificationTaskExecutor() {
		return notificationTaskExecutor;
	}

	public void setNotificationTaskExecutor(NotificationTaskExecutor notificationTaskExecutor) {
		this.notificationTaskExecutor = notificationTaskExecutor;
	}

	public EnumerationServiceImpl getEnumerationServiceImpl() {
		return enumerationServiceImpl;
	}

	public void setEnumerationServiceImpl(EnumerationServiceImpl enumerationServiceImpl) {
		this.enumerationServiceImpl = enumerationServiceImpl;
	}

	public BillingService getBillingService() {
		return billingService;
	}

	public void setBillingService(BillingService billingService) {
		this.billingService = billingService;
	}

	public FileBasedServiceImpl getFileBasedServiceImpl() {
		return fileBasedServiceImpl;
	}

	public void setFileBasedServiceImpl(FileBasedServiceImpl fileBasedServiceImpl) {
		this.fileBasedServiceImpl = fileBasedServiceImpl;
	}
    
    
    
    
    


}
