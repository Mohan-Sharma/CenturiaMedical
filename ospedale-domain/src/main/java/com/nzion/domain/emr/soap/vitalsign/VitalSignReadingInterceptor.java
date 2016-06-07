package com.nzion.domain.emr.soap.vitalsign;

import org.hibernate.event.SaveOrUpdateEvent;
import org.hibernate.event.def.DefaultSaveOrUpdateEventListener;

public class VitalSignReadingInterceptor extends DefaultSaveOrUpdateEventListener{

	@Override
	public void onSaveOrUpdate(SaveOrUpdateEvent event) {
	super.onSaveOrUpdate(event);
	}

}
