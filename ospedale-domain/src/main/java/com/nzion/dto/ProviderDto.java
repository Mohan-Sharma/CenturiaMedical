package com.nzion.dto;

import com.nzion.domain.DataResource;
import com.nzion.domain.Provider;
import com.nzion.domain.Speciality;
import com.nzion.domain.emr.SpokenLanguage;
import com.nzion.domain.emr.Certification;
import com.nzion.util.UtilValidator;
import org.apache.commons.lang.SerializationUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Mohan Sharma on 6/4/2015.
 */
public class ProviderDto {
    private String doctorId;
    private String salutation;
    private String firstName;
    private String lastName;
    private String clinicName;
    private String qualification;
    private String address;
    private String additionalAddress;
    private String city;
    private String state;
    private String country;
    private String zip;
    private Set<SpecialityDto> specialities = new HashSet<SpecialityDto>();
    private Set<SpokenLanguageDto> spokenLanguages = new HashSet<SpokenLanguageDto>();
    private Set<CertificationDto> certifications = new HashSet<CertificationDto>();
    private byte[] profilePicture;
    private long resourceId;
    private String visitingHours;
    private String specialityToDisplayInPortal;

    private String nationality;
    private String experience;
    private String priceRange;
    private String keyword;
    private String gender;
    private String mobileNo;

    private String userName;
    private String emailId;

    private String expertise;

    private String qualificationAr;

    private String disease;

    private String diseaseAr;

    private String expertiseAr;

    private String certificationAr;

    private String certificationEng;

