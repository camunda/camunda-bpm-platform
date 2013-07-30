package org.camunda.bpm.engine.rest.dto.history;

import org.camunda.bpm.engine.history.HistoricProcessInstance;

public class HistoricProcessInstanceDto {

	  private String id;
	  private String businessKey;
	  private String processDefinitionId;
	  private String startTime;
	  private String endTime;
	  private Long durationInMillis;
	  private String startUserId;
	  private String startActivityId;
	  private String deleteReason;
	  private String superProcessInstanceId;
	
	public static HistoricProcessInstanceDto fromHistoricProcessInstance(
			HistoricProcessInstance historicProcessInstance) {
		HistoricProcessInstanceDto dto = new HistoricProcessInstanceDto();
		  dto.id = historicProcessInstance.getId();
		  dto.businessKey = historicProcessInstance.getBusinessKey();
		  dto.processDefinitionId = historicProcessInstance.getProcessDefinitionId();
		  dto.startTime = historicProcessInstance.getStartTime().toString();
		  if(historicProcessInstance.getEndTime() != null) {
			  dto.endTime = historicProcessInstance.getEndTime().toString();	
		  } else {
			  dto.endTime = null;	
		  }		 
		  dto.durationInMillis = historicProcessInstance.getDurationInMillis();
		  dto.startUserId = historicProcessInstance.getStartUserId();
		  dto.startActivityId = historicProcessInstance.getStartActivityId();
		  dto.deleteReason = historicProcessInstance.getDeleteReason();
		  dto.superProcessInstanceId = historicProcessInstance.getSuperProcessInstanceId();		
		return dto;
	}

	public String getId() {
		return id;
	}

	public String getBusinessKey() {
		return businessKey;
	}

	public String getProcessDefinitionId() {
		return processDefinitionId;
	}

	public String getStartTime() {
		return startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public Long getDurationInMillis() {
		return durationInMillis;
	}

	public String getStartUserId() {
		return startUserId;
	}

	public String getStartActivityId() {
		return startActivityId;
	}

	public String getDeleteReason() {
		return deleteReason;
	}

	public String getSuperProcessInstanceId() {
		return superProcessInstanceId;
	}

}
