package com.nzion.view.practice;

import com.nzion.domain.NotificationSetup;
import com.nzion.domain.Practice;
import com.nzion.dto.NotificationSettingDto;
import com.nzion.repository.notifier.utility.SmsUtil;
import com.nzion.service.NotificationService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.util.Infrastructure;
import com.nzion.util.RestServiceConsumer;
import com.nzion.util.UtilMessagesAndPopups;
import com.nzion.util.UtilValidator;
import org.zkoss.bind.annotation.*;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Mohan Sharma on 6/3/2015.
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class NotificationSettingVM {

    private boolean showNotificationDiv = Boolean.FALSE;
    private String notificationRadioSelectedItem;
    private NotificationSettingDto appointmentGiven = new NotificationSettingDto();
    private NotificationSettingDto appointmentRescheduled = new NotificationSettingDto();
    private NotificationSettingDto appointmentCancelled = new NotificationSettingDto();
    private NotificationSettingDto appointmentFirstReminder = new NotificationSettingDto();
    private NotificationSettingDto appointmentSecondReminder = new NotificationSettingDto();
    private String SMS_SENDER;
    @WireVariable
    private NotificationService notificationService;
    @Init
    public void init(){
        Properties properties = new Properties();
        try {
            String profileName = System.getProperty("profile.name") != null ? System.getProperty("profile.name") : "dev";
            properties.load(SmsUtil.class.getClassLoader().getResourceAsStream("application-"+profileName+".properties"));
            SMS_SENDER = "Your Notifications will be sent from the Sender ID : " + (String)properties.get("SMS_SENDER");
        } catch (IOException e) {
            e.printStackTrace();
        }
        getSenderNameFromTenant();
    }

    @AfterCompose
    public void afterCompose(@ContextParam(ContextType.VIEW) Component view){
        Selectors.wireComponents(view, this, false);
        populateNotification();
    }

    private void populateNotification() {
        List<NotificationSetup> notificationSetupList = notificationService.getAllNotifications();
        for(NotificationSetup notificationSetup : notificationSetupList){
            if(notificationSetup.getStatus().equals(NotificationSetup.STATUS.SCHEDULED))
                appointmentGiven.setPropertiesToNotificationDto(notificationSetup);
            if(notificationSetup.getStatus().equals(NotificationSetup.STATUS.RESCHEDULED))
                appointmentRescheduled.setPropertiesToNotificationDto(notificationSetup);
            if(notificationSetup.getStatus().equals(NotificationSetup.STATUS.CANCELLED))
                appointmentCancelled.setPropertiesToNotificationDto(notificationSetup);
            if(notificationSetup.getStatus().equals(NotificationSetup.STATUS.FIRST_REMINDER))
                appointmentFirstReminder.setPropertiesToNotificationDto(notificationSetup);
            if(notificationSetup.getStatus().equals(NotificationSetup.STATUS.SECOND_REMINDER))
                appointmentSecondReminder.setPropertiesToNotificationDto(notificationSetup);
        }
    }

    @Command
    public void saveNotification(){
        NotificationSetup scheduledNotification = getExistingNotificationSet(NotificationSetup.STATUS.SCHEDULED);
        setPropertiesAndSave(appointmentGiven, scheduledNotification);
        NotificationSetup rescheduledNotification = getExistingNotificationSet(NotificationSetup.STATUS.RESCHEDULED);
        setPropertiesAndSave(appointmentRescheduled, rescheduledNotification);
        NotificationSetup cancelledNotification = getExistingNotificationSet(NotificationSetup.STATUS.CANCELLED);
        setPropertiesAndSave(appointmentCancelled, cancelledNotification);
        NotificationSetup firstReminderNotification = getExistingNotificationSet(NotificationSetup.STATUS.FIRST_REMINDER);
        setPropertiesAndSave(appointmentFirstReminder, firstReminderNotification);
        NotificationSetup secondReminderNotification = getExistingNotificationSet(NotificationSetup.STATUS.SECOND_REMINDER);
        setPropertiesAndSave(appointmentSecondReminder, secondReminderNotification);
        UtilMessagesAndPopups.showMessage("operation successful");
    }

    private void setPropertiesAndSave(NotificationSettingDto notificationSettingDto, NotificationSetup notificationSetup) {
        notificationSetup.setPropertiesToEntity(notificationSettingDto);
        notificationService.save(notificationSetup);
    }

    private NotificationSetup getExistingNotificationSet(NotificationSetup.STATUS status){
        NotificationSetup notificationSetup = notificationService.findOneByCriteria(NotificationSetup.class, new String[]{"status"}, new Object[]{status});
        if(notificationSetup == null) {
            notificationSetup = new NotificationSetup();
            notificationSetup.setStatus(status);
        }
        return notificationSetup;
    }

    public void getSenderNameFromTenant(){
        Practice practice = Infrastructure.getPractice();
        if(practice != null){
            Map<String, Object> senderNameMap = RestServiceConsumer.getSMSSenderNameForGivenTenant(practice.getTenantId().toString());
            if(UtilValidator.isNotEmpty(senderNameMap) && (Boolean) senderNameMap.get("sms_sender_name_verified")) {
                SMS_SENDER =  "Your Notifications will be sent from the Sender ID : " + senderNameMap.get("sms_sender_name").toString();
            }
        }
    }

    public boolean isShowNotificationDiv() {
        return showNotificationDiv;
    }

    public void setShowNotificationDiv(boolean showNotificationDiv) {
        this.showNotificationDiv = showNotificationDiv;
    }

    public String getNotificationRadioSelectedItem() {
        return notificationRadioSelectedItem;
    }

    public void setNotificationRadioSelectedItem(String notificationRadioSelectedItem) {
        this.notificationRadioSelectedItem = notificationRadioSelectedItem;
    }

    public NotificationSettingDto getAppointmentGiven() {
        return appointmentGiven;
    }

    public void setAppointmentGiven(NotificationSettingDto appointmentGiven) {
        this.appointmentGiven = appointmentGiven;
    }

    public NotificationSettingDto getAppointmentRescheduled() {
        return appointmentRescheduled;
    }

    public void setAppointmentRescheduled(NotificationSettingDto appointmentRescheduled) {
        this.appointmentRescheduled = appointmentRescheduled;
    }

    public NotificationSettingDto getAppointmentCancelled() {
        return appointmentCancelled;
    }

    public void setAppointmentCancelled(NotificationSettingDto appointmentCancelled) {
        this.appointmentCancelled = appointmentCancelled;
    }

    public NotificationSettingDto getAppointmentFirstReminder() {
        return appointmentFirstReminder;
    }

    public void setAppointmentFirstReminder(NotificationSettingDto appointmentFirstReminder) {
        this.appointmentFirstReminder = appointmentFirstReminder;
    }

    public NotificationSettingDto getAppointmentSecondReminder() {
        return appointmentSecondReminder;
    }

    public void setAppointmentSecondReminder(NotificationSettingDto appointmentSecondReminder) {
        this.appointmentSecondReminder = appointmentSecondReminder;
    }

    public String getSMS_SENDER() {
        return SMS_SENDER;
    }

    public void setSMS_SENDER(String SMS_SENDER) {
        this.SMS_SENDER = SMS_SENDER;
    }
}
