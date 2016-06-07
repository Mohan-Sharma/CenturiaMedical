package com.nzion.hibernate.ext.multitenant;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.nzion.util.UtilValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: USER
 * Date: 8/29/15
 * Time: 10:51 AM
 * To change this template use File | Settings | File Templates.
 */

@Configuration
public class RepositoryConfig {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceFactory.class);

    Map<Object, Object> tenantIdToDataSourceMap = null;

    @Autowired private ApplicationContext applicationContext;

    private SimpleJdbcTemplate jdbcTemplate;

    @Bean
    public AbstractRoutingDataSource routingDataSource(TenantRoutingDataSource routingDataSource){
        //Map<Object, Object> customerIndexMap = initialiseConfiguredTenantDataSources();
        //routingDataSource.setTargetDataSources(customerIndexMap);
        return routingDataSource;
    }

    public Map<Object, Object> initialiseConfiguredTenantDataSources() {
        Map<Object, Object> tenantIdToDataSourceMap = new HashMap();
        jdbcTemplate = new SimpleJdbcTemplate(createDataSource());
        List<Tenant> allConfiguredTenants = jdbcTemplate.query("select * from my_afya_portal.tenant", new RowMapper<Tenant>() {
            @Override
            public Tenant mapRow(ResultSet rs, int rowNum) throws SQLException {
                Tenant tenant = new Tenant(rs.getString("tenant_id"), rs.getString("tenant_name"), rs.getBoolean("is_enabled"));
                return tenant;
            }
        });
        for (Tenant tenant : allConfiguredTenants) {
            tenantIdToDataSourceMap.put(tenant.getTenantId(), createDataSource(tenant));
        }
        this.tenantIdToDataSourceMap = tenantIdToDataSourceMap;
        return tenantIdToDataSourceMap;
    }

    public Map<Object, Object> fetchConfiguredTenantDataSources() {
        if (UtilValidator.isNotEmpty(this.tenantIdToDataSourceMap)) {
            logger.debug("Picking data from cache");
            return tenantIdToDataSourceMap;
        } else {
            logger.debug("initialiseConfiguredTenantDataSources");
            return initialiseConfiguredTenantDataSources();
        }
    }

    protected SimpleJdbcTemplate getSimpleJdbcTemplate() {
        return jdbcTemplate;
    }

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
    }

    DataSource createDataSource() {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUrl("jdbc:mysql://localhost:3306/my_afya_portal");
        dataSource.setUser("root");
        dataSource.setPassword("welcome");
        return dataSource;
    }

    DataSource createDataSource(ITenantAware tenant) {
        logger.debug("Going to create a brand new data source for tenant " + tenant.getTenantId());
        MysqlDataSource dataSource = new MysqlDataSource();
        TenantCustomisationDetails customisationDetails = getCustomisationDetails(tenant);
        dataSource.setUrl(customisationDetails.getJdbcUrl());
        dataSource.setUser(customisationDetails.getJdbcUsername());
        dataSource.setPassword(customisationDetails.getJdbcPassword());
        return dataSource;
    }

    TenantCustomisationDetails getCustomisationDetails(ITenantAware tenant) {
        jdbcTemplate = new SimpleJdbcTemplate(createDataSource());
        Map<String, Object> tenantResult = jdbcTemplate.queryForMap("select * from my_afya_portal.tenant where tenant_id = ?", tenant.getTenantId());
        TenantCustomisationDetails tenantCustomisationDetails = new TenantCustomisationDetails((String) tenantResult.get("jdbc_url"), (String) tenantResult.get("jdbc_username"), (String) tenantResult.get("jdbc_password"));
        return tenantCustomisationDetails;
    }
}
