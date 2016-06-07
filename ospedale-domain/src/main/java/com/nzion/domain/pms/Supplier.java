package com.nzion.domain.pms;

import javax.persistence.Embedded;
import javax.persistence.Entity;

import org.hibernate.annotations.Filter;

import com.nzion.domain.ContactFields;
import com.nzion.domain.base.IdGeneratingBaseEntity;

@Entity
@Filter(name = "EnabledFilter", condition = "(IS_ACTIVE=1 OR IS_ACTIVE IS NULL)")
public class Supplier extends IdGeneratingBaseEntity{
	private static final long serialVersionUID = 1L;
	
	private String name;
	
    private ContactFields contactFields;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Embedded
    public ContactFields getContactFields() {
	    if(contactFields == null)
	    	contactFields = new ContactFields();
	    return contactFields;
    }

    public void setContactFields(ContactFields contactFields) {
        this.contactFields = contactFields;
    }

}
