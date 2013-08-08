package org.camunda.bpm.engine.rest.dto;

public class BulkCommandExceptionDto {

	protected String id;
	protected String exceptionMessage;
	
	public String getId() {
		return id;
	}

	public String getExceptionMessage() {
		return exceptionMessage;
	}
		
	public static BulkCommandExceptionDto fromExceptionDetails(String id, String message) {
		BulkCommandExceptionDto dto = new BulkCommandExceptionDto();
		dto.id = id;
		dto.exceptionMessage = message;
		return dto;
	}
	
}
