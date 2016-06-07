package com.nzion.domain.person;

import com.nzion.domain.base.IdGeneratingBaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by Mohan Sharma on 4/22/2015.
 */
@Entity
public class AgeGroup {
    private long id;
    private String ageGroup;

    @Id
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column(name = "AGE_GROUP")
    public String getAgeGroup() {
        return ageGroup;
    }

    public void setAgeGroup(String ageGroup) {
        this.ageGroup = ageGroup;
    }
}