    public byte[] getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }

    public long getResourceId() {
        return resourceId;
    }

    public void setResourceId(long resourceId) {
        this.resourceId = resourceId;
    }

    public Set<SpecialityDto> getSpecialities() {
        return specialities;
    }

    public void setSpecialities(Set<SpecialityDto> specialities) {
        this.specialities = specialities;
    }

    public Set<SpokenLanguageDto> getSpokenLanguages() {
        return spokenLanguages;
    }

    public void setSpokenLanguages(Set<SpokenLanguageDto> spokenLanguages) {
        this.spokenLanguages = spokenLanguages;
    }

    public Set<CertificationDto> getCertifications() {
        return certifications;
    }

    public void setCertifications(Set<CertificationDto> certifications) {
        this.certifications = certifications;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getSalutation() {
        return salutation;
    }

    public void setSalutation(String salutation) {
        this.salutation = salutation;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getClinicName() {
        return clinicName;
    }

    public void setClinicName(String clinicName) {
        this.clinicName = clinicName;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAdditionalAddress() {
        return additionalAddress;
    }

    public void setAdditionalAddress(String additionalAddress) {
        this.additionalAddress = additionalAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getVisitingHours() {
        return visitingHours;
    }

    public void setVisitingHours(String visitingHours) {
        this.visitingHours = visitingHours;
    }

    public String getSpecialityToDisplayInPortal() {
        return specialityToDisplayInPortal;
    }

    public void setSpecialityToDisplayInPortal(String specialityToDisplayInPortal) {
        this.specialityToDisplayInPortal = specialityToDisplayInPortal;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getPriceRange() {
        return priceRange;
    }

    public void setPriceRange(String priceRange) {
        this.priceRange = priceRange;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getExpertise() {
        return expertise;
    }

    public void setExpertise(String expertise) {
        this.expertise = expertise;
    }

    public String getQualificationAr() {
        return qualificationAr;
    }

    public void setQualificationAr(String qualificationAr) {
        this.qualificationAr = qualificationAr;
    }

    public String getDisease() {
        return disease;
    }

    public void setDisease(String disease) {
        this.disease = disease;
    }

    public String getDiseaseAr() {
        return diseaseAr;
    }

    public void setDiseaseAr(String diseaseAr) {
        this.diseaseAr = diseaseAr;
    }

    public String getExpertiseAr() {
        return expertiseAr;
    }

    public void setExpertiseAr(String expertiseAr) {
        this.expertiseAr = expertiseAr;
    }

    public String getCertificationAr() {
        return certificationAr;
    }

    public void setCertificationAr(String certificationAr) {
        this.certificationAr = certificationAr;
    }

    public String getCertificationEng() {
        return certificationEng;
    }

    public void setCertificationEng(String certificationEng) {
        this.certificationEng = certificationEng;
    }

    public void setPropertiesToProviderDto(Provider provider, String practiceName) {
        Set<SpecialityDto> specialityDtos = new HashSet<SpecialityDto>();
        SpecialityDto specialityDto = null;
        for (Speciality speciality : provider.getSpecialities()) {
            specialityDto = new SpecialityDto(speciality.getCode(), speciality.getDescription());
            specialityDtos.add(specialityDto);
        }

        Set<SpokenLanguageDto> spokenLanguageyDtos = new HashSet<SpokenLanguageDto>();
        SpokenLanguageDto spokenLanguageDto = null;
        if (provider.getSpokenLanguages() != null) {
            for (SpokenLanguage spokenLanguage : provider.getSpokenLanguages()) {
                spokenLanguageDto = new SpokenLanguageDto(spokenLanguage.getName(), spokenLanguage.getDescription());
                spokenLanguageyDtos.add(spokenLanguageDto);
            }
        }

        Set<CertificationDto> certificationDtos = new HashSet<CertificationDto>();
        CertificationDto certificationDto = null;
        if (provider.getCertification() != null) {
            for (Certification certification : provider.getCertification()) {
                certificationDto = new CertificationDto(certification.getName(), certification.getDescription());
                certificationDtos.add(certificationDto);
            }
        }
        this.doctorId = (String.valueOf(provider.getId()));
        this.salutation = provider.getSalutation();
        this.firstName = provider.getFirstName();
        this.lastName = provider.getLastName();
        //this.qualification = provider.getQualifications();
        this.qualification = provider.getQualificationEng();
        this.address = provider.getContacts() != null ? provider.getContacts().getPostalAddress() != null ? provider.getContacts().getPostalAddress().getAddress1() : null : null;
        this.additionalAddress = provider.getContacts() != null ? provider.getContacts().getPostalAddress() != null ? provider.getContacts().getPostalAddress().getAddress2() : null : null;
        this.city = provider.getContacts() != null ? provider.getContacts().getPostalAddress() != null ? provider.getContacts().getPostalAddress().getCity() : null : null;
        this.state = provider.getContacts() != null ? provider.getContacts().getPostalAddress() != null ? provider.getContacts().getPostalAddress().getStateProvinceGeo() : null : null;
        this.country = provider.getContacts() != null ? provider.getContacts().getPostalAddress() != null ? provider.getContacts().getPostalAddress().getCountryGeo() : null : null;
        this.zip = provider.getContacts() != null ? provider.getContacts().getPostalAddress() != null ? provider.getContacts().getPostalAddress().getZipCode() : null : null;
        this.specialities = specialityDtos;
        this.spokenLanguages = spokenLanguageyDtos;
        this.certifications = certificationDtos;
        this.clinicName = practiceName;
        this.visitingHours = provider.getVisitingHours();
        if (UtilValidator.isNotEmpty(provider.getProfilePicture())) {
            this.resourceId = provider.getProfilePicture().getResourceId();
            this.profilePicture = getBytesForBlob(provider.getProfilePicture().getResource());
        }
        this.setSpecialityToDisplayInPortal(provider.getSpecialityToDisplayInPortal());

        this.setPriceRange(provider.getPriceRange());
        this.setExperience(provider.getExperience());
        this.setKeyword(provider.getKeyword());
        if (provider.getNationality() != null) {
            this.setNationality(provider.getNationality().getName());
        }
        if (provider.getGender() != null) {
            this.setGender(provider.getGender().description);
        }
        this.mobileNo = provider.getContacts() != null ? provider.getContacts().getMobileNumber() : null;

        this.userName = provider.getUserLogin() != null ? provider.getUserLogin().getUsername() : null;
        this.emailId = provider.getContacts() != null ? provider.getContacts().getEmail() : null;

        this.expertise = provider.getExpertise();

        this.expertiseAr = provider.getExpertiseAr();

        this.qualificationAr = provider.getQualificationAr();

        this.disease = provider.getDisease();

        this.diseaseAr = provider.getDiseaseAr();

        this.certificationAr = provider.getCertificationAr();

        this.certificationEng = provider.getCertificationEng();
    }

    private byte[] getBytesForBlob(Blob blob) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        InputStream in = null;
        try {
            in = blob.getBinaryStream();
            int n = 0;
            while ((n=in.read(buf)) >= 0){
                baos.write(buf, 0, n);
            }
            in.close();
            return baos.toByteArray();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buf;
    }

    private class SpecialityDto {
        String specialityCode;
        String description;

        public SpecialityDto(String specialityCode, String description) {
            this.specialityCode = specialityCode;
            this.description = description;
        }
    }
    private class CertificationDto {
        String certificationName;
        String description;

        public CertificationDto(String certificationName, String description) {
            this.certificationName = certificationName;
            this.description = description;
        }
    }
    private class SpokenLanguageDto {
        String SpokenLanguageName;
        String description;

        public SpokenLanguageDto(String SpokenLanguageName, String description) {
            this.SpokenLanguageName = SpokenLanguageName;
            this.description = description;
        }
    }
}
