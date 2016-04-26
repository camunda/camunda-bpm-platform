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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.BatchQuery;
import org.camunda.bpm.engine.batch.BatchStatistics;
import org.camunda.bpm.engine.batch.BatchStatisticsQuery;
import org.camunda.bpm.engine.rest.BatchRestService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.batch.BatchDto;
import org.camunda.bpm.engine.rest.dto.batch.BatchQueryDto;
import org.camunda.bpm.engine.rest.dto.batch.BatchStatisticsDto;
import org.camunda.bpm.engine.rest.dto.batch.BatchStatisticsQueryDto;
import org.camunda.bpm.engine.rest.sub.batch.BatchResource;
import org.camunda.bpm.engine.rest.sub.batch.impl.BatchResourceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;

public class BatchRestServiceImpl extends AbstractRestProcessEngineAware implements BatchRestService {

  public BatchRestServiceImpl(String engineName, ObjectMapper objectMapper) {
    super(engineName, objectMapper);
  }

  public BatchResource getBatch(String batchId) {
    return new BatchResourceImpl(getProcessEngine(), batchId);
  }

  public List<BatchDto> getBatches(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    BatchQueryDto queryDto = new BatchQueryDto(getObjectMapper(), uriInfo.getQueryParameters());
    BatchQuery query = queryDto.toQuery(getProcessEngine());

    List<Batch> matchingBatches;
    if (firstResult != null || maxResults != null) {
      matchingBatches = executePaginatedQuery(query, firstResult, maxResults);
    }
    else {
      matchingBatches = query.list();
    }

    List<BatchDto> batchResults = new ArrayList<BatchDto>();
    for (Batch matchingBatch : matchingBatches) {
      batchResults.add(BatchDto.fromBatch(matchingBatch));
    }
    return batchResults;
  }

  public CountResultDto getBatchesCount(UriInfo uriInfo) {
    ProcessEngine processEngine = getProcessEngine();
    BatchQueryDto queryDto = new BatchQueryDto(getObjectMapper(), uriInfo.getQueryParameters());
    BatchQuery query = queryDto.toQuery(processEngine);

    long count = query.count();
    return new CountResultDto(count);
  }

  public List<BatchStatisticsDto> getStatistics(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    BatchStatisticsQueryDto queryDto = new BatchStatisticsQueryDto(getObjectMapper(), uriInfo.getQueryParameters());
    BatchStatisticsQuery query = queryDto.toQuery(getProcessEngine());

    List<BatchStatistics> batchStatisticsList;
    if (firstResult != null || maxResults != null) {
      batchStatisticsList = executePaginatedStatisticsQuery(query, firstResult, maxResults);
    }
    else {
      batchStatisticsList = query.list();
    }

    List<BatchStatisticsDto> statisticsResults = new ArrayList<BatchStatisticsDto>();
    for (BatchStatistics batchStatistics : batchStatisticsList) {
      statisticsResults.add(BatchStatisticsDto.fromBatchStatistics(batchStatistics));
    }

    return statisticsResults;
  }

  public CountResultDto getStatisticsCount(UriInfo uriInfo) {
    BatchStatisticsQueryDto queryDto = new BatchStatisticsQueryDto(getObjectMapper(), uriInfo.getQueryParameters());
    BatchStatisticsQuery query = queryDto.toQuery(getProcessEngine());

    long count = query.count();
    return new CountResultDto(count);
  }

  protected List<Batch> executePaginatedQuery(BatchQuery query, Integer firstResult, Integer maxResults) {
    if (firstResult == null) {
      firstResult = 0;
    }
    if (maxResults == null) {
      maxResults = Integer.MAX_VALUE;
    }

    return query.listPage(firstResult, maxResults);
  }

  protected List<BatchStatistics> executePaginatedStatisticsQuery(BatchStatisticsQuery query, Integer firstResult, Integer maxResults) {
    if (firstResult == null) {
      firstResult = 0;
    }
    if (maxResults == null) {
      maxResults = Integer.MAX_VALUE;
    }

    return query.listPage(firstResult, maxResults);
  }
}
