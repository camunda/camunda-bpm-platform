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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.VariableInstanceRestService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.runtime.VariableInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.VariableInstanceQueryDto;
import org.camunda.bpm.engine.rest.sub.runtime.VariableInstanceResource;
import org.camunda.bpm.engine.rest.sub.runtime.impl.VariableInstanceResourceImpl;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.runtime.VariableInstanceQuery;

import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

public class VariableInstanceRestServiceImpl extends AbstractRestProcessEngineAware implements VariableInstanceRestService {

  public VariableInstanceRestServiceImpl(String engineName, ObjectMapper objectMapper) {
    super(engineName, objectMapper);
  }

  @Override
  public VariableInstanceResource getVariableInstance(String id) {
    return new VariableInstanceResourceImpl(id, processEngine);
  }

  @Override
  public List<VariableInstanceDto> getVariableInstances(UriInfo uriInfo, Integer firstResult, Integer maxResults, boolean deserializeObjectValues) {
    VariableInstanceQueryDto queryDto = new VariableInstanceQueryDto(getObjectMapper(), uriInfo.getQueryParameters());
    return queryVariableInstances(queryDto, firstResult, maxResults, deserializeObjectValues);
  }

  @Override
  public List<VariableInstanceDto> queryVariableInstances(VariableInstanceQueryDto queryDto, Integer firstResult, Integer maxResults, boolean deserializeObjectValues) {
    ProcessEngine engine = getProcessEngine();
    queryDto.setObjectMapper(getObjectMapper());
    VariableInstanceQuery query = queryDto.toQuery(engine);

    // disable binary fetching by default.
    query.disableBinaryFetching();

    // disable custom object fetching by default. Cannot be done to not break existing API
    if (!deserializeObjectValues) {
      query.disableCustomObjectDeserialization();
    }

    List<VariableInstance> matchingInstances;
    if (firstResult != null || maxResults != null) {
      matchingInstances = executePaginatedQuery(query, firstResult, maxResults);
    } else {
      matchingInstances = query.list();
    }

    List<VariableInstanceDto> instanceResults = new ArrayList<VariableInstanceDto>();
    for (VariableInstance instance : matchingInstances) {
      VariableInstanceDto resultInstance = VariableInstanceDto.fromVariableInstance(instance);
      instanceResults.add(resultInstance);
    }
    return instanceResults;
  }

  private List<VariableInstance> executePaginatedQuery(VariableInstanceQuery query, Integer firstResult, Integer maxResults) {
    if (firstResult == null) {
      firstResult = 0;
    }
    if (maxResults == null) {
      maxResults = Integer.MAX_VALUE;
    }
    return query.listPage(firstResult, maxResults);
  }

  @Override
  public CountResultDto getVariableInstancesCount(UriInfo uriInfo) {
    VariableInstanceQueryDto queryDto = new VariableInstanceQueryDto(getObjectMapper(), uriInfo.getQueryParameters());
    return queryVariableInstancesCount(queryDto);
  }

  @Override
  public CountResultDto queryVariableInstancesCount(VariableInstanceQueryDto queryDto) {
    ProcessEngine engine = getProcessEngine();
    queryDto.setObjectMapper(getObjectMapper());
    VariableInstanceQuery query = queryDto.toQuery(engine);

    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);

    return result;
  }

}
