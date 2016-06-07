package com.nzion.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nzion.domain.Patient;
import com.nzion.domain.Person;
import com.nzion.domain.Provider;
import com.nzion.domain.messaging.Message;
import com.nzion.hibernate.ext.multitenant.TenantIdHolder;
import com.nzion.service.PatientService;
import com.nzion.service.PersonService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.view.RolesValueObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.annotation.Resource;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;

/**
 * Created by Nthdimenzion on 4/23/2015.
 */
public class RequestAppointmentServlet extends HttpServlet {

    @Autowired
    private PatientService patientService;

    public void init(ServletConfig config) throws ServletException{
        super.init(config);
        System.out.println(" ==============  RequestAppointmentServlet Bean Created  ================= \n");
        System.out.println(" ==============  servlet context  ================= \n" + config.getServletContext());
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        System.out.println(" ==============  Inside Request AppointmentServlet  ================= \n");
        String tenantId = (String)request.getParameter("clinicId");
        TenantIdHolder.setTenantId(tenantId);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false);
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        /*ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));*/

        ServletInputStream json = request.getInputStream();
        System.out.println(" ==============  Data  ================= \n" + request.getInputStream());
        RequestAppointmentDto requestAppointmentDto = objectMapper.readValue(request.getInputStream(), RequestAppointmentDto.class);

        patientService.createAppointmentReqFromMobile(requestAppointmentDto.isUrgent(),requestAppointmentDto.getSentOn(),requestAppointmentDto.getPatientId(),
                requestAppointmentDto.getDoctorId(),requestAppointmentDto.getPatientFirstName(),requestAppointmentDto.getPatientLastName(),
                requestAppointmentDto.getPatientContactNo(),requestAppointmentDto.getPatientEmail(),requestAppointmentDto.getMessageText(), requestAppointmentDto.getVisitType());

        //HttpSession session = request.getSession();
        //session.setAttribute("Type", "success");

        System.out.println(" ==============  End  ================= \n" + request.getInputStream());
       // doGet(request, response);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false);
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        RequestAppointmentDto requestAppointmentDto = new RequestAppointmentDto();
        requestAppointmentDto.setDoctorId("1");
        requestAppointmentDto.setPatientFirstName("FirstName");
        Gson gson = new GsonBuilder().serializeNulls().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        request.setCharacterEncoding("utf8");
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(requestAppointmentDto));
    }


}
