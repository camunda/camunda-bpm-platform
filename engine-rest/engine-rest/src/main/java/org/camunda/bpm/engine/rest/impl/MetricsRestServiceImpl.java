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

import org.camunda.bpm.engine.rest.MetricsRestService;
import org.camunda.bpm.engine.rest.sub.metrics.MetricsResource;
import org.camunda.bpm.engine.rest.sub.metrics.MetricsResourceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.camunda.bpm.engine.management.MetricsQuery;
import org.camunda.bpm.engine.rest.dto.metrics.MetricsIntervalResultDto;
import org.camunda.bpm.engine.management.MetricIntervalValue;
import org.camunda.bpm.engine.rest.dto.converter.DateConverter;
import org.camunda.bpm.engine.rest.dto.converter.IntegerConverter;
import org.camunda.bpm.engine.rest.dto.converter.LongConverter;

/**
 * @author Daniel Meyer
 *
 */
public class MetricsRestServiceImpl extends AbstractRestProcessEngineAware implements MetricsRestService {

  public static final String QUERY_PARAM_NAME = "name";
  public static final String QUERY_PARAM_REPORTER = "reporter";
  public static final String QUERY_PARAM_START_DATE = "startDate";
  public static final String QUERY_PARAM_END_DATE = "endDate";
  public static final String QUERY_PARAM_FIRST_RESULT = "firstResult";
  public static final String QUERY_PARAM_MAX_RESULTS = "maxResults";
  public static final String QUERY_PARAM_INTERVAL = "interval";
  public static final String QUERY_PARAM_DATE = "date";
  public static final String QUERY_PARAM_AGG_BY_REPORTER = "aggregateByReporter";

  protected final DateConverter dateConverter;

  public MetricsRestServiceImpl(String engineName, ObjectMapper objectMapper) {
    super(engineName, objectMapper);
    dateConverter = new DateConverter();
    dateConverter.setObjectMapper(objectMapper);
  }

  @Override
  public MetricsResource getMetrics(String name) {
    return new MetricsResourceImpl(name, getProcessEngine(), objectMapper);
  }

  @Override
  public List<MetricsIntervalResultDto> interval(UriInfo uriInfo) {
    MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
    final String name = queryParameters.getFirst(QUERY_PARAM_NAME);
    MetricsQuery query = getProcessEngine().getManagementService()
      .createMetricsQuery()
      .name(name)
      .reporter(queryParameters.getFirst(QUERY_PARAM_REPORTER));

    applyQueryParams(query, queryParameters);

    List<MetricIntervalValue> metrics;
    LongConverter longConverter = new LongConverter();
    longConverter.setObjectMapper(objectMapper);
    if (queryParameters.getFirst(QUERY_PARAM_INTERVAL) != null) {
      long interval = longConverter.convertQueryParameterToType(queryParameters.getFirst(QUERY_PARAM_INTERVAL));
      metrics = query.interval(interval);
    } else {
      metrics = query.interval();
    }

    final List<MetricsIntervalResultDto> dtoList = convertToDtos(metrics);
    if (name != null) {
      dtoList.forEach(dto -> dto.setName(name));
    }
    return dtoList;
  }

  @Override
  public Response deleteTaskMetrics(String dateString) {
    Date date = dateConverter.convertQueryParameterToType(dateString);
    getProcessEngine().getManagementService().deleteTaskMetrics(date);

    // return no content (204) since resource is deleted
    return Response.noContent().build();
  }

  protected void applyQueryParams(MetricsQuery query, MultivaluedMap<String, String> queryParameters) {
    if(queryParameters.getFirst(QUERY_PARAM_START_DATE) != null) {
      Date startDate = dateConverter.convertQueryParameterToType(queryParameters.getFirst(QUERY_PARAM_START_DATE));
      query.startDate(startDate);
    }

    if(queryParameters.getFirst(QUERY_PARAM_END_DATE) != null) {
      Date endDate = dateConverter.convertQueryParameterToType(queryParameters.getFirst(QUERY_PARAM_END_DATE));
      query.endDate(endDate);
    }

    IntegerConverter intConverter = new IntegerConverter();
    intConverter.setObjectMapper(objectMapper);

    if (queryParameters.getFirst(QUERY_PARAM_FIRST_RESULT) != null) {
      int firstResult = intConverter.convertQueryParameterToType(queryParameters.getFirst(QUERY_PARAM_FIRST_RESULT));
      query.offset(firstResult);
    }

    if (queryParameters.getFirst(QUERY_PARAM_MAX_RESULTS) != null) {
      int maxResults = intConverter.convertQueryParameterToType(queryParameters.getFirst(QUERY_PARAM_MAX_RESULTS));
      query.limit(maxResults);
    }

    if(queryParameters.getFirst(QUERY_PARAM_AGG_BY_REPORTER) != null) {
      query.aggregateByReporter();
    }
  }

  protected List<MetricsIntervalResultDto> convertToDtos(List<MetricIntervalValue> metrics) {
    List<MetricsIntervalResultDto> intervalMetrics = new ArrayList<>();
    for (MetricIntervalValue m : metrics) {
      intervalMetrics.add(new MetricsIntervalResultDto(m));
    }
    return intervalMetrics;
  }
}
