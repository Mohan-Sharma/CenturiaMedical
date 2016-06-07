package com.nzion.domain.messaging;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.nzion.domain.Authorizable;
import com.nzion.domain.Authorization;
import com.nzion.domain.Person;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.util.Infrastructure;
import org.hibernate.annotations.Type;

@Entity
@Table
public class Message extends IdGeneratingBaseEntity implements Authorizable{

	private String messageText;

	private boolean urgent=false;
	
	private boolean task=false;

	private Set<Person> persons;
	
	private Authorization authorization;
	
	private Date fromDate;
	
	private Date thruDate;
	
	private Person sentBy;
	
	private Date sentOn;

    private boolean read_msg=false;

    private String url;

    @Transient
    public String getUrl() {
        if(isRead_msg()){
        return "/images/readMessage.png";
        }else{
            return "/images/unReadMessage.png";
        }
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isRead_msg() {
        return read_msg;
    }

    public void setRead_msg(boolean read_msg) {
        this.read_msg = read_msg;
    }

    @ManyToMany
	public Set<Person> getPersons() {

	if(persons == null)
		persons = new HashSet<Person>();
	return persons;
	}

	public void setPersons(Set<Person> persons) {
	this.persons = persons;
	}

    //@Type(type = "encryptedString")
    @Column(name = "MESSAGE_TEXT",length=1000)
    public String getMessageText() {
        return messageText;
    }

	public void setMessageText(String messageText) {
	this.messageText = messageText;
	}

	public boolean check(Authorization authorization){
	return persons.contains(Infrastructure.getUserLogin().getPerson()) || authorization.check(this.authorization);
	}
	
	@Column(nullable=true)
	public Boolean isUrgent() {
	return urgent;
	}
	
	@Transient
	public Boolean isNotUrgent() {
	return !isUrgent();
	}

	public void setUrgent(Boolean urgent) {
	this.urgent = urgent;
	}

	@Column(nullable=true)
	public Boolean isTask() {
	return task;
	}

	public void setTask(Boolean task) {
	this.task = task;
	}

	public Date getFromDate() {
	return fromDate;
	}

	public void setFromDate(Date fromDate) {
	this.fromDate = fromDate;
	}

	public Date getThruDate() {
	return thruDate;
	}

	public void setThruDate(Date thruDate) {
	this.thruDate = thruDate;
	}

	@Embedded
	public Authorization getAuthorization() {
	if(authorization == null)
		authorization = new Authorization();
	return authorization;
	}

	public void setAuthorization(Authorization authorization) {
	this.authorization = authorization;
	}
	
	@OneToOne
	@JoinColumn(name="PERSON_ID")
	public Person getSentBy() {
	return sentBy;
	}

	public void setSentBy(Person sentBy) {
	this.sentBy = sentBy;
	}
	
	@Column(name = "SENT_ON")
	@Temporal(value = TemporalType.TIMESTAMP)
    //@Type(type = "encryptedDate")
	public Date getSentOn() {
	return sentOn;
	}

	public void setSentOn(Date sentOn) {
	this.sentOn = sentOn;
	}

	private static final long serialVersionUID = 1L;
}