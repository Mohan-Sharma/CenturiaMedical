package com.nzion.service.utility;

import com.nzion.domain.Referral;
import com.nzion.util.UtilValidator;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.*;

/**
 * Created by Mohan Sharma on 9/16/2015.
 */
public class UtilityFinder {

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final String QUERY_TO_FETCH_SCHEDULE_DETAILS_BY_ID ="select s.id as scheduleId, s.start_date as appointmentDate, s.start_time as appointmentStartTime, s.end_time as appointmentEndTime,\n" +
            "s.person_id as doctorId, p.id as patientId from schedule s join patient p on s.patient_id = p.id and s.id=:id";

    private final String GET_ALL_RECEIVED_CONTRACT_BY_TENANT_ID ="SELECT REFEREE_CLINIC_ID AS refereeClinicId FROM referral_contract WHERE REFERRAL_CLINIC_ID =:tenantId";

    private final String GET_ALL_SEND_CONTRACT_BY_TENANT_ID ="SELECT REFERRAL_CLINIC_ID AS referralClinicId FROM referral_contract WHERE REFEREE_CLINIC_ID =:tenantId";

    private final String GET_ALL_CLINIC_TARIFF ="SELECT * FROM clinic_tariff";

    @Resource
    public void setDataSource(DataSource dataSource) {
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public Map<String, Object> getScheduleDetailsById(Long id){
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(QUERY_TO_FETCH_SCHEDULE_DETAILS_BY_ID, new MapSqlParameterSource("id", id));
        if(UtilValidator.isNotEmpty(result)){
            return result.get(0);
        }
        return Collections.EMPTY_MAP;
    }

    public List<Referral> getUpdatedReferral(List referrals,String tenantId){
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_ALL_RECEIVED_CONTRACT_BY_TENANT_ID, new MapSqlParameterSource("tenantId", tenantId));

        List tmpList = new ArrayList();
        List resultList = new LinkedList();

        if(UtilValidator.isNotEmpty(result)){
            for (Map map : result){
                tmpList.add(map.get("refereeClinicId"));
            }
        }
        int count = 0;
        for (Object object : referrals){
            Referral referral = (Referral) object;
            if(tmpList.contains(referral.getTenantId())){
                referral.setDisplayName(referral.getClinicName()+" *");
                resultList.add(count, referral);
                count++;
            } else {
                referral.setDisplayName(referral.getClinicName());
                resultList.add(referral);
            }
        }

        if(UtilValidator.isNotEmpty(resultList)){
            return resultList;
        }
        return Collections.EMPTY_LIST;
    }

    public List<Referral> getUpdatedReferralForEncounter(List referrals,String tenantId){
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_ALL_SEND_CONTRACT_BY_TENANT_ID, new MapSqlParameterSource("tenantId", tenantId));

        List tmpList = new ArrayList();
        List resultList = new LinkedList();

        if(UtilValidator.isNotEmpty(result)){
            for (Map map : result){
                tmpList.add(map.get("referralClinicId"));
            }
        }
        int count = 0;
        for (Object object : referrals){
            Referral referral = (Referral) object;
            if(tmpList.contains(referral.getTenantId())){
                referral.setDisplayName(referral.getClinicName()+" *");
                resultList.add(count, referral);
                count++;
            } else {
                referral.setDisplayName(referral.getClinicName());
                resultList.add(referral);
            }
        }

        if(UtilValidator.isNotEmpty(resultList)){
            return resultList;
        }
        return Collections.EMPTY_LIST;
    }

    public List<Map<String, Object>> getAllClinicTariff(){
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_ALL_CLINIC_TARIFF, new MapSqlParameterSource());

        if(UtilValidator.isNotEmpty(result)){
            return result;
        }
        return Collections.EMPTY_LIST;
    }

}
