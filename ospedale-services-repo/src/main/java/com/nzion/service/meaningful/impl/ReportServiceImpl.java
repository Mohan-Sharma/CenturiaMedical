package com.nzion.service.meaningful.impl;

import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;

import com.nzion.domain.Patient;
import com.nzion.domain.util.EncounterSearchResult;
import com.nzion.report.search.view.PatientEncounterSearchVo;
import com.nzion.report.search.view.PatientSearchVO;
import com.nzion.repository.meaningful.ReportRepository;
import com.nzion.service.meaningful.ReportService;

@Service("reportService")
public class ReportServiceImpl implements ReportService {
	
	private ReportRepository reportRepository;
	
	@Override
	public Set<EncounterSearchResult> getPatientSoapNote(PatientEncounterSearchVo patientEncounterSearchVo) {
	return reportRepository.getPatientSoapNote(patientEncounterSearchVo);
	}

	@Override
	public List<String> getAllObxTestNames() {
	List<String> obxTestNames = reportRepository.getAllObxTestNames();
	//Collections.sort(obxTestNames);
	return obxTestNames;
	}
	
	@Resource
	@Required
	public void setReportRepository(ReportRepository reportRepository) {
	this.reportRepository = reportRepository;
	}

	@Override
	public List<Patient> searchPatient(PatientSearchVO patientSearchVO) {
	return reportRepository.searchPatient(patientSearchVO);
	}

}
