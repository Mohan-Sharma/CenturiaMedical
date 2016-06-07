package com.nzion.domain.emr.soap;



import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.nzion.domain.base.IdGeneratingBaseEntity;


@Entity
@Table
@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="com.nzion.domain")
public class PatientPastHistory extends IdGeneratingBaseEntity  {

	private  String operationName;
	
	private Date occuranceDate;
	
	private String locationName;
	
	private String operatedBy;
	
	private String pastHistoryType;

	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	public Date getOccuranceDate() {
		return occuranceDate;
	}

	public void setOccuranceDate(Date occuranceDate) {
		this.occuranceDate = occuranceDate;
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public String getOperatedBy() {
		return operatedBy;
	}

	public void setOperatedBy(String operatedBy) {
		this.operatedBy = operatedBy;
	}
	
	public String getPastHistoryType() {
		return pastHistoryType;
	}

	public void setPastHistoryType(String pastHistoryType) {
		this.pastHistoryType = pastHistoryType;
	}
	private static final long serialVersionUID = 1L;
	
}
