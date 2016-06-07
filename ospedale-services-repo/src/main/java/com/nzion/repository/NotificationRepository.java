package com.nzion.repository;

import com.nzion.domain.NotificationSetup;

import java.util.List;

/**
 * Created by Mohan Sharma on 6/3/2015.
 */
public interface NotificationRepository{

    void saveNotification(NotificationSetup notificationSetup);

    void mergeEntity(NotificationSetup notificationSetup);

    List<NotificationSetup> getAllNotifications();

    NotificationSetup findOneByCriteria(Class<NotificationSetup> persistentClass, String[] fields, Object[] values);
}
