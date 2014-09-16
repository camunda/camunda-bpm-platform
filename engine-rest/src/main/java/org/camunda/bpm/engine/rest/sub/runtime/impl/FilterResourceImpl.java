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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.FilterService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.runtime.FilterDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.hal.EmptyHalCollection;
import org.camunda.bpm.engine.rest.hal.EmptyHalResource;
import org.camunda.bpm.engine.rest.hal.HalResource;
import org.camunda.bpm.engine.rest.hal.task.HalTask;
import org.camunda.bpm.engine.rest.hal.task.HalTaskList;
import org.camunda.bpm.engine.rest.sub.runtime.FilterResource;

/**
 * @author Sebastian Menski
 */
public class FilterResourceImpl implements FilterResource {

  protected ProcessEngine engine;
  protected String filterId;

  protected FilterService filterService;

  public static final String DTO_MAPPING = "dto";
  public static final String HAL_MAPPING = "hal";
  public static final String HAL_LIST_MAPPING = "hal-list";

  public static final Map<Class<?>, Map<String, Class<?>>> ENTITY_MAPPING = new HashMap<Class<?>, Map<String, Class<?>>>();

  static {
    // Task
    Map<String, Class<?>> mapping = new HashMap<String, Class<?>>();
    mapping.put(DTO_MAPPING, TaskDto.class);
    mapping.put(HAL_MAPPING, HalTask.class);
    mapping.put(HAL_LIST_MAPPING, HalTaskList.class);
    ENTITY_MAPPING.put(TaskEntity.class, mapping);
  }

  public FilterResourceImpl(ProcessEngine engine, String filterId) {
    this.engine = engine;
    this.filterId = filterId;
    filterService = engine.getFilterService();
  }

  public FilterDto getFilter() {
    Filter filter = getDbFilter();
    return FilterDto.fromFilter(filter);
  }

  public void deleteFilter() {
    try {
      filterService.deleteFilter(filterId);
    }
    catch (NullValueException e) {
      throw new InvalidRequestException(Status.NOT_FOUND, e, "No filter found for id '" + filterId + "'");
    }
  }

  public void updateFilter(FilterDto filterDto) {
    Filter filter = getDbFilter();

    try {
      filterDto.updateFilter(filter);
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
      return (HalResource) invokeMappingMethod(fromEntity, entity, engine);
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
    List<Object> entities = executeFilterList(extendingQuery, firstResult, maxResults);

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
    List<Object> entities = executeFilterList(extendingQuery, firstResult, maxResults);

    if (entities != null && !entities.isEmpty()) {
      long count = executeFilterCount(null);
      Method fromEntityList = getMethodFromMapping(entities.get(0).getClass(), HAL_LIST_MAPPING, "generate");
      return (HalResource) invokeMappingMethod(fromEntityList, entities, count, engine);
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
      return filterService.singleResult(filterId, extendingQuery);
    }
    catch (NullValueException e) {
      throw new InvalidRequestException(Status.NOT_FOUND, e, "Filter with id '" + filterId + "' does not exist.");
    }
    catch (NotValidException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e, "Filter cannot be extended by an invalid query");
    }
    catch (ProcessEngineException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e, "Filter does not returns a valid single result");
    }
  }

  protected List<Object> executeFilterList(String extendingQuery, Integer firstResult, Integer maxResults) {
    try {
      if (firstResult != null || maxResults != null) {
        if (firstResult == null) {
          firstResult = 0;
        }
        if (maxResults == null) {
          maxResults = Integer.MAX_VALUE;
        }
        return filterService.listPage(filterId, extendingQuery, firstResult, maxResults);
      } else {
        return filterService.list(filterId, extendingQuery);
      }
    }
    catch (NullValueException e) {
      throw new InvalidRequestException(Status.NOT_FOUND, e, "Filter with id '" + filterId + "' does not exist.");
    }
    catch (NotValidException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e, "Filter cannot be extended by an invalid query");
    }
  }

  protected long executeFilterCount(String extendingQuery) {
    try {
      return filterService.count(filterId, extendingQuery);
    }
    catch (NullValueException e) {
      throw new InvalidRequestException(Status.NOT_FOUND, e, "Filter with id '" + filterId + "' does not exist.");
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
      .getFilter(filterId);

    if (filter == null) {
      throw new InvalidRequestException(Status.NOT_FOUND, "Filter with id '" + filterId + "' does not exist.");
    }
    return filter;
  }
}
