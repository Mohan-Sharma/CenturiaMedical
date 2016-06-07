package com.nzion.repository.notifier.utility;

import com.nzion.domain.Practice;
import com.nzion.domain.Schedule;
import com.nzion.domain.UserLogin;
import com.nzion.service.common.CommonCrudService;
import com.nzion.service.impl.EncryptionService;
import com.nzion.util.AfyaServiceConsumer;
import com.nzion.util.Infrastructure;
import com.nzion.util.RestServiceConsumer;
import com.nzion.view.PatientViewObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.Map;

/**
 * Created by Mohan Sharma on 6/12/2015.
 */
@Component
public class NotificationTask {
    @Autowired
    CommonCrudService commonCrudService;
    @Autowired
    EncryptionService encryptionService;
    public enum STATUS{
        SCHEDULED{
            @Override
            public String toString() {
                return "SCHEDULED";
            }
        }, RESCHEDULED{
            @Override
            public String toString() {
                return "RESCHEDULED";
            }
        }, CANCELLED{
            @Override
            public String toString() {
                return "CANCELLED";
            }
        }, REMINDER{
            @Override
            public String toString() {
                return "REMINDER";
            }
        }, DOCTOR_REGISTRATION{
            @Override
            public String toString() {
                return "REMINDER";
            }
        }
    }

    public void prepareDetailsAndNotifyAppointmentSchedule(Schedule schedule, boolean byEmail, boolean bySMS, Map<String, Object> clinicDetails){
        constructDetailsAndCallNotificationFunction(schedule, STATUS.SCHEDULED, byEmail, bySMS, clinicDetails);
    }


    public void prepareDetailsAndNotifyAppointmentRescheduled(Schedule schedule, boolean byEmail, boolean bySMS, Map<String, Object> clinicDetails){
        constructDetailsAndCallNotificationFunction(schedule, STATUS.RESCHEDULED, byEmail, bySMS,clinicDetails);
    }
    public void prepareDetailsAndNotifyAppointmentCancelled(Schedule schedule, boolean byEmail, boolean bySMS, Map<String, Object> clinicDetails){
        constructDetailsAndCallNotificationFunction(schedule, STATUS.CANCELLED, byEmail, bySMS, clinicDetails);
    }

    public void prepareDetailsAndNotifyAppointmentReminder(Schedule schedule, boolean byEmail, boolean bySMS, Map<String, Object> clinicDetails){
        constructDetailsAndCallNotificationFunction(schedule, STATUS.REMINDER, byEmail, bySMS, clinicDetails);
    }

    private void constructDetailsAndCallNotificationFunction(Schedule schedule, STATUS status, boolean byEmail, boolean bySMS, Map<String, Object> clinicDetails){
        try {
            if(schedule != null && schedule.getPatient() != null && schedule.getPerson() != null && !clinicDetails.isEmpty()) {
                if(status.equals(STATUS.SCHEDULED)){
                    if(byEmail)
                        EmailUtil.sendAppointmentConfirmationMail(schedule, schedule.getPatient(), schedule.getPerson(), clinicDetails);
                    if(bySMS)
                        SmsUtil.sendAppointmentConfirmationSMS(schedule, schedule.getPatient(), schedule.getPerson(), clinicDetails, String.valueOf(clinicDetails.get("tenant_id")));
                }
                if(status.equals(STATUS.RESCHEDULED)) {
                    if (byEmail)
                        EmailUtil.sendAppointmentRescheduledMail(schedule, schedule.getPatient(), schedule.getPerson(), clinicDetails);
                    if (bySMS)
                        SmsUtil.sendAppointmentRescheduledSMS(schedule, schedule.getPatient(), schedule.getPerson(), clinicDetails, String.valueOf(clinicDetails.get("tenant_id")));
                }
                if(status.equals(STATUS.CANCELLED)) {
                    if (byEmail)
                        EmailUtil.sendAppointmentCancelledMail(schedule, schedule.getPatient(), schedule.getPerson(), clinicDetails);
                    if(bySMS)
                        SmsUtil.sendAppointmentCancelledSMS(schedule, schedule.getPatient(), schedule.getPerson(), clinicDetails, String.valueOf(clinicDetails.get("tenant_id")));
                }
                if(status.equals(STATUS.REMINDER)) {
                    if (byEmail)
                        EmailUtil.sendAppointmentReminderMail(schedule, schedule.getPatient(), schedule.getPerson(), clinicDetails);
                    if(bySMS)
                        SmsUtil.sendAppointmentReminderSMS(schedule, schedule.getPatient(), schedule.getPerson(), clinicDetails, String.valueOf(clinicDetails.get("tenant_id")));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
    public void sendRegistrationMailToDoctor(UserLogin user){

        String userPass;

        try{
            userPass = user.getPassword();
            if (userPass != null) {
                user.setPassword(encryptionService.getDecrypted(userPass));
            }

            Map<String, Object> premiumMemberMap = AfyaServiceConsumer.getUserLoginByUserName(Infrastructure.getPractice().getAdminUserLogin().getUsername());
            Map<String, Object> adminMap = AfyaServiceConsumer.getUserLoginByUserName(Infrastructure.getLoggedInPerson().getUserLogin().getUsername());

            EmailUtil.sendRegistrationMailToDoctor(user, adminMap.get("email_id").toString(), premiumMemberMap.get("email_id").toString());
            //sms for user
            SmsUtil.sendRegistrationMail(user.getPerson().getContacts().getOfficePhone(), constructMessageForDoctorRegistartion(user));
            //sms for admin
            /*SmsUtil.sendRegistrationMail(adminMap.get("mobile_number").toString(), constructMessageForDoctorRegistartion(user));
            //sms for premium member
            SmsUtil.sendRegistrationMail(premiumMemberMap.get("mobile_number").toString(), constructMessageForDoctorRegistartion(user));*/


        }catch (IOException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void sendRegistrationMailToPatient(PatientViewObject patientVO){

        try {
            //EmailUtil.sendRegistrationMailToPatient(patientVO);
            SmsUtil.sendRegistrationMail(patientVO.getPatient().getContacts().getMobileNumber(), constructMessageForPatientRegistartion(patientVO));
        /*}catch (IOException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }*/
        }catch(Exception e){
            e.printStackTrace();
        }

    }
    private String constructMessageForDoctorRegistartion(UserLogin user) {
        StringBuilder builder = new StringBuilder();

        builder.append("Welcome to Afya.\n");

        builder.append("login to Afya with User-name " +user.getUsername() +" password "+user.getPassword()+".\n Request to change password once you login.");
        return builder.toString();
    }

    private String constructMessageForPatientRegistartion(PatientViewObject patientVO) {
        StringBuilder builder = new StringBuilder();

        builder.append("Welcome to AfyaArabia.\n");

        builder.append("Please access all Premium Member Care Providers to request care services.\n Download afya app by visiting www.afyaarabia.com ");
        return builder.toString();
    }
}
