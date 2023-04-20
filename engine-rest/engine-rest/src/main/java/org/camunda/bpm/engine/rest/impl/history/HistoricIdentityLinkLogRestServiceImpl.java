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
import org.camunda.bpm.engine.history.HistoricIdentityLinkLog;
import org.camunda.bpm.engine.history.HistoricIdentityLinkLogQuery;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricIdentityLinkLogDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricIdentityLinkLogQueryDto;
import org.camunda.bpm.engine.rest.history.HistoricIdentityLinkLogRestService;
import org.camunda.bpm.engine.rest.util.QueryUtil;
/**
 *
 * @author Deivarayan Azhagappan
 *
 */
public class HistoricIdentityLinkLogRestServiceImpl implements HistoricIdentityLinkLogRestService{

  protected ObjectMapper objectMapper;
  protected ProcessEngine processEngine;

  public HistoricIdentityLinkLogRestServiceImpl(ObjectMapper objectMapper, ProcessEngine processEngine) {
    this.objectMapper = objectMapper;
    this.processEngine = processEngine;
  }

  @Override
  public List<HistoricIdentityLinkLogDto> getHistoricIdentityLinks(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    HistoricIdentityLinkLogQueryDto queryDto = new HistoricIdentityLinkLogQueryDto(objectMapper, uriInfo.getQueryParameters());
    HistoricIdentityLinkLogQuery query = queryDto.toQuery(processEngine);

    List<HistoricIdentityLinkLog> queryResult = QueryUtil.list(query, firstResult, maxResults);
    List<HistoricIdentityLinkLogDto> result = new ArrayList<HistoricIdentityLinkLogDto>();
    for (HistoricIdentityLinkLog historicIdentityLink : queryResult) {
      HistoricIdentityLinkLogDto dto = HistoricIdentityLinkLogDto.fromHistoricIdentityLink(historicIdentityLink);
      result.add(dto);
    }
    return result;
  }

  @Override
  public CountResultDto getHistoricIdentityLinksCount(UriInfo uriInfo) {
    HistoricIdentityLinkLogQueryDto queryDto = new HistoricIdentityLinkLogQueryDto(objectMapper, uriInfo.getQueryParameters());
    HistoricIdentityLinkLogQuery query = queryDto.toQuery(processEngine);

    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);

    return result;

  }

}
