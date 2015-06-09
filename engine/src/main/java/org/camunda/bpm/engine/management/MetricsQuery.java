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
package org.camunda.bpm.engine.management;

import java.util.Date;

/**
 * @author Daniel Meyer
 * @since 7.3
 */
public interface MetricsQuery {

  /**
   * @see constants in {@link Metrics} for a list of names which can be used here.
   *
   * @param name The name of the metrics to query for
   */
  MetricsQuery name(String name);

  /**
   * Restrict to data collected by the reported with the given identifier
   */
  MetricsQuery reporter(String reporter);

  /**
   * Restrict to data collected after the given date (inclusive)
   */
  MetricsQuery startDate(Date startTime);

  /**
   * Restrict to data collected before the given date (exclusive)
   */
  MetricsQuery endDate(Date endTime);

  /**
   * @return the aggregated sum
   */
  long sum();

}