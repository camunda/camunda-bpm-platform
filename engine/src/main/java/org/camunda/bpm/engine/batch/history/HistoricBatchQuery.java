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
package org.camunda.bpm.engine.batch.history;

import org.camunda.bpm.engine.query.Query;

/**
 * Query for {@link HistoricBatch} instances.
 */
public interface HistoricBatchQuery extends Query<HistoricBatchQuery, HistoricBatch> {

  /**
   * Only select historic batch instances for the given batch id.
   */
  HistoricBatchQuery batchId(String batchId);

  /**
   * Only select historic batches of the given type.
   */
  HistoricBatchQuery type(String type);

  /**
   * Only select historic batches which are completed or not.
   */
  HistoricBatchQuery completed(boolean completed);

  /** Only selects historic batches with one of the given tenant ids. */
  HistoricBatchQuery tenantIdIn(String... tenantIds);

  /** Only selects historic batches which have no tenant id. */
  HistoricBatchQuery withoutTenantId();

  /**
   * Returns historic batches sorted by id; must be followed by an invocation of {@link #asc()} or {@link #desc()}.
   */
  HistoricBatchQuery orderById();

  /**
   * Returns historic batches sorted by start time; must be followed by an invocation of {@link #asc()} or {@link #desc()}.
   */
  HistoricBatchQuery orderByStartTime();

  /**
   * Returns historic batches sorted by end time; must be followed by an invocation of {@link #asc()} or {@link #desc()}.
   */
  HistoricBatchQuery orderByEndTime();

  /**
   * Returns historic batches sorted by tenant id; must be followed by an invocation of {@link #asc()} or {@link #desc()}.
   */
  HistoricBatchQuery orderByTenantId();


}
