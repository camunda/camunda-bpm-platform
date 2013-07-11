package org.camunda.bpm.engine.rest.dto.job;

public class JobDeleteMessageDto {

	private String exceptionMessage;

	public static JobDeleteMessageDto fromMessage(String message){
		JobDeleteMessageDto jobDeleteMessageDto = new JobDeleteMessageDto();
		jobDeleteMessageDto.exceptionMessage = message;
		return jobDeleteMessageDto;
	}
	
	public String getExceptionMessage() {
		return exceptionMessage;
	}
	
}
