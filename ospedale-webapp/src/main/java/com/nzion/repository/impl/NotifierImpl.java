package com.nzion.repository.impl;

import com.nzion.repository.notifier.NotifierDao;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * Created by Mohan Sharma on 5/18/2015.
 */
@Repository
public class NotifierImpl implements NotifierDao{

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private static final String GET_ALL_PATIENTS_TO_BE_NOTIFIED_FOR_FOLLOWUP= "SELECT psn.provider_id AS providerId, psn.PATIENT_ID AS patientId FROM `soap_section` sc JOIN `patient_soap_note` psn ON psn.id=sc.PATIENT_SOAP_NOTE_ID JOIN patient p \n" +
            "ON p.id = psn.PATIENT_ID JOIN SCHEDULE s ON s.id = psn.schedule_id AND sc.followup_date IN (CURDATE()+INTERVAL 1 DAY) ORDER BY psn.provider_id,psn.PATIENT_ID";

    private static final String CHECK_PATIENT_BOOKED_APPOINTMENT_BY_GIVEN_DETAILS = "SELECT * FROM SCHEDULE WHERE person_id =:providerId AND patient_id =:patientId AND start_date IN (CURDATE()+INTERVAL 1 DAY)";

    @Resource
    public void setDataSource(DataSource dataSource) {
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public List<Map<String, Object>> getAllPatientsFollowupToBeNotified(){
        return namedParameterJdbcTemplate.queryForList(GET_ALL_PATIENTS_TO_BE_NOTIFIED_FOR_FOLLOWUP, new MapSqlParameterSource());
    }

    @Override
    public Boolean checkIfPatientBookedAppointment(String providerId, String patientId) {
        List<Map<String, Object>> result =namedParameterJdbcTemplate.queryForList(CHECK_PATIENT_BOOKED_APPOINTMENT_BY_GIVEN_DETAILS, new MapSqlParameterSource("providerId", providerId).addValue("patientId", patientId));
        if(result.size() > 0)
            return false;
        else
            return true;
    }
}
