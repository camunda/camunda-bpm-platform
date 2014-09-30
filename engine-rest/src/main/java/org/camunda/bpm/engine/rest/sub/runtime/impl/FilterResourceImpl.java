/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.engine.rest.sub.runtime.impl;

import static org.camunda.bpm.engine.authorization.Permissions.DELETE;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Resources.FILTER;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.FilterService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.query.Query;
import org.camunda.bpm.engine.rest.FilterRestService;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.ResourceOptionsDto;
import org.camunda.bpm.engine.rest.dto.runtime.FilterDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.hal.EmptyHalCollection;
import org.camunda.bpm.engine.rest.hal.EmptyHalResource;
import org.camunda.bpm.engine.rest.hal.HalResource;
import org.camunda.bpm.engine.rest.hal.task.HalTask;
import org.camunda.bpm.engine.rest.hal.task.HalTaskList;
import org.camunda.bpm.engine.rest.impl.AbstractAuthorizedRestResource;
import org.camunda.bpm.engine.rest.sub.runtime.FilterResource;
import org.camunda.bpm.engine.task.Task;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * @author Sebastian Menski
 */
public class FilterResourceImpl extends AbstractAuthorizedRestResource implements FilterResource {

  protected ObjectMapper objectMapper;
  protected String filterId;

  protected FilterService filterService;
  protected String relativeRootResourcePath;

  public static final Pattern EMPTY_JSON_BODY = Pattern.compile("\\s*\\{\\s*\\}\\s*");

  public FilterResourceImpl(String processEngineName, ObjectMapper objectMapper, String filterId, String relativeRootResourcePath) {
    super(processEngineName, FILTER, filterId);
    this.relativeRootResourcePath = relativeRootResourcePath;
    this.objectMapper = objectMapper;
    this.filterId = filterId;
    filterService = processEngine.getFilterService();
  }

  public FilterDto getFilter(Boolean itemCount) {
    Filter filter = getDbFilter();
    FilterDto dto = FilterDto.fromFilter(filter);
    if (itemCount != null && itemCount) {
      dto.setItemCount(filterService.count(filter.getId()));
    }
    return dto;
  }

  public void deleteFilter() {
    try {
      filterService.deleteFilter(resourceId);
    }
    catch (NullValueException e) {
      throw new InvalidRequestException(Status.NOT_FOUND, e, "No filter found for id '" + resourceId + "'");
    }
  }

  public void updateFilter(FilterDto filterDto) {
    Filter filter = getDbFilter();

    try {
      filterDto.updateFilter(filter, processEngine);
    }
    catch (NotValidException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e, "Unable to update filter with invalid content");
    }

    filterService.saveFilter(filter);
  }

  public Object executeSingleResult() {
    return querySingleResult(null);
  }

  public HalResource executeHalSingleResult() {
    return queryHalSingleResult(null);
  }

  public Object querySingleResult(String extendingQuery) {
    Object entity = executeFilterSingleResult(extendingQuery);

    if (entity != null) {
      return convertToDto(entity);
    }
    else {
      return null;
    }
  }

  public HalResource queryHalSingleResult(String extendingQuery) {
    Object entity = executeFilterSingleResult(extendingQuery);

    if (entity != null) {
      return convertToHalResource(entity);
    }
    else {
      return EmptyHalResource.INSTANCE;
    }
  }

  public List<Object> executeList(Integer firstResult, Integer maxResults) {
    return queryList(null, firstResult, maxResults);
  }

  public HalResource executeHalList(Integer firstResult, Integer maxResults) {
    return queryHalList(null, firstResult, maxResults);
  }

  public List<Object> queryList(String extendingQuery, Integer firstResult, Integer maxResults) {
    List<?> entities = executeFilterList(extendingQuery, firstResult, maxResults);

    if (entities != null && !entities.isEmpty()) {
      return convertToDtoList(entities);
    }
    else {
      return Collections.emptyList();
    }
  }

  private List<Object> convertToDtoList(List<?> entities) {
    List<Object> dtoList = new ArrayList<Object>();
    for (Object entity : entities) {
      dtoList.add(convertToDto(entity));
    }
    return dtoList;
  }

  public HalResource queryHalList(String extendingQuery, Integer firstResult, Integer maxResults) {
    List<?> entities = executeFilterList(extendingQuery, firstResult, maxResults);

    if (entities != null && !entities.isEmpty()) {
      return convertToHalList(entities);
    }
    else {
      return EmptyHalCollection.INSTANCE;
    }
  }

  public CountResultDto executeCount() {
    return queryCount(null);
  }

  public CountResultDto queryCount(String extendingQuery) {
    return new CountResultDto(executeFilterCount(extendingQuery));
  }

  protected Object executeFilterSingleResult(String extendingQuery) {
    try {
      return  filterService.singleResult(filterId, convertQuery(extendingQuery));
    }
    catch (NullValueException e) {
      throw new InvalidRequestException(Status.NOT_FOUND, e, "Filter with id '" + resourceId + "' does not exist.");
    }
    catch (NotValidException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e, "Filter cannot be extended by an invalid query");
    }
    catch (ProcessEngineException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e, "Filter does not returns a valid single result");
    }
  }

  protected Query convertQuery(String queryString) {
    if (queryString == null || queryString.trim().isEmpty() || EMPTY_JSON_BODY.matcher(queryString).matches()) {
      return null;
    }
    else {
      String resourceType = getDbFilter().getResourceType();
      AbstractQueryDto<?> queryDto;
      try {
        if (EntityTypes.TASK.equals(resourceType)) {
          queryDto = objectMapper.readValue(queryString, TaskQueryDto.class);
        } else {
          throw new InvalidRequestException(Status.BAD_REQUEST, "Queries for resource type '" + resourceType + "' are currently not supported by filters.");
        }
      } catch (IOException e) {
        throw new InvalidRequestException(Status.BAD_REQUEST, e, "Invalid query for resource type '" + resourceType + "'");
      }

      if (queryDto != null) {
        return queryDto.toQuery(processEngine);
      } else {
        throw new InvalidRequestException(Status.BAD_REQUEST, "Unable to convert query for resource type '" + resourceType + "'.");
      }
    }
  }

  protected List<?> executeFilterList(String extendingQueryString, Integer firstResult, Integer maxResults) {
    Query<?, ?> extendingQuery = convertQuery(extendingQueryString);
    try {
      if (firstResult != null || maxResults != null) {
        if (firstResult == null) {
          firstResult = 0;
        }
        if (maxResults == null) {
          maxResults = Integer.MAX_VALUE;
        }
        return filterService.listPage(resourceId, extendingQuery, firstResult, maxResults);
      } else {
        return filterService.list(resourceId, extendingQuery);
      }
    }
    catch (NullValueException e) {
      throw new InvalidRequestException(Status.NOT_FOUND, e, "Filter with id '" + resourceId + "' does not exist.");
    }
    catch (NotValidException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e, "Filter cannot be extended by an invalid query");
    }
  }

  protected long executeFilterCount(String extendingQuery) {
    try {
      return filterService.count(filterId, convertQuery(extendingQuery));
    }
    catch (NullValueException e) {
      throw new InvalidRequestException(Status.NOT_FOUND, e, "Filter with id '" + resourceId + "' does not exist.");
    }
    catch (NotValidException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e, "Filter cannot be extended by an invalid query");
    }
  }

  protected Filter getDbFilter() {
    Filter filter = filterService
      .getFilter(resourceId);

    if (filter == null) {
      throw new InvalidRequestException(Status.NOT_FOUND, "Filter with id '" + resourceId + "' does not exist.");
    }
    return filter;
  }

  public ResourceOptionsDto availableOperations(UriInfo context) {

    ResourceOptionsDto dto = new ResourceOptionsDto();

    UriBuilder baseUriBuilder = context.getBaseUriBuilder()
        .path(relativeRootResourcePath)
        .path(FilterRestService.class)
        .path(resourceId);

    URI baseUri = baseUriBuilder.build();

    if (isAuthorized(READ)) {
      dto.addReflexiveLink(baseUri, HttpMethod.GET, "self");

      URI singleResultUri = baseUriBuilder.clone().path("/singleResult").build();
      dto.addReflexiveLink(singleResultUri, HttpMethod.GET, "singleResult");
      dto.addReflexiveLink(singleResultUri, HttpMethod.POST, "singleResult");

      URI listUri = baseUriBuilder.clone().path("/list").build();
      dto.addReflexiveLink(listUri, HttpMethod.GET, "list");
      dto.addReflexiveLink(listUri, HttpMethod.POST, "list");

      URI countUri = baseUriBuilder.clone().path("/count").build();
      dto.addReflexiveLink(countUri, HttpMethod.GET, "count");
      dto.addReflexiveLink(countUri, HttpMethod.POST, "count");
    }

    if (isAuthorized(DELETE)) {
      dto.addReflexiveLink(baseUri, HttpMethod.DELETE, "delete");
    }

    if (isAuthorized(UPDATE)) {
      dto.addReflexiveLink(baseUri, HttpMethod.PUT, "update");
    }

    return dto;
  }

  protected Object convertToDto(Object entity) {
    if (TaskEntity.class.equals(entity.getClass())) {
      return TaskDto.fromEntity((Task) entity);
    }
    else {
      throw new InvalidRequestException(Status.BAD_REQUEST, "Entities of class '" + entity.getClass().getCanonicalName() + "' are currently not supported by filters.");
    }
  }

  protected HalResource<?> convertToHalResource(Object entity) {
    if (TaskEntity.class.equals(entity.getClass())) {
      return HalTask.generate((Task) entity, processEngine);
    }
    else {
      throw new InvalidRequestException(Status.BAD_REQUEST, "Entities of class '" + entity.getClass().getCanonicalName() + "' are currently not supported by filters and HAL media type.");
    }
  }

  @SuppressWarnings("unchecked")
  protected HalResource convertToHalList(List<?> entities) {
    long count = executeFilterCount(null);

    if (TaskEntity.class.equals(entities.get(0).getClass())) {
      return HalTaskList.generate((List<Task>) entities, count, processEngine);
    }
    else {
      throw new InvalidRequestException(Status.BAD_REQUEST, "Entities of class '" + entities.get(0).getClass().getCanonicalName() + "' are currently not supported by filters and HAL media type.");
    }
  }

}
