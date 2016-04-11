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

package org.camunda.bpm.engine.rest.impl.history;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.batch.history.HistoricBatchQuery;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.history.batch.HistoricBatchDto;
import org.camunda.bpm.engine.rest.dto.history.batch.HistoricBatchQueryDto;
import org.camunda.bpm.engine.rest.history.HistoricBatchRestService;
import org.camunda.bpm.engine.rest.sub.history.HistoricBatchResource;
import org.camunda.bpm.engine.rest.sub.history.impl.HistoricBatchResourceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;

public class HistoricBatchRestServiceImpl implements HistoricBatchRestService {

  protected ObjectMapper objectMapper;
  protected ProcessEngine processEngine;

  public HistoricBatchRestServiceImpl(ObjectMapper objectMapper, ProcessEngine processEngine) {
    this.objectMapper = objectMapper;
    this.processEngine = processEngine;
  }

  public HistoricBatchResource getHistoricBatch(String batchId) {
    return new HistoricBatchResourceImpl(processEngine, batchId);
  }

  public List<HistoricBatchDto> getHistoricBatches(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    HistoricBatchQueryDto queryDto = new HistoricBatchQueryDto(objectMapper, uriInfo.getQueryParameters());
    HistoricBatchQuery query = queryDto.toQuery(processEngine);

    List<HistoricBatch> matchingBatches;
    if (firstResult != null || maxResults != null) {
      matchingBatches = executePaginatedQuery(query, firstResult, maxResults);
    }
    else {
      matchingBatches = query.list();
    }

    List<HistoricBatchDto> batchResults = new ArrayList<HistoricBatchDto>();
    for (HistoricBatch matchingBatch : matchingBatches) {
      batchResults.add(HistoricBatchDto.fromBatch(matchingBatch));
    }
    return batchResults;
  }

  public CountResultDto getHistoricBatchesCount(UriInfo uriInfo) {
    HistoricBatchQueryDto queryDto = new HistoricBatchQueryDto(objectMapper, uriInfo.getQueryParameters());
    HistoricBatchQuery query = queryDto.toQuery(processEngine);

    long count = query.count();
    return new CountResultDto(count);
  }

  protected List<HistoricBatch> executePaginatedQuery(HistoricBatchQuery query, Integer firstResult, Integer maxResults) {
    if (firstResult == null) {
      firstResult = 0;
    }
    if (maxResults == null) {
      maxResults = Integer.MAX_VALUE;
    }

    return query.listPage(firstResult, maxResults);
  }
}
