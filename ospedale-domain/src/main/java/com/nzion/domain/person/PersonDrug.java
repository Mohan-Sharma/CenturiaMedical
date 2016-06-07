package com.nzion.domain.person;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.nzion.domain.Person;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.drug.Drug;
import com.nzion.domain.drug.DrugDosageForm;
import com.nzion.domain.drug.DrugDosageRoute;
import com.nzion.domain.drug.DrugSig;

@Entity
@Table(name="PERSON_DRUG")
public class PersonDrug extends IdGeneratingBaseEntity {
	private static final long serialVersionUID = -7816514043351454591L;
	
	private Person person;
	private Drug drug;
	private DrugSig quantityQualifier;
	private DrugDosageForm drugDosageForm;
	private DrugDosageRoute drugDosageRoute;
	private DrugSig drugDirection;
	private String specialInstruction;
	private DrugSig quantity;
	private String strength;
	
	public PersonDrug() {
	}

	public PersonDrug(Person person, Drug drug) {
	this.person = person;
	this.drug = drug;
	}

	@ManyToOne
	@JoinColumn(name="PERSON_ID")
	public Person getPerson() {
	return person;
	}
	
	public void setPerson(Person person) {
	this.person = person;
	}
	
	@ManyToOne
	@JoinColumn(name="DRUG_ID")
	public Drug getDrug() {
	return drug;
	}

	public void setDrug(Drug drug) {
	this.drug = drug;
	}
	
	public String getStrength() {
	return strength;
	}

	public void setStrength(String strength) {
	this.strength = strength;
	}

	@ManyToOne
	@JoinColumn(name="QUANTITY_QUALIFIER_ID")
	public DrugSig getQuantityQualifier() {
	return quantityQualifier;
	}

	public void setQuantityQualifier(DrugSig quantityQualifier) {
	this.quantityQualifier = quantityQualifier;
	}

	@ManyToOne
	@JoinColumn(name="DRUG_DOSAGE_FORM_ID")
	public DrugDosageForm getDrugDosageForm() {
	return drugDosageForm;
	}

	public void setDrugDosageForm(DrugDosageForm drugDosageForm) {
	this.drugDosageForm = drugDosageForm;
	}

	@ManyToOne
	@JoinColumn(name="DRUG_DOSAGE_ROUTE_ID")
	public DrugDosageRoute getDrugDosageRoute() {
	return drugDosageRoute;
	}

	public void setDrugDosageRoute(DrugDosageRoute drugDosageRoute) {
	this.drugDosageRoute = drugDosageRoute;
	}

	@ManyToOne
	@JoinColumn(name="DRUG_DIRECTION_ID")
	public DrugSig getDrugDirection() {
	return drugDirection;
	}

	public void setDrugDirection(DrugSig drugDirection) {
	this.drugDirection = drugDirection;
	}

	@ManyToOne
	@JoinColumn(name="QUANTITY_ID")
	public DrugSig getQuantity() {
	return quantity;
	}

	public void setQuantity(DrugSig quantity) {
	this.quantity = quantity;
	}
	
	
	@Column(name="SPECIAL_INSTRUCTION")
	public String getSpecialInstruction() {
	return specialInstruction;
	}

	public void setSpecialInstruction(String specialInstruction) {
	this.specialInstruction = specialInstruction;
	}

	@Override
	public boolean equals(Object obj) {
	if(!(obj instanceof PersonDrug))
		return false;
	PersonDrug that = (PersonDrug)obj;
	return this.person.equals(that.person) && this.drug.equals(that.drug);
	}

	@Override
	public int hashCode() {
	return person.getId().intValue();
	}
}