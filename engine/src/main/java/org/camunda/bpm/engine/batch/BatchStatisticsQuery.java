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
package org.camunda.bpm.engine.batch;

import org.camunda.bpm.engine.query.Query;

import java.util.Date;

public interface BatchStatisticsQuery extends Query<BatchStatisticsQuery, BatchStatistics> {

  /**
   * Only select batch statistics for the given batch id.
   */
  BatchStatisticsQuery batchId(String batchId);

  /**
   * Only select batch statistics of the given type.
   */
  BatchStatisticsQuery type(String type);

  /** Only selects batch statistics with one of the given tenant ids. */
  BatchStatisticsQuery tenantIdIn(String... tenantIds);

  /** Only selects batch statistics which have no tenant id. */
  BatchStatisticsQuery withoutTenantId();

  /** Only selects batches which are active **/
  BatchStatisticsQuery active();

  /** Only selects batches which are suspended **/
  BatchStatisticsQuery suspended();

  /** Only selects batches that are started by the given user id **/
  BatchStatisticsQuery createdBy(String userId);

  /** Only select historic activity instances that were started before the given date. */
  BatchStatisticsQuery startedBefore(Date date);

  /** Only select historic activity instances that were started after the given date. */
  BatchStatisticsQuery startedAfter(Date date);

  /** Only selects batches with failed jobs **/
  BatchStatisticsQuery withFailures();

  /** Only selects batches without failed jobs **/
  BatchStatisticsQuery withoutFailures();

  /**
   * Returns batch statistics sorted by batch id; must be followed by an invocation of {@link #asc()} or {@link #desc()}.
   */
  BatchStatisticsQuery orderById();

  /**
   * Returns batch statistics sorted by tenant id; must be followed by an invocation of {@link #asc()} or {@link #desc()}.
   */
  BatchStatisticsQuery orderByTenantId();

  /**
   * Returns batch statistics sorted by start time; must be followed by an invocation of {@link #asc()} or {@link #desc()}.
   */
  BatchStatisticsQuery orderByStartTime();

}
