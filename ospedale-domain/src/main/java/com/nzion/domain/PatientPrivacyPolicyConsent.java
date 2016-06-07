package com.nzion.domain;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by Mohan Sharma on 7/3/2015.
 */
@Entity
@Table(name = "PATIENT_PRIVACY_POLICY_CONSENT_ASSOC")
public class PatientPrivacyPolicyConsent implements Serializable{
    private Long id;
    private PrivacyPolicyConsent privacyPolicyConsent;
    private boolean required = Boolean.FALSE;

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @OneToOne
    @JoinColumn(name = "PRIVACY_POLICY_CONSENT_ID")
    public PrivacyPolicyConsent getPrivacyPolicyConsent() {
        return privacyPolicyConsent;
    }

    public void setPrivacyPolicyConsent(PrivacyPolicyConsent privacyPolicyConsent) {
        this.privacyPolicyConsent = privacyPolicyConsent;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PatientPrivacyPolicyConsent)) return false;

        PatientPrivacyPolicyConsent that = (PatientPrivacyPolicyConsent) o;

        if (required != that.required) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (privacyPolicyConsent != null ? !privacyPolicyConsent.equals(that.privacyPolicyConsent) : that.privacyPolicyConsent != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (privacyPolicyConsent != null ? privacyPolicyConsent.hashCode() : 0);
        result = 31 * result + (required ? 1 : 0);
        return result;
    }
}
