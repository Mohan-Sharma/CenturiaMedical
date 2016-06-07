package com.nzion.service.meaningful;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.nzion.domain.Patient;
import com.nzion.domain.emr.soap.PatientSoapNote;
import com.nzion.util.UtilValidator;

/**
 * @author Sandeep Prusty
 * Aug 24, 2011
 */
public class PatientsAndSoapNotes {
	
	private Map<Patient, SortedSet<PatientSoapNote>> holder = new HashMap<Patient, SortedSet<PatientSoapNote>>();
	
	public PatientsAndSoapNotes() {	}
	
	public PatientsAndSoapNotes(Collection<PatientSoapNote> soapNotes) {	
	add(soapNotes);
	}
	
	public void add(PatientSoapNote soapNote){
	getSoapNotes(soapNote.getPatient()).add(soapNote);
	}
	
	public void add(Collection<PatientSoapNote> soapNotes){
	if(UtilValidator.isEmpty(soapNotes))
		return;
	for(PatientSoapNote soapNote : soapNotes)
		add(soapNote);
	}

	public SortedSet<PatientSoapNote> getSoapNotes(Patient patient){
	SortedSet<PatientSoapNote> soapNotes = holder.get(patient);
	if(soapNotes != null)
		return soapNotes; 
	soapNotes = new TreeSet<PatientSoapNote>(soapNoteDateComparator);
	holder.put(patient, soapNotes);
	return soapNotes;
	}
	
	public Set<Patient> getAllPatients(){
	return holder.keySet();
	}
	
	public Set<PatientSoapNote> getAllSoapNotes(){
	Set<PatientSoapNote> result = new HashSet<PatientSoapNote>();
	Collection<SortedSet<PatientSoapNote>> soapNoteSets = holder.values();
	for(Set<PatientSoapNote> soapNotes : soapNoteSets)
		result.addAll(soapNotes);
	return result;
	}
	
	public int getSoapNoteCountForPatient(Patient patient){
	return getSoapNotes(patient).size();
	}
	
	public void remove(Patient patient){
	holder.remove(patient);
	}
	
	public boolean isEmpty(){
	return holder.isEmpty();
	}
	
	public PatientSoapNote getLatestSoapNoteForPatient(Patient patient)	{
	return getSoapNotes(patient).last();
	}
	
	public PatientSoapNote getEarliestSoapNoteForPatient(Patient patient)	{
	return getSoapNotes(patient).first();
	}
	
	public void retainAllPatientsFromSoapnotes(Collection<PatientSoapNote> soapNotes){
	if(UtilValidator.isEmpty(soapNotes))
		return;
	Set<Patient> givenPatients = new HashSet<Patient>();
	for(PatientSoapNote soapNote : soapNotes)
		givenPatients.add(soapNote.getPatient());
	retainAllPatients(givenPatients);
	}
	
	public void retainAllPatients(Collection<Patient> givenPatients){
	Set<Patient> existingPatients = new HashSet<Patient>(getAllPatients());
	for(Patient existingPatient : existingPatients){ 
		if(!givenPatients.contains(existingPatient))
			remove(existingPatient);
	}
	}
	
	public static Comparator<PatientSoapNote> soapNoteDateComparator = new Comparator<PatientSoapNote>() {
		@Override
		public int compare(PatientSoapNote o1, PatientSoapNote o2) {
		return o1.getDate().compareTo(o2.getDate());
		}
	};
}