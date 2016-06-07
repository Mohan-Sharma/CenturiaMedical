package com.nzion.view.component;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import com.nzion.domain.*;
import com.nzion.util.RestServiceConsumer;
import com.nzion.util.UtilValidator;
import org.zkoss.bind.BindContext;
import org.zkoss.zk.ui.Component;
import org.zkoss.zkplus.databind.TypeConverter;
import org.zkoss.zul.Image;

import com.nzion.service.common.CommonCrudService;
import com.nzion.util.Infrastructure;

public class GenderImageConverter implements TypeConverter ,org.zkoss.bind.Converter{

    private static CommonCrudService commonCrudService = Infrastructure.getSpringBean("commonCrudService");

    public Object coerceToBean(java.lang.Object val, org.zkoss.zk.ui.Component comp) {
        return null;
    }
    /*
    * Modified To read consents from portal by Mohan Sharma 22 Aug 15
    * */
    public static void coerceToUiStatic(java.lang.Object val, org.zkoss.zk.ui.Component comp) {
        if (val == null) {
            ((Image) comp).setSrc("/images/blank.gif");
            return;
        }
        if(val instanceof Provider) {
            if(UtilValidator.isEmpty(((Person) val).getId())){
                ((Image) comp).setSrc("/images/blank-male.gif");
                return;
            }
            Person person = commonCrudService.getById(Person.class, ((Person) val).getId());
            if(UtilValidator.isNotEmpty(person.getProfilePicture())) {
                try {
                    ((Image) comp).setContent(new org.zkoss.image.AImage("Patient Image", person.getProfilePicture().getResource().getBinaryStream()));
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else {
                Provider provider = (Provider) val;
                if (provider.getGender() != null) {
                    if ("M".equals(provider.getGender().getEnumCode()))
                        ((Image) comp).setSrc("/images/blank-male.gif");
                    return;
                } else {
                    ((Image) comp).setSrc("/images/blank.gif");
                    return;
                }
            }
        }
        if(val instanceof Patient) {
            Patient patient = (Patient) val;
            Person person = commonCrudService.getById(Person.class, ((Person) val).getId());
            //&& !"F".equals(person.getGender().getEnumCode())
            if (person.getProfilePicture() != null && (person.getGender() != null)) {
                try {
                    String q = "Can we show your photo along with your user profile in the application?";
                    boolean displayImage = Boolean.FALSE;
                    //Set<PatientPrivacyPolicyConsent> patientPrivacyPolicyConsentSet = ((Patient)person).getPatientPrivacyPolicyConsents();
                    List<Map<String, Object>> patientPrivacyPolicyConsents = RestServiceConsumer.getPatientPrivacyPolicyConsents(patient.getAfyaId());
                    for (Map<String, Object> patientPrivacyPolicyConsent : patientPrivacyPolicyConsents) {
                        if (patientPrivacyPolicyConsent.get("question").equals(q)) {
                            if (UtilValidator.isEmpty(patientPrivacyPolicyConsent.get("answer"))) {
                                displayImage = Boolean.FALSE;
                                return;
                            }
                            displayImage = Boolean.valueOf(patientPrivacyPolicyConsent.get("answer").toString());
                        }
                    }
                    if (displayImage) {
                        ((Image) comp).setContent(new org.zkoss.image.AImage("Patient Image", person.getProfilePicture()
                                .getResource().getBinaryStream()));
                    } else {
                        ((Image) comp).setSrc("/images/blank.gif");
                    }
                    return;
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                if (person.getGender() != null) {
                    if ("M".equals(person.getGender().getEnumCode()))
                        ((Image) comp).setSrc("/images/blank-male.gif");
                    return;
                    //else
                    //	if ("F".equals(person.getGender().getEnumCode())) ((Image) comp).setSrc("/images/blank-female.gif");
                } else {
                    ((Image) comp).setSrc("/images/blank.gif");
                    return;
                }
            }
        }
    }

    private static Set<PatientPrivacyPolicyConsent> constructDefaultPatientPrivacyPolicyConsent() {
        Set<PatientPrivacyPolicyConsent> patientPrivacyPolicyConsentSet = new HashSet<>();
        for(PrivacyPolicyConsent question : commonCrudService.getAll(PrivacyPolicyConsent.class)){
            PatientPrivacyPolicyConsent patientPrivacyPolicyConsent = new PatientPrivacyPolicyConsent();
            patientPrivacyPolicyConsent.setPrivacyPolicyConsent(question);
            patientPrivacyPolicyConsentSet.add(patientPrivacyPolicyConsent);
        }
        return patientPrivacyPolicyConsentSet;
    }

    public Object coerceToUi(java.lang.Object val, org.zkoss.zk.ui.Component comp) {
        coerceToUiStatic(val, comp);
        return null;
    }

    @Override
    public Object coerceToUi(Object val, Component component, BindContext bindContext) {
        coerceToUiStatic(val, component);
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object coerceToBean(Object o, Component component, BindContext bindContext) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
