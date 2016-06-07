package com.nzion.repository.notifier.dto;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Mohan Sharma on 5/19/2015.
 */
public class NotifierDto {
    private String providerId;
    private String patientId;
    private boolean notificationNeeded;

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public boolean isNotificationNeeded() {
        return notificationNeeded;
    }

    public void setNotificationNeeded(boolean notificationNeeded) {
        this.notificationNeeded = notificationNeeded;
    }

    public static List<NotifierDto> convertToNofierDto(List<Map<String, Object>> patientsWithFollowup) {
        NotifierDto notifierDto = null;
        List<NotifierDto> notifierDtos = new LinkedList<>();
        for(Map<String, Object> map : patientsWithFollowup){
            notifierDto = new NotifierDto();
            notifierDto.setProviderId(map.get("providerId") != null ? map.get("providerId").toString() : null);
            notifierDto.setPatientId(map.get("patientId") != null ? map.get("patientId").toString() : null);
            notifierDto.setNotificationNeeded(true);
            notifierDtos.add(notifierDto);
        }
        return notifierDtos;
    }
}
