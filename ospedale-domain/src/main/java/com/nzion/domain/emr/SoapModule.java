package com.nzion.domain.emr;

import com.nzion.domain.Authorizable;
import com.nzion.domain.Authorization;
import com.nzion.domain.SoapComponentAuthorization;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.soap.OtherSoapNoteSection;
import com.nzion.enums.SoapModuleType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

/**
 * @author Sudarshan
 */

@Entity
@Table(name = "SOAP_MODULE", uniqueConstraints = {@UniqueConstraint(name = "UNIQUE_PRACTICE_MODULE_NAME", columnNames = {"NAME"})})
@Filters({@Filter(name = "EnabledFilter", condition = "(IS_ACTIVE=1 OR IS_ACTIVE IS NULL)")})
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "com.nzion.domain")
public class SoapModule extends IdGeneratingBaseEntity implements ModuleMarker, Authorizable, Comparable<SoapModule> {

    private static final long serialVersionUID = 1L;
    private String moduleName;
    private String moduleShortName;
    private Integer sortOrder;
    private SoapModuleType soapModuleType = SoapModuleType.QA;
    private SoapComponentAuthorization soapComponentAuthorization;
    private String moduleDescription;

    private Authorization authorization;
    private String className = OtherSoapNoteSection.class.getName();
    private List<QATemplate> qaTemplates;

    public static final SortOrderComparator SORTORDERCOMPARATOR = new SortOrderComparator();

    public SoapModule() {
    }

    public SoapModule(String soapModuleId) {
        super.id = soapModuleId;
    }

    @Embedded
    public Authorization getAuthorization() {
        return authorization;
    }

    public void setAuthorization(Authorization authorization) {
        this.authorization = authorization;
    }

    @Column(name = "NAME")
    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    @Column(name = "MODULE_SHORT_NAME")
    public String getModuleShortName() {
        return moduleShortName;
    }

    public void setModuleShortName(String moduleShortName) {
        this.moduleShortName = moduleShortName;
    }

    @Column(name = "SORT_ORDER")
    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Column(name = "SOAP_MODULE_TYPE")
    @Enumerated(EnumType.STRING)
    public SoapModuleType getSoapModuleType() {
        return soapModuleType;
    }

    public void setSoapModuleType(SoapModuleType soapModuleType) {
        this.soapModuleType = soapModuleType;
    }

    @OneToOne
    @JoinColumn(name = "SOAP_COMPONENT_AUTHORIZATION_ID")
    public SoapComponentAuthorization getSoapComponentAuthorization() {
        return soapComponentAuthorization;
    }

    public void setSoapComponentAuthorization(SoapComponentAuthorization soapComponentAuthorization) {
        this.soapComponentAuthorization = soapComponentAuthorization;
    }

    @Column(name = "MODULE_DESCRIPTION")
    public String getModuleDescription() {
        return moduleDescription;
    }

    public void setModuleDescription(String moduleDescription) {
        this.moduleDescription = moduleDescription;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((moduleName == null) ? 0 : moduleName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass() != obj.getClass()) return false;
        SoapModule other = (SoapModule) obj;
        return (moduleName != null && moduleName.toLowerCase().equals(other.getModuleName().toLowerCase()));
    }

    public boolean check(Authorization authorization) {
        return (this.authorization == null) ? soapComponentAuthorization.check(authorization) : this.authorization
                .check(authorization);
    }

    public Authorization effectiveAuthorization() {
        return authorization != null ? authorization : soapComponentAuthorization.getAuthorization();
    }

    private static class SortOrderComparator implements Comparator<SoapModule>, Serializable {

        private static final long serialVersionUID = 1L;

        public int compare(SoapModule soapModule1, SoapModule soapModule2) {
            if (soapModule1 == null || soapModule2 == null) return 0;
            if (soapModule1.getSortOrder() == null || soapModule2.getSortOrder() == null) return 0;
            return soapModule1.getSortOrder().compareTo(soapModule2.getSortOrder());
        }
    }

    @Override
    public int compareTo(SoapModule o) {
        if (sortOrder == null) return 1;
        if(o == null) return -1;
        if (o.sortOrder == null) return -1;
        return sortOrder.compareTo(o.sortOrder);
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @OneToMany(mappedBy = "soapModule", fetch = FetchType.EAGER)
    public List<QATemplate> getQaTemplates() {
        return qaTemplates;
    }

    public void setQaTemplates(List<QATemplate> qaTemplates) {
        this.qaTemplates = qaTemplates;
    }
}