package com.nzion.domain.emr.soap;

import java.util.List;
import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.nzion.domain.File;
import com.nzion.domain.emr.SoapModule;
import com.nzion.util.UtilValidator;

@Entity
@DiscriminatorValue("IMAGE")
@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="com.nzion.domain")
public class ImageSection extends SoapSection {

	private static final long serialVersionUID = 1L;
	
	List<File> files;

	@OneToMany(targetEntity = File.class,fetch=FetchType.EAGER,orphanRemoval = true)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "IMAGE_SECTION_ID")
	public List<File> getFiles() {
	return files;
	}

	public void setFiles(List<File> files) {
	this.files = files;
	}

	@Override
	public void init(PatientSoapNote soapNote, SoapModule module, PatientSoapNote lastEncounter,
			Map<String, SoapSection> previousSoapSections) {

	}

	@Override
	public boolean edited() {
	if(UtilValidator.isEmpty(files))
		return false;
	return true;
	}
	
	public static final String MODULE_NAME = "Image";

}
