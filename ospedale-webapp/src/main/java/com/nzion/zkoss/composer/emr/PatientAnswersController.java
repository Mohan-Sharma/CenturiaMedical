package com.nzion.zkoss.composer.emr;

import java.util.HashMap;
import java.util.Map;

import com.nzion.zkoss.composer.OspedaleAutowirableComposer;
import org.zkoss.zk.ui.Component;

import com.nzion.domain.emr.Question;
import com.nzion.domain.emr.soap.PatientQuestionAnswerBindingFactory;
import com.nzion.enums.AnswerType;

/**
 * @author Sandeep Prusty
 * Dec 28, 2010
 */
public class PatientAnswersController extends OspedaleAutowirableComposer {
	
	final private PatientQuestionAnswerBindingFactory factory;
	
	public PatientAnswersController(PatientQuestionAnswerBindingFactory factory) {
	this.factory = factory;
	}
	
	public void renderAnswerInput(Component parent){
	final Question question = (Question)parent.getAttribute("question");
	AnswerRenderer renderer = RENDERERS.get(question.getAnswerType()); 
	if(question.getAnswerType()!=null)
	renderer.renderAnswer(question, parent, factory.getPatientQuestionAnswerFor(question));
	}
	
	private static final Map<AnswerType, AnswerRenderer> RENDERERS = new HashMap<AnswerType, AnswerRenderer>();
	
	static{
		RENDERERS.put(AnswerType.FREE_TEXT, AnswerRenderer.FREE_TEXT_RENDERER);
		RENDERERS.put(AnswerType.SINGLE_CHOICE, AnswerRenderer.SINGLE_CHOICE_RENDERER);
		RENDERERS.put(AnswerType.MULTIPLE_CHOICE, AnswerRenderer.MULTIPLE_CHOICE_RENDERER);
		RENDERERS.put(AnswerType.YES_NO, AnswerRenderer.YES_NO_RENDERER);
	}
	
	private static final long serialVersionUID = 1L;
}