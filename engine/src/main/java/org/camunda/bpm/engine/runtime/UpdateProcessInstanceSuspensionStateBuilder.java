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

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions;
import org.camunda.bpm.engine.authorization.ProcessInstancePermissions;
import org.camunda.bpm.engine.authorization.Resources;

/**
 * Fluent builder to update the suspension state of process instances.
 */
public interface UpdateProcessInstanceSuspensionStateBuilder {

  /**
   * <p>
   * Activates the provided process instances.
   * </p>
   *
   * <p>
   * If you have a process instance hierarchy, activating one process instance
   * from the hierarchy will not activate other process instances from that
   * hierarchy.
   * </p>
   *
   * @throws ProcessEngineException
   *           If no such processDefinition can be found.
   * @throws AuthorizationException
   *           if the user has none of the following:
   *           <li>{@link ProcessInstancePermissions#SUSPEND} permission on {@link Resources#PROCESS_INSTANCE}</li>
   *           <li>{@link ProcessDefinitionPermissions#SUSPEND_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}</li>
   *           <li>{@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}</li>
   *           <li>{@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}</li>
   * @throws BadUserRequestException
   *           When the affected instances count exceeds the maximum results limit. A maximum results
   *           limit can be specified with the process engine configuration property
   *           <code>queryMaxResultsLimit</code> (default {@link Integer#MAX_VALUE}).
   *           Please use the batch operation
   *           {@link UpdateProcessInstancesSuspensionStateBuilder#activateAsync()} instead.
   */
  void activate();

  /**
   * <p>
   * Suspends the provided process instances. This means that the execution is
   * stopped, so the <i>token state</i> will not change. However, actions that
   * do not change token state, like setting/removing variables, etc. will
   * succeed.
   * </p>
   *
   * <p>
   * Tasks belonging to the suspended process instance will also be suspended.
   * This means that any actions influencing the tasks' lifecycles will fail,
   * such as
   * <ul>
   * <li>claiming</li>
   * <li>completing</li>
   * <li>delegation</li>
   * <li>changes in task assignees, owners, etc.</li>
   * </ul>
   * Actions that only change task properties will succeed, such as changing
   * variables or adding comments.
   * </p>
   *
   * <p>
   * If a process instance is in state suspended, the engine will also not
   * execute jobs (timers, messages) associated with this instance.
   * </p>
   *
   * <p>
   * If you have a process instance hierarchy, suspending one process instance
   * from the hierarchy will not suspend other process instances from that
   * hierarchy.
   * </p>
   *
   * @throws ProcessEngineException
   *           If no such processDefinition can be found.
   * @throws AuthorizationException
   *            if the user has none of the following:
   *           <li>{@link ProcessInstancePermissions#SUSPEND} permission on {@link Resources#PROCESS_INSTANCE}</li>
   *           <li>{@link ProcessDefinitionPermissions#SUSPEND_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}</li>
   *           <li>{@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}</li>
   *           <li>{@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}</li>
   * @throws BadUserRequestException
   *           When the affected instances count exceeds the maximum results limit. A maximum results
   *           limit can be specified with the process engine configuration property
   *           <code>queryMaxResultsLimit</code> (default {@link Integer#MAX_VALUE}).
   *           Please see the batch operation
   *           {@link UpdateProcessInstancesSuspensionStateBuilder#suspendAsync()} instead.
   */
  void suspend();

}
