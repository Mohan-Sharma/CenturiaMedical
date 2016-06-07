package com.nzion.service;

import java.lang.reflect.Field;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Component;

import com.nzion.domain.Patient;
import com.nzion.domain.emr.soap.PatientVitalSign;
import com.nzion.domain.emr.soap.RuleCondition;
import com.nzion.domain.emr.soap.VitalSignRule;
import com.nzion.repository.common.CommonCrudRepository;

@Component(value = "ruleService")
public class RuleServiceImpl {

	private CommonCrudRepository commonCrudRepository;

	@Resource
	@Required
	public void setCommonCrudRepository(CommonCrudRepository commonCrudRepository) {
	this.commonCrudRepository = commonCrudRepository;
	}

	public CommonCrudRepository getCommonCrudRepository() {
	return commonCrudRepository;
	}

	public void saveVitalSignRule(VitalSignRule rule) {
	commonCrudRepository.save(rule);
	}

	public boolean checkVitalSign(Patient patient, PatientVitalSign pVitalSign) {
	VitalSignRule vRule = new VitalSignRule();
	vRule.setVitalSign(pVitalSign.getVitalSign());
	List<VitalSignRule> rules = commonCrudRepository.simulateExampleSearch(new String[] { "vitalSign" }, vRule);
	for (VitalSignRule rule : rules) {
		try {
			return executeRule(patient, rule, pVitalSign.getValue());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	return false;
	}

	private boolean executeRule(Patient patient, VitalSignRule rule, String value) throws Exception {
	boolean output = true;
	for (RuleCondition each : rule.getConditions()) {
		Class<?> klass = Class.forName(each.getDomainName());
		Field field = klass.getField(each.getColumnName());
		String s = field.get(patient).toString();
		output &= each.getOperator().evaluate(each.getRhs(), each.getLhs(), s);
	}
	return output;
	}
	
	public static void main(String[] args){
		Patient p = new Patient();
	}
}