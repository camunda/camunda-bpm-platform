package org.camunda.bpm.engine.rest.dto.runtime;

public class SuspensionStateDto {

	protected boolean suspended;	
	
	public boolean getSuspended() {
		return suspended;
	}

	public void setState(boolean suspended) {
		this.suspended = suspended;
	}
	
	public static SuspensionStateDto fromState(boolean suspended) {
		SuspensionStateDto dto = new SuspensionStateDto();
		dto.suspended = suspended;
		return dto;
	}	
}
