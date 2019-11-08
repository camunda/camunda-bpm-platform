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

import java.util.Date;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricActivityStatisticsQuery;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.converter.BooleanConverter;
import org.camunda.bpm.engine.rest.dto.converter.DateConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringArrayConverter;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HistoricActivityStatisticsQueryDto extends AbstractQueryDto<HistoricActivityStatisticsQuery> {

  protected static final String SORT_ORDER_ACTIVITY_ID = "activityId";

  protected String processDefinitionId;

  protected Boolean includeCanceled;
  protected Boolean includeFinished;
  protected Boolean includeCompleteScope;
  protected Boolean includeIncidents;

  protected Date startedAfter;
  protected Date startedBefore;
  protected Date finishedAfter;
  protected Date finishedBefore;

  protected String[] processInstanceIdIn;

  public HistoricActivityStatisticsQueryDto(ObjectMapper objectMapper, String processDefinitionId, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
    this.processDefinitionId = processDefinitionId;
  }

  /*
   * This is a path param and set from within the resource method
   */
  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  @CamundaQueryParam(value = "canceled", converter = BooleanConverter.class)
  public void setIncludeCanceled(Boolean includeCanceled) {
    this.includeCanceled = includeCanceled;
  }

  @CamundaQueryParam(value = "finished", converter = BooleanConverter.class)
  public void setIncludeFinished(Boolean includeFinished) {
    this.includeFinished = includeFinished;
  }

  @CamundaQueryParam(value = "completeScope", converter = BooleanConverter.class)
  public void setIncludeCompleteScope(Boolean includeCompleteScope) {
    this.includeCompleteScope = includeCompleteScope;
  }

  @CamundaQueryParam(value = "incidents", converter = BooleanConverter.class)
  public void setIncludeIncidents(Boolean includeClosedIncidents) {
    this.includeIncidents = includeClosedIncidents;
  }

  @CamundaQueryParam(value = "startedAfter", converter = DateConverter.class)
  public void setStartedAfter(Date startedAfter) {
    this.startedAfter = startedAfter;
  }

  @CamundaQueryParam(value = "startedBefore", converter = DateConverter.class)
  public void setStartedBefore(Date startedBefore) {
    this.startedBefore = startedBefore;
  }

  @CamundaQueryParam(value = "finishedAfter", converter = DateConverter.class)
  public void setFinishedAfter(Date finishedAfter) {
    this.finishedAfter = finishedAfter;
  }

  @CamundaQueryParam(value = "finishedBefore", converter = DateConverter.class)
  public void setFinishedBefore(Date finishedBefore) {
    this.finishedBefore = finishedBefore;
  }

  @CamundaQueryParam(value = "processInstanceIdIn", converter = StringArrayConverter.class)
  public void setProcessInstanceIdIn(String[] processInstanceIdIn) {
    this.processInstanceIdIn = processInstanceIdIn;
  }

  @Override
  protected boolean isValidSortByValue(String value) {
    return SORT_ORDER_ACTIVITY_ID.equals(value);
  }

  @Override
  protected HistoricActivityStatisticsQuery createNewQuery(ProcessEngine engine) {
    return engine.getHistoryService().createHistoricActivityStatisticsQuery(processDefinitionId);
  }

  @Override
  protected void applyFilters(HistoricActivityStatisticsQuery query) {

    if (includeCanceled != null && includeCanceled) {
      query.includeCanceled();
    }

    if (includeFinished != null && includeFinished) {
      query.includeFinished();
    }

    if (includeCompleteScope != null && includeCompleteScope) {
      query.includeCompleteScope();
    }

    if (includeIncidents !=null && includeIncidents) {
      query.includeIncidents();
    }

    if (startedAfter != null) {
      query.startedAfter(startedAfter);
    }

    if (startedBefore != null) {
      query.startedBefore(startedBefore);
    }

    if (finishedAfter != null) {
      query.finishedAfter(finishedAfter);
    }

    if (finishedBefore != null) {
      query.finishedBefore(finishedBefore);
    }

    if (processInstanceIdIn != null) {
      query.processInstanceIdIn(processInstanceIdIn);
    }
  }

  @Override
  protected void applySortBy(HistoricActivityStatisticsQuery query, String sortBy, Map<String, Object> parameters, ProcessEngine engine) {
    if (SORT_ORDER_ACTIVITY_ID.equals(sortBy)) {
      query.orderByActivityId();
    }
  }
}
