package com.nzion.domain.person;

import com.nzion.domain.Person;
import com.nzion.domain.drug.Drug;
import com.nzion.domain.drug.Frequency;
import com.nzion.domain.drug.FrequencyQualifier;

import javax.persistence.*;

/**
 * Created by Mohan Sharma on 4/23/2015.
 */
@Entity
public class ProviderDrug {
    private long id;
    private DrugGroup drugGroup;
    private Drug drug;
    private Frequency frequency;
    private FrequencyQualifier frequencyQualifier;
    private int numberOfDays;
    private int totalCount;
    private Person person;

    public ProviderDrug(){}
    public ProviderDrug(Drug drug, Frequency frequency, FrequencyQualifier frequencyQualifier, int numberOfDays, Person person, int totalCount) {
        this.drug = drug;
        this.frequency = frequency;
        this.frequencyQualifier = frequencyQualifier;
        this.numberOfDays = numberOfDays;
        this.person = person;
        this.totalCount = totalCount;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @OneToOne(fetch = FetchType.EAGER, targetEntity = DrugGroup.class)
    @JoinColumn(name = "DRUG_GROUP_ID")
    public DrugGroup getDrugGroup() {
        return drugGroup;
    }

    public void setDrugGroup(DrugGroup drugGroup) {
        this.drugGroup = drugGroup;
    }

    @OneToOne(fetch = FetchType.EAGER, targetEntity = Drug.class)
    @JoinColumn(name = "DRUG_ID")
    public Drug getDrug() {
        return drug;
    }

    public void setDrug(Drug drug) {
        this.drug = drug;
    }

    @OneToOne(fetch = FetchType.EAGER, targetEntity = Frequency.class)
    @JoinColumn(name = "FREQUENCY_ID")
    public Frequency getFrequency() {
        return frequency;
    }

    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }

    @OneToOne(fetch = FetchType.EAGER, targetEntity = FrequencyQualifier.class)
    @JoinColumn(name = "FREQUENCY_QUALIFIER_ID")
    public FrequencyQualifier getFrequencyQualifier() {
        return frequencyQualifier;
    }

    public void setFrequencyQualifier(FrequencyQualifier frequencyQualifier) {
        this.frequencyQualifier = frequencyQualifier;
    }

    @Column(name = "NUMBER_OF_DAYS")
    public int getNumberOfDays() {
        return numberOfDays;
    }

    public void setNumberOfDays(int numberOfDays) {
        this.numberOfDays = numberOfDays;
    }

    @Column(name = "TOTAL_COUNT")
    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
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
