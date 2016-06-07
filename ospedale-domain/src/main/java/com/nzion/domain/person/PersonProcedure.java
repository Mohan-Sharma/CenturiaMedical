package com.nzion.domain.person;

import com.nzion.domain.Person;
import com.nzion.domain.emr.Cpt;

import javax.persistence.*;

/**
 * Created by Mohan Sharma on 4/24/2015.
 */
@Entity
public class PersonProcedure {
    private long id;
    private Person person;
    private ProcedureGroup procedureGroup;
    private Cpt procedure;
    private int unit;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @ManyToOne
    @JoinColumn(name = "PERSON_ID")
    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    @OneToOne
    @JoinColumn(name = "PROCEDURE_GROUP_ID")
    public ProcedureGroup getProcedureGroup() {
        return procedureGroup;
    }

    public void setProcedureGroup(ProcedureGroup procedureGroup) {
        this.procedureGroup = procedureGroup;
    }

    @OneToOne
    @JoinColumn(name = "PROCEDURE_ID")
    public Cpt getProcedure() {
        return procedure;
    }

    public void setProcedure(Cpt procedure) {
        this.procedure = procedure;
    }

    @Column(name = "UNIT")
    public int getUnit() {
        return unit;
    }

    public void setUnit(int unit) {
        this.unit = unit;
    }
}
