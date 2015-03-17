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

package org.camunda.bpm.engine.rest.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.FilterService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.filter.FilterQuery;
import org.camunda.bpm.engine.rest.FilterRestService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.ResourceOptionsDto;
import org.camunda.bpm.engine.rest.dto.runtime.FilterDto;
import org.camunda.bpm.engine.rest.dto.runtime.FilterQueryDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.sub.runtime.FilterResource;
import org.camunda.bpm.engine.rest.sub.runtime.impl.FilterResourceImpl;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.CREATE;
import static org.camunda.bpm.engine.authorization.Resources.FILTER;


/**
 * @author Sebastian Menski
 */
public class FilterRestServiceImpl extends AbstractAuthorizedRestResource implements FilterRestService {

  public FilterRestServiceImpl(String engineName, ObjectMapper objectMapper) {
    super(engineName, FILTER, ANY, objectMapper);
  }

  public FilterResource getFilter(String filterId) {
    return new FilterResourceImpl(getProcessEngine().getName(), getObjectMapper(), filterId, relativeRootResourcePath);
  }

  public List<FilterDto> getFilters(UriInfo uriInfo, Boolean itemCount, Integer firstResult, Integer maxResults) {
    FilterService filterService = getProcessEngine().getFilterService();
    FilterQuery query = getQueryFromQueryParameters(uriInfo.getQueryParameters());

    List<Filter> matchingFilters = executeFilterQuery(query, firstResult, maxResults);

    List<FilterDto> filters = new ArrayList<FilterDto>();
    for (Filter filter : matchingFilters) {
      FilterDto dto = FilterDto.fromFilter(filter);
      if (itemCount != null && itemCount) {
        dto.setItemCount(filterService.count(filter.getId()));
      }
      filters.add(dto);
    }

    return filters;
  }

  public List<Filter> executeFilterQuery(FilterQuery query, Integer firstResult, Integer maxResults) {
    if (firstResult != null || maxResults != null) {
      return executePaginatedQuery(query, firstResult, maxResults);
    }
    else {
      return query.list();
    }
  }

  protected List<Filter> executePaginatedQuery(FilterQuery query, Integer firstResult, Integer maxResults) {
    if (firstResult == null) {
      firstResult = 0;
    }
    if (maxResults == null) {
      maxResults = Integer.MAX_VALUE;
    }
    return query.listPage(firstResult, maxResults);
  }

  public CountResultDto getFiltersCount(UriInfo uriInfo) {
    FilterQuery query = getQueryFromQueryParameters(uriInfo.getQueryParameters());
    return new CountResultDto(query.count());
  }

  public FilterDto createFilter(FilterDto filterDto) {
    FilterService filterService = getProcessEngine().getFilterService();

    String resourceType = filterDto.getResourceType();

    Filter filter;

    if (EntityTypes.TASK.equals(resourceType)) {
      filter = filterService.newTaskFilter();
    }
    else {
      throw new InvalidRequestException(Response.Status.BAD_REQUEST, "Unable to create filter with invalid resource type '" + resourceType + "'");
    }

    try {
      filterDto.updateFilter(filter, getProcessEngine());
    }
    catch (NotValidException e) {
      throw new InvalidRequestException(Response.Status.BAD_REQUEST, e, "Unable to create filter with invalid content");
    }

    filterService.saveFilter(filter);

    return FilterDto.fromFilter(filter);
  }

  protected FilterQuery getQueryFromQueryParameters(MultivaluedMap<String, String> queryParameters) {
    ProcessEngine engine = getProcessEngine();
    FilterQueryDto queryDto = new FilterQueryDto(getObjectMapper(), queryParameters);
    return queryDto.toQuery(engine);
  }

  public ResourceOptionsDto availableOperations(UriInfo context) {

    UriBuilder baseUriBuilder = context.getBaseUriBuilder()
      .path(relativeRootResourcePath)
      .path(FilterRestService.PATH);

    ResourceOptionsDto resourceOptionsDto = new ResourceOptionsDto();

    // GET /
    URI baseUri = baseUriBuilder.build();
    resourceOptionsDto.addReflexiveLink(baseUri, HttpMethod.GET, "list");

    // GET /count
    URI countUri = baseUriBuilder.clone().path("/count").build();
    resourceOptionsDto.addReflexiveLink(countUri, HttpMethod.GET, "count");

    // POST /create
    if (isAuthorized(CREATE)) {
      URI createUri = baseUriBuilder.clone().path("/create").build();
      resourceOptionsDto.addReflexiveLink(createUri, HttpMethod.POST, "create");
    }

    return resourceOptionsDto;
  }

}
