package com.nzion.service.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nzion.domain.emr.soap.PatientRx;
import com.nzion.hibernate.ext.multitenant.TenantIdHolder;
import com.nzion.service.billing.BillingService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.service.dto.ActivePrescriptionDto;
import com.nzion.service.servlet.dto.MemberCareClinicIncomeDto;
import com.nzion.util.UtilDateTime;

public class MemberCareClinicIncomeServlet extends HttpServlet{
	private static final long serialVersionUID = 1L;

	@Autowired
	private CommonCrudService commonCrudService;
	
	@Autowired
	private BillingService billingService;
	
	public void init(ServletConfig config) throws ServletException{
        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
    }
	
	@Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		String tenantId = request.getParameter("clinicId");
        TenantIdHolder.setTenantId(tenantId);
        
        
        
    }
	
	@Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String tenantId = (String)request.getParameter("clinicId");
        TenantIdHolder.setTenantId(tenantId);
        List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false);
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        MemberCareClinicIncomeDto myObject = objectMapper.readValue(request.getInputStream(), MemberCareClinicIncomeDto.class);
        
        if("Service".equals(myObject.getType())){
        	list = getIncomeAnalysisByServiceType(myObject.getFromDate(),myObject.getThruDate());
        }
        if("Specialty".equals(myObject.getType())){
        	list = getIncomeAnalysisBySpecialty(myObject.getFromDate(),myObject.getThruDate());
        }
        if("Category".equals(myObject.getType())){
        	
        }
        if("Doctor".equals(myObject.getType())){
            list = getIncomeAnalysisByDoctor(myObject.getFromDate(), myObject.getThruDate());
        }
        if("PatientCategory".equals(myObject.getType())){
            list = getIncomeAnalysisByPatientCategory(myObject.getFromDate(), myObject.getThruDate());
        }
        
        Gson gson = new GsonBuilder().serializeNulls().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        request.setCharacterEncoding("utf8");
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(list));

    }
	
	private List<Map<String,Object>> getIncomeAnalysisByServiceType(Date fromDate, Date thruDate){
		return billingService.getIncomeAnalysisByServiceType(fromDate,thruDate);
	}
	
	private List<Map<String,Object>> getIncomeAnalysisBySpecialty(Date fromDate, Date thruDate){
		return billingService.getIncomeAnalysisBySpecialty(fromDate,thruDate);
	}
    private List<Map<String,Object>> getIncomeAnalysisByDoctor(Date fromDate, Date thruDate){
        return billingService.getIncomeAnalysisByDoctor(fromDate,thruDate);
    }
    private List<Map<String,Object>> getIncomeAnalysisByPatientCategory(Date fromDate, Date thruDate){
        return billingService.getIncomeAnalysisByPatientCategory(fromDate,thruDate);
    }


}
