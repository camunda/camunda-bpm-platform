package org.camunda.bpm.engine.rest.dto.history;

import java.util.Date;

public class HistoryCleanupConfigurationDto {

	protected Date batchWindowStartTime;
	protected Date batchWindowEndTime;

	public Date getBatchWindowStartTime() {
		return batchWindowStartTime;
	}

	public void setBatchWindowStartTime(Date batchWindowStartTime) {
		this.batchWindowStartTime = batchWindowStartTime;
	}

	public Date getBatchWindowEndTime() {
		return batchWindowEndTime;
	}

	public void setBatchWindowEndTime(Date batchWindowEndTime) {
		this.batchWindowEndTime = batchWindowEndTime;
	}

}
