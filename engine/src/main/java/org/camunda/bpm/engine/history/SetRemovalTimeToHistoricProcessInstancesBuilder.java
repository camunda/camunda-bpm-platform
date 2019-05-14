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
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.batch.Batch;

/**
 * Fluent builder to set the removal time to historic process instances and
 * all associated historic entities.
 *
 * @author Tassilo Weidner
 */
public interface SetRemovalTimeToHistoricProcessInstancesBuilder {

  /**
   * Selects historic process instances by the given query.
   *
   * @param historicProcessInstanceQuery to be evaluated.
   * @return the builder.
   */
  SetRemovalTimeToHistoricProcessInstancesBuilder byQuery(HistoricProcessInstanceQuery historicProcessInstanceQuery);

  /**
   * Selects historic decision instances by the given ids.
   *
   * @param historicProcessInstanceIds supposed to be affected.
   * @return the builder.
   */
  SetRemovalTimeToHistoricProcessInstancesBuilder byIds(String... historicProcessInstanceIds);

  /**
   * Takes additionally those historic process instances into account that are part of
   * the hierarchy of the given historic process instance.
   *
   * If the root process instance id of the given historic process instance is {@code null},
   * the hierarchy is ignored. This is the case for instances that were started with a version
   * prior 7.10.
   *
   * @return the builder.
   */
  SetRemovalTimeToHistoricProcessInstancesBuilder hierarchical();

  /**
   * Sets the removal time asynchronously as batch. The returned batch can be used to
   * track the progress of setting a removal time.
   *
   * @throws BadUserRequestException when no historic process instances could be found.
   * @throws AuthorizationException
   * when no {@link BatchPermissions#CREATE_BATCH_SET_REMOVAL_TIME CREATE_BATCH_SET_REMOVAL_TIME}
   * or no permission {@link Permissions#CREATE CREATE} permission is granted on {@link Resources#BATCH}.
   *
   * @return the batch which sets the removal time asynchronously.
   */
  Batch executeAsync();

}
