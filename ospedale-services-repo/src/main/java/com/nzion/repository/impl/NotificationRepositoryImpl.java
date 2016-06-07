package com.nzion.repository.impl;

import com.nzion.domain.NotificationSetup;
import com.nzion.repository.NotificationRepository;

import java.util.List;

public class NotificationRepositoryImpl extends HibernateBaseRepository implements NotificationRepository {

    public void saveNotification(NotificationSetup notificationSetup) {
        save(notificationSetup);
    }

    public List<NotificationSetup> getAllNotifications(){
        return getAll(NotificationSetup.class);
    }

    public  NotificationSetup findOneByCriteria(Class<NotificationSetup> persistentClass, String[] fields, Object[] values) {
        return findUniqueByCriteria(persistentClass, fields, values);
    }

    public void mergeEntity(NotificationSetup notificationSetup){
        merge(notificationSetup);
    }
}
