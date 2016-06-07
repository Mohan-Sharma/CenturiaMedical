package com.nzion.domain.drug;

import com.nzion.domain.emr.MasterEntity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by Mohan Sharma on 3/19/2015.
 */
@Entity
@Table(name = "DRUG_FORM", uniqueConstraints = { @UniqueConstraint(columnNames = { "CODE" }) })
public class DrugForm implements Serializable{
    private static final long serialVersionUID = 6747315876398580852L;
    @Id
    public Long id;
    public String code;
    public String drugForm;

    @Column(name = "CODE")
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Column(name = "DRUG_FORM")
    public String getDrugForm() {
        return drugForm;
    }

    public void setDrugForm(String drugForm) {
        this.drugForm = drugForm;
    }
}
