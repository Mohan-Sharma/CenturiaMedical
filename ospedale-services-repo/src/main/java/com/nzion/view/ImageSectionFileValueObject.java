package com.nzion.view;

import java.util.Date;

import com.nzion.domain.File;

public class ImageSectionFileValueObject {
	
	private File file;
	
	private Date visitDate;
	
	public ImageSectionFileValueObject(){
	}

	public ImageSectionFileValueObject(File file,Date visitDate){
	this.file = file;
	this.visitDate = visitDate;
	}
	
	public File getFile() {
	return file;
	}

	public void setFile(File file) {
	this.file = file;
	}

	public Date getVisitDate() {
	return visitDate;
	}

	public void setVisitDate(Date visitDate) {
	this.visitDate = visitDate;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ImageSectionFileValueObject)) return false;

		ImageSectionFileValueObject that = (ImageSectionFileValueObject) o;

		if (file != null ? !file.equals(that.file) : that.file != null) return false;
		if (visitDate != null ? !visitDate.equals(that.visitDate) : that.visitDate != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = file != null ? file.hashCode() : 0;
		result = 31 * result + (visitDate != null ? visitDate.hashCode() : 0);
		return result;
	}
}
