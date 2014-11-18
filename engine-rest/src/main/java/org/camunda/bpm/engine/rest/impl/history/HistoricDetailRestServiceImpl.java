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
package org.camunda.bpm.engine.rest.impl.history;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricDetailQuery;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricDetailDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricDetailQueryDto;
import org.camunda.bpm.engine.rest.history.HistoricDetailRestService;
import org.camunda.bpm.engine.rest.sub.history.HistoricDetailResource;
import org.camunda.bpm.engine.rest.sub.history.impl.HistoricDetailResourceImpl;

import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Roman Smirnov
 *
 */
public class HistoricDetailRestServiceImpl implements HistoricDetailRestService {

  protected ObjectMapper objectMapper;
  protected ProcessEngine processEngine;

  public HistoricDetailRestServiceImpl(ObjectMapper objectMapper, ProcessEngine processEngine) {
    this.objectMapper = objectMapper;
    this.processEngine = processEngine;
  }

  public HistoricDetailResource historicDetail(String detailId) {
    return new HistoricDetailResourceImpl(detailId, processEngine);
  }

  @Override
  public List<HistoricDetailDto> getHistoricDetails(UriInfo uriInfo, Integer firstResult,
      Integer maxResults, boolean deserializeObjectValues) {
    HistoricDetailQueryDto queryDto = new HistoricDetailQueryDto(objectMapper, uriInfo.getQueryParameters());
    HistoricDetailQuery query = queryDto.toQuery(processEngine);
    query.disableBinaryFetching();

    if (!deserializeObjectValues) {
      query.disableCustomObjectDeserialization();
    }

    List<HistoricDetail> queryResult;
    if (firstResult != null || maxResults != null) {
      queryResult = executePaginatedQuery(query, firstResult, maxResults);
    } else {
      queryResult = query.list();
    }

    List<HistoricDetailDto> result = new ArrayList<HistoricDetailDto>();
    for (HistoricDetail historicDetail : queryResult) {
      HistoricDetailDto dto = HistoricDetailDto.fromHistoricDetail(historicDetail);
      result.add(dto);
    }

    return result;
  }

  @Override
  public CountResultDto getHistoricDetailsCount(UriInfo uriInfo) {
    HistoricDetailQueryDto queryDto = new HistoricDetailQueryDto(objectMapper, uriInfo.getQueryParameters());
    HistoricDetailQuery query = queryDto.toQuery(processEngine);

    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);

    return result;
  }

  private List<HistoricDetail> executePaginatedQuery(HistoricDetailQuery query, Integer firstResult, Integer maxResults) {
    if (firstResult == null) {
      firstResult = 0;
    }
    if (maxResults == null) {
      maxResults = Integer.MAX_VALUE;
    }
    return query.listPage(firstResult, maxResults);
  }

}
