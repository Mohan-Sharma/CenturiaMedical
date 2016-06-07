package com.nzion.service.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.nzion.domain.Schedule;
import com.nzion.domain.billing.Invoice;
import com.nzion.hibernate.ext.multitenant.TenantIdHolder;
import com.nzion.service.PatientService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.util.PatientInsuranceUtility;
import com.nzion.util.UtilValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mohan Sharma on 8/11/2015.
 */
public class InsuranceDetailsProviderServlet extends HttpServlet {

    @Autowired
    private CommonCrudService commonCrudService;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String tenantId = request.getParameter("clinicId");
        if(tenantId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ClinicId cannot be null");
            return;
        }
        TenantIdHolder.setTenantId(tenantId);
        String invoiceId = request.getParameter("invoiceId");
        String afyaId = request.getParameter("afyaId");
        String startDate = request.getParameter("startDate");
        String doctorId = request.getParameter("doctorId");
        String visitStartDateTime = request.getParameter("visitStartDateTime");
        String visitEndDateTime = request.getParameter("visitEndDateTime");
        Map<String, Object> insuranceDetails = new HashMap<>();
        if(UtilValidator.isNotEmpty(invoiceId)){
            insuranceDetails = getInsuranceDetails(Long.parseLong(invoiceId), null);
        }else {
            if(UtilValidator.isEmpty(afyaId) || UtilValidator.isEmpty(startDate) || UtilValidator.isEmpty(doctorId) || UtilValidator.isEmpty(visitStartDateTime) || UtilValidator.isEmpty(visitEndDateTime)){
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request parameters cannot be null");
                return;
            } else {
                Schedule schedule = commonCrudService.findUniqueByEquality(Schedule.class, new String[]{"patient.afyaId", "startDate", "person.id", "startTime", "endTime"}, new Object[]{afyaId, startDate, Long.parseLong(doctorId), visitStartDateTime, visitEndDateTime});
                insuranceDetails = getInsuranceDetails(null, schedule);
            }
        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false);
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        PrintWriter writer = response.getWriter();
        objectMapper.writeValue(writer, insuranceDetails);
        writer.close();
    }

    private Map<String, Object> getInsuranceDetails(Long invoiceId, Schedule schedule) {
        PatientInsuranceUtility patientInsuranceUtility = new PatientInsuranceUtility();
        if(invoiceId != null){
            Invoice invoice = commonCrudService.getById(Invoice.class, invoiceId);
            return patientInsuranceUtility.getPatientPayableAndInsurancePayable(invoice);
        } else {
            Invoice invoice = commonCrudService.findUniqueByEquality(Invoice.class, new String[]{"schedule.id"}, new Object[]{schedule.getId()});
            return patientInsuranceUtility.getPatientPayableAndInsurancePayable(invoice);
        }

    }
}
