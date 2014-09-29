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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  public static final String DTO_MAPPING = "dto";
  public static final String HAL_MAPPING = "hal";
  public static final String HAL_LIST_MAPPING = "hal-list";

  public static final Map<Class<?>, Map<String, Class<?>>> ENTITY_MAPPING = new HashMap<Class<?>, Map<String, Class<?>>>();
  public static final Map<String, Class<? extends AbstractQueryDto<?>>> QUERY_MAPPING = new HashMap<String, Class<? extends AbstractQueryDto<?>>>();

  static {
    // Task
    Map<String, Class<?>> mapping = new HashMap<String, Class<?>>();
    mapping.put(DTO_MAPPING, TaskDto.class);
    mapping.put(HAL_MAPPING, HalTask.class);
    mapping.put(HAL_LIST_MAPPING, HalTaskList.class);
    ENTITY_MAPPING.put(TaskEntity.class, mapping);

    QUERY_MAPPING.put(EntityTypes.TASK, TaskQueryDto.class);
  }

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
      Method fromEntity = getMethodFromMapping(entity.getClass(), DTO_MAPPING, "fromEntity");
      return invokeMappingMethod(fromEntity, entity);
    }
    else {
      return null;
    }
  }

  public HalResource queryHalSingleResult(String extendingQuery) {
    Object entity = executeFilterSingleResult(extendingQuery);

    if (entity != null) {
      Method fromEntity = getMethodFromMapping(entity.getClass(), HAL_MAPPING, "generate");
      return (HalResource) invokeMappingMethod(fromEntity, entity, processEngine);
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
      Method fromEntity = getMethodFromMapping(entities.get(0).getClass(), DTO_MAPPING, "fromEntity");
      List<Object> results = new ArrayList<Object>();
      for (Object entity : entities) {
        results.add(invokeMappingMethod(fromEntity, entity));
      }
      return results;
    }
    else {
      return Collections.emptyList();
    }
  }

  public HalResource queryHalList(String extendingQuery, Integer firstResult, Integer maxResults) {
    List<?> entities = executeFilterList(extendingQuery, firstResult, maxResults);

    if (entities != null && !entities.isEmpty()) {
      long count = executeFilterCount(null);
      Method fromEntityList = getMethodFromMapping(entities.get(0).getClass(), HAL_LIST_MAPPING, "generate");
      return (HalResource) invokeMappingMethod(fromEntityList, entities, count, processEngine);
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

  protected Method getMethodFromMapping(Class<?> entityClass, String mappingKey, String methodName) {
    Map<String, Class<?>> mapping = ENTITY_MAPPING.get(entityClass);
    if (mapping != null) {
      Class<?> mapClass = mapping.get(mappingKey);
      if (mapClass != null) {
        for (Method method : mapClass.getMethods()) {
          if (method.getName().equals(methodName)) {
            return method;
          }
        }
        throw new InvalidRequestException(Status.INTERNAL_SERVER_ERROR, "Unable to find method '" + methodName + "' of class '" + mapClass.getCanonicalName() + "'");
      }
      else {
        throw new InvalidRequestException(Status.INTERNAL_SERVER_ERROR, "No mapping '" + mappingKey + "' for class '" + entityClass.getCanonicalName() + "' defined");
      }
    }
    else {
      throw new InvalidRequestException(Status.INTERNAL_SERVER_ERROR, "Unable to find mapping for class '" + entityClass.getCanonicalName() + "'");
    }
  }

  protected Object invokeMappingMethod(Method mappingMethod, Object... arguments) {
    try {
      return mappingMethod.invoke(null, arguments);
    } catch (IllegalAccessException e) {
      throw new InvalidRequestException(Status.INTERNAL_SERVER_ERROR, e, "Unable to access method '" + mappingMethod.getName() + "'");
    } catch (InvocationTargetException e) {
      throw new InvalidRequestException(Status.INTERNAL_SERVER_ERROR, e, "Unable to invoke method '" + mappingMethod.getName() + "'");
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

  protected Query<?, ?> convertQuery(String queryString) {
    if (queryString == null || queryString.trim().isEmpty() || EMPTY_JSON_BODY.matcher(queryString).matches()) {
      return null;
    }
    else {
      String resourceType = getDbFilter().getResourceType();
      Class<? extends AbstractQueryDto<?>> queryDtoClass = QUERY_MAPPING.get(resourceType);
      if (queryDtoClass != null) {
        try {
          AbstractQueryDto<?> queryDto = objectMapper.readValue(queryString, queryDtoClass);
          if (queryDto != null) {
            return queryDto.toQuery(processEngine);
          }
          else {
            return null;
          }
        } catch (IOException e) {
          throw new InvalidRequestException(Status.BAD_REQUEST, e, "Unable to convert query of type '" + resourceType + "' to query dto class '" + queryDtoClass.getCanonicalName() + "'");
        }
      }
      else {
        throw new InvalidRequestException(Status.BAD_REQUEST, "Unsupported filter type '" + resourceType + "'");
      }
    }
  }

}
