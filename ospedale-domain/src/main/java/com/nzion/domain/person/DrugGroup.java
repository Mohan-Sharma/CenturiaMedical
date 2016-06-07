package com.nzion.domain.person;

import com.nzion.domain.Person;

import javax.persistence.*;

/**
 * Created by Mohan Sharma on 4/22/2015.
 */
@Entity
public class DrugGroup {
    private long id;
    private String drugGroup;
    private AgeGroup ageGroup;
    private Person person;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column(name = "DRUG_GROUP")
    public String getDrugGroup() {
        return drugGroup;
    }

    public void setDrugGroup(String drugGroup) {
        this.drugGroup = drugGroup;
    }

    @OneToOne(fetch = FetchType.EAGER, targetEntity = AgeGroup.class)
    @JoinColumn(name = "AGE_GROUP_ID")
    public AgeGroup getAgeGroup() {
        return ageGroup;
    }

    public void setAgeGroup(AgeGroup ageGroup) {
        this.ageGroup = ageGroup;
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
