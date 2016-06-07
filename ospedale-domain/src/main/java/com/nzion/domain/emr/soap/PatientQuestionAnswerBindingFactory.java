package com.nzion.domain.emr.soap;

import com.nzion.domain.emr.Question;

/**
 * @author Sandeep Prusty
 * Dec 25, 2010
 */
public interface PatientQuestionAnswerBindingFactory {

	PatientQuestionAnswer getPatientQuestionAnswerFor(Question question);

	void setRemarks(String remark);

	String getRemarks();
	
	boolean hasAnswered(Question question);
}
