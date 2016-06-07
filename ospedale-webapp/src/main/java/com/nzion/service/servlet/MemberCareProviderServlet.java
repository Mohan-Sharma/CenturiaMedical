package com.nzion.service.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nzion.hibernate.ext.multitenant.TenantIdHolder;
import com.nzion.service.billing.BillingService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.util.UtilDateTime;


//@WebServlet("/clinicMaster/clinicYesterdayRevenue")
public class MemberCareProviderServlet extends HttpServlet{
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
        Map<String,Object> map = new HashMap<String, Object>();
        String isFullMonth = request.getParameter("isFullMonth");
        Date thruDate = UtilDateTime.getDayStart(new Date());
        Date fromDate = UtilDateTime.getMonthStart(thruDate);
        if("N".equals(isFullMonth)){
        	fromDate = thruDate;
        }

        Map<String, BigDecimal> clinicRevenueResult = billingService.getClinicRevenueByDate(fromDate,thruDate, false);
        Map<String, BigDecimal> smartServicesRevenueResult = billingService.getClinicRevenueByDate(fromDate,thruDate, true);

        BigDecimal clinicRevenue = clinicRevenueResult.get("REVENUE");
        BigDecimal smartServicesRevenue = smartServicesRevenueResult.get("REVENUE");

        BigDecimal totalRevenue = clinicRevenue.add(smartServicesRevenue);
        map.put("clinicRevenue", clinicRevenue);
        map.put("clinicRevenueCollectable", clinicRevenueResult.get("COLLECTABLE"));
        map.put("smartServicesRevenue", smartServicesRevenue);
        map.put("smartServicesRevenueCollectable", smartServicesRevenueResult.get("COLLECTABLE"));
        map.put("totalRevenue", totalRevenue);
        Gson gson = new GsonBuilder().serializeNulls().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        request.setCharacterEncoding("utf8");
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(map));
    }
	
}
