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
package org.camunda.bpm.engine.rest.impl.optimize;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricVariableUpdate;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricIncidentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.optimize.OptimizeHistoricIdentityLinkLogEntity;
import org.camunda.bpm.engine.rest.dto.converter.DateConverter;
import org.camunda.bpm.engine.rest.dto.history.HistoricDecisionInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricIncidentDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricTaskInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.UserOperationLogEntryDto;
import org.camunda.bpm.engine.rest.dto.history.optimize.OptimizeHistoricActivityInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.optimize.OptimizeHistoricIdentityLinkLogDto;
import org.camunda.bpm.engine.rest.dto.history.optimize.OptimizeHistoricVariableUpdateDto;
import org.camunda.bpm.engine.rest.impl.AbstractRestProcessEngineAware;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Produces(MediaType.APPLICATION_JSON)
public class OptimizeRestService extends AbstractRestProcessEngineAware {

  public static final String PATH = "/optimize";

  private final DateConverter dateConverter;

  public OptimizeRestService(String engineName, ObjectMapper objectMapper) {
    super(engineName, objectMapper);
    dateConverter = new DateConverter();
    dateConverter.setObjectMapper(objectMapper);
  }

  @GET
  @Path("/activity-instance/completed")
  public List<OptimizeHistoricActivityInstanceDto> getCompletedHistoricActivityInstances(@QueryParam("finishedAfter") String finishedAfterAsString,
                                                                                         @QueryParam("finishedAt") String finishedAtAsString,
                                                                                         @QueryParam("maxResults") int maxResults) {

    Date finishedAfter = dateConverter.convertQueryParameterToType(finishedAfterAsString);
    Date finishedAt = dateConverter.convertQueryParameterToType(finishedAtAsString);
    maxResults = ensureValidMaxResults(maxResults);

    ProcessEngineConfigurationImpl config =
      (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();

    List<HistoricActivityInstance> historicActivityInstances =
      config.getOptimizeService().getCompletedHistoricActivityInstances(finishedAfter, finishedAt, maxResults);

    List<OptimizeHistoricActivityInstanceDto> result = new ArrayList<>();
    for (HistoricActivityInstance instance : historicActivityInstances) {
      OptimizeHistoricActivityInstanceDto dto = OptimizeHistoricActivityInstanceDto.fromHistoricActivityInstance(instance);
      result.add(dto);
    }
    return result;
  }

  @GET
  @Path("/activity-instance/running")
  public List<OptimizeHistoricActivityInstanceDto> getRunningHistoricActivityInstances(@QueryParam("startedAfter") String startedAfterAsString,
                                                                                       @QueryParam("startedAt") String startedAtAsString,
                                                                                       @QueryParam("maxResults") int maxResults) {

    Date startedAfter = dateConverter.convertQueryParameterToType(startedAfterAsString);
    Date startedAt = dateConverter.convertQueryParameterToType(startedAtAsString);
    maxResults = ensureValidMaxResults(maxResults);

    ProcessEngineConfigurationImpl config =
      (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();

    List<HistoricActivityInstance> historicActivityInstances =
      config.getOptimizeService().getRunningHistoricActivityInstances(startedAfter, startedAt, maxResults);

    List<OptimizeHistoricActivityInstanceDto> result = new ArrayList<>();
    for (HistoricActivityInstance instance : historicActivityInstances) {
      OptimizeHistoricActivityInstanceDto dto = OptimizeHistoricActivityInstanceDto.fromHistoricActivityInstance(instance);
      result.add(dto);
    }
    return result;
  }

  @GET
  @Path("/task-instance/completed")
  public List<HistoricTaskInstanceDto> getCompletedHistoricTaskInstances(@QueryParam("finishedAfter") String finishedAfterAsString,
                                                                         @QueryParam("finishedAt") String finishedAtAsString,
                                                                         @QueryParam("maxResults") int maxResults) {

    Date finishedAfter = dateConverter.convertQueryParameterToType(finishedAfterAsString);
    Date finishedAt = dateConverter.convertQueryParameterToType(finishedAtAsString);
    maxResults = ensureValidMaxResults(maxResults);

    ProcessEngineConfigurationImpl config =
      (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();

    List<HistoricTaskInstance> historicTaskInstances =
      config.getOptimizeService().getCompletedHistoricTaskInstances(finishedAfter, finishedAt, maxResults);

    List<HistoricTaskInstanceDto> result = new ArrayList<>();
    for (HistoricTaskInstance instance : historicTaskInstances) {
      HistoricTaskInstanceDto dto = HistoricTaskInstanceDto.fromHistoricTaskInstance(instance);
      result.add(dto);
    }
    return result;
  }

  @GET
  @Path("/task-instance/running")
  public List<HistoricTaskInstanceDto> getRunningHistoricTaskInstances(@QueryParam("startedAfter") String startedAfterAsString,
                                                                       @QueryParam("startedAt") String startedAtAsString,
                                                                       @QueryParam("maxResults") int maxResults) {

    Date startedAfter = dateConverter.convertQueryParameterToType(startedAfterAsString);
    Date startedAt = dateConverter.convertQueryParameterToType(startedAtAsString);
    maxResults = ensureValidMaxResults(maxResults);

    ProcessEngineConfigurationImpl config =
      (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();

    List<HistoricTaskInstance> historicTaskInstances =
      config.getOptimizeService().getRunningHistoricTaskInstances(startedAfter, startedAt, maxResults);

    List<HistoricTaskInstanceDto> result = new ArrayList<>();
    for (HistoricTaskInstance instance : historicTaskInstances) {
      HistoricTaskInstanceDto dto = HistoricTaskInstanceDto.fromHistoricTaskInstance(instance);
      result.add(dto);
    }
    return result;
  }

  @GET
  @Path("/user-operation")
  public List<UserOperationLogEntryDto> getHistoricUserOperationLogs(@QueryParam("occurredAfter") String occurredAfterAsString,
                                                                     @QueryParam("occurredAt") String occurredAtAsString,
                                                                     @QueryParam("maxResults") int maxResults) {

    Date occurredAfter = dateConverter.convertQueryParameterToType(occurredAfterAsString);
    Date occurredAt = dateConverter.convertQueryParameterToType(occurredAtAsString);
    maxResults = ensureValidMaxResults(maxResults);

    ProcessEngineConfigurationImpl config =
      (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();

    List<UserOperationLogEntry> operationLogEntries =
      config.getOptimizeService().getHistoricUserOperationLogs(occurredAfter, occurredAt, maxResults);

    List<UserOperationLogEntryDto> result = new ArrayList<>();
    for (UserOperationLogEntry logEntry : operationLogEntries) {
      UserOperationLogEntryDto dto = UserOperationLogEntryDto.map(logEntry);
      result.add(dto);
    }
    return result;
  }

  @GET
  @Path("/identity-link-log")
  public List<OptimizeHistoricIdentityLinkLogDto> getHistoricIdentityLinkLogs(@QueryParam("occurredAfter") String occurredAfterAsString,
                                                                              @QueryParam("occurredAt") String occurredAtAsString,
                                                                              @QueryParam("maxResults") int maxResults) {

    Date occurredAfter = dateConverter.convertQueryParameterToType(occurredAfterAsString);
    Date occurredAt = dateConverter.convertQueryParameterToType(occurredAtAsString);
    maxResults = ensureValidMaxResults(maxResults);

    ProcessEngineConfigurationImpl config =
      (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();

    List<OptimizeHistoricIdentityLinkLogEntity> operationLogEntries =
      config.getOptimizeService().getHistoricIdentityLinkLogs(occurredAfter, occurredAt, maxResults);

    List<OptimizeHistoricIdentityLinkLogDto> result = new ArrayList<>();
    for (OptimizeHistoricIdentityLinkLogEntity logEntry : operationLogEntries) {
      OptimizeHistoricIdentityLinkLogDto dto = OptimizeHistoricIdentityLinkLogDto.fromHistoricIdentityLink(logEntry);
      result.add(dto);
    }
    return result;
  }

  @GET
  @Path("/process-instance/completed")
  public List<HistoricProcessInstanceDto> getCompletedHistoricProcessInstances(@QueryParam("finishedAfter") String finishedAfterAsString,
                                                                               @QueryParam("finishedAt") String finishedAtAsString,
                                                                               @QueryParam("maxResults") int maxResults) {
    Date finishedAfter = dateConverter.convertQueryParameterToType(finishedAfterAsString);
    Date finishedAt = dateConverter.convertQueryParameterToType(finishedAtAsString);
    maxResults = ensureValidMaxResults(maxResults);

    ProcessEngineConfigurationImpl config =
      (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();
    List<HistoricProcessInstance> historicProcessInstances =
      config.getOptimizeService().getCompletedHistoricProcessInstances(finishedAfter, finishedAt, maxResults);

    List<HistoricProcessInstanceDto> result = new ArrayList<>();
    for (HistoricProcessInstance instance : historicProcessInstances) {
      HistoricProcessInstanceDto dto = HistoricProcessInstanceDto.fromHistoricProcessInstance(instance);
      result.add(dto);
    }
    return result;
  }

  @GET
  @Path("/process-instance/running")
  public List<HistoricProcessInstanceDto> getRunningHistoricProcessInstances(@QueryParam("startedAfter") String startedAfterAsString,
                                                                             @QueryParam("startedAt") String startedAtAsString,
                                                                             @QueryParam("maxResults") int maxResults) {
    Date startedAfter = dateConverter.convertQueryParameterToType(startedAfterAsString);
    Date startedAt = dateConverter.convertQueryParameterToType(startedAtAsString);
    maxResults = ensureValidMaxResults(maxResults);

    ProcessEngineConfigurationImpl config =
      (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();
    List<HistoricProcessInstance> historicProcessInstances =
      config.getOptimizeService().getRunningHistoricProcessInstances(startedAfter, startedAt, maxResults);

    List<HistoricProcessInstanceDto> result = new ArrayList<>();
    for (HistoricProcessInstance instance : historicProcessInstances) {
      HistoricProcessInstanceDto dto = HistoricProcessInstanceDto.fromHistoricProcessInstance(instance);
      result.add(dto);
    }
    return result;
  }

  @GET
  @Path("/variable-update")
  public List<OptimizeHistoricVariableUpdateDto> getHistoricVariableUpdates(@QueryParam("occurredAfter") String occurredAfterAsString,
                                                                            @QueryParam("occurredAt") String occurredAtAsString,
                                                                            @QueryParam("excludeObjectValues") boolean excludeObjectValues,
                                                                            @QueryParam("maxResults") int maxResults) {
    Date occurredAfter = dateConverter.convertQueryParameterToType(occurredAfterAsString);
    Date occurredAt = dateConverter.convertQueryParameterToType(occurredAtAsString);
    maxResults = ensureValidMaxResults(maxResults);

    ProcessEngineConfigurationImpl config =
      (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();
    List<HistoricVariableUpdate> historicVariableUpdates =
      config.getOptimizeService().getHistoricVariableUpdates(occurredAfter, occurredAt, excludeObjectValues, maxResults);

    List<OptimizeHistoricVariableUpdateDto> result = new ArrayList<>();
    for (HistoricVariableUpdate instance : historicVariableUpdates) {
      OptimizeHistoricVariableUpdateDto dto =
        OptimizeHistoricVariableUpdateDto.fromHistoricVariableUpdate(instance);
      result.add(dto);
    }
    return result;
  }

  @GET
  @Path("/incident/completed")
  public List<HistoricIncidentDto> getCompletedHistoricIncidents(@QueryParam("finishedAfter") String finishedAfterAsString,
                                                                 @QueryParam("finishedAt") String finishedAtAsString,
                                                                 @QueryParam("maxResults") int maxResults) {
    Date finishedAfter = dateConverter.convertQueryParameterToType(finishedAfterAsString);
    Date finishedAt = dateConverter.convertQueryParameterToType(finishedAtAsString);
    maxResults = ensureValidMaxResults(maxResults);

    ProcessEngineConfigurationImpl config =
      (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();
    List<HistoricIncidentEntity> historicIncidents =
      config.getOptimizeService().getCompletedHistoricIncidents(finishedAfter, finishedAt, maxResults);

    List<HistoricIncidentDto> result = new ArrayList<>();
    for (HistoricIncident instance : historicIncidents) {
      HistoricIncidentDto dto = HistoricIncidentDto.fromHistoricIncident(instance);
      result.add(dto);
    }
    return result;
  }

  @GET
  @Path("/incident/open")
  public List<HistoricIncidentDto> getOpenHistoricIncidents(@QueryParam("createdAfter") String createdAfterAsString,
                                                            @QueryParam("createdAt") String createdAtAsString,
                                                            @QueryParam("maxResults") int maxResults) {
    Date createdAfter = dateConverter.convertQueryParameterToType(createdAfterAsString);
    Date createdAt = dateConverter.convertQueryParameterToType(createdAtAsString);
    maxResults = ensureValidMaxResults(maxResults);

    ProcessEngineConfigurationImpl config =
      (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();
    List<HistoricIncidentEntity> historicIncidents =
      config.getOptimizeService().getOpenHistoricIncidents(createdAfter, createdAt, maxResults);

    List<HistoricIncidentDto> result = new ArrayList<>();
    for (HistoricIncident instance : historicIncidents) {
      HistoricIncidentDto dto = HistoricIncidentDto.fromHistoricIncident(instance);
      result.add(dto);
    }
    return result;
  }

  @GET
  @Path("/decision-instance")
  public List<HistoricDecisionInstanceDto> getHistoricDecisionInstances(@QueryParam("evaluatedAfter") String evaluatedAfterAsString,
                                                                        @QueryParam("evaluatedAt") String evaluatedAtAsString,
                                                                        @QueryParam("maxResults") int maxResults) {
    Date evaluatedAfter = dateConverter.convertQueryParameterToType(evaluatedAfterAsString);
    Date evaluatedAt = dateConverter.convertQueryParameterToType(evaluatedAtAsString);
    maxResults = ensureValidMaxResults(maxResults);

    ProcessEngineConfigurationImpl config =
      (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();
    List<HistoricDecisionInstance> historicDecisionInstances =
      config.getOptimizeService().getHistoricDecisionInstances(evaluatedAfter, evaluatedAt, maxResults);

    List<HistoricDecisionInstanceDto> resultList = new ArrayList<>();
    for (HistoricDecisionInstance historicDecisionInstance : historicDecisionInstances) {
      HistoricDecisionInstanceDto dto =
        HistoricDecisionInstanceDto.fromHistoricDecisionInstance(historicDecisionInstance);
      resultList.add(dto);
    }

    return resultList;
  }

  protected int ensureValidMaxResults(int givenMaxResults) {
    return givenMaxResults > 0 ? givenMaxResults : Integer.MAX_VALUE;
  }
}
