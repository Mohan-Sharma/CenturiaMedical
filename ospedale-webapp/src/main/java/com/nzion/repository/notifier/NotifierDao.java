package com.nzion.repository.notifier;

import java.util.List;
import java.util.Map;

/**
 * Created by Mohan Sharma on 5/18/2015.
 */
public interface NotifierDao {
    List<Map<String, Object>> getAllPatientsFollowupToBeNotified();
    Boolean checkIfPatientBookedAppointment(String providerId, String patientId);
}
