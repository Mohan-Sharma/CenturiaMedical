package com.nzion.zkoss.composer.emr;

import org.apache.commons.lang.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Textbox;

import com.nzion.domain.emr.Answer;
import com.nzion.domain.emr.Question;
import com.nzion.domain.emr.soap.PatientQuestionAnswer;
import com.nzion.util.Constants;

/**
 * @author Sandeep Prusty
 * Dec 28, 2010
 */
public interface AnswerRenderer {
	
	void renderAnswer(Question question, Component parent, PatientQuestionAnswer patientQuestionAnswer);
	
	AnswerRenderer FREE_TEXT_RENDERER = new AnswerRenderer() {
		public void renderAnswer(Question question, Component parent, final PatientQuestionAnswer patientQuestionAnswer) {
		final Textbox textbox = new Textbox(patientQuestionAnswer.getAnswerString());
		textbox.setRows(4);
		textbox.setCols(60);
		textbox.setParent(parent);
		textbox.addEventListener("onChange", new EventListener() {
			@Override
			public void onEvent(Event event) throws Exception {
			patientQuestionAnswer.setAnswerString(textbox.getValue());
			}
		});
		}
	};

	AnswerRenderer YES_NO_RENDERER = new AnswerRenderer() {
		public void renderAnswer(final Question question, Component parent, final PatientQuestionAnswer patientQuestionAnswer) {
		Radiogroup radiogroup = new Radiogroup();
		radiogroup.setParent(parent);
		Radio yesRadio = new Radio(Constants.YES);
		yesRadio.setParent(radiogroup);
		yesRadio.setChecked(Constants.YES.equals(patientQuestionAnswer.getAnswerString()));
		Radio noRadio = new Radio(Constants.NO);
		noRadio.setParent(radiogroup);
		noRadio.setChecked(Constants.NO.equals(patientQuestionAnswer.getAnswerString()));
		radiogroup.addEventListener("onCheck", new EventListener() {
			@Override
			public void onEvent(Event event) throws Exception {
			String answerString = ((Radio)event.getTarget()).getLabel();
			patientQuestionAnswer.setAnswerString(answerString);
			Answer answer = question.retrieveAnswer(answerString);
			if(answer != null)
				patientQuestionAnswer.setSentence(answer.getSentence());
			}
		});
		}
	};

	AnswerRenderer SINGLE_CHOICE_RENDERER = new AnswerRenderer() {
		public void renderAnswer(Question question, Component parent, final PatientQuestionAnswer patientQuestionAnswer) {
		final Combobox combobox = new Combobox();
		combobox.setParent(parent);
		for (Answer answer : question.getAnswers()) {
			Comboitem comboitem = new Comboitem(answer.getName());
			comboitem.setValue(answer);
			comboitem.setParent(combobox);
		}
		combobox.setValue(patientQuestionAnswer.getAnswerString());
		combobox.addEventListener("onChange", new EventListener() {
			@Override
			public void onEvent(Event event) throws Exception {
			patientQuestionAnswer.setAnswerString(combobox.getValue());
			patientQuestionAnswer.setSentence(((Answer)combobox.getSelectedItem().getValue()).getSentence());
			}
		});
		}
	};
	
	AnswerRenderer MULTIPLE_CHOICE_RENDERER = new AnswerRenderer() {
		public void renderAnswer(final Question question, Component parent, final PatientQuestionAnswer patientQuestionAnswer) {
			for(Answer answer : question.getAnswers()){
				final Checkbox checkbox = new Checkbox(answer.getName());
				checkbox.setParent(parent);
				checkbox.setAttribute("answer", answer);
				checkbox.setChecked(patientQuestionAnswer.getAnswerString() != null && (patientQuestionAnswer.getAnswerString().indexOf(answer.getName()) != -1));
				checkbox.addEventListener("onCheck", new EventListener() {
					@Override
					public void onEvent(Event event) throws Exception {
					CheckEvent checkEvent = (CheckEvent)event;
					patientQuestionAnswer.appendAnswerString(checkbox.getLabel(), checkEvent.isChecked());
					patientQuestionAnswer.setSentence(((Answer)checkEvent.getTarget().getAttribute("answer")).getSentence());
					}
				});
			}
			return;
		}
	};
}