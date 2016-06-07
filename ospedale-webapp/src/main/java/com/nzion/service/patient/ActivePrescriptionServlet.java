package com.nzion.service.patient;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nzion.domain.Patient;
import com.nzion.domain.emr.soap.PatientRx;
import com.nzion.dto.OrderDto;
import com.nzion.hibernate.ext.multitenant.TenantIdHolder;
import com.nzion.service.SoapNoteService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.service.dto.ActivePrescriptionDto;
import com.nzion.util.UtilMessagesAndPopups;

public class ActivePrescriptionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	@Autowired
    private SoapNoteService soapNoteService;
	
	@Autowired
	private CommonCrudService commonCrudService;

	public void init(ServletConfig config) throws ServletException{
        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
    }
	
	@Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String tenantId = (String)request.getParameter("clinicId");
        TenantIdHolder.setTenantId(tenantId);
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false);
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        List<ActivePrescriptionDto> myObjects = objectMapper.readValue(request.getInputStream(), objectMapper.getTypeFactory().constructCollectionType(List.class, ActivePrescriptionDto.class));
        List<PatientRx> patientRxs = new ArrayList<PatientRx>();
        PatientRx lastPatientRx = null;
        for(ActivePrescriptionDto activePrescriptionDto : myObjects){
            PatientRx patientRx = commonCrudService.getById(PatientRx.class, Long.valueOf(activePrescriptionDto.getPatientRxId()));
            patientRx.setHomeDelivery(true);
            patientRx.setTotalCountTransient(activePrescriptionDto.getOrderQuantity());
            if(lastPatientRx != null && !lastPatientRx.getRxSection().getPharmacyTenantId().equals(patientRx.getRxSection().getPharmacyTenantId())){
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Drugs selected for multiple Pharmacy. Please select drugs for single Pharmacy and place order");
                return;
            }
            patientRxs.add(patientRx);
        }
        
        OrderDto dto = soapNoteService.prepairedPharmacyPrescription(patientRxs);
        Gson gson = new GsonBuilder().serializeNulls().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        request.setCharacterEncoding("utf8");
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(dto));

    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        String tenantId = (String)request.getParameter("clinicId");
        TenantIdHolder.setTenantId(tenantId);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false);
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        String afyaId = request.getParameter("afyaId") != null ? request.getParameter("afyaId").trim() : request.getParameter("afyaId");
        if(afyaId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing expected parameters");
            return;
        }
        Patient patient = commonCrudService.getByUniqueValue(Patient.class, "afyaId", afyaId);
        List<PatientRx> medications = soapNoteService.getAllPatientActivePrescription(patient);
        List<ActivePrescriptionDto> activePrescriptionDtos = new ArrayList<ActivePrescriptionDto>();
        for(PatientRx patientRx : medications){
            ActivePrescriptionDto activePrescriptionDto = new ActivePrescriptionDto();
            activePrescriptionDto.setPatientRxId(patientRx.getId().toString());
            activePrescriptionDto.setDrugName(patientRx.getDrug().getTradeName());
            activePrescriptionDto.setDoctorName(patientRx.getProvider().getFirstName() + " " + patientRx.getProvider().getLastName());
            activePrescriptionDto.setFrequency(patientRx.getFrequency().toString());
            activePrescriptionDto.setFreqQualifier(patientRx.getFrequencyQualifier().toString());
            activePrescriptionDto.setNoOfDays(patientRx.getNumberOfDays());
            activePrescriptionDto.setTotalCount(patientRx.getTotalCount());
            activePrescriptionDto.setStartDate(patientRx.getStartDate().toString());
            activePrescriptionDto.setPharmacyTenantId(patientRx.getRxSection().getPharmacyTenantId());
            activePrescriptionDto.setPharmacyTenantName(patientRx.getRxSection().getPharmacyTenantId());
            activePrescriptionDtos.add(activePrescriptionDto);
        }

        Gson gson = new GsonBuilder().serializeNulls().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        request.setCharacterEncoding("utf8");
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(activePrescriptionDtos));
    }
}
