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
package org.camunda.bpm.engine.history;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.authorization.BatchPermissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatchQuery;

import java.util.Date;

/**
 * Fluent builder to set the removal time to historic batches and
 * all associated historic entities.
 *
 * @author Tassilo Weidner
 */
public interface SetRemovalTimeToHistoricBatchesBuilder {

  /**
   * Selects historic batches by the given query.
   *
   * @param historicBatchQuery to be evaluated.
   * @return the builder.
   */
  SetRemovalTimeToHistoricBatchesBuilder byQuery(HistoricBatchQuery historicBatchQuery);

  /**
   * Selects historic batches by the given ids.
   *
   * @param historicBatchIds supposed to be affected.
   * @return the builder.
   */
  SetRemovalTimeToHistoricBatchesBuilder byIds(String... historicBatchIds);

  /**
   * Sets the removal time to an absolute date or {@code null} (clears the removal time).
   *
   * @param removalTime supposed to be set to historic entities.
   * @return the builder.
   */
  SetRemovalTimeToHistoricBatchesBuilder absoluteRemovalTime(Date removalTime);

  /**
   * Calculates the removal time dynamically based on the time to
   * live of the respective batch and the engine's removal time strategy.
   *
   * @return the builder.
   */
  SetRemovalTimeToHistoricBatchesBuilder calculatedRemovalTime();

  /**
   * Sets the removal time asynchronously as batch. The returned batch can be used to
   * track the progress of setting a removal time.
   *
   * @throws BadUserRequestException when no historic batches could be found.
   * @throws AuthorizationException when no {@link BatchPermissions#CREATE_BATCH_SET_REMOVAL_TIME} permission
   * is granted on {@link Resources#BATCH}.
   *
   * @return the batch which sets the removal time asynchronously.
   */
  Batch executeAsync();

}
