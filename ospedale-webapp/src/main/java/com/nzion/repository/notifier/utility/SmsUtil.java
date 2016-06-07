package com.nzion.repository.notifier.utility;

import com.nzion.domain.*;
import com.nzion.util.*;
import org.hibernate.classic.Session;
import org.joda.time.LocalDate;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.nzion.repository.notifier.utility.NotificationTask.STATUS;

/**
 * Created by Mohan Sharma on 6/24/2015.
 */
public class SmsUtil {

    static String SMS_SERVER_URL = null;
    static String SMS_SENDER = null;
    static String SMS_UID = null;
    static String SMS_PASSWORD = null;
    static String defaultLanguage = "L";
    static Locale locale = null;
    static {
        Properties properties = new Properties();
        try {
            String profileName = System.getProperty("profile.name") != null ? System.getProperty("profile.name") : "dev";
            properties.load(SmsUtil.class.getClassLoader().getResourceAsStream("application-"+profileName+".properties"));
            SMS_SERVER_URL = (String)properties.get("SMS_SERVER_URL");
            SMS_SENDER = (String)properties.get("SMS_SENDER");
            SMS_UID = (String)properties.get("SMS_UID");
            SMS_PASSWORD = (String)properties.get("SMS_PASSWORD");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendAppointmentConfirmationSMS(Schedule schedule, Patient patient, Person person, Map<String, Object> clinicDetails, String tenantId) {
        //sendSMS(schedule, clinicDetails, STATUS.SCHEDULED, tenantId);
        clinicDetails.put("clinicName",clinicDetails.get("clinic_name"));
        clinicDetails.put("key","CONSULT_VISIT_APPOINTMENT_CONFIRMATION_SMS");
        sendStatusSms(schedule, clinicDetails);
    }

    public static void sendAppointmentRescheduledSMS(Schedule schedule, Patient patient, Person person, Map<String, Object> clinicDetails, String tenantId) {
        //sendSMS(schedule, clinicDetails, STATUS.RESCHEDULED, tenantId);
        /*clinicDetails.put("key","CONSULT_VISIT_APPOINTMENT_RESCHEDULE_SMS");
        sendStatusSms(schedule, clinicDetails);*/
    }

    public static void sendAppointmentCancelledSMS(Schedule schedule, Patient patient, Person person, Map<String, Object> clinicDetails, String tenantId) {
        //sendSMS(schedule, clinicDetails, STATUS.CANCELLED, tenantId);
        clinicDetails.put("key","CONSULT_VISIT_APPOINTMENT_CANCEL_SMS");
        sendStatusSms(schedule, clinicDetails);
    }

    public static void sendAppointmentReminderSMS(Schedule schedule, Patient patient, Person person, Map<String, Object> clinicDetails, String tenantId) {
        //sendSMS(schedule, clinicDetails, STATUS.REMINDER, tenantId);
        clinicDetails.put("key","CONSULT_VISIT_APPOINTMENT_REMAINDER_SMS");
        sendStatusSms(schedule, clinicDetails);
    }

    private static void sendSMS(Schedule schedule, Map<String, Object> clinicDetails, STATUS status, String tenantId){
        try {
            String message= null;
            String senderName = getSenderNameForGivenTenant(tenantId);
            RestTemplate restTemplate = new RestTemplate(RestServiceConsumer.getHttpComponentsClientHttpRequestFactory());
            HttpHeaders headers = getHttpHeader();
            HttpEntity<String> requestEntity = new HttpEntity<String>(headers);
            String phoneNumber = constructPhoneNumber(schedule);
            if(phoneNumber == null || !phoneNumber.matches("\\d+"))
                return;
            if(status.equals(STATUS.SCHEDULED))
                message = constructMessageForAppointmentScheduled(schedule, clinicDetails);
            if(status.equals(STATUS.RESCHEDULED))
                message = constructMessageForAppointmentReScheduled(schedule, clinicDetails);
            if(status.equals(STATUS.CANCELLED))
                message = constructMessageForAppointmentCancelled(schedule, clinicDetails);
            if(status.equals(STATUS.REMINDER))
                message = constructMessageForAppointmentReminder(schedule, clinicDetails);
            if(checkIfSmsAvailableForTenant(tenantId)) {
                ResponseEntity<String> responseEntity = restTemplate.exchange(SMS_SERVER_URL, HttpMethod.POST, requestEntity, String.class, SMS_UID, SMS_PASSWORD, senderName, "L", phoneNumber, message);
                if(responseEntity.getStatusCode().equals(HttpStatus.OK)){
                    RestServiceConsumer.updateSMSCountForGivenTenant(tenantId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getSenderNameForGivenTenant(String tenantId) {
        Map<String, Object> senderNameMap = RestServiceConsumer.getSMSSenderNameForGivenTenant(tenantId);
        if(UtilValidator.isNotEmpty(senderNameMap) && (Boolean) senderNameMap.get("sms_sender_name_verified")) {
            return senderNameMap.get("sms_sender_name").toString();
        }
        else {
            return SMS_SENDER;
        }
    }

    private static boolean checkIfSmsAvailableForTenant(String tenantId){
        return RestServiceConsumer.checkIfSmsAvailableForTenant(tenantId);
    }

    private static HttpHeaders getHttpHeader(){
        HttpHeaders headers = new HttpHeaders();
        List<MediaType> mediaTypes = new ArrayList<>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        headers.setAccept(mediaTypes);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private static String constructMessageForAppointmentScheduled(Schedule schedule, Map<String, Object> clinicDetails) {
        StringBuilder builder = new StringBuilder();
        builder.append("Hello "+constructName(schedule.getPatient())+",\n");
        builder.append(" Your appointment on " + constructDate(schedule.getStartTime(), schedule.getStartDate()) + " at " + constructTime(schedule.getStartTime(), schedule.getStartDate()) + " with " + constructName(schedule.getPerson()) + " has been confirmed as per your request.\n");
        builder.append(" Thanks\n");
        builder.append(clinicDetails.get("first_name")+" "+clinicDetails.get("last_name")+"\n");
        builder.append(getClinicName(clinicDetails));
        return builder.toString();
    }

    private static String constructMessageForAppointmentReScheduled(Schedule schedule, Map<String, Object> clinicDetails) {
        StringBuilder builder = new StringBuilder();
        builder.append("Hello "+constructName(schedule.getPatient())+",\n");
        builder.append(" Your appointment with "+constructName(schedule.getPerson())+" has been rescheduled on "+constructDate(schedule.getStartTime(), schedule.getStartDate())+" at "+ constructTime(schedule.getStartTime(), schedule.getStartDate())+".\n");
        builder.append(" Thanks\n");
        builder.append(clinicDetails.get("first_name")+" "+clinicDetails.get("last_name")+"\n");
        builder.append(getClinicName(clinicDetails));
        return builder.toString();
    }

    private static String constructMessageForAppointmentCancelled(Schedule schedule, Map<String, Object> clinicDetails) {
        StringBuilder builder = new StringBuilder();
        builder.append("Hello "+constructName(schedule.getPatient())+",\n");
        builder.append(" Your appointment on "+constructDate(schedule.getStartTime(), schedule.getStartDate())+" at "+ constructTime(schedule.getStartTime(), schedule.getStartDate())+" with " +constructName(schedule.getPerson())+" has been cancelled("+constructCancelReason(schedule)+").\n");
        builder.append(" Thanks\n");
        builder.append(clinicDetails.get("first_name")+" "+clinicDetails.get("last_name")+"\n");
        builder.append(getClinicName(clinicDetails));
        return builder.toString();
    }

    private static String constructCancelReason(Schedule schedule) {
        if(schedule.getCancelReason() == null || schedule.getCancelReason().equals(""))
            return "No Reason Specified";
        else
            return schedule.getCancelReason();
    }

    private static String constructMessageForAppointmentReminder(Schedule schedule, Map<String, Object> clinicDetails) {
        StringBuilder builder = new StringBuilder();
        builder.append("Hello " + constructName(schedule.getPatient())+",\n");
        builder.append(" This is a reminder SMS for your scheduled appointment with "+constructName(schedule.getPerson())+" on "+constructDate(schedule.getStartTime(), schedule.getStartDate())+" at "+ constructTime(schedule.getStartTime(), schedule.getStartDate())+".\n");
        builder.append(" Thanks\n");
        builder.append(getClinicName(clinicDetails));
        return builder.toString();
    }

    private static String constructPhoneNumber(Schedule schedule) {
        return schedule.getPatient() != null ? schedule.getPatient().getContacts() != null ? schedule.getPatient().getContacts().getIsdCode()+schedule.getPatient().getContacts().getMobileNumber() : null : null;
    }

    private static String getClinicName(Map<String, Object> clinicDetails){
        String clinicName = "";
        if(!clinicDetails.isEmpty()) {
            clinicName = (String)clinicDetails.get("clinic_name");
        }
        return clinicName;
    }

    public static String constructName(Object object){
        StringBuilder stringBuilder = new StringBuilder();
        String name=null;
        if(object instanceof Patient) {
            Patient patient = (Patient)object;
            if(patient.getSalutation() != null)
                stringBuilder.append(patient.getSalutation()+". ");
            if(patient.getFirstName() != null)
                stringBuilder.append(patient.getFirstName()+" ");
            if(patient.getLastName() != null)
                stringBuilder.append(patient.getLastName());
            name = stringBuilder.toString();
        }

        if(object instanceof Provider) {
            Provider provider = (Provider)object;
            if(provider.getSalutation() != null)
                stringBuilder.append(provider.getSalutation()+". ");
            if(provider.getFirstName() != null)
                stringBuilder.append(provider.getFirstName()+" ");
            if(provider.getLastName() != null)
                stringBuilder.append(provider.getLastName());
            name =  stringBuilder.toString();
        }
        return name;
    }

    private static String constructTime(Date date, Date scheduleDate){
        LocalDate localDate = new LocalDate(scheduleDate);
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTime(date);
        calendar.set(Calendar.YEAR, localDate.getYear());
        calendar.set(Calendar.MONTH, localDate.getMonthOfYear());
        calendar.set(Calendar.DAY_OF_MONTH, localDate.getDayOfMonth());
        Date furnishedDate = calendar.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a");
        return dateFormat.format(furnishedDate);
    }

    private static String constructDate(Date date, Date scheduleDate){
        LocalDate localDate = new LocalDate(scheduleDate);
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTime(date);
        calendar.set(Calendar.YEAR, localDate.getYear());
        calendar.set(Calendar.MONTH, localDate.getMonthOfYear()-1);
        calendar.set(Calendar.DAY_OF_MONTH, localDate.getDayOfMonth());
        Date furnishedDate = calendar.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE dd,MMMM yyyy");
        return dateFormat.format(furnishedDate);
    }

    public static void sendRegistrationMail(String phoneNumber,String message) {//UserLogin user

        String  isdCode = "965";
        if ((phoneNumber != null) && (phoneNumber != "")){
            phoneNumber = isdCode + phoneNumber;
        }
        RestTemplate restTemplate = new RestTemplate(RestServiceConsumer.getHttpComponentsClientHttpRequestFactory());
        HttpHeaders headers = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<String>(headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(SMS_SERVER_URL, HttpMethod.POST, requestEntity, String.class, SMS_UID, SMS_PASSWORD, SMS_SENDER, "L", phoneNumber, message);
    }

    public static void sendStatusSms(Schedule schedule, Map<String, Object> clinicDetails){

        if(((clinicDetails.get("forDoctor") == null) || ((Boolean)clinicDetails.get("forDoctor")).equals(false)) && ((clinicDetails.get("forAdmin") == null) || ((Boolean)clinicDetails.get("forAdmin")).equals(false))){
            String preferredLanguage = schedule.getPatient().getLanguage() != null ? schedule.getPatient().getLanguage().getEnumCode() : null;
            if(preferredLanguage != null) {
                clinicDetails.put("languagePreference", preferredLanguage);
            }
        }

        locale = LocaleContextHolder.getLocale();
        if(clinicDetails.get("languagePreference") != null){
            locale = new Locale((String)clinicDetails.get("languagePreference"));
        }

        String language = defaultLanguage;

        clinicDetails.put("patientName", schedule.getPatient().getFirstName()+" "+schedule.getPatient().getLastName());
        String drSalutation = schedule.getPerson().getSalutation() != null ? schedule.getPerson().getSalutation()+". " : "";
        String drMiddleName = schedule.getPerson().getMiddleName() != null ? " "+schedule.getPerson().getMiddleName() : "";
        String doctorNameWithSalutation = drSalutation + schedule.getPerson().getFirstName() + drMiddleName+" "+schedule.getPerson().getLastName();
        clinicDetails.put("doctorNameWithSalutation",doctorNameWithSalutation);
        clinicDetails.put("doctorName", schedule.getPerson().getFirstName()+" "+schedule.getPerson().getLastName());
        clinicDetails.put("date", constructDate(schedule.getStartTime(), schedule.getStartDate()));
        clinicDetails.put("time", constructTime(schedule.getStartTime(), schedule.getStartDate()));
        String patientMobNumber = schedule.getPatient().getContacts().getMobileNumber() != null ? "("+ schedule.getPatient().getContacts().getMobileNumber() +")" : "";
        clinicDetails.put("patientMobNumber", patientMobNumber);

        List<String> mobileList = new ArrayList<>();

        ResponseEntity<String> responseEntity = null;
        String message= null;
        try {

            String tenantId = clinicDetails.get("tenant_id") != null ? clinicDetails.get("tenant_id").toString() : null;
            if (tenantId == null){
                tenantId = getTenantId();
                if (tenantId == null){
                    return;
                }
            }

            String senderName = getSenderNameForGivenTenant(tenantId);

            RestTemplate restTemplate = new RestTemplate(RestServiceConsumer.getHttpComponentsClientHttpRequestFactory());
            HttpHeaders headers = getHttpHeader();
            HttpEntity<String> requestEntity = new HttpEntity<String>(headers);
            String phoneNumber = "";
            if((clinicDetails.get("forDoctor") != null) && ((Boolean)clinicDetails.get("forDoctor")).equals(true)){
                phoneNumber = constructPhoneNumberForDoctor(schedule);
                if(phoneNumber == null || !phoneNumber.matches("\\d+"))
                    return;
            }else if((clinicDetails.get("forAdmin") != null) && ((Boolean)clinicDetails.get("forAdmin")).equals(true)){
                String isdCode = clinicDetails.get("isdCode") != null ? clinicDetails.get("isdCode").toString() : "965";
                String mobileNumber = clinicDetails.get("mobileNumber") != null ? clinicDetails.get("mobileNumber").toString() : null;
                phoneNumber = mobileNumber != null ? isdCode+mobileNumber : null;
                if(phoneNumber == null || !phoneNumber.matches("\\d+"))
                    return;
            } else {
                if ((schedule.getPatient().getNotificationRequired() == null) || (schedule.getPatient().getNotificationRequired().equals("NO"))){
                    return;
                }
                //List<Map<String, Object>> mapList = RestServiceConsumer.getPatientContactsFromAfyaId(schedule.getPatient().getAfyaId());
                List<Map<String, Object>> mapList = RestServiceConsumer.getPatientContactsFromAfyaIdAndType(schedule.getPatient().getAfyaId(), null);
                Iterator iterator = mapList.iterator();
                while (iterator.hasNext()){
                    Map<String, Object> map = (Map)iterator.next();
                    if ((map.get("contactType").equals("MOBILE")) && (map.get("contactValue") != null)){
                        mobileList.add((String)map.get("contactValue"));
                    }
                }

               /* phoneNumber = constructPhoneNumber(schedule);
                if(phoneNumber == null || !phoneNumber.matches("\\d+"))
                    return;*/

            }
            if (locale.getDisplayLanguage().equals("Arabic")) {
                language = "A";
            }

            //message = constructOTPMessage(detail);
            //The above methos was commented out and replaced by the below method to make use of
            // ResourceBundle created by Raghu
            message = constructMessage(schedule, clinicDetails);
            if(message != null) {
                if(((clinicDetails.get("forDoctor") == null) || ((Boolean)clinicDetails.get("forDoctor")).equals(false)) && ((clinicDetails.get("forAdmin") == null) || ((Boolean)clinicDetails.get("forAdmin")).equals(false))){
                    Iterator iterator = mobileList.iterator();
                    while (iterator.hasNext()){
                        String alternateMobile = "965" + iterator.next();
                        if(checkIfSmsAvailableForTenant(tenantId)) {
                            responseEntity = restTemplate.exchange(SMS_SERVER_URL, HttpMethod.POST, requestEntity, String.class, SMS_UID, SMS_PASSWORD, senderName, language, alternateMobile, message);
                            if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
                                RestServiceConsumer.updateSMSCountForGivenTenant(tenantId);
                            }
                        }
                    }
                } else {
                    if(checkIfSmsAvailableForTenant(tenantId)){
                        responseEntity = restTemplate.exchange(SMS_SERVER_URL, HttpMethod.POST, requestEntity, String.class, SMS_UID, SMS_PASSWORD, senderName, language, phoneNumber, message);
                        if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
                            RestServiceConsumer.updateSMSCountForGivenTenant(tenantId);
                        }
                    }
                }
            }
            } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String constructPhoneNumberForDoctor(Schedule schedule) {
        return schedule.getPerson() != null ? schedule.getPerson().getContacts() != null ? schedule.getPerson().getContacts().getIsdCode()+schedule.getPerson().getContacts().getMobileNumber() : null : null;
    }
    private static String constructMessage(Schedule schedule, Map<String, Object> detail) {
        MessageSource messageSource = Infrastructure.getSpringBean("messageSource");
        locale = LocaleContextHolder.getLocale();
        if(detail.get("languagePreference") != null){
            locale = new Locale((String)detail.get("languagePreference"));
        }

        StringBuilder builder = new StringBuilder();
        String message = null;

        /*if ( detail.get("trial") != null && ((Boolean)detail.get("trial")).equals(Boolean.TRUE)) {
            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, (String) detail.get("expiryDate"));
            message = message.substring(1,message.length()-1);
            //message = messageSource.getMessage((String) detail.get("key"), new Object[]{(String) detail.get("expiryDate")}, locale);
        } else {*/

        // }

        if(detail.get("key").toString().equals("PREMIUM_APPOINTMENT_SUCCESS_PAYMENT_SMS_FOR_PATIENT")){
            Object[] arguments = {detail.get("patientName"), detail.get("collectedAmount")};
            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, arguments);
            message = message.substring(1, message.length() - 1);
        }
        if(detail.get("key").toString().equals("PREMIUM_APPOINTMENT_SUCCESS_PAYMENT_SMS_FOR_DOCTOR")) {
            Object[] arguments = {detail.get("doctorNameWithSalutation"), detail.get("patientName"), detail.get("patientMobNumber"), detail.get("date"), detail.get("time")};

            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, arguments);
            message = message.substring(1,message.length()-1);
        }
        if(detail.get("key").toString().equals("TELECONSULTATION_VISIT_SUCCESS_PAYMENT_SMS_FOR_PATIENT")){
            Object[] arguments = {detail.get("patientName"), detail.get("collectedAmount")};
            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, arguments);
            message = message.substring(1, message.length() - 1);
        }
        if(detail.get("key").toString().equals("TELECONSULTATION_VISIT_SUCCESS_PAYMENT_SMS_FOR_DOCTOR")) {
            Object[] arguments = {detail.get("doctorNameWithSalutation"), detail.get("patientName"), detail.get("patientMobNumber"), detail.get("date"), detail.get("time")};

            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, arguments);
            message = message.substring(1,message.length()-1);
        }
        if(detail.get("key").toString().equals("HOME_VISIT_APPOINTMENT_SUCCESS_PAYMENT_SMS_FOR_PATIENT")){
            Object[] arguments = {detail.get("patientName"), detail.get("collectedAmount")};
            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, arguments);
            message = message.substring(1,message.length()-1);
        }
        if(detail.get("key").toString().equals("HOME_VISIT_APPOINTMENT_SUCCESS_PAYMENT_SMS_FOR_DOCTOR")) {
            Object[] arguments = {detail.get("doctorNameWithSalutation"), detail.get("patientName"), detail.get("patientMobNumber"), detail.get("date"), detail.get("time")};

            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, arguments);
            message = message.substring(1,message.length()-1);
        }
        if(detail.get("key").toString().equals("TELECONSULTATION_VISIT_RESCHEDULE_BY_CLINIC")){
            Object[] arguments = {detail.get("patientName"), detail.get("clinic_name"), detail.get("doctorName"), detail.get("date"), detail.get("time")};

            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, arguments);
            message = message.substring(1, message.length() - 1);

        }
        if(detail.get("key").toString().equals("HOME_VISIT_APPOINTMENT_RESCHEDULE_BY_CLINIC")){
            Object[] arguments = {detail.get("patientName"), detail.get("clinic_name"), detail.get("doctorName"), detail.get("date"), detail.get("time")};

            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, arguments);
            message = message.substring(1, message.length() - 1);
        }
        if(detail.get("key").toString().equals("APPOINTMENT_REQUEST_RESCHEDULE_BY_CLINIC")){
            Object[] arguments = {detail.get("patientName"), detail.get("clinic_name"), detail.get("doctorName"), detail.get("date"), detail.get("time")};

            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, arguments);
            message = message.substring(1, message.length() - 1);
        }
        if(detail.get("key").toString().equals("APPOINTMENT_REQUEST_SCHEDULE_FOR_PATIENT")){
            Object[] arguments = {detail.get("afyaId"), detail.get("patientName"), detail.get("clinic_name"), detail.get("clinic_name")};

            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, arguments);
            message = message.substring(1, message.length() - 1);
        }
        if(detail.get("key").toString().equals("APPOINTMENT_REQUEST_SCHEDULE_FOR_DOCTOR")){
            Object[] arguments = {detail.get("doctorNameWithSalutation"), detail.get("patientName"), detail.get("patientMobNumber"), detail.get("date"), detail.get("time")};

            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, arguments);
            message = message.substring(1, message.length() - 1);
        }
        if(detail.get("key").toString().equals("APPOINTMENT_REQUEST_CONFIRMED_BY_CLINIC")){
            Object[] arguments = {detail.get("patientName"), detail.get("date"), detail.get("time"), detail.get("doctorName"), detail.get("clinic_name"), detail.get("url")};

            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, arguments);
            message = message.substring(1, message.length() - 1);
        }
        if(detail.get("key").toString().equals("APPOINTMENT_REQUEST_SUCCESS_PAYMENT_SMS_FOR_PATIENT")){
            Object[] arguments = {detail.get("patientName"), detail.get("collectedAmount")};
            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, arguments);
            message = message.substring(1, message.length() - 1);
        }
        if(detail.get("key").toString().equals("APPOINTMENT_REQUEST_SUCCESS_PAYMENT_SMS_FOR_DOCTOR")){
            Object[] arguments = {detail.get("doctorNameWithSalutation"), detail.get("patientName"), detail.get("patientMobNumber"), detail.get("date"), detail.get("time")};

            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, arguments);
            message = message.substring(1, message.length() - 1);
        }else if (detail.get("key").toString().equals("APPOINTMENT_REQUEST_CANCELLED_BY_PATIENT_SMS_TO_DOCTOR")) {
            Object[] arguments = {detail.get("doctorNameWithSalutation"), detail.get("patientName"), detail.get("patientMobNumber"), detail.get("date"), detail.get("time")};

            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, arguments);
            message = message.substring(1, message.length() - 1);
        }
        else if (detail.get("key").toString().equals("APPOINTMENT_REQUEST_CANCELLED_BY_PATIENT_SMS_TO_PATIENT")) {
            Object[] arguments = {detail.get("patientName"), detail.get("date"), detail.get("time"), detail.get("patientName"), detail.get("refundAmount")};

            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, arguments);
            message = message.substring(1, message.length() - 1);
        }else if (detail.get("key").toString().equals("APPOINTMENT_REQUEST_RESCHEDULE_BY_PATIENT_SMS_TO_DOCTOR")) {
            Object[] arguments = {detail.get("doctorNameWithSalutation"),detail.get("patientName"), detail.get("patientMobNumber"), detail.get("date"), detail.get("time")};

            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, arguments);
            message = message.substring(1, message.length() - 1);
        }else if (detail.get("key").toString().equals("PREMIUM_APPOINTMENT_RESCHEDULE_BY_PATIENT")) {
            String existingStartDate = constructDate((Date)detail.get("existingStartTime"), (Date)detail.get("existingStartDate"));
            String existingStartTime = constructTime((Date) detail.get("existingStartTime"), (Date) detail.get("existingStartDate"));
            Object[] arguments = {detail.get("doctorNameWithSalutation"), detail.get("patientName"), detail.get("patientMobNumber"), detail.get("date"), detail.get("time"), existingStartDate, existingStartTime};

            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, arguments);
            message = message.substring(1, message.length() - 1);
        }else if (detail.get("key").toString().equals("TELECONSULTATION_VISIT_RESCHEDULE_BY_PATIENT")) {
            String existingStartDate = constructDate((Date)detail.get("existingStartTime"), (Date)detail.get("existingStartDate"));
            String existingStartTime = constructTime((Date) detail.get("existingStartTime"), (Date) detail.get("existingStartDate"));
            Object[] arguments = {detail.get("doctorNameWithSalutation"), detail.get("patientName"), detail.get("patientMobNumber"), detail.get("date"), detail.get("time"), existingStartDate, existingStartTime};

            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, arguments);
            message = message.substring(1, message.length() - 1);
        }else if (detail.get("key").toString().equals("HOME_VISIT_APPOINTMENT_RESCHEDULE_BY_PATIENT")) {
            String existingStartDate = constructDate((Date)detail.get("existingStartTime"), (Date)detail.get("existingStartDate"));
            String existingStartTime = constructTime((Date) detail.get("existingStartTime"), (Date) detail.get("existingStartDate"));
            Object[] arguments = {detail.get("doctorNameWithSalutation"), detail.get("patientName"), detail.get("patientMobNumber"), detail.get("date"), detail.get("time"), existingStartDate, existingStartTime};

            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, arguments);
            message = message.substring(1, message.length() - 1);
        }else if (detail.get("key").toString().equals("HOME_VISIT_REQUEST_CANCELLATION_BY_CLINIC_APPOINTMENT_DET")) {
            Object[] arguments = {detail.get("patientName"), detail.get("date"), detail.get("time"), detail.get("doctorName"), detail.get("clinic_name"), detail.get("refundAmount")};

            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, arguments);
            message = message.substring(1, message.length() - 1);
        }else if (detail.get("key").toString().equals("HOME_VISIT_REQUEST_CANCELLATION_BY_CLINIC_REFUND_DET")) {
            Object[] arguments = {detail.get("patientName"), detail.get("refundAmount"), detail.get("clinic_name"), detail.get("doctorName")};

            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, arguments);
            message = message.substring(1, message.length() - 1);
        }else if (detail.get("key").toString().equals("TELECONSULTATION_VISIT_CANCELLATION_BY_CLINIC")) {
            Object[] arguments = {detail.get("patientName"), detail.get("date"), detail.get("time"), detail.get("doctorName"), detail.get("clinic_name"), detail.get("refundAmount")};

            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, arguments);
            message = message.substring(1, message.length() - 1);
        }else if (detail.get("key").toString().equals("PREMIUM_APPOINTMENT_CANCELLED_BY_CLINIC")) {
            Object[] arguments = {detail.get("patientName"), detail.get("date"), detail.get("time"), detail.get("doctorName"), detail.get("clinic_name"), detail.get("refundAmount")};

            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, arguments);
            message = message.substring(1, message.length() - 1);
        } else if (detail.get("key").toString().equals("CONSULT_VISIT_APPOINTMENT_CONFIRMATION_SMS")) {
            Object[] arguments = {detail.get("patientName"), detail.get("clinic_name"), detail.get("doctorName"), detail.get("date"), detail.get("time")};

            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, arguments);
            message = message.substring(1, message.length() - 1);
        }else if (detail.get("key").toString().equals("CONSULT_VISIT_APPOINTMENT_RESCHEDULE_SMS")) {
            Object[] arguments = {detail.get("patientName"), detail.get("date"), detail.get("time"), detail.get("doctorName"), detail.get("clinic_name"), detail.get("refundAmount")};

            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, arguments);
            message = message.substring(1, message.length() - 1);
        }else if (detail.get("key").toString().equals("CONSULT_VISIT_APPOINTMENT_CANCEL_SMS")) {
            Object[] arguments = {detail.get("patientName"), detail.get("clinic_name"), detail.get("doctorName"), detail.get("date"), detail.get("time")};

            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, arguments);
            message = message.substring(1, message.length() - 1);
        }else if (detail.get("key").toString().equals("CONSULT_VISIT_APPOINTMENT_REMAINDER_SMS")) {
            Object[] arguments = {detail.get("patientName"), detail.get("clinic_name"), detail.get("doctorName"), detail.get("date"), detail.get("time")};

            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, arguments);
            message = message.substring(1, message.length() - 1);
        }else if (detail.get("key").toString().equals("HOME_VISIT_REQUEST_CANCELLATION_BY_CLINIC_SMS_DOCTOR")) {
            Object[] arguments = {detail.get("doctorNameWithSalutation"), detail.get("refundAmount")};

            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, arguments);
            message = message.substring(1, message.length() - 1);
        }else if (detail.get("key").toString().equals("APPOINTMENT_CHECKEDOUT_SMS_PATIENT")) {
            Object[] arguments = {detail.get("patientName")};
            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, arguments);
            message = message.substring(1, message.length() - 1);
        }else if (detail.get("key").toString().equals("WALKIN_PATIENT_APPOINTMENT_SMS_REFERRAL_DOC")) {
            Object[] arguments = {detail.get("patientName"), detail.get("doctorName"), detail.get("clinic_name")};

            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, arguments);
            message = message.substring(1, message.length() - 1);
        }else if (detail.get("key").toString().equals("SOAP_REFERRAL_ENCOUNTER_NOTE")) {
            Object[] arguments = {detail.get("refDoctorName"), detail.get("doctorName"), detail.get("clinic_name"), detail.get("patientName"), detail.get("patientMobNumber")};

            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, arguments);
            message = message.substring(1, message.length() - 1);
        }else if (detail.get("key").toString().equals("SOAP_REFERRAL_ENCOUNTER_NOTE_PATIENT")) {
            Object[] arguments = {detail.get("patientName"), detail.get("doctorName"), detail.get("clinic_name"), detail.get("referralDocName"), detail.get("referralClinicName")};

            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, arguments);
            message = message.substring(1, message.length() - 1);
        }else if (detail.get("key").toString().equals("RECOMMENDATION_ENCOUNTER_NOTE")) {
            Object[] arguments = {detail.get("patientName"), detail.get("doctorName"), detail.get("followupDate")};

            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, arguments);
            message = message.substring(1, message.length() - 1);
        }
        builder.append(message);
        //    builder.append("Thanks for registering with Afyaarabia. OTP for completing your registration process is " + detail.get("token") + ".\nThanks\nCommunity Care\nAfyaarabia");
        return builder.toString();
    }

    public static void sendSmsForReferral(Map<String, Object> details){
        locale = LocaleContextHolder.getLocale();
        if(details.get("languagePreference") != null){
            locale = new Locale((String)details.get("languagePreference"));
        }
        String language = defaultLanguage;
        ResponseEntity<String> responseEntity = null;
        String message= null;
        try {

            String tenantId = details.get("tenant_id") != null ? details.get("tenant_id").toString() : null;
            if (tenantId == null){
                tenantId = getTenantId();
                if (tenantId == null){
                    return;
                }
            }

            String senderName = getSenderNameForGivenTenant(tenantId);

            RestTemplate restTemplate = new RestTemplate(RestServiceConsumer.getHttpComponentsClientHttpRequestFactory());
            HttpHeaders headers = getHttpHeader();
            HttpEntity<String> requestEntity = new HttpEntity<String>(headers);
            String phoneNumber = "";
            phoneNumber = constructPhoneNumberForReferral(details);
            if(phoneNumber == null || !phoneNumber.matches("\\d+"))
                return;
            if (locale.getDisplayLanguage().equals("Arabic")) {
                language = "A";
            }

            message = constructMessageForReferral(details);
            if (tenantId.equals("irfanclinic")){
                System.out.println("*****************************************************************************************");
                System.out.println("*****************************************************************************************");
                System.out.println("*****************************************************************************************");
                System.out.println("message-"+message);
                System.out.println("*****************************************************************************************");
                System.out.println("*****************************************************************************************");
                System.out.println("*****************************************************************************************");
            }
            if((checkIfSmsAvailableForTenant(tenantId)) && (message != null)) {
                responseEntity = restTemplate.exchange(SMS_SERVER_URL, HttpMethod.POST, requestEntity, String.class, SMS_UID, SMS_PASSWORD, SMS_SENDER, language, phoneNumber, message);

                if (tenantId.equals("irfanclinic")){
                    System.out.println("*****************************************************************************************");
                    System.out.println("*****************************************************************************************");
                    System.out.println("*****************************************************************************************");
                    System.out.println("responseEntity-"+responseEntity+" "+"phone-"+phoneNumber);
                    System.out.println("*****************************************************************************************");
                    System.out.println("*****************************************************************************************");
                    System.out.println("*****************************************************************************************");
                }

                if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
                    RestServiceConsumer.updateSMSCountForGivenTenant(tenantId);
                }
            }
            } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String constructPhoneNumberForReferral(Map<String, Object> detail) {
        String  isdCode = "965"; // To Do: This should be sent in detail
        return detail.get("mobile") != null ? (isdCode + detail.get("mobile").toString()) : null;
    }

    private static String constructMessageForReferral(Map<String, Object> detail) {
        MessageSource messageSource = Infrastructure.getSpringBean("messageSource");
        locale = LocaleContextHolder.getLocale();
        if (detail.get("languagePreference") != null) {
            locale = new Locale((String) detail.get("languagePreference"));
        }

        StringBuilder builder = new StringBuilder();
        String message = null;

        if (detail.get("key").toString().equals("REFERRAL_CONTRACT_REJECTED_SMS")) {
            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, (String) detail.get("refereeName"));
            message = message.substring(1,message.length()-1);
        }else if (detail.get("key").toString().equals("REFERRAL_CONTRACT_ACCEPTED_SMS")) {
            Object[] arguments = {detail.get("referralName"), detail.get("referralClinicName")};

            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, arguments);
            message = message.substring(1,message.length()-1);
        }else if (detail.get("key").toString().equals("REFERRAL_CONTRACT_VIEWED_SMS")) {
            Object[] arguments = {detail.get("referralName"), detail.get("referralClinicName")};

            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, arguments);
            message = message.substring(1,message.length()-1);
        }else if (detail.get("key").toString().equals("REFERRAL_CONTRACT_ACCEPTED_SMS_RECEIPIENT")) {
            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, (String) detail.get("refereeName"));
            message = message.substring(1,message.length()-1);
        }else if (detail.get("key").toString().equals("REFERRAL_CONTRACT_SAVED_SMS")) {
            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, (String) detail.get("user"));
            message = message.substring(1,message.length()-1);
        }else if (detail.get("key").toString().equals("NOTIFY_DOCTOR_TO_SET_CALENDAR")) {
            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = message.substring(1,message.length()-1);
        }else if (detail.get("key").toString().equals("NOTIFY_DOCTOR_TO_CONFIRM_APPOINTMENT")) {
            Object[] arguments = {detail.get("doctorNameWithSalutation"), detail.get("patientName"), detail.get("patientMobNumber")};

            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, arguments);
            message = message.substring(1,message.length()-1);

            /*message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, (String) detail.get("patientName"));
            message = message.substring(1,message.length()-1);*/
        }
        builder.append(message);
        //    builder.append("Thanks for registering with Afyaarabia. OTP for completing your registration process is " + detail.get("token") + ".\nThanks\nCommunity Care\nAfyaarabia");
        return builder.toString();
    }
    public static String getTenantId(){
        Session session = null;
        //Boolean newSession = false;
        String tenantId = null;
        try {
             /*session = Infrastructure.getSessionFactory().getCurrentSession();
            if (session == null){*/
                session = Infrastructure.getSessionFactory().openSession();
                //newSession = true;
            //}
            List <Practice> results = session.createQuery("from Practice order by id DESC").list();
            Map<String, Object> details = null;
            if (results.size() > 0) {
                tenantId = ((Practice) results.get(0)).getTenantId();
            }
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            if (session != null){
                session.close();
            }
        }
        return tenantId;
    }
}
