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
package org.camunda.bpm.engine.runtime;

import java.util.List;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.authorization.BatchPermissions;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.batch.Batch;

public interface ModificationBuilder extends InstantiationBuilder<ModificationBuilder>{

  /**
   * <p><i>Submits the instruction:</i></p>
   *
   * <p>Cancel all instances of the given activity in an arbitrary order, which are:
   * <ul>
   *   <li>activity instances of that activity
   *   <li>transition instances entering or leaving that activity
   * </ul></p>
   *
   * <p>The cancellation order of the instances is arbitrary</p>
   *
   * @param activityId the activity for which all instances should be cancelled
   */
  ModificationBuilder cancelAllForActivity(String activityId);

  /**
   * <p><i>Submits the instruction:</i></p>
   *
   * <p>Cancel all instances of the given activity in an arbitrary order, which are:
   * <ul>
   *   <li>activity instances of that activity
   *   <li>transition instances entering or leaving that activity
   * </ul></p>
   *
   * <p>The cancellation order of the instances is arbitrary</p>
   *
   * @param activityId the activity for which all instances should be cancelled
   * @param cancelCurrentActiveActivityInstances
   */
  ModificationBuilder cancelAllForActivity(String activityId, boolean cancelCurrentActiveActivityInstances);

  /**
   * @param processInstanceIds the process instance ids to modify.
   */
  ModificationBuilder processInstanceIds(List<String> processInstanceIds);

  /**
   * @param processInstanceIds the process instance ids to modify.
   */
  ModificationBuilder processInstanceIds(String... processInstanceIds);

  /**
   * @param processInstanceQuery a query which selects the process instances to modify.
   *   Query results are restricted to process instances for which the user has {@link Permissions#READ} permission.
   */
  ModificationBuilder processInstanceQuery(ProcessInstanceQuery processInstanceQuery);

  /**
   * Skips custom execution listeners when creating/removing activity instances during modification
   */
  ModificationBuilder skipCustomListeners();

  /**
   * Skips io mappings when creating/removing activity instances during modification
   */
  ModificationBuilder skipIoMappings();

  /** Provides annotation for the current modification. */
  ModificationBuilder setAnnotation(String annotation);

  /**
   * Execute the modification synchronously.
   *
   * @throws AuthorizationException
   *   if the user has not all of the following permissions
   *   <ul>
   *      <li>if the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE} or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}</li>
   *   </ul>
   * @throws BadUserRequestException
   *   When the affected instances count exceeds the maximum results limit. A maximum results
   *   limit can be specified with the process engine configuration property
   *   <code>queryMaxResultsLimit</code> (default {@link Integer#MAX_VALUE}).
   *   Please use the batch operation {@link #executeAsync()} instead.
   */
  void execute();

  /**
   * Execute the modification asynchronously as batch. The returned batch
   * can be used to track the progress of the modification.
   *
   * @return the batch which executes the modification asynchronously.
   *
   * @throws AuthorizationException
   *   if the user has not all of the following permissions
   *   <ul>
   *     <li>{@link Permissions#CREATE} or {@link BatchPermissions#CREATE_BATCH_MODIFY_PROCESS_INSTANCES} permission on {@link Resources#BATCH}</li>
   *   </ul>
   */
  Batch executeAsync();
}

