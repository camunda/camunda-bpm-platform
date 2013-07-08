package org.camunda.bpm.engine.rest.dto.job;

import org.camunda.bpm.engine.runtime.Job;

public class JobDto {

	private String jobId;
	private String processInstanceId;
	private String executionId;
	private String exceptionMessage;
	private int retries;
	private String dueDate;
	
	public static JobDto fromJob(Job job) {
		 JobDto dto = new JobDto();
		 dto.jobId = job.getId();
		 dto.processInstanceId = job.getProcessInstanceId();
		 dto.executionId = job.getExecutionId();
		 dto.exceptionMessage = job.getExceptionMessage();
		 dto.retries = job.getRetries();
		 dto.dueDate = job.getDuedate().toString();	  
		 return dto;
	 }

	public String getJobId() {
		return jobId;
	}

	public String getProcessInstanceId() {
		return processInstanceId;
	}

	public String getExecutionId() {
		return executionId;
	}

	public String getExceptionMessage() {
		return exceptionMessage;
	}

	public int getRetries() {
		return retries;
	}

	public String getDueDate() {
		return dueDate;
	}	

	
}
