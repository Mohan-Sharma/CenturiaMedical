package com.nzion.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Mohan Sharma on 4/2/2015.
 */
@RestController
public class ClinicalRestController {

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final String FETCH_ALL_SERVICES = "SELECT smg.`CODE` AS serviceMainGroupCode, smg.`SERVICE_MAIN_GROUP` AS serviceMainGroupDescription, \n" +
            "ssg.`code` AS serviceSubGroupCode, ssg.`description` AS serviceSubGroupDescription, s.`service_code` AS serviceCode,\n" +
            "s.`service_name` AS serviceName, s.`service_pneumonic` AS servicePneumonic FROM `service_main_group` smg JOIN `service_sub_group` ssg\n" +
            "ON smg.`CODE` = ssg.`service_main_group_id` JOIN `services` s ON s.`service_sub_group_id` = ssg.`code`";

    @Autowired
    public ClinicalRestController(DataSource dataSource){
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        System.out.println("\n\n\ndatasource\n\n\n"+dataSource);
    }

    @RequestMapping(value = "/anon/getallservices", method = RequestMethod.GET)
    public @ResponseBody List<Map<String, Object>> getAllServices(){
        return namedParameterJdbcTemplate.queryForList(FETCH_ALL_SERVICES, new TreeMap<String, Object>());
    }
}
