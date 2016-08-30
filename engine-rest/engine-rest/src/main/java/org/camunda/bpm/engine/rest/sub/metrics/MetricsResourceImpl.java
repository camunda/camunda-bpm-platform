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
package org.camunda.bpm.engine.rest.sub.metrics;

import org.camunda.bpm.engine.rest.util.DateParam;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.management.MetricsQuery;
import org.camunda.bpm.engine.rest.dto.metrics.MetricsResultDto;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Daniel Meyer
 *
 */
public class MetricsResourceImpl implements MetricsResource {

  protected String metricsName;
  protected ProcessEngine processEngine;
  protected ObjectMapper objectMapper;

  public MetricsResourceImpl(String metricsName, ProcessEngine processEngine, ObjectMapper objectMapper) {
    this.metricsName = metricsName;
    this.processEngine = processEngine;
    this.objectMapper = objectMapper;
  }

  @Override
  public MetricsResultDto sum(DateParam startDate, DateParam endDate) {
    MetricsQuery query = processEngine.getManagementService()
      .createMetricsQuery()
      .name(metricsName);

    if (startDate != null) {
      query.startDate(startDate.getDate());
    }

    if (endDate != null) {
      query.endDate(endDate.getDate());
    }

    return new MetricsResultDto(query.sum());
  }
}
