package com.nzion.domain.screen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nzion.domain.Schedule.STATUS;
import com.nzion.domain.ScheduleStatusConfig;

/**
 * @author Sandeep Prusty
 * May 13, 2010
 */

public class ScheduleConfig  {

	private List<ScheduleStatusConfig> statusConfigs;
	
	private transient Map<STATUS, ScheduleStatusConfig> statusConfigMap;
	
	public List<ScheduleStatusConfig> getStatusConfigs() {
	return statusConfigs;
	}

	public void setStatusConfigs(List<ScheduleStatusConfig> statusConfigs) {
	this.statusConfigs = statusConfigs;
	}

	public Map<STATUS, ScheduleStatusConfig> getStatusConfigMap() {
	if(statusConfigMap == null){
		statusConfigMap = new HashMap<STATUS, ScheduleStatusConfig>();
		for(ScheduleStatusConfig statusConfig : statusConfigs)
			statusConfigMap.put(statusConfig.getStatus(), statusConfig);
	}
	return statusConfigMap;
	}

	public String getColor(STATUS status){
	if(status == null)
		return "";
	if(getStatusConfigMap().get(status) == null)
		return "";
	return getStatusConfigMap().get(status).getColor();
	}
	
	private static final long serialVersionUID = 1L;
}