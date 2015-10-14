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

import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery;
import org.camunda.bpm.engine.externaltask.ExternalTaskQueryBuilder;
import org.camunda.bpm.engine.externaltask.ExternalTaskQueryTopicBuilder;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.rest.ExternalTaskRestService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.externaltask.ExternalTaskDto;
import org.camunda.bpm.engine.rest.dto.externaltask.ExternalTaskQueryDto;
import org.camunda.bpm.engine.rest.dto.externaltask.FetchExternalTasksDto;
import org.camunda.bpm.engine.rest.dto.externaltask.FetchExternalTasksDto.FetchExternalTaskTopicDto;
import org.camunda.bpm.engine.rest.dto.externaltask.LockedExternalTaskDto;
import org.camunda.bpm.engine.rest.sub.externaltask.ExternalTaskResource;
import org.camunda.bpm.engine.rest.sub.externaltask.impl.ExternalTaskResourceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;

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

    List<ExternalTask> matchingTasks;
    if (firstResult != null || maxResults != null) {
      matchingTasks = executePaginatedQuery(query, firstResult, maxResults);
    } else {
      matchingTasks = query.list();
    }

    List<ExternalTaskDto> taskResults = new ArrayList<ExternalTaskDto>();
    for (ExternalTask task : matchingTasks) {
      ExternalTaskDto resultInstance = ExternalTaskDto.fromExternalTask(task);
      taskResults.add(resultInstance);
    }
    return taskResults;
  }

  protected List<ExternalTask> executePaginatedQuery(ExternalTaskQuery query, Integer firstResult, Integer maxResults) {
    if (firstResult == null) {
      firstResult = 0;
    }
    if (maxResults == null) {
      maxResults = Integer.MAX_VALUE;
    }
    return query.listPage(firstResult, maxResults);
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
  public List<LockedExternalTaskDto> fetchAndLock(FetchExternalTasksDto fetchingDto) {
    ExternalTaskQueryBuilder fetchBuilder = processEngine
      .getExternalTaskService()
      .fetchAndLock(fetchingDto.getMaxTasks(), fetchingDto.getWorkerId());

    if (fetchingDto.getTopics() != null) {
      for (FetchExternalTaskTopicDto topicDto : fetchingDto.getTopics()) {
        ExternalTaskQueryTopicBuilder topicFetchBuilder =
            fetchBuilder.topic(topicDto.getTopicName(), topicDto.getLockDuration());

        if (topicDto.getVariables() != null) {
          topicFetchBuilder.variables(topicDto.getVariables());
          fetchBuilder = topicFetchBuilder;
        }
      }
    }

    List<LockedExternalTask> tasks = fetchBuilder.execute();

    return LockedExternalTaskDto.fromLockedExternalTasks(tasks);
  }

  @Override
  public ExternalTaskResource getExternalTask(String externalTaskId) {
    return new ExternalTaskResourceImpl(getProcessEngine(), externalTaskId, getObjectMapper());

  }

}
