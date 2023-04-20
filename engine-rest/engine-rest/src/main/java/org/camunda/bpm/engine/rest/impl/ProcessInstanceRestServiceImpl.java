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
package org.camunda.bpm.engine.rest.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.impl.util.EnsureUtil;
import org.camunda.bpm.engine.management.SetJobRetriesByProcessAsyncBuilder;
import org.camunda.bpm.engine.query.Query;
import org.camunda.bpm.engine.rest.ProcessInstanceRestService;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.batch.BatchDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceQueryDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceQueryDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceSuspensionStateAsyncDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceSuspensionStateDto;
import org.camunda.bpm.engine.rest.dto.runtime.SetJobRetriesByProcessDto;
import org.camunda.bpm.engine.rest.dto.runtime.batch.CorrelationMessageAsyncDto;
import org.camunda.bpm.engine.rest.dto.runtime.batch.DeleteProcessInstancesDto;
import org.camunda.bpm.engine.rest.dto.runtime.batch.SetVariablesAsyncDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.sub.runtime.ProcessInstanceResource;
import org.camunda.bpm.engine.rest.sub.runtime.impl.ProcessInstanceResourceImpl;
import org.camunda.bpm.engine.rest.util.QueryUtil;
import org.camunda.bpm.engine.runtime.MessageCorrelationAsyncBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.variable.VariableMap;

