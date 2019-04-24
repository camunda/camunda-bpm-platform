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

/**
 * Query for {@link Batch} instances.
 */
public interface BatchQuery extends Query<BatchQuery, Batch> {

  /** Only select batch instances for the given batch id. */
  BatchQuery batchId(String batchId);

  /**
   * Only select batches of the given type.
   */
  BatchQuery type(String type);

  /** Only selects batches with one of the given tenant ids. */
  BatchQuery tenantIdIn(String... tenantIds);

  /** Only selects batches which have no tenant id. */
  BatchQuery withoutTenantId();

  /** Only selects batches which are active **/
  BatchQuery active();

  /** Only selects batches which are suspended **/
  BatchQuery suspended();

  /**
   * Returns batches sorted by id; must be followed by an invocation of {@link #asc()} or {@link #desc()}.
   */
  BatchQuery orderById();

  /**
   * Returns batches sorted by tenant id; must be followed by an invocation of {@link #asc()} or {@link #desc()}.
   */
  BatchQuery orderByTenantId();

}
