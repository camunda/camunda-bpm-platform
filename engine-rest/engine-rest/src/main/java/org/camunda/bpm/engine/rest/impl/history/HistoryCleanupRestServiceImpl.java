/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	  ProcessEngineConfigurationImpl engineConfiguration =
        (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();

    HistoryCleanupConfigurationDto configurationDto = new HistoryCleanupConfigurationDto();
    configurationDto.setEnabled(engineConfiguration.isHistoryCleanupEnabled());

    BatchWindow batchWindow = engineConfiguration.getBatchWindowManager()
      .getCurrentOrNextBatchWindow(ClockUtil.getCurrentTime(), engineConfiguration);

    if (batchWindow != null) {
      configurationDto.setBatchWindowStartTime(batchWindow.getStart());
      configurationDto.setBatchWindowEndTime(batchWindow.getEnd());
    }

    return configurationDto;
  }
}
