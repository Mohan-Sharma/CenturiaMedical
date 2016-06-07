package com.nzion.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nzion.hibernate.ext.multitenant.TenantIdHolder;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.nzion.domain.PasswordPolicy;
import com.nzion.domain.Roles;
import com.nzion.domain.UserLogin;
import com.nzion.service.PasswordPolicyService;
import com.nzion.service.PracticeService;
import com.nzion.util.UserLoginUserDetailsAdapter;
import com.nzion.util.UtilMessagesAndPopups;
import org.springframework.util.Assert;

@Transactional(propagation = Propagation.REQUIRED)
public class AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final Logger logger = LoggerFactory.getLogger(AuthenticationFailureHandler.class);

    private SessionFactory sessionFactory = null;

    private String tenantFailureUrl;
    private static final String TENANT_HTTP_KEY = "j_tenantId";

    // This is directly invoked by Service rather than fetching from the Practice
    // /as the second level cache would cache the Password Policy.
    private PasswordPolicyService passwordPolicyService = null;

    private PracticeService practiceService;

    public PracticeService getPracticeService() {
        return practiceService;
    }

    @Required
    public void setPracticeService(PracticeService practiceService) {
        this.practiceService = practiceService;
    }

    public PasswordPolicyService getPasswordPolicyService() {
        return passwordPolicyService;
    }

    @Required
    public void setPasswordPolicyService(PasswordPolicyService passwordPolicyService) {
        this.passwordPolicyService = passwordPolicyService;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        logger.trace(" UserLogin ID is " + exception.getAuthentication().getName());
        UserLoginUserDetailsAdapter adapter = (UserLoginUserDetailsAdapter) exception.getExtraInformation();
        if (adapter != null) {
            Session session = sessionFactory.getCurrentSession();
            UserLogin userLogin = adapter.getUserLogin();
            userLogin.setSuccessiveFailedLogins(userLogin.getSuccessiveFailedLogins() + 1);
            if (userLogin != null && !userLogin.hasRole(Roles.ADMIN)) {
                PasswordPolicy policy = passwordPolicyService.getEffectivePasswordPolicy();
                Integer maxFailureCount = policy.getPwdmaxfailure() == null ? Integer.MAX_VALUE : policy.getPwdmaxfailure();
                if (userLogin.getSuccessiveFailedLogins() >= maxFailureCount + (policy.getPwdGraceLoginLimit() == null ? 0 : policy.getPwdGraceLoginLimit()))
                    userLogin.setAccountNonLocked(false);

            }
            session.disableFilter("PracticeFilter");
            session.merge(userLogin);
        }

        if (TenantIdHolder.getTenantId() != null && adapter == null) {
            setDefaultFailureUrl(getTenantFailureUrl());
            response.sendRedirect(request.getContextPath() + getTenantFailureUrl());
            //System.out.println("Redirecting to the page :" + request.getContextPath() + getTenantFailureUrl());

            /*
            System.out.println("getRemoteAddr :" + request.getRemoteAddr());
            System.out.println("getRemoteHost :" + request.getRemoteHost());
            System.out.println("getRemoteHost :" + request.getRemoteHost());

            System.out.println("getPathInfo :" + request.getPathInfo());
            System.out.println("getPathInfo translated :" + request.getPathTranslated());
            System.out.println("Local port :" + request.getLocalPort());
            System.out.println("Local name :" + request.getLocalName());
            System.out.println("server name :" + request.getServerName());
            System.out.println("server port :" + request.getServerPort());
            System.out.println("Scheme name :" + request.getScheme());
            */
            //response.sendRedirect("http://localhost:8080/ospedale" + getTenantFailureUrl());
        }

        super.onAuthenticationFailure(request, response, exception);
    }

    public void setTenantFailureUrl(String tenantFailureUrl) {
        this.tenantFailureUrl = tenantFailureUrl;
    }

    public String getTenantFailureUrl() {
        return this.tenantFailureUrl;
    }
}