package com.nzion.service.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.nzion.domain.PatientReschedulingPreference;
import com.nzion.domain.RCMPreference;
import com.nzion.domain.Schedule;
import com.nzion.domain.RCMPreference.RCMVisitType;
import com.nzion.domain.UserLogin;
import com.nzion.hibernate.ext.multitenant.TenantIdHolder;
import com.nzion.service.UserLoginService;
import com.nzion.service.dto.ReschedulePatientAppointmentDto;
import com.nzion.util.UtilDateTime;

public class UpdatePassword extends HttpServlet{
	
	@Autowired
	private UserLoginService userLoginService;
	
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
        
        String userName = request.getParameter("userName");
        String password = request.getParameter("password");
        
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        objectMapper.setDateFormat(df);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        
        UserLogin userLogin = userLoginService.getUserByUsername(userName);
        userLoginService.changePassword(userLogin, password);
        
        PrintWriter writer = response.getWriter();
		writer.print( "Success");
		writer.close();
        return;
		}

}
