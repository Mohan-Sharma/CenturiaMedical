package com.nzion.util;

import com.nzion.domain.*;
import com.nzion.hibernate.ext.multitenant.TenantIdHolder;
import com.nzion.repository.notifier.NotifierDao;
import com.nzion.repository.notifier.dto.NotifierDto;
import com.nzion.repository.notifier.utility.EmailUtil;
import com.nzion.repository.notifier.utility.SmsUtil;
import com.nzion.repository.notifier.utility.TemplateNames;
import com.nzion.service.common.CommonCrudService;
import org.hibernate.classic.Session;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

/**
 * Created by Mohan Sharma on 5/16/2015.
 */
@Configuration
@EnableScheduling
public class FollowupNotifier {

    @Resource
    private NotifierDao notifierDao;
    @Resource
    CommonCrudService commonCrudService;

    //@Scheduled(cron = "30 14 * * * ?")
    public void notifyPatientFollowup() throws IOException, MessagingException {
        //System.out.println("\n\nNotifying\n\n ");
        List<Map<String, Object>> patientsWithFollowup = notifierDao.getAllPatientsFollowupToBeNotified();
        List<NotifierDto> notifierDtos = NotifierDto.convertToNofierDto(patientsWithFollowup);
        List<NotifierDto> notifierDtoList = checkIfNeedsToBeNotified(notifierDtos);
        //System.out.println("\n\nnotifierDtoList\n\n "+notifierDtoList);
        emailPatientForFollowup(notifierDtoList);
    }

    private void emailPatientForFollowup(List<NotifierDto> notifierDtoList) throws IOException, MessagingException {
        for(NotifierDto notifierDto :  notifierDtoList){
            if(notifierDto.isNotificationNeeded()){
                //System.out.println("\n\nnotifierDto.isNotificationNeeded()\n\n "+notifierDto.isNotificationNeeded());
                Patient patient = commonCrudService.findUniqueByEquality(Patient.class, new String[]{"id"}, new Object[]{Long.valueOf(notifierDto.getPatientId())});
                Provider provider = commonCrudService.findUniqueByEquality(Provider.class, new String[]{"id"}, new Object[]{Long.valueOf(notifierDto.getProviderId())});
                if(patient == null || provider== null)
                    continue;
                else{
                    String email = patient.getContacts() != null ? patient.getContacts().getEmail() : null;
                    if(email == null || email.equals(""))
                        continue;
                }
                EmailUtil.sendNotificationEmailForFollowup(patient, provider);
            }
        }
    }

    private List<NotifierDto> checkIfNeedsToBeNotified(List<NotifierDto> notifierDtos) {
        for(NotifierDto notifierDto : notifierDtos){
            Boolean result = notifierDao.checkIfPatientBookedAppointment(notifierDto.getProviderId(), notifierDto.getPatientId());
            notifierDto.setNotificationNeeded(result);
        }
        return notifierDtos;
    }

