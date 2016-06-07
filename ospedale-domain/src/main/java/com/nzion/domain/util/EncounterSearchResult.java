package com.nzion.domain.util;

import com.nzion.domain.emr.soap.PatientSoapNote;
import com.nzion.util.UtilValidator;

import java.util.*;

public class EncounterSearchResult {

    private String groupingType;

    private String groupingValue;

    private Set<PatientSoapNote> patientSoapNotes;

    public String getGroupingType() {
        return groupingType;
    }

    public void setGroupingType(String groupingType) {
        this.groupingType = groupingType;
    }

    public String getGroupingValue() {
        return groupingValue;
    }

    public void setGroupingValue(String groupingValue) {
        this.groupingValue = groupingValue;
    }

    public Set<PatientSoapNote> getPatientSoapNotes() {
        return patientSoapNotes;
    }

    public void setPatientSoapNotes(Set<PatientSoapNote> patientSoapNotes) {
        this.patientSoapNotes = patientSoapNotes;
    }

    public static List<PatientSoapNote> mergeResults(Collection<EncounterSearchResult> searchResults) {
        if (UtilValidator.isEmpty(searchResults)) return null;
        Set<PatientSoapNote> mergedList = new HashSet<PatientSoapNote>();
        for (EncounterSearchResult searchResult : searchResults)
            mergedList.addAll(searchResult.getPatientSoapNotes());
        return new ArrayList<PatientSoapNote>(mergedList);
    }

    public void add(PatientSoapNote patientSoapNote) {
        if (UtilValidator.isEmpty(patientSoapNotes)) patientSoapNotes = new HashSet<PatientSoapNote>();
        patientSoapNotes.add(patientSoapNote);
    }

}
