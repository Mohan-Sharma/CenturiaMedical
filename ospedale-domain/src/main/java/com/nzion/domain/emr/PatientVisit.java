package com.nzion.domain.emr;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.nzion.domain.FixedAsset;
import com.nzion.domain.Person;
import com.nzion.domain.Schedule;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.util.Constants;

/**
 * @author Sandeep Prusty
 * Jul 14, 2010
 */

@Entity
@Table(name = "PATIENT_VISIT")
public class PatientVisit extends IdGeneratingBaseEntity implements Comparable<PatientVisit>{

	private FixedAsset room;

	private Person metWith;
	
	private Date time;
	
	public PatientVisit(){
	time = new Date();
	}
	
	public PatientVisit(Schedule schedule) {
	time = new Date();
	}

	@Column(name="TIME")
	public Date getTime() {
	return time;
	}

	public void setTime(Date time) {
	this.time = time;
	}

	@OneToOne(targetEntity = FixedAsset.class, fetch = FetchType.EAGER)
	@JoinColumn(name = "ROOM_ID")
	public FixedAsset getRoom() {
	return room;
	}

	public void setRoom(FixedAsset room) {
	this.room = room;
	}

	@OneToOne(targetEntity = Person.class, fetch = FetchType.EAGER)
	@JoinColumn(name = "MET_WITH_PERSON_ID")
	public Person getMetWith() {
	return metWith;
	}

	public void setMetWith(Person metWith) {
	this.metWith = metWith;
	}

	public int compareTo(PatientVisit another) {
	return time.compareTo(another.time);
	}
	
	@Override
	public String toString() {
	StringBuilder builder = new StringBuilder();
	if(metWith != null){
		builder.append(" with ").append(metWith.getFirstName() == null ? "" : metWith.getFirstName())
				.append(Constants.BLANK_CHAR).append(metWith.getLastName() == null ? "" : metWith.getLastName());
	}
	if(room != null){
		builder.append(" in ").append(room.getFixedAssetName());
	}
	return builder.toString();
	}

	private static final long serialVersionUID = 1L;
}