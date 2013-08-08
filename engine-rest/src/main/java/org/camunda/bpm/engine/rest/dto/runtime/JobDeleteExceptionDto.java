package org.camunda.bpm.engine.rest.dto.runtime;

public class JobDeleteExceptionDto {

	private String jobId;
    private String exceptionMessage;

	public static JobDeleteExceptionDto fromExceptionDetails(String jobId, String message){
		JobDeleteExceptionDto dto = new JobDeleteExceptionDto();
		dto.jobId = jobId;
		dto.exceptionMessage = message;
		return dto;
	}
	
	public String getJobId() {
		return jobId;
	}
	
	public String getExceptionMessage() {
		return exceptionMessage;
	}
	
}
