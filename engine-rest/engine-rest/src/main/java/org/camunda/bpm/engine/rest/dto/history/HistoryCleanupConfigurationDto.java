package org.camunda.bpm.engine.rest.dto.history;

import java.util.Date;

public class HistoryCleanupConfigurationDto {

	protected Date historyCleanupBatchWindowStartTime;
	protected Date historyCleanupBatchWindowEndTime;

	public Date getHistoryCleanupBatchWindowStartTime() {
		return historyCleanupBatchWindowStartTime;
	}

	public void setHistoryCleanupBatchWindowStartTime(Date historyCleanupBatchWindowStartTime) {
		this.historyCleanupBatchWindowStartTime = historyCleanupBatchWindowStartTime;
	}

	public Date getHistoryCleanupBatchWindowEndTime() {
		return historyCleanupBatchWindowEndTime;
	}

	public void setHistoryCleanupBatchWindowEndTime(Date historyCleanupBatchWindowEndTime) {
		this.historyCleanupBatchWindowEndTime = historyCleanupBatchWindowEndTime;
	}

}
