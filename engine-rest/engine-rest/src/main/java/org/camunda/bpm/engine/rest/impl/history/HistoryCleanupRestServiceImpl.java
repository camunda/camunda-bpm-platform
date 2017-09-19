package org.camunda.bpm.engine.rest.impl.history;

import java.util.Date;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupHelper;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.rest.dto.history.HistoryCleanupConfigurationDto;
import org.camunda.bpm.engine.rest.dto.runtime.JobDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.history.HistoryCleanupRestService;
import org.camunda.bpm.engine.runtime.Job;

import com.fasterxml.jackson.databind.ObjectMapper;

public class HistoryCleanupRestServiceImpl implements HistoryCleanupRestService {

  protected ObjectMapper objectMapper;
  protected ProcessEngine processEngine;

	public HistoryCleanupRestServiceImpl(ObjectMapper objectMapper, ProcessEngine processEngine) {
    this.objectMapper = objectMapper;
    this.processEngine = processEngine;
	}

  public JobDto cleanupAsync(boolean immediatelyDue) {
    Job job = processEngine.getHistoryService().cleanUpHistoryAsync(immediatelyDue);
    return JobDto.fromJob(job);
  }

  public JobDto findCleanupJob() {
    Job job = processEngine.getHistoryService().findHistoryCleanupJob();
    if (job == null) {
      throw new RestException(Status.NOT_FOUND, "History cleanup job does not exist");
    }
    return JobDto.fromJob(job);
  }

  public HistoryCleanupConfigurationDto getHistoryCleanupConfiguration() {
    HistoryCleanupConfigurationDto configurationDto = new HistoryCleanupConfigurationDto();
    Date startTime = ((ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration())
        .getHistoryCleanupBatchWindowStartTimeAsDate();
    Date endTime = ((ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration())
        .getHistoryCleanupBatchWindowEndTimeAsDate();
    if (startTime == null || endTime == null) {
      return configurationDto;
    }
    Date now = ClockUtil.getCurrentTime();
    Date startDate = HistoryCleanupHelper.getCurrentOrNextBatchWindowStartTime(now, startTime, endTime);
    Date endDate = HistoryCleanupHelper.getNextBatchWindowEndTime(now, endTime);
    configurationDto.setBatchWindowStartTime(startDate);
    configurationDto.setBatchWindowEndTime(endDate);
    return configurationDto;
  }
}