    //@Scheduled(cron = "0 0 7 * * ?")
    public void notifyUserToSetCalendar() throws IOException, MessagingException {
        System.out.println("***********************   notifyUserToSetCalendar  ***************************");
        //Session session = null;
        try {
            List<Map<String,Object>> tenantFromPortal = AfyaServiceConsumer.getAllTenantFromPortal();

            Iterator tenantIterator = tenantFromPortal.iterator();
            while (tenantIterator.hasNext()) {
                Map<String, Object> tenantMap = (Map) tenantIterator.next();
                String tenant_Id = (String)tenantMap.get("tenantId");
                String adminUserLogin = (String)tenantMap.get("adminUsername");
                String languagePreference = (String)tenantMap.get("languagePreference");

                TenantIdHolder.setTenantId(tenant_Id);

                try {
                    List<Provider> providerList = commonCrudService.getAll(Provider.class);
                    Iterator providerIterator = providerList.iterator();
                    List mobileNoList = new ArrayList();
                    while (providerIterator.hasNext()) {
                        Provider provider = (Provider) providerIterator.next();
                        if ((provider.getContacts().getMobileNumber() == null) || (provider.getContacts().getMobileNumber().equals(""))) {
                            continue;
                        }
                        int count = 0;
                        Map<Date, Boolean> map = new LinkedHashMap<Date, Boolean>();
                        for (int i = 1; i <= 7; i++) {
                            Date date = UtilDateTime.addDaysToDate(new Date(), i);
                            map.put(date, false);
                        }
                        List<CalendarResourceAssoc> calendarResourceAssocList = commonCrudService.findByEquality(CalendarResourceAssoc.class, new String[]{"person"}, new Object[]{provider});
                        if (UtilValidator.isNotEmpty(calendarResourceAssocList)) {
                            Iterator calendarIterator = calendarResourceAssocList.iterator();
                            while (calendarIterator.hasNext()) {
                                CalendarResourceAssoc calendarResourceAssoc = (CalendarResourceAssoc) calendarIterator.next();
                                if ((calendarResourceAssoc.getThruDate() == null) || (calendarResourceAssoc.getFromDate() == null)) {
                                    continue;
                                }

                                Set set = map.keySet();
                                for (Object o : set) {
                                    Date date = (Date) o;
                                    if (map.get(date).equals(false)) {
                                        if (((date.after(calendarResourceAssoc.getFromDate())) || (date.equals(calendarResourceAssoc.getFromDate()))) && ((date.before(calendarResourceAssoc.getThruDate())) || (date.equals(calendarResourceAssoc.getThruDate())))) {
                                            map.put(date, true);
                                        }
                                    }
                                }

                            }
                        }
                        for (Boolean b : map.values()) {
                            if (b.equals(false)) {
                                mobileNoList.add(provider.getContacts().getMobileNumber());
                                break;
                            }
                        }
                    }
                    Iterator iterator = mobileNoList.iterator();
                    //Map<String, Object> details = AfyaServiceConsumer.getUserLoginByUserName(adminUserLogin);
                    Map<String, Object> details = new HashMap<String, Object>();
                    details.put("tenant_id", tenant_Id);
                    details.put("languagePreference", languagePreference);
                    //}
                    details.put("key", TemplateNames.NOTIFY_DOCTOR_TO_SET_CALENDAR.name());
                    while (iterator.hasNext()) {
                        details.put("mobile", (String) iterator.next());
                        SmsUtil.sendSmsForReferral(details);
                    }
                }catch (Exception e){e.printStackTrace();}
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //@Scheduled(cron = "0 30 7 * * ?")
    public void notifyDoctorToConfirm() throws IOException, MessagingException {
        System.out.println("***************************   notifyDoctorToConfirm   **************************");
        try {
            //session = Infrastructure.getSessionFactory().openSession();
            List<Map<String,Object>> tenantFromPortal = AfyaServiceConsumer.getAllTenantFromPortal();

            Iterator tenantIterator = tenantFromPortal.iterator();
            while (tenantIterator.hasNext()) {
                Map<String, Object> tenantMap = (Map) tenantIterator.next();
                final String tenant_Id = (String)tenantMap.get("tenantId");
                String adminUserLogin = (String)tenantMap.get("adminUsername");
                final String languagePreference = (String)tenantMap.get("languagePreference");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            TenantIdHolder.setTenantId(tenant_Id);
                        /*}catch (Exception e){
                            e.printStackTrace();
                        }*/
                    /*}
                }).start();*/

                //TenantIdHolder.setTenantId(tenant_Id);

            //try {
                List<Schedule> scheduleList = commonCrudService.findByEquality(Schedule.class, new String[]{"tentativeStatus"}, new Object[]{"Tentative"});
                if (tenant_Id.equals("irfanclinic")){
                    System.out.println("*****************************************************************************************");
                    System.out.println("*****************************************************************************************");
                    System.out.println("*****************************************************************************************");
                    System.out.println("scheduleList-"+scheduleList.size());
                    System.out.println("*****************************************************************************************");
                    System.out.println("*****************************************************************************************");
                    System.out.println("*****************************************************************************************");
                }

                //Map<String, Object> details = AfyaServiceConsumer.getUserLoginByUserName(adminUserLogin);
                Map<String, Object> details = new HashMap<String, Object>();
                details.put("tenant_id", tenant_Id);
                details.put("key", TemplateNames.NOTIFY_DOCTOR_TO_CONFIRM_APPOINTMENT.name());
                details.put("languagePreference", languagePreference);

                Iterator scheduleIterator = scheduleList.iterator();
                if (scheduleIterator != null) {
                    while (scheduleIterator.hasNext()) {
                        Schedule schedule = (Schedule) scheduleIterator.next();
                        Date cretedDate = schedule.getCreatedTxTimestamp();

                        if (tenant_Id.equals("irfanclinic")){
                            System.out.println("*****************************************************************************************");
                            System.out.println("*****************************************************************************************");
                            System.out.println("*****************************************************************************************");
                            System.out.println("cretedDate-"+cretedDate+" "+schedule.getId());
                            System.out.println("*****************************************************************************************");
                            System.out.println("*****************************************************************************************");
                            System.out.println("*****************************************************************************************");
                        }

                        if (UtilDateTime.getIntervalInHours(cretedDate, new Date()) > 24) {
                            if (UtilDateTime.getDateOnly(schedule.getStartDate()).before(UtilDateTime.getDateOnly(new Date()))){
                                continue;
                            }
                            if (schedule.getPerson().getContacts().getMobileNumber() == null) {
                                continue;
                            }
                            details.put("mobile", schedule.getPerson().getContacts().getMobileNumber());
                            details.put("patientName", schedule.getPatient().getFirstName() + " " + schedule.getPatient().getLastName());
                            details.put("patientMobNumber", schedule.getPatient().getContacts().getMobileNumber() != null ? "("+ schedule.getPatient().getContacts().getMobileNumber() +")" : "");

                            String drSalutation = schedule.getPerson().getSalutation() != null ? schedule.getPerson().getSalutation()+". " : "";
                            String drMiddleName = schedule.getPerson().getMiddleName() != null ? " "+schedule.getPerson().getMiddleName() : "";
                            String doctorNameWithSalutation = drSalutation + schedule.getPerson().getFirstName() + drMiddleName+" "+schedule.getPerson().getLastName();

                            details.put("doctorNameWithSalutation",doctorNameWithSalutation);
                            if (tenant_Id.equals("irfanclinic")){
                                System.out.println("*****************************************************************************************");
                                System.out.println("*****************************************************************************************");
                                System.out.println("*****************************************************************************************");
                                System.out.println("details-"+details);
                                System.out.println("*****************************************************************************************");
                                System.out.println("*****************************************************************************************");
                                System.out.println("*****************************************************************************************");
                            }
                            SmsUtil.sendSmsForReferral(details);
                        }
                        //System.out.println(cretedDate);

                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }}
                }).start();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
