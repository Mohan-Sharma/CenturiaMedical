package com.nzion.domain.drug;

import com.nzion.domain.emr.MasterEntity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Created by Mohan Sharma on 3/20/2015.
 */
@Entity
@Table(name = "FREQUENCY", uniqueConstraints = { @UniqueConstraint(columnNames = { "CODE" }) })
public class Frequency extends MasterEntity {
    private static final long serialVersionUID = 890393010614834247L;
}
