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

package org.camunda.bpm.engine.rest.dto.runtime;

import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.query.Query;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;

public class FilterDto {

  protected String id;
  protected String resourceType;
  protected String name;
  protected String owner;
  protected JsonNode query;
  protected AbstractQueryDto<?> resolvedQuery;
  protected Map<String, Object> properties;

  protected Long itemCount;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getResourceType() {
    return resourceType;
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public JsonNode getQuery() {
    return query;
  }

  public AbstractQueryDto<?> resolveQuery(ObjectMapper objectMapper) {
    if (resolvedQuery == null) {
      try {
        this.resolvedQuery = objectMapper.readValue(query, TaskQueryDto.class);
      } catch (Exception e) {
        throw new InvalidRequestException(Status.BAD_REQUEST, e, "Unable to convert query");
      }
    }

    return resolvedQuery;
  }

  public void setQuery(JsonNode query) {
    this.query = query;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, Object> properties) {
    this.properties = properties;
  }

  @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
  public Long getItemCount() {
    return itemCount;
  }

  public void setItemCount(Long itemCount) {
    this.itemCount = itemCount;
  }

  public static FilterDto fromFilter(Filter filter, ObjectMapper objectMapper) {
    FilterDto dto = new FilterDto();
    dto.id = filter.getId();
    dto.resourceType = filter.getResourceType();
    dto.name = filter.getName();
    dto.owner = filter.getOwner();

    if (EntityTypes.TASK.equals(filter.getResourceType())) {
      dto.query = objectMapper.valueToTree(TaskQueryDto.fromQuery(filter.getQuery()));
    }

    dto.properties = filter.getProperties();
    return dto;
  }

  public void updateFilter(Filter filter, ProcessEngine engine, ObjectMapper objectMapper) {
    if (getResourceType() != null && !getResourceType().equals(filter.getResourceType())) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "Unable to update filter from resource type '" + filter.getResourceType() + "' to '" + getResourceType() + "'");
    }
    filter.setName(getName());
    filter.setOwner(getOwner());
    Query<?, ?> query = resolveQuery(objectMapper).toQuery(engine);
    filter.setQuery(query);
    filter.setProperties(getProperties());
  }

}
