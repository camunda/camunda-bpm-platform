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
import org.camunda.bpm.engine.rest.CaseInstanceRestService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.runtime.CaseInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.CaseInstanceQueryDto;
import org.camunda.bpm.engine.rest.sub.runtime.CaseInstanceResource;
import org.camunda.bpm.engine.rest.sub.runtime.impl.CaseInstanceResourceImpl;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.CaseInstanceQuery;

import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Roman Smirnov
 *
 */
public class CaseInstanceRestServiceImpl extends AbstractRestProcessEngineAware implements CaseInstanceRestService {

  public CaseInstanceRestServiceImpl(String engineName, ObjectMapper objectMapper) {
    super(engineName, objectMapper);
  }

  public CaseInstanceResource getCaseInstance(String caseInstanceId) {
    return new CaseInstanceResourceImpl(getProcessEngine(), caseInstanceId, getObjectMapper());
  }

  public List<CaseInstanceDto> getCaseInstances(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    CaseInstanceQueryDto queryDto = new CaseInstanceQueryDto(getObjectMapper(), uriInfo.getQueryParameters());
    return queryCaseInstances(queryDto, firstResult, maxResults);
  }

  public List<CaseInstanceDto> queryCaseInstances(CaseInstanceQueryDto queryDto, Integer firstResult, Integer maxResults) {
    ProcessEngine engine = getProcessEngine();
    queryDto.setObjectMapper(getObjectMapper());
    CaseInstanceQuery query = queryDto.toQuery(engine);

    List<CaseInstance> matchingInstances;
    if (firstResult != null || maxResults != null) {
      matchingInstances = executePaginatedQuery(query, firstResult, maxResults);
    } else {
      matchingInstances = query.list();
    }

    List<CaseInstanceDto> instanceResults = new ArrayList<CaseInstanceDto>();
    for (CaseInstance instance : matchingInstances) {
      CaseInstanceDto resultInstance = CaseInstanceDto.fromCaseInstance(instance);
      instanceResults.add(resultInstance);
    }
    return instanceResults;
  }

  private List<CaseInstance> executePaginatedQuery(CaseInstanceQuery query, Integer firstResult, Integer maxResults) {
    if (firstResult == null) {
      firstResult = 0;
    }
    if (maxResults == null) {
      maxResults = Integer.MAX_VALUE;
    }
    return query.listPage(firstResult, maxResults);
  }

  public CountResultDto getCaseInstancesCount(UriInfo uriInfo) {
    CaseInstanceQueryDto queryDto = new CaseInstanceQueryDto(getObjectMapper(), uriInfo.getQueryParameters());
    return queryCaseInstancesCount(queryDto);
  }

  public CountResultDto queryCaseInstancesCount(CaseInstanceQueryDto queryDto) {
    ProcessEngine engine = getProcessEngine();
    queryDto.setObjectMapper(getObjectMapper());
    CaseInstanceQuery query = queryDto.toQuery(engine);

    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);

    return result;
  }

}
