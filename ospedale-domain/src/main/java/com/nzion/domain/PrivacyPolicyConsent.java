package com.nzion.domain;

import com.nzion.domain.base.IdGeneratingBaseEntity;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by Mohan Sharma on 7/3/2015.
 */
@Entity
public class PrivacyPolicyConsent implements Serializable{
    private Long id;
    private String question;

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    @Lob
    @Column(columnDefinition = "text")
    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PrivacyPolicyConsent)) return false;

        PrivacyPolicyConsent that = (PrivacyPolicyConsent) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (question != null ? !question.equals(that.question) : that.question != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (question != null ? question.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return question;
    }
}
