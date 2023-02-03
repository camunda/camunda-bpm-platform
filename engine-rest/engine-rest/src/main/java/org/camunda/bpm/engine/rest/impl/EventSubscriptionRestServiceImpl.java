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
package org.camunda.bpm.engine.rest.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.UriInfo;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.EventSubscriptionRestService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.runtime.EventSubscriptionDto;
import org.camunda.bpm.engine.rest.dto.runtime.EventSubscriptionQueryDto;
import org.camunda.bpm.engine.rest.util.QueryUtil;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.EventSubscriptionQuery;

public class EventSubscriptionRestServiceImpl extends AbstractRestProcessEngineAware implements EventSubscriptionRestService {

  public EventSubscriptionRestServiceImpl(String engineName, ObjectMapper objectMapper) {
    super(engineName, objectMapper);
  }

  @Override
  public List<EventSubscriptionDto> getEventSubscriptions(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    EventSubscriptionQueryDto queryDto = new EventSubscriptionQueryDto(getObjectMapper(), uriInfo.getQueryParameters());
    return queryEventSubscriptions(queryDto, firstResult, maxResults);
  }

  public List<EventSubscriptionDto> queryEventSubscriptions(EventSubscriptionQueryDto queryDto, Integer firstResult, Integer maxResults) {
    ProcessEngine engine = getProcessEngine();
    queryDto.setObjectMapper(getObjectMapper());
    EventSubscriptionQuery query = queryDto.toQuery(engine);

    List<EventSubscription> matchingEventSubscriptions = QueryUtil.list(query, firstResult, maxResults);

    List<EventSubscriptionDto> eventSubscriptionResults = new ArrayList<EventSubscriptionDto>();
    for (EventSubscription eventSubscription : matchingEventSubscriptions) {
      EventSubscriptionDto resultEventSubscription = EventSubscriptionDto.fromEventSubscription(eventSubscription);
      eventSubscriptionResults.add(resultEventSubscription);
    }
    return eventSubscriptionResults;
  }

  @Override
  public CountResultDto getEventSubscriptionsCount(UriInfo uriInfo) {
    EventSubscriptionQueryDto queryDto = new EventSubscriptionQueryDto(getObjectMapper(), uriInfo.getQueryParameters());
    return queryEventSubscriptionsCount(queryDto);
  }

  public CountResultDto queryEventSubscriptionsCount(EventSubscriptionQueryDto queryDto) {
    ProcessEngine engine = getProcessEngine();
    queryDto.setObjectMapper(getObjectMapper());
    EventSubscriptionQuery query = queryDto.toQuery(engine);

    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);

    return result;
  }
}
