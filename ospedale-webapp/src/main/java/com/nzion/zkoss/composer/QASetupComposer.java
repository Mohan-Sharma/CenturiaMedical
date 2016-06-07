package com.nzion.zkoss.composer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;

import com.nzion.domain.emr.Answer;
import com.nzion.domain.emr.OrganSystem;
import com.nzion.domain.emr.QATemplate;
import com.nzion.domain.emr.Question;
import com.nzion.domain.emr.SoapModule;
import com.nzion.domain.emr.soap.PatientExamination;
import com.nzion.domain.emr.soap.PatientRosQA;
import com.nzion.enums.AnswerType;
import com.nzion.service.common.CommonCrudService;
import com.nzion.util.UtilMessagesAndPopups;
import com.nzion.util.UtilValidator;

public class QASetupComposer extends OspedaleAutowirableComposer {

	private QATemplate selectedQATemplate;
	private SoapModule selectedSoapModule;
	private OrganSystem selectedOrganSystem;
	private Question selectedQuestion;
	private Set<Answer> answers;
	private CommonCrudService commonCrudService;
	private AnswerType answerType;

	public Set<Answer> getAnswers() {
	if (selectedQuestion != null) {
		answers = new HashSet<Answer>();
		answers.addAll(selectedQuestion.getAnswers());
		if (AnswerType.YES_NO.equals(answerType)) {
			if (!AnswerType.YES_NO.equals(selectedQuestion.getAnswerType())) {
				Answer a = new Answer();
				a.setName("Yes");
				selectedQuestion.addAnswer(a);
				Answer a2 = new Answer();
				a2.setName("No");
				selectedQuestion.addAnswer(a2);
				answers.add(a);
				answers.add(a2);
			}
		}
	}
	return answers;
	}

	public void newQuestion() {
	Question q = new Question();
	if (selectedOrganSystem != null) {
		q.setOrganSystem(selectedOrganSystem);
	} else {
		selectedQATemplate.addQuestion(q);
	}
	}

	public void saveQuestion() {
	if (selectedQuestion != null) {
		Set<Answer> answers = selectedQuestion.getAnswers();
		for (Iterator<Answer> iter = answers.iterator(); iter.hasNext();) {
			if (StringUtils.isEmpty(iter.next().getName())) {
				iter.remove();
			}
		}
	}
	if (selectedOrganSystem != null) {
		selectedOrganSystem.addQuestion(selectedQuestion);
	} else {
		selectedQATemplate.addQuestion(selectedQuestion);
	}
	selectedQuestion.setQaTemplate(selectedQATemplate);
	selectedQuestion.setSoapModule(selectedSoapModule);
	selectedQuestion.setAnswerType(answerType);
	commonCrudService.save(selectedQuestion);
	}

	public QATemplate getSelectedQATemplate() {
	return selectedQATemplate;
	}

	public void setSelectedQATemplate(QATemplate selectedQATemplate) {
	this.selectedQATemplate = selectedQATemplate;
	if (selectedQATemplate == null) {
		selectedQuestion = null;
	}
	}

	public SoapModule getSelectedSoapModule() {
	return selectedSoapModule;
	}

	public void setSelectedSoapModule(SoapModule selectedSoapModule) {
	this.selectedSoapModule = selectedSoapModule;
	}

	public OrganSystem getSelectedOrganSystem() {
	return selectedOrganSystem;
	}

	public void setSelectedOrganSystem(OrganSystem selectedOrganSystem) {
	this.selectedOrganSystem = selectedOrganSystem;
	}

	public Question getSelectedQuestion() {
	return selectedQuestion;
	}

	public void setSelectedQuestion(Question selectedQuestion) {
	this.selectedQuestion = selectedQuestion;
	}

	public CommonCrudService getCommonCrudService() {
	return commonCrudService;
	}

	public void setCommonCrudService(CommonCrudService commonCrudService) {
	this.commonCrudService = commonCrudService;
	}

	public void setAnswers(Set<Answer> answers) {
	this.answers = answers;
	}

	public void saveQATemplate() {
	commonCrudService.save(selectedQATemplate);
	}

	public AnswerType getAnswerType() {
	return answerType;
	}

	public void setAnswerType(final AnswerType answerType) {
	this.answerType = answerType;
	if (!selectedQuestion.getAnswerType().equals(answerType) && UtilValidator.isNotEmpty(selectedQuestion.getAnswers())) {
		UtilMessagesAndPopups.confirm("Click 'Yes' to remove all existing Answers.", "Information", Messagebox.YES
				| Messagebox.NO, Messagebox.QUESTION, new EventListener() {
			public void onEvent(Event event) {
			if ("onYes".equals(event.getName())) {
				QASetupComposer.this.answers.clear();
				QASetupComposer.this.selectedQuestion.getAnswers().clear();
				if (AnswerType.SINGLE_CHOICE.equals(answerType))
					Events.postEvent("onReload", event.getTarget().getFellow("answerlist", true), null);
				if (AnswerType.MULTIPLE_CHOICE.equals(answerType))
					Events.postEvent("onReload", event.getTarget().getFellow("multipleAnswersList", true), null);
				if (AnswerType.YES_NO.equals(answerType))
					Events.postEvent("onReload", event.getTarget().getFellow("yesNoAnswersList", true), null);
			}
			}
		});
	}
	}

	public void removeAnswer(Listitem li) {
	Answer answer = (Answer) li.getValue();
	selectedQuestion.getAnswers().remove(answer);
	commonCrudService.delete(answer);
	li.detach();
	}

	public List<Question> getQuestions() {
	return commonCrudService.getQuestions(selectedQATemplate, selectedOrganSystem);
	}

	public List<Question> getOrganQuestions() {
	if (selectedOrganSystem != null && selectedQATemplate != null)
		return commonCrudService.getQuestions(selectedQATemplate, selectedOrganSystem);
	return Collections.emptyList();
	}

	public boolean isQuestionAnswerConfigured() {
	if (UtilValidator.isNotEmpty(commonCrudService.findByEquality(PatientRosQA.class, new String[] { "organSystem" },
			new Object[] { selectedOrganSystem }))
			|| UtilValidator.isNotEmpty(commonCrudService.findByEquality(PatientExamination.class,
					new String[] { "organSystem" }, new Object[] { selectedOrganSystem }))) return true;
	return false;
	}

	private static final long serialVersionUID = 1L;
}
