/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest.impl.history;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.rest.dto.runtime.JobDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.history.HistoricActivityInstanceRestService;
import org.camunda.bpm.engine.rest.history.HistoricProcessDefinitionRestService;
import org.camunda.bpm.engine.rest.history.HistoricBatchRestService;
import org.camunda.bpm.engine.rest.history.HistoricCaseActivityInstanceRestService;
import org.camunda.bpm.engine.rest.history.HistoricCaseActivityStatisticsRestService;
import org.camunda.bpm.engine.rest.history.HistoricCaseInstanceRestService;
import org.camunda.bpm.engine.rest.history.HistoricDecisionInstanceRestService;
import org.camunda.bpm.engine.rest.history.HistoricDecisionStatisticsRestService;
import org.camunda.bpm.engine.rest.history.HistoricDetailRestService;
import org.camunda.bpm.engine.rest.history.HistoricExternalTaskLogRestService;
import org.camunda.bpm.engine.rest.history.HistoricIdentityLinkLogRestService;
import org.camunda.bpm.engine.rest.history.HistoricIncidentRestService;
import org.camunda.bpm.engine.rest.history.HistoricJobLogRestService;
import org.camunda.bpm.engine.rest.history.HistoricProcessInstanceRestService;
import org.camunda.bpm.engine.rest.history.HistoricTaskInstanceRestService;
import org.camunda.bpm.engine.rest.history.HistoricVariableInstanceRestService;
import org.camunda.bpm.engine.rest.history.HistoryRestService;
import org.camunda.bpm.engine.rest.history.UserOperationLogRestService;

import org.camunda.bpm.engine.rest.impl.AbstractRestProcessEngineAware;
import org.camunda.bpm.engine.runtime.Job;

import com.fasterxml.jackson.databind.ObjectMapper;

public class HistoryRestServiceImpl extends AbstractRestProcessEngineAware implements HistoryRestService {

  public HistoryRestServiceImpl(String engineName, ObjectMapper objectMapper) {
    super(engineName, objectMapper);
  }

  public HistoricProcessInstanceRestService getProcessInstanceService() {
    return new HistoricProcessInstanceRestServiceImpl(getObjectMapper(), getProcessEngine());
  }

  public HistoricCaseInstanceRestService getCaseInstanceService() {
    return new HistoricCaseInstanceRestServiceImpl(getObjectMapper(), getProcessEngine());
  }

  public HistoricActivityInstanceRestService getActivityInstanceService() {
    return new HistoricActivityInstanceRestServiceImpl(getObjectMapper(), getProcessEngine());
  }

  public HistoricCaseActivityInstanceRestService getCaseActivityInstanceService() {
    return new HistoricCaseActivityInstanceRestServiceImpl(getObjectMapper(), getProcessEngine());
  }

  public HistoricVariableInstanceRestService getVariableInstanceService() {
    return new HistoricVariableInstanceRestServiceImpl(getObjectMapper(), getProcessEngine());
  }

  public HistoricProcessDefinitionRestService getActivityStatisticsService() {
    return new HistoricProcessDefinitionRestServiceImpl(getProcessEngine());
  }

  public HistoricDecisionStatisticsRestService getDecisionStatisticsService() {
    return new HistoricDecisionStatisticsRestServiceImpl(getProcessEngine());
  }

  public HistoricCaseActivityStatisticsRestService getCaseActivityStatisticsService() {
    return new HistoricCaseActivityStatisticsRestServiceImpl(getProcessEngine());
  }

  public UserOperationLogRestService getUserOperationLogRestService() {
    return new UserOperationLogRestServiceImpl(getObjectMapper(), getProcessEngine());
  }

  public HistoricDetailRestService getDetailService() {
    return new HistoricDetailRestServiceImpl(getObjectMapper(), getProcessEngine());
  }

  public HistoricTaskInstanceRestService getTaskInstanceService() {
    return new HistoricTaskInstanceRestServiceImpl(getObjectMapper(), getProcessEngine());
  }

  public HistoricIncidentRestService getIncidentService() {
    return new HistoricIncidentRestServiceImpl(getObjectMapper(), getProcessEngine());
  }

  public HistoricIdentityLinkLogRestService getIdentityLinkService() {
    return new HistoricIdentityLinkLogRestServiceImpl(getObjectMapper(), getProcessEngine());
  }

  public HistoricJobLogRestService getJobLogService() {
    return new HistoricJobLogRestServiceImpl(getObjectMapper(), getProcessEngine());
  }

  public HistoricDecisionInstanceRestService getDecisionInstanceService() {
    return new HistoricDecisionInstanceRestServiceImpl(getObjectMapper(), getProcessEngine());
  }

  public HistoricBatchRestService getBatchService() {
    return new HistoricBatchRestServiceImpl(getObjectMapper(), getProcessEngine());
  }

  @Override
  public HistoricExternalTaskLogRestService getExternalTaskLogService() {
    return new HistoricExternalTaskLogRestServiceImpl(getObjectMapper(), getProcessEngine());
  }

  @Override
  public JobDto cleanupAsync(boolean immediatelyDue) {
    Job job = processEngine.getHistoryService().cleanUpHistoryAsync(immediatelyDue);
    return JobDto.fromJob(job);
  }

  @Override
  public JobDto findCleanupJob() {
    Job job = processEngine.getHistoryService().findHistoryCleanupJob();
    if (job == null) {
      throw new RestException(Status.NOT_FOUND, "History cleanup job does not exist");
    }
    return JobDto.fromJob(job);
  }
}
