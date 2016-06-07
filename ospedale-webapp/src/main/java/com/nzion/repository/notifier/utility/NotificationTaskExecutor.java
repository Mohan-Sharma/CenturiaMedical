package com.nzion.repository.notifier.utility;

import com.nzion.domain.Schedule;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Mohan Sharma on 6/12/2015.
 */
@Component
public class NotificationTaskExecutor {

    private SchedulerFactoryBean schedulerFactoryBean;
    private NotificationTask notificationTask;

    @Autowired
    public NotificationTaskExecutor(SchedulerFactoryBean schedulerFactoryBean, NotificationTask notificationTask){
        this.schedulerFactoryBean = schedulerFactoryBean;
        this.notificationTask = notificationTask;
    }

    public void prepareDetailsAndNotifyAppointmentSchedule(Schedule schedule, String cronExpression, boolean byEmail, boolean bySMS, Map<String, Object> clinicDetails) throws NoSuchMethodException, ClassNotFoundException, ParseException, SchedulerException {
        if(byEmail || bySMS)
            prepareSchedulingConfiguration(schedule, byEmail, bySMS,cronExpression,"prepareDetailsAndNotifyAppointmentSchedule",NotificationTaskExecutor.getJobName(), NotificationTaskExecutor.getTriggerName(), clinicDetails);
    }

    public void prepareDetailsAndNotifyAppointmentRescheduled(Schedule schedule, String cronExpression, boolean byEmail, boolean bySMS, Map<String, Object> clinicDetails) throws NoSuchMethodException, ClassNotFoundException, ParseException, SchedulerException {
        if(byEmail || bySMS)
            prepareSchedulingConfiguration(schedule, byEmail, bySMS,cronExpression,"prepareDetailsAndNotifyAppointmentRescheduled", NotificationTaskExecutor.getJobName(), NotificationTaskExecutor.getTriggerName(), clinicDetails);
    }

    public void prepareDetailsAndNotifyAppointmentCancelled(Schedule schedule, String cronExpression, boolean byEmail, boolean bySMS, Map<String, Object> clinicDetails) throws NoSuchMethodException, ClassNotFoundException, ParseException, SchedulerException {
        if(byEmail || bySMS)
            prepareSchedulingConfiguration(schedule, byEmail, bySMS,cronExpression,"prepareDetailsAndNotifyAppointmentCancelled", NotificationTaskExecutor.getJobName(), NotificationTaskExecutor.getTriggerName(), clinicDetails);
    }

    public void prepareDetailsAndNotifyAppointmentReminder(Schedule schedule, String cronExpression, boolean byEmail, boolean bySMS, Map<String, Object> clinicDetails) throws NoSuchMethodException, ClassNotFoundException, ParseException, SchedulerException {
        if(byEmail || bySMS)
            prepareSchedulingConfiguration(schedule, byEmail, bySMS,cronExpression,"prepareDetailsAndNotifyAppointmentReminder", NotificationTaskExecutor.getJobName(), NotificationTaskExecutor.getTriggerName(), clinicDetails);
    }

    public static String getJobName(){
        return "EmailAppointmentScheduledJob"+UUID.randomUUID().toString();
    }

    public static String getTriggerName(){
        return "EmailAppointmentScheduledTrigger"+UUID.randomUUID().toString();
    }
    public void prepareSchedulingConfiguration(Schedule schedule, boolean byEmail, boolean bySMS, String cronExpression, String methodToExecute, String jobDetailName, String triggerName, Map<String, Object> clinicDetails) throws NoSuchMethodException, ClassNotFoundException, ParseException, SchedulerException {
        MethodInvokingJobDetailFactoryBean jobDetail = new MethodInvokingJobDetailFactoryBean();
        jobDetail.setTargetObject(notificationTask);
        jobDetail.setTargetMethod(methodToExecute);
        jobDetail.setArguments(new Object[]{schedule, byEmail, bySMS, clinicDetails});
        jobDetail.setName(jobDetailName);
        jobDetail.setConcurrent(false);
        jobDetail.afterPropertiesSet();

        CronTriggerFactoryBean cronTrigger = new CronTriggerFactoryBean();
        cronTrigger.setJobDetail(jobDetail.getObject());
        cronTrigger.setName(triggerName);
        cronTrigger.setCronExpression(cronExpression);
        cronTrigger.afterPropertiesSet();

        schedulerFactoryBean.setTriggers(cronTrigger.getObject());
        schedulerFactoryBean.getScheduler().scheduleJob(jobDetail.getObject(), cronTrigger.getObject());
        schedulerFactoryBean.start();
    }

}
