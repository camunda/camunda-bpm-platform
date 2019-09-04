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
package org.camunda.bpm.engine.externaltask;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.authorization.BatchPermissions;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.batch.Batch;

public interface UpdateExternalTaskRetriesBuilder extends UpdateExternalTaskRetriesSelectBuilder {

  /**
   * Sets the retries for external tasks.
   *
   * If the new value is 0, a new incident with a <code>null</code> message is created.
   * If the old value is 0 and the new value is greater than 0, an existing incident
   * is resolved.
   *
   * @param retries
   *
   * @throws org.camunda.bpm.engine.BadUserRequestException
   *           If no external tasks are found
   *           If a external task id is set to null
   *
   * @throws AuthorizationException thrown if the current user does not possess any of the following permissions:
   *   <ul>
   *     <li>{@link Permissions#UPDATE} on {@link Resources#PROCESS_INSTANCE}</li>
   *     <li>{@link Permissions#UPDATE_INSTANCE} on {@link Resources#PROCESS_DEFINITION}</li>
   *   </ul>
   * @throws BadUserRequestException
   *   When the affected instances count exceeds the maximum results limit. A maximum results
   *   limit can be specified with the process engine configuration property
   *   <code>queryMaxResultsLimit</code> (default {@link Integer#MAX_VALUE}).
   *   Please use the batch operation {@link #setAsync} instead.
   */
  void set(int retries);

  /**
   * Sets the retries for external tasks asynchronously as batch. The returned batch
   * can be used to track the progress.
   *
   * If the new value is 0, a new incident with a <code>null</code> message is created.
   * If the old value is 0 and the new value is greater than 0, an existing incident
   * is resolved.
   *
   * @param retries
   *
   * @throws org.camunda.bpm.engine.BadUserRequestException
   *           If no external tasks are found or if a external task id is set to null
   *
   * @throws AuthorizationException
   *           if the user has no {@link Permissions#CREATE} or
   *           {@link BatchPermissions#CREATE_BATCH_SET_EXTERNAL_TASK_RETRIES} permission on {@link Resources#BATCH}.
   */
  Batch setAsync(int retries);

}
