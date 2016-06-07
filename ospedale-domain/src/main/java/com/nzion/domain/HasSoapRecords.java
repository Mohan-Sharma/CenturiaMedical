package com.nzion.domain;

import java.util.Collection;

import com.nzion.domain.emr.soap.SoapAnchorEntity;

public interface HasSoapRecords {

	Collection<? extends SoapAnchorEntity> soapRecords();
}
