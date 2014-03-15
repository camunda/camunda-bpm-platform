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
package org.camunda.bpm.engine.history;

import org.camunda.bpm.engine.query.Query;

/**
 *
 * @author Roman Smirnov
 *
 */
public interface HistoricActivityStatisticsQuery extends Query<HistoricActivityStatisticsQuery, HistoricActivityStatistics> {

  /**
   * Include an aggregation of finished instances in the result.
   */
  HistoricActivityStatisticsQuery includeFinished();

  /**
   * Include an aggregation of canceled instances in the result.
   */
  HistoricActivityStatisticsQuery includeCanceled();

  /**
   * Include an aggregation of instances, which complete a scope (ie. in bpmn manner: an activity
   * which consumed a token and did not produced a new one), in the result.
   */
  HistoricActivityStatisticsQuery includeCompleteScope();

  /**
   * Order by activity id (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  HistoricActivityStatisticsQuery orderByActivityId();

}
