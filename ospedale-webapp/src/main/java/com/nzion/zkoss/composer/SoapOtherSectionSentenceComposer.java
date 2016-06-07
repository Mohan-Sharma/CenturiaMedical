package com.nzion.zkoss.composer;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zkplus.databind.AnnotateDataBinder;

public class SoapOtherSectionSentenceComposer extends GenericForwardComposer{
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
	super.doAfterCompose(comp);
	AnnotateDataBinder binder = new AnnotateDataBinder(comp);
	binder.loadAll();
	}
	
}