public class ProcessInstanceRestServiceImpl extends AbstractRestProcessEngineAware implements
    ProcessInstanceRestService {

  public ProcessInstanceRestServiceImpl(String engineName, ObjectMapper objectMapper) {
    super(engineName, objectMapper);
  }


  @Override
  public List<ProcessInstanceDto> getProcessInstances(
      UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    ProcessInstanceQueryDto queryDto = new ProcessInstanceQueryDto(getObjectMapper(), uriInfo.getQueryParameters());
    return queryProcessInstances(queryDto, firstResult, maxResults);
  }

  @Override
  public List<ProcessInstanceDto> queryProcessInstances(
      ProcessInstanceQueryDto queryDto, Integer firstResult, Integer maxResults) {
    ProcessEngine engine = getProcessEngine();
    queryDto.setObjectMapper(getObjectMapper());
    ProcessInstanceQuery query = queryDto.toQuery(engine);

    List<ProcessInstance> matchingInstances = QueryUtil.list(query, firstResult, maxResults);

    List<ProcessInstanceDto> instanceResults = new ArrayList<>();
    for (ProcessInstance instance : matchingInstances) {
      ProcessInstanceDto resultInstance = ProcessInstanceDto.fromProcessInstance(instance);
      instanceResults.add(resultInstance);
    }
    return instanceResults;
  }

  @Override
  public CountResultDto getProcessInstancesCount(UriInfo uriInfo) {
    ProcessInstanceQueryDto queryDto = new ProcessInstanceQueryDto(getObjectMapper(), uriInfo.getQueryParameters());
    return queryProcessInstancesCount(queryDto);
  }

  @Override
  public CountResultDto queryProcessInstancesCount(ProcessInstanceQueryDto queryDto) {
    ProcessEngine engine = getProcessEngine();
    queryDto.setObjectMapper(getObjectMapper());
    ProcessInstanceQuery query = queryDto.toQuery(engine);

    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);

    return result;
  }

  @Override
  public ProcessInstanceResource getProcessInstance(String processInstanceId) {
    return new ProcessInstanceResourceImpl(getProcessEngine(), processInstanceId, getObjectMapper());
  }

  @Override
  public void updateSuspensionState(ProcessInstanceSuspensionStateDto dto) {
    dto.updateSuspensionState(getProcessEngine());
  }

  @Override
  public BatchDto updateSuspensionStateAsync(ProcessInstanceSuspensionStateAsyncDto dto){
    Batch batch = null;
    try {
      batch = dto.updateSuspensionStateAsync(getProcessEngine());
      return BatchDto.fromBatch(batch);

    } catch (BadUserRequestException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e.getMessage());
    }
  }

  @Override
  public BatchDto deleteAsync(DeleteProcessInstancesDto dto) {
    RuntimeService runtimeService = getProcessEngine().getRuntimeService();

    ProcessInstanceQuery processInstanceQuery = null;
    if (dto.getProcessInstanceQuery() != null) {
      processInstanceQuery = dto.getProcessInstanceQuery().toQuery(getProcessEngine());
    }

    Batch batch = null;

    try {
      batch = runtimeService.deleteProcessInstancesAsync(dto.getProcessInstanceIds(), processInstanceQuery, dto.getDeleteReason(), dto.isSkipCustomListeners(),
          dto.isSkipSubprocesses());
      return BatchDto.fromBatch(batch);
    }
    catch (BadUserRequestException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e.getMessage());
    }
  }

  @Override
  public BatchDto deleteAsyncHistoricQueryBased(DeleteProcessInstancesDto deleteProcessInstancesDto) {
    HistoricProcessInstanceQuery historicProcessInstanceQuery = null;
    if (deleteProcessInstancesDto.getHistoricProcessInstanceQuery() != null) {
      historicProcessInstanceQuery = deleteProcessInstancesDto.getHistoricProcessInstanceQuery()
          .toQuery(getProcessEngine());
    }

    RuntimeService runtimeService = getProcessEngine().getRuntimeService();

    try {
      Batch batch = runtimeService.deleteProcessInstancesAsync(
        deleteProcessInstancesDto.getProcessInstanceIds(),
        null,
        historicProcessInstanceQuery,
        deleteProcessInstancesDto.getDeleteReason(),
        deleteProcessInstancesDto.isSkipCustomListeners(),
        deleteProcessInstancesDto.isSkipSubprocesses());

      return BatchDto.fromBatch(batch);
    } catch (BadUserRequestException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e.getMessage());
    }
  }

  @Override
  public BatchDto setRetriesByProcess(SetJobRetriesByProcessDto setJobRetriesDto) {
    try {
      EnsureUtil.ensureNotNull("setJobRetriesDto", setJobRetriesDto);
      EnsureUtil.ensureNotNull("retries", setJobRetriesDto.getRetries());
    } catch (NullValueException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e.getMessage());
    }
    ProcessInstanceQuery processInstanceQuery = null;
    if (setJobRetriesDto.getProcessInstanceQuery() != null) {
      processInstanceQuery = setJobRetriesDto.getProcessInstanceQuery().toQuery(getProcessEngine());
    }

    try {
      SetJobRetriesByProcessAsyncBuilder builder = getProcessEngine().getManagementService()
          .setJobRetriesByProcessAsync(setJobRetriesDto.getRetries().intValue())
          .processInstanceIds(setJobRetriesDto.getProcessInstances())
          .processInstanceQuery(processInstanceQuery);
      if(setJobRetriesDto.isDueDateSet()) {
        builder.dueDate(setJobRetriesDto.getDueDate());
      }
      Batch batch = builder.executeAsync();
      return BatchDto.fromBatch(batch);
    } catch (BadUserRequestException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e.getMessage());
    }
  }

  @Override
  public BatchDto setRetriesByProcessHistoricQueryBased(SetJobRetriesByProcessDto setJobRetriesDto) {
    HistoricProcessInstanceQueryDto queryDto = setJobRetriesDto.getHistoricProcessInstanceQuery();
    HistoricProcessInstanceQuery query = null;
    if (queryDto != null) {
      query = queryDto.toQuery(getProcessEngine());
    }

    try {
      ManagementService managementService = getProcessEngine().getManagementService();
      SetJobRetriesByProcessAsyncBuilder builder = managementService
          .setJobRetriesByProcessAsync(setJobRetriesDto.getRetries())
          .processInstanceIds(setJobRetriesDto.getProcessInstances())
          .historicProcessInstanceQuery(query);
      if(setJobRetriesDto.isDueDateSet()) {
        builder.dueDate(setJobRetriesDto.getDueDate());
      }
      Batch batch = builder.executeAsync();
      return BatchDto.fromBatch(batch);
    } catch (BadUserRequestException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e.getMessage());
    }
  }

  @Override
  public BatchDto setVariablesAsync(SetVariablesAsyncDto setVariablesAsyncDto) {
    Map<String, VariableValueDto> variables = setVariablesAsyncDto.getVariables();

    VariableMap variableMap = null;
    try {
      variableMap = VariableValueDto.toMap(variables, getProcessEngine(), objectMapper);

    } catch (RestException e) {
      String errorMessage = String.format("Cannot set variables: %s", e.getMessage());
      throw new InvalidRequestException(e.getStatus(), e, errorMessage);

    }

    List<String> ids = setVariablesAsyncDto.getProcessInstanceIds();
    ProcessInstanceQuery runtimeQuery = toQuery(setVariablesAsyncDto.getProcessInstanceQuery());
    HistoricProcessInstanceQuery historyQuery = toQuery(setVariablesAsyncDto.getHistoricProcessInstanceQuery());

    RuntimeService runtimeService = getProcessEngine().getRuntimeService();

    Batch batch = null;
    try {
      batch = runtimeService.setVariablesAsync(ids, runtimeQuery, historyQuery, variableMap);

    } catch (BadUserRequestException | AuthorizationException e) {
      throw e;

    } catch (ProcessEngineException e) {
      // 1) the java serialization format is prohibited
      // 2) null value is given
      throw new InvalidRequestException(Status.BAD_REQUEST, e.getMessage());

    }

    return BatchDto.fromBatch(batch);
  }

  @Override
  public BatchDto correlateMessageAsync(CorrelationMessageAsyncDto correlationMessageAsyncDto) {
    Map<String, VariableValueDto> variables = correlationMessageAsyncDto.getVariables();

    VariableMap variableMap = null;
    try {
      variableMap = VariableValueDto.toMap(variables, getProcessEngine(), objectMapper);
    } catch (RestException e) {
      String errorMessage = String.format("Cannot set variables: %s", e.getMessage());
      throw new InvalidRequestException(e.getStatus(), e, errorMessage);
    }

    String messageName = correlationMessageAsyncDto.getMessageName();
    List<String> ids = correlationMessageAsyncDto.getProcessInstanceIds();
    ProcessInstanceQuery runtimeQuery = toQuery(correlationMessageAsyncDto.getProcessInstanceQuery());
    HistoricProcessInstanceQuery historyQuery = toQuery(correlationMessageAsyncDto.getHistoricProcessInstanceQuery());

    RuntimeService runtimeService = getProcessEngine().getRuntimeService();

    Batch batch = null;
    try {
      MessageCorrelationAsyncBuilder messageCorrelationBuilder = runtimeService
        .createMessageCorrelationAsync(messageName)
        .setVariables(variableMap);
      if (ids != null) {
        messageCorrelationBuilder.processInstanceIds(ids);
      }
      if (runtimeQuery != null) {
        messageCorrelationBuilder.processInstanceQuery(runtimeQuery);
      }
      if (historyQuery != null) {
        messageCorrelationBuilder.historicProcessInstanceQuery(historyQuery);
      }
      batch = messageCorrelationBuilder.correlateAllAsync();
    } catch (NullValueException e) {
      // null values are given
      throw new InvalidRequestException(Status.BAD_REQUEST, e.getMessage());
    }

    return BatchDto.fromBatch(batch);
  }

  protected <T extends Query<?,?>, R extends AbstractQueryDto<T>> T toQuery(R query) {
    if (query == null) {
      return null;
    }

    return query.toQuery(getProcessEngine());
  }

}
