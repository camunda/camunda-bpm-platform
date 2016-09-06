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
import java.util.List;

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
   * Sets the offset of the returned results.
   *
   * @param offset indicates after which row the result begins
   * @return the adjusted MetricsQuery
   */
  MetricsQuery offset(int offset);

  /**
   * Sets the limit row count of the result.
   * Can't be set larger than 200, since it is the maximum row count which should be returned.
   *
   * @param maxResults the new row limit of the result
   * @return the adjusted MetricsQuery
   */
  MetricsQuery limit(int maxResults);

  /**
   * Returns the metrics summed up and aggregated on a time interval.
   * Default interval is 900 (15 minutes). The list size has a maximum of 200
   * the maximum can be decreased with the MetricsQuery#limit method. Paging
   * is enabled with the help of the offset.
   *
   * @return the aggregated metrics
   */
  List<MetricIntervalValue> interval();



  /**
   * Returns the metrics summed up and aggregated on a time interval.
   * The size of the interval is given via parameter.
   * The time unit is seconds! The list size has a maximum of 200
   * the maximum can be decreased with the MetricsQuery#limit method. Paging
   * is enabled with the help of the offset.
   *
   * @param interval The time interval on which the metrics should be aggregated.
   *                  The time unit is seconds.
   * @return the aggregated metrics
   */
  List<MetricIntervalValue> interval(long interval);

  /**
   * @return the aggregated sum
   */
  long sum();

}