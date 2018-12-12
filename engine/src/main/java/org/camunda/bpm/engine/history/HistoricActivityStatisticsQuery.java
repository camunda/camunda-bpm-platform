/*
 * Copyright © 2013-2018 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.history;

import java.util.Date;
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

  /** Only select historic activities of process instances that were started before the given date. */
  HistoricActivityStatisticsQuery startedBefore(Date date);

  /** Only select historic activities of process instances that were started after the given date. */
  HistoricActivityStatisticsQuery startedAfter(Date date);

  /** Only select historic activities of process instances that were finished before the given date. */
  HistoricActivityStatisticsQuery finishedBefore(Date date);

  /** Only select historic activities of process instances that were finished after the given date. */
  HistoricActivityStatisticsQuery finishedAfter(Date date);

  /**
   * Order by activity id (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  HistoricActivityStatisticsQuery orderByActivityId();

}
