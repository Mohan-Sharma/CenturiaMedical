package com.nzion.service.patient;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;

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
import com.nzion.domain.PharmacyOrder;
import com.nzion.external.ExternalServiceClient;
import com.nzion.hibernate.ext.multitenant.TenantIdHolder;
import com.nzion.service.SoapNoteService;
import com.nzion.service.common.CommonCrudService;

public class ActivePrescriptionCancelServlet extends HttpServlet{
	
private static final long serialVersionUID = 1L;
	
	@Autowired
    private SoapNoteService soapNoteService;
	
	@Autowired
	private CommonCrudService commonCrudService;
	
	@Autowired
    private ExternalServiceClient externalServiceClient;
	
	public void init(ServletConfig config) throws ServletException{
        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
    }
	
	
	@Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
    }
	
	@Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String tenantId = (String)request.getParameter("clinicId");
        TenantIdHolder.setTenantId(tenantId);
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false);
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        String afyaId = request.getParameter("afyaId") != null ? request.getParameter("afyaId").trim() : request.getParameter("afyaId");
        String orderId = request.getParameter("orderId") != null ? request.getParameter("orderId").trim() : request.getParameter("orderId");        
        if(afyaId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing expected parameters");
            return;
        }
        Patient patient = commonCrudService.getByUniqueValue(Patient.class, "afyaId", afyaId);
        PharmacyOrder pharmacyOrder = commonCrudService.findUniqueByEquality(PharmacyOrder.class, new String[]{"orderId","patient"}, new Object[]{orderId,patient});
        
        String message = externalServiceClient.cancelOrder(orderId,pharmacyOrder.getPharmacyTennantId());
        
        Gson gson = new GsonBuilder().serializeNulls().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        request.setCharacterEncoding("utf8");
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(message));

    }

}
