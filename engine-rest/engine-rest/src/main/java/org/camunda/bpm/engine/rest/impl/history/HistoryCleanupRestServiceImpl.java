package org.camunda.bpm.engine.rest.impl.history;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.jobexecutor.historycleanup.BatchWindow;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.rest.dto.history.HistoryCleanupConfigurationDto;
import org.camunda.bpm.engine.rest.dto.runtime.JobDto;
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

  public List<JobDto> findCleanupJobs() {
    List<Job> jobs = processEngine.getHistoryService().findHistoryCleanupJobs();
    if (jobs == null || jobs.isEmpty()) {
      throw new RestException(Status.NOT_FOUND, "History cleanup jobs are empty");
    }
    List<JobDto> dtos = new ArrayList<JobDto>();
    for (Job job : jobs) {
      JobDto dto = JobDto.fromJob(job);
      dtos.add(dto);
    }
    return dtos;
  }

  public HistoryCleanupConfigurationDto getHistoryCleanupConfiguration() {
    HistoryCleanupConfigurationDto configurationDto = new HistoryCleanupConfigurationDto();
    final ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
    Date now = ClockUtil.getCurrentTime();
    final BatchWindow batchWindow = processEngineConfiguration.getBatchWindowManager()
      .getCurrentOrNextBatchWindow(now, processEngineConfiguration);
    if (batchWindow == null) {
      return configurationDto;
    }
    configurationDto.setBatchWindowStartTime(batchWindow.getStart());
    configurationDto.setBatchWindowEndTime(batchWindow.getEnd());
    return configurationDto;
  }
}
