package org.camunda.bpm.engine.rest.dto.history;

import org.camunda.bpm.engine.history.HistoricActivityInstance;

public class HistoricActivityInstanceDto {
	
	  private String id;
	  private String activityId;
	  private String activityName;
	  private String activityType;
	  private String processDefinitionId;
	  private String processInstanceId;
	  private String executionId;
	  private String taskId;
	  private String calledProcessInstanceId;
	  private String assignee;
	  private String startTime;
	  private String endTime;
	  private Long durationInMillis;
	
	public static HistoricActivityInstanceDto fromHistoricActivityInstance(
		HistoricActivityInstance historicActivityInstance) {
		HistoricActivityInstanceDto dto = new HistoricActivityInstanceDto();
		dto.id = historicActivityInstance.getId();
		dto.activityId = historicActivityInstance.getActivityId();
		dto.activityName = historicActivityInstance.getActivityName();	
		dto.activityType = historicActivityInstance.getActivityType();
		dto.processDefinitionId = historicActivityInstance.getProcessDefinitionId();
		dto.processInstanceId = historicActivityInstance.getProcessInstanceId();
		dto.executionId = historicActivityInstance.getExecutionId();
		dto.taskId = historicActivityInstance.getTaskId();
		dto.calledProcessInstanceId = historicActivityInstance.getCalledProcessInstanceId();
		dto.assignee = historicActivityInstance.getAssignee();
		dto.startTime = historicActivityInstance.getStartTime().toString();
		dto.endTime = historicActivityInstance.getEndTime().toString();	
		dto.durationInMillis = historicActivityInstance.getDurationInMillis();
		return dto;
	}
	
	public String getId() {
		return id;
	}

	public String getActivityId() {
		return activityId;
	}

	public String getActivityName() {
		return activityName;
	}

	public String getActivityType() {
		return activityType;
	}

	public String getProcessDefinitionId() {
		return processDefinitionId;
	}

	public String getProcessInstanceId() {
		return processInstanceId;
	}

	public String getExecutionId() {
		return executionId;
	}

	public String getTaskId() {
		return taskId;
	}

	public String getCalledProcessInstanceId() {
		return calledProcessInstanceId;
	}

	public String getAssignee() {
		return assignee;
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
}
