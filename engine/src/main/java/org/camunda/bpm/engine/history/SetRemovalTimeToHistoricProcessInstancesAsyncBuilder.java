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

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.batch.Batch;

import java.util.Date;

/**
 * Fluent builder to set the removal time to historic process instances and
 * all associated historic entities asynchronously.
 *
 * @author Tassilo Weidner
 */
public interface SetRemovalTimeToHistoricProcessInstancesAsyncBuilder {

  /**
   * Selects historic process instances by the given query.
   *
   * @param historicProcessInstanceQuery to be evaluated.
   * @return the builder.
   */
  SetRemovalTimeToHistoricProcessInstancesAsyncBuilder byQuery(HistoricProcessInstanceQuery historicProcessInstanceQuery);

  /**
   * Sets the removal time to an absolute date or {@code null}.
   *
   * @param removalTime supposed to be set to historic entities.
   * @return the builder.
   */
  SetRemovalTimeToHistoricProcessInstancesAsyncBuilder removalTime(Date removalTime);

  /**
   * Sets the removal time asynchronously as batch. The returned batch can be used to
   * track the progress of setting a removal time.
   *
   * @throws BadUserRequestException when no historic process instances could be found.
   *
   * @return the batch which sets the removal time asynchronously.
   */
  Batch executeAsync();

}
