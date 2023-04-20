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
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery;
import org.camunda.bpm.engine.externaltask.UpdateExternalTaskRetriesBuilder;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.rest.ExternalTaskRestService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.batch.BatchDto;
import org.camunda.bpm.engine.rest.dto.externaltask.ExternalTaskDto;
import org.camunda.bpm.engine.rest.dto.externaltask.ExternalTaskQueryDto;
import org.camunda.bpm.engine.rest.dto.externaltask.FetchExternalTasksExtendedDto;
import org.camunda.bpm.engine.rest.dto.externaltask.SetRetriesForExternalTasksDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceQueryDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceQueryDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.spi.FetchAndLockHandler;
import org.camunda.bpm.engine.rest.sub.externaltask.ExternalTaskResource;
import org.camunda.bpm.engine.rest.sub.externaltask.impl.ExternalTaskResourceImpl;
import org.camunda.bpm.engine.rest.util.QueryUtil;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;

/**
 * @author Thorben Lindhauer
 *
 */
public class ExternalTaskRestServiceImpl extends AbstractRestProcessEngineAware implements ExternalTaskRestService {

  public ExternalTaskRestServiceImpl(String processEngine, ObjectMapper objectMapper) {
    super(processEngine, objectMapper);
  }

  @Override
  public List<ExternalTaskDto> getExternalTasks(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    ExternalTaskQueryDto queryDto = new ExternalTaskQueryDto(getObjectMapper(), uriInfo.getQueryParameters());
    return queryExternalTasks(queryDto, firstResult, maxResults);
  }

  @Override
  public List<ExternalTaskDto> queryExternalTasks(ExternalTaskQueryDto queryDto, Integer firstResult, Integer maxResults) {
    ProcessEngine engine = getProcessEngine();
    queryDto.setObjectMapper(getObjectMapper());
    ExternalTaskQuery query = queryDto.toQuery(engine);

    List<ExternalTask> matchingTasks = QueryUtil.list(query, firstResult, maxResults);

    List<ExternalTaskDto> taskResults = new ArrayList<>();
    for (ExternalTask task : matchingTasks) {
      ExternalTaskDto resultInstance = ExternalTaskDto.fromExternalTask(task);
      taskResults.add(resultInstance);
    }
    return taskResults;
  }

  @Override
  public CountResultDto getExternalTasksCount(UriInfo uriInfo) {
    ExternalTaskQueryDto queryDto = new ExternalTaskQueryDto(getObjectMapper(), uriInfo.getQueryParameters());
    return queryExternalTasksCount(queryDto);
  }

  @Override
  public CountResultDto queryExternalTasksCount(ExternalTaskQueryDto queryDto) {
    ProcessEngine engine = getProcessEngine();
    queryDto.setObjectMapper(getObjectMapper());
    ExternalTaskQuery query = queryDto.toQuery(engine);

    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);

    return result;
  }

  @Override
  public void fetchAndLock(FetchExternalTasksExtendedDto dto, AsyncResponse asyncResponse) {
    FetchAndLockHandler fetchAndLockHandler = FetchAndLockContextListener.getFetchAndLockHandler();
    fetchAndLockHandler.addPendingRequest(dto, asyncResponse, getProcessEngine());
  }

  @Override
  public ExternalTaskResource getExternalTask(String externalTaskId) {
    return new ExternalTaskResourceImpl(getProcessEngine(), externalTaskId, getObjectMapper());
  }

  @Override
  public BatchDto setRetriesAsync(SetRetriesForExternalTasksDto retriesDto) {

    UpdateExternalTaskRetriesBuilder builder = updateRetries(retriesDto);
    Integer retries = retriesDto.getRetries();

    if (retries == null) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "The number of retries cannot be null.");
    }

    try {
      Batch batch = builder.setAsync(retries);
      return BatchDto.fromBatch(batch);
    }
    catch (NotFoundException e) {
      throw new InvalidRequestException(Status.NOT_FOUND, e.getMessage());
    }
    catch (BadUserRequestException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e.getMessage());
    }

  }

  @Override
  public List<String> getTopicNames(boolean withLockedTasks, boolean withUnlockedTasks,
                                    boolean withRetriesLeft) {
    return getProcessEngine().getExternalTaskService()
                        .getTopicNames(withLockedTasks, withUnlockedTasks, withRetriesLeft);
  }

  @Override
  public void setRetries(SetRetriesForExternalTasksDto retriesDto){

    UpdateExternalTaskRetriesBuilder builder = updateRetries(retriesDto);
    Integer retries = retriesDto.getRetries();

    if (retries == null) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "The number of retries cannot be null.");
    }

    try {
      builder.set(retries);
    }
    catch (NotFoundException e) {
      throw new InvalidRequestException(Status.NOT_FOUND, e.getMessage());
    }
    catch (BadUserRequestException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e.getMessage());
    }
  }

  protected UpdateExternalTaskRetriesBuilder updateRetries(SetRetriesForExternalTasksDto retriesDto) {

    ExternalTaskService externalTaskService = getProcessEngine().getExternalTaskService();

    List<String> externalTaskIds = retriesDto.getExternalTaskIds();
    List<String> processInstanceIds = retriesDto.getProcessInstanceIds();

    ExternalTaskQuery externalTaskQuery = null;
    ProcessInstanceQuery processInstanceQuery = null;
    HistoricProcessInstanceQuery historicProcessInstanceQuery = null;

    ExternalTaskQueryDto externalTaskQueryDto = retriesDto.getExternalTaskQuery();
    if (externalTaskQueryDto != null) {
      externalTaskQuery = externalTaskQueryDto.toQuery(getProcessEngine());
    }

    ProcessInstanceQueryDto processInstanceQueryDto = retriesDto.getProcessInstanceQuery();
    if (processInstanceQueryDto != null) {
      processInstanceQuery = processInstanceQueryDto.toQuery(getProcessEngine());
    }

    HistoricProcessInstanceQueryDto historicProcessInstanceQueryDto = retriesDto.getHistoricProcessInstanceQuery();
    if (historicProcessInstanceQueryDto != null) {
      historicProcessInstanceQuery = historicProcessInstanceQueryDto.toQuery(getProcessEngine());
    }

    return externalTaskService.updateRetries()
      .externalTaskIds(externalTaskIds)
      .processInstanceIds(processInstanceIds)
      .externalTaskQuery(externalTaskQuery)
      .processInstanceQuery(processInstanceQuery)
      .historicProcessInstanceQuery(historicProcessInstanceQuery);
  }

}
