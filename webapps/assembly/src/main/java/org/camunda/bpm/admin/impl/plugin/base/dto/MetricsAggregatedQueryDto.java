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
package org.camunda.bpm.admin.impl.plugin.base.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.webapp.rest.dto.AbstractRestQueryParametersDto;
import org.camunda.bpm.engine.impl.metrics.util.MetricsUtil;
import org.camunda.bpm.engine.management.Metrics;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.converter.DateConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringListConverter;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MetricsAggregatedQueryDto extends AbstractRestQueryParametersDto<MetricsAggregatedQueryDto> {

  private static final Set<String> VALID_GROUP_BY_VALUES;
  private static final Set<String> VALID_METRIC_VALUES;

  static {
    VALID_GROUP_BY_VALUES = new HashSet<>();
    VALID_GROUP_BY_VALUES.add("year");
    VALID_GROUP_BY_VALUES.add("month");

    VALID_METRIC_VALUES = new HashSet<>();
    VALID_METRIC_VALUES.add(Metrics.PROCESS_INSTANCES);
    VALID_METRIC_VALUES.add(Metrics.DECISION_INSTANCES);
    VALID_METRIC_VALUES.add(Metrics.FLOW_NODE_INSTANCES);
    VALID_METRIC_VALUES.add(Metrics.EXECUTED_DECISION_ELEMENTS);
    VALID_METRIC_VALUES.add(Metrics.TASK_USERS);
  }

  protected String groupBy;
  protected Set<String> metrics;
  protected Date subscriptionStartDate;
  protected Date startDate;
  protected Date endDate;

  protected int subscriptionMonth;
  protected int subscriptionDay;

  public MetricsAggregatedQueryDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
    maxResultsLimitEnabled = false;
  }

  @CamundaQueryParam("groupBy")
  public void setGroupBy(String groupBy) {
    this.groupBy = groupBy;
  }

  public String getGroupBy() {
    return groupBy;
  }

  @CamundaQueryParam(value = "metrics", converter = StringListConverter.class)
  public void setMetrics(List<String> metrics) {
    boolean valid = new HashSet<>(VALID_METRIC_VALUES).containsAll(metrics);
    if (!valid) {
      throw new InvalidRequestException(Response.Status.BAD_REQUEST, "metrics parameter has invalid value: " + metrics);
    }
    this.metrics = new HashSet<>(metrics);
  }

  public Set<String> getMetrics() {
    return metrics;
  }

  @CamundaQueryParam(value = "subscriptionStartDate", converter = DateConverter.class)
  public void setSubscriptionStartDate(Date subscriptionStartDate) {
    this.subscriptionStartDate = subscriptionStartDate;

    if (subscriptionStartDate != null) {
      // calculate subscription year and month
      Calendar cal = Calendar.getInstance();
      cal.setTime(subscriptionStartDate);
      subscriptionMonth = cal.get(Calendar.MONTH) + 1;
      subscriptionDay = cal.get(Calendar.DAY_OF_MONTH);
    }
  }

  @CamundaQueryParam(value = "startDate", converter = DateConverter.class)
  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  @CamundaQueryParam(value = "endDate", converter = DateConverter.class)
  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  @Override
  protected String getOrderByValue(String sortBy) {
    return null;
  }

  @Override
  protected boolean isValidSortByValue(String value) {
    return false;
  }

  public void validateAndPrepareQuery() {
    if (subscriptionStartDate == null || !subscriptionStartDate.before(ClockUtil.now())) {
      throw new InvalidRequestException(Response.Status.BAD_REQUEST,
          "subscriptionStartDate parameter has invalid value: " + subscriptionStartDate);
    }
    if (startDate != null && endDate != null && !endDate.after(startDate)) {
      throw new InvalidRequestException(Response.Status.BAD_REQUEST, "endDate parameter must be after startDate");
    }
    if (!VALID_GROUP_BY_VALUES.contains(groupBy)) {
      throw new InvalidRequestException(Response.Status.BAD_REQUEST, "groupBy parameter has invalid value: " + groupBy);
    }
    if (metrics == null || metrics.isEmpty()) {
       metrics = VALID_METRIC_VALUES;
    }
    // convert metrics to internal names
    this.metrics = metrics.stream()
        .map(MetricsUtil::resolveInternalName)
        .collect(Collectors.toSet());
  }

  public int getSubscriptionMonth() {
    return subscriptionMonth;
  }

  public int getSubscriptionDay() {
    return subscriptionDay;
  }

}
