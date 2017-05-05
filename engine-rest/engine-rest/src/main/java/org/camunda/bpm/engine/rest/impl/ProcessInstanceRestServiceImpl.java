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
package org.camunda.bpm.engine.rest.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.util.EnsureUtil;
import org.camunda.bpm.engine.rest.ProcessInstanceRestService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.batch.BatchDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceQueryDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceSuspensionStateDto;
import org.camunda.bpm.engine.rest.dto.runtime.RestartProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.SetJobRetriesByProcessDto;
import org.camunda.bpm.engine.rest.dto.runtime.batch.DeleteProcessInstancesDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.sub.runtime.ProcessInstanceResource;
import org.camunda.bpm.engine.rest.sub.runtime.impl.ProcessInstanceResourceImpl;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.runtime.RestartProcessInstanceBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

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

    List<ProcessInstance> matchingInstances;
    if (firstResult != null || maxResults != null) {
      matchingInstances = executePaginatedQuery(query, firstResult, maxResults);
    } else {
      matchingInstances = query.list();
    }

    List<ProcessInstanceDto> instanceResults = new ArrayList<ProcessInstanceDto>();
    for (ProcessInstance instance : matchingInstances) {
      ProcessInstanceDto resultInstance = ProcessInstanceDto.fromProcessInstance(instance);
      instanceResults.add(resultInstance);
    }
    return instanceResults;
  }

  private List<ProcessInstance> executePaginatedQuery(ProcessInstanceQuery query, Integer firstResult, Integer maxResults) {
    if (firstResult == null) {
      firstResult = 0;
    }
    if (maxResults == null) {
      maxResults = Integer.MAX_VALUE;
    }
    return query.listPage(firstResult, maxResults);
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
    if (dto.getProcessInstanceId() != null) {
      String message = "Either processDefinitionId or processDefinitionKey can be set to update the suspension state.";
      throw new InvalidRequestException(Status.BAD_REQUEST, message);
    }

    dto.updateSuspensionState(getProcessEngine());
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

        batch = runtimeService.deleteProcessInstancesAsync(
        dto.getProcessInstanceIds(),
        processInstanceQuery,
        dto.getDeleteReason(),
        dto.isSkipCustomListeners());

      return BatchDto.fromBatch(batch);
    }
    catch (BadUserRequestException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e.getMessage());
    }
  }

  @Override
  public BatchDto deleteAsyncHistoricQueryBased(DeleteProcessInstancesDto deleteProcessInstancesDto) {
    try {
      EnsureUtil.ensureNotNull("deleteProcessInstancesDto", deleteProcessInstancesDto);
    } catch (NullValueException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e.getMessage());
    }

    RuntimeService runtimeService = getProcessEngine().getRuntimeService();

    List<HistoricProcessInstance> historicProcessInstances = null;
    if (deleteProcessInstancesDto.getHistoricProcessInstanceQuery() != null) {
      historicProcessInstances = deleteProcessInstancesDto.getHistoricProcessInstanceQuery().toQuery(getProcessEngine()).list();
    }

    List<String> processInstanceIds = new ArrayList<String>();
    if (historicProcessInstances != null) {
      for (HistoricProcessInstance historicProcessInstance: historicProcessInstances) {
        processInstanceIds.add(historicProcessInstance.getId());
      }
    }

    if (deleteProcessInstancesDto.getProcessInstanceIds() != null) {
      processInstanceIds.addAll(deleteProcessInstancesDto.getProcessInstanceIds());
    }

    Batch batch = null;

    try {

      batch = runtimeService.deleteProcessInstancesAsync(
        processInstanceIds.isEmpty() ? null : processInstanceIds,
        null,
        deleteProcessInstancesDto.getDeleteReason(),
        deleteProcessInstancesDto.isSkipCustomListeners());

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
      Batch batch = getProcessEngine().getManagementService().setJobRetriesAsync(
          setJobRetriesDto.getProcessInstances(),
          processInstanceQuery,
          setJobRetriesDto.getRetries().intValue()
      );
      return BatchDto.fromBatch(batch);
    } catch (BadUserRequestException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e.getMessage());
    }
  }

  @Override
  public BatchDto setRetriesByProcessHistoricQueryBased(SetJobRetriesByProcessDto setJobRetriesDto) {
    try {
      EnsureUtil.ensureNotNull("setJobRetriesDto", setJobRetriesDto);
      EnsureUtil.ensureNotNull("retries", setJobRetriesDto.getRetries());
    } catch (NullValueException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e.getMessage());
    }

    ManagementService managementService = getProcessEngine().getManagementService();

    List<HistoricProcessInstance> historicProcessInstances = null;
    if (setJobRetriesDto.getHistoricProcessInstanceQuery() != null) {
      historicProcessInstances = setJobRetriesDto.getHistoricProcessInstanceQuery().toQuery(getProcessEngine()).list();
    }

    List<String> processInstanceIds = new ArrayList<String>();
    if (historicProcessInstances != null) {
      for (HistoricProcessInstance historicProcessInstance: historicProcessInstances) {
        processInstanceIds.add(historicProcessInstance.getId());
      }
    }

    if (setJobRetriesDto.getProcessInstances() != null) {
      processInstanceIds.addAll(setJobRetriesDto.getProcessInstances());
    }

    Batch batch = null;

    try {

      batch = managementService.setJobRetriesAsync(
        processInstanceIds.isEmpty() ? null : processInstanceIds,
        setJobRetriesDto.getRetries());

      return BatchDto.fromBatch(batch);
    } catch (BadUserRequestException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e.getMessage());
    }
  }

  @Override
  public void restartProcessInstance(RestartProcessInstanceDto restartProcessInstanceDto) {
    try {
      createRestartProcessInstanceBuilder(restartProcessInstanceDto).execute();
    } catch (BadUserRequestException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e.getMessage());
    }
  }

  @Override
  public BatchDto restartProcessInstanceAsync(RestartProcessInstanceDto restartProcessInstanceDto) {
    Batch batch = null;
    try {
       batch = createRestartProcessInstanceBuilder(restartProcessInstanceDto).executeAsync();
    } catch (BadUserRequestException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e.getMessage());
    }
    return BatchDto.fromBatch(batch);
  }

  private RestartProcessInstanceBuilder createRestartProcessInstanceBuilder(RestartProcessInstanceDto restartProcessInstanceDto) {
    RuntimeService runtimeService = getProcessEngine().getRuntimeService();
    RestartProcessInstanceBuilder builder = runtimeService
        .restartProcessInstances(restartProcessInstanceDto.getProcessDefinitionId())
        .processInstanceIds(restartProcessInstanceDto.getProcessInstanceIds());
    if (restartProcessInstanceDto.getHistoricProcessInstanceQuery() != null) {
      builder.historicProcessInstanceQuery(restartProcessInstanceDto.getHistoricProcessInstanceQuery().toQuery(processEngine));
    }
    restartProcessInstanceDto.applyTo(builder, getProcessEngine(), objectMapper);
    return builder;
  }

}
