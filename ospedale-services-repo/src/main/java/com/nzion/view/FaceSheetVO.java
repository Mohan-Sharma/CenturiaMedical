package com.nzion.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.nzion.domain.base.BaseEntity;
import com.nzion.domain.emr.soap.SoapSection;
import com.nzion.util.UtilDateTime;
import com.nzion.util.UtilReflection;

public class FaceSheetVO extends HashMap<String,String> {
	
	private static final long serialVersionUID = 1L;

	public static FaceSheetVO create(Object entity, String... fields){
	FaceSheetVO faceSheetVO = new FaceSheetVO();
	for(String field : fields){
		Object value = UtilReflection.getNestedFieldValue(entity, field);
		String valueAsString = value == null ? "" : value instanceof Date ? UtilDateTime.format((Date) value) : value.toString();
		faceSheetVO.put(field.replaceAll("\\.", ""), valueAsString);
	}
	return faceSheetVO;
	}
	
	public static List<FaceSheetVO> create(SoapSection soapSection, Collection<? extends BaseEntity> soapRecords, String ...fields){
	List<FaceSheetVO> faceSheetVOs = new ArrayList<FaceSheetVO>();
	for(BaseEntity soapRecord : soapRecords){
		FaceSheetVO vo = create(soapRecord, fields);
		vo.put("visitDate", UtilDateTime.format(soapSection.getSoapNote().getDate()));
		faceSheetVOs.add(vo);
	}
	return faceSheetVOs;
	}
	
	public static FaceSheetVO create(SoapSection section,BaseEntity soapRecord,String ...fields){
	FaceSheetVO vo = create(soapRecord, fields);
	vo.put("visitDate", UtilDateTime.format(section.getSoapNote().getDate()));
	return vo;
	}
}