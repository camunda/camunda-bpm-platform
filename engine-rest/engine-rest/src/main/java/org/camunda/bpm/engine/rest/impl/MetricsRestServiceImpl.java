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

import org.camunda.bpm.engine.rest.MetricsRestService;
import org.camunda.bpm.engine.rest.sub.metrics.MetricsResource;
import org.camunda.bpm.engine.rest.sub.metrics.MetricsResourceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.camunda.bpm.engine.management.Metric;
import org.camunda.bpm.engine.management.MetricsQuery;
import org.camunda.bpm.engine.rest.dto.metrics.MetricsIntervalResultDto;
import org.camunda.bpm.engine.rest.util.DateParam;

/**
 * @author Daniel Meyer
 *
 */
public class MetricsRestServiceImpl extends AbstractRestProcessEngineAware implements MetricsRestService {

  public MetricsRestServiceImpl(String engineName, ObjectMapper objectMapper) {
    super(engineName, objectMapper);
  }

  @Override
  public MetricsResource getMetrics(String name) {
    return new MetricsResourceImpl(name, processEngine);
  }

  @Override
  public List<MetricsIntervalResultDto> interval(String name, String reporter, DateParam startDate,
          DateParam endDate, Integer firstResult, Integer maxResults, Long interval) {

    MetricsQuery query = processEngine.getManagementService()
      .createMetricsQuery()
      .name(name)
      .reporter(reporter);

    if (startDate != null) {
      query.startDate(startDate.getDate());
    }

    if (endDate != null) {
      query.endDate(endDate.getDate());
    }

    if (firstResult != null) {
      query.offset(firstResult);
    }

    if (maxResults != null) {
      query.limit(maxResults);
    }

    List<Metric> metrics;
    if (interval != null) {
      metrics = query.interval(interval);
    } else {
      metrics = query.interval();
    }

    return convertToDtos(metrics);
  }

  protected List<MetricsIntervalResultDto> convertToDtos(List<Metric> metrics) {
    List<MetricsIntervalResultDto> intervalMetrics = new ArrayList<MetricsIntervalResultDto>();
    for (Metric m : metrics) {
      intervalMetrics.add(new MetricsIntervalResultDto(m));
    }
    return intervalMetrics;
  }
}
