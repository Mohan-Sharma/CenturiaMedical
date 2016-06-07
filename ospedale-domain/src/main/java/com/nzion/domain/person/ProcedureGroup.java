package com.nzion.domain.person;

import com.nzion.domain.Person;
import com.nzion.domain.base.IdGeneratingBaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Created by Mohan Sharma on 4/24/2015.
 */
@Entity
public class ProcedureGroup extends IdGeneratingBaseEntity{
    private String procedureGroupName;
    private Person person;

    @Column(name = "PROCEDURE_GROUP_NAME")
    public String getProcedureGroupName() {
        return procedureGroupName;
    }

    public void setProcedureGroupName(String procedureGroupName) {
        this.procedureGroupName = procedureGroupName;
    }

    @ManyToOne
    @JoinColumn(name = "PERSON_ID")
    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }
}
