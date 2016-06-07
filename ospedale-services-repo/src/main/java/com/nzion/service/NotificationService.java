package com.nzion.service;

import com.nzion.domain.NotificationSetup;

import java.util.List;

/**
 * Created by Mohan Sharma on 6/3/2015.
 */
public interface NotificationService {
    boolean save(NotificationSetup notificationSetup);

    boolean merge(NotificationSetup notificationSetup);

    List<NotificationSetup> getAllNotifications();

    NotificationSetup findOneByCriteria(Class<NotificationSetup> persistentClass, String[] fileds, Object[] values);
}
