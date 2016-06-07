package com.nzion.service.patient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.nzion.hibernate.ext.multitenant.TenantIdHolder;
import com.nzion.service.PatientService;
import com.nzion.service.dto.RequestAppointmentDto;
import com.nzion.util.UtilValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * Created by Mohan Sharma on 7/28/2015.
 */
public class CancelPatientAppointmentRequestServlet extends HttpServlet {

    @Autowired
    private PatientService patientService;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String tenantId = request.getParameter("clinicId");
        if(tenantId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ClinicId cannot be null");
            return;
        }
        TenantIdHolder.setTenantId(tenantId);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false);
        objectMapper.setDateFormat(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"));
        RequestAppointmentDto requestAppointmentDto = objectMapper.readValue(request.getInputStream(), RequestAppointmentDto.class);
        if(requestAppointmentDto == null){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "request appointment details cannot be null");
            return;
        }
        if(UtilValidator.isEmpty(requestAppointmentDto.isUrgent()) || UtilValidator.isEmpty(requestAppointmentDto.getSentOn()) || UtilValidator.isEmpty(requestAppointmentDto.getPatientFirstName())
                || UtilValidator.isEmpty(requestAppointmentDto.getDoctorId()) || UtilValidator.isEmpty(requestAppointmentDto.getPatientLastName()) || UtilValidator.isEmpty(requestAppointmentDto.getPatientContactNo())
                || UtilValidator.isEmpty(requestAppointmentDto.getPatientEmail()) || UtilValidator.isEmpty(requestAppointmentDto.getVisitType())){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "request appointment details cannot be null");
            return;
        }
        boolean result = patientService.cancelAppointmentReqFromMobile(requestAppointmentDto.isUrgent(), requestAppointmentDto.getSentOn(), requestAppointmentDto.getPatientId(),
                requestAppointmentDto.getDoctorId(), requestAppointmentDto.getPatientFirstName(), requestAppointmentDto.getPatientLastName(),
                requestAppointmentDto.getPatientContactNo(), requestAppointmentDto.getPatientEmail(), requestAppointmentDto.getMessageText(), requestAppointmentDto.getVisitType());
        if(result == Boolean.TRUE)
            response.setStatus(HttpServletResponse.SC_OK, "action successfully completed");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
