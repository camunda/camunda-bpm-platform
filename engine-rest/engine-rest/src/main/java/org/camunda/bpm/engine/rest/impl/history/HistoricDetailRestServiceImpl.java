/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.UriInfo;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricDetailQuery;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricDetailDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricDetailQueryDto;
import org.camunda.bpm.engine.rest.history.HistoricDetailRestService;
import org.camunda.bpm.engine.rest.sub.history.HistoricDetailResource;
import org.camunda.bpm.engine.rest.sub.history.impl.HistoricDetailResourceImpl;
import org.camunda.bpm.engine.rest.util.QueryUtil;

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

  @Override
  public HistoricDetailResource historicDetail(String detailId) {
    return new HistoricDetailResourceImpl(detailId, processEngine);
  }

  @Override
  public List<HistoricDetailDto> getHistoricDetails(UriInfo uriInfo, Integer firstResult,
      Integer maxResults, boolean deserializeObjectValues) {
    HistoricDetailQueryDto queryDto = new HistoricDetailQueryDto(objectMapper, uriInfo.getQueryParameters());
    HistoricDetailQuery query = queryDto.toQuery(processEngine);

    return executeHistoricDetailQuery(query, firstResult, maxResults, deserializeObjectValues);
  }

  @Override
  public List<HistoricDetailDto> queryHistoricDetails(HistoricDetailQueryDto queryDto, Integer firstResult,
      Integer maxResults, boolean deserializeObjectValues) {
    HistoricDetailQuery query = queryDto.toQuery(processEngine);

    return executeHistoricDetailQuery(query, firstResult, maxResults, deserializeObjectValues);
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

  private List<HistoricDetailDto> executeHistoricDetailQuery(HistoricDetailQuery query, Integer firstResult,
      Integer maxResults, boolean deserializeObjectValues) {

    query.disableBinaryFetching();
    if (!deserializeObjectValues) {
      query.disableCustomObjectDeserialization();
    }

    List<HistoricDetail> queryResult = QueryUtil.list(query, firstResult, maxResults);

    List<HistoricDetailDto> result = new ArrayList<HistoricDetailDto>();
    for (HistoricDetail historicDetail : queryResult) {
      HistoricDetailDto dto = HistoricDetailDto.fromHistoricDetail(historicDetail);
      result.add(dto);
    }

    return result;
  }

}
