package com.nzion.service.servlet;

import com.mchange.v2.c3p0.DriverManagerDataSource;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.nzion.hibernate.ext.multitenant.RepositoryConfig;
import com.nzion.hibernate.ext.multitenant.TenantIdHolder;
import com.nzion.hibernate.ext.multitenant.TenantRoutingDataSource;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: USER
 * Date: 8/31/15
 * Time: 3:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class RepositoryConfigServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	private static final String TENANT_HTTP_KEY = "j_tenantId";
	
    public void init(ServletConfig config) throws ServletException{
        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        /*XmlWebApplicationContext xmlWebApplicationContext = (XmlWebApplicationContext) ContextLoader.getCurrentWebApplicationContext();
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) xmlWebApplicationContext.getBeanFactory();*/

       /* BeanDefinition beanDefinition = defaultListableBeanFactory.getBeanDefinition("tenantDataSource");
        beanDefinition.setBeanClassName(DriverManagerDataSource.class.getName());
        defaultListableBeanFactory.registerBeanDefinition( "tenantDataSource", beanDefinition);*/

        /*BeanDefinition beanDefinition1 = defaultListableBeanFactory.getBeanDefinition("dataSource");
        beanDefinition1.setBeanClassName(TenantRoutingDataSource.class.getName());
        defaultListableBeanFactory.registerBeanDefinition( "dataSource", beanDefinition1);*/
        String tenantId = request.getParameter("clinicId");
        WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        TenantRoutingDataSource tenantRoutingDataSource = (TenantRoutingDataSource) applicationContext.getBean("dataSource");
        RepositoryConfig repositoryConfig = new RepositoryConfig();
        repositoryConfig.routingDataSource(tenantRoutingDataSource);
        
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUrl("jdbc:mysql://localhost:3306/"+tenantId);
        dataSource.setUser("root");
        dataSource.setPassword("welcome");

        tenantRoutingDataSource.setDefaultTargetDataSource(dataSource);
        TenantIdHolder.setTenantId(tenantId);
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        httpRequest.getSession().setAttribute(TENANT_HTTP_KEY,tenantId);

    }
}
