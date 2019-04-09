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
package org.camunda.bpm.engine.repository;

import java.util.Date;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions;
import org.camunda.bpm.engine.authorization.ProcessInstancePermissions;
import org.camunda.bpm.engine.authorization.Resources;

/**
 * Fluent builder to update the suspension state of process definitions.
 */
public interface UpdateProcessDefinitionSuspensionStateBuilder {

  /**
   * Specify if the suspension states of the process instances of the provided
   * process definitions should also be updated. Default is <code>false</code>.
   *
   * @param includeProcessInstances
   *          if <code>true</code>, all related process instances will be
   *          activated / suspended too.
   * @return the builder
   */
  UpdateProcessDefinitionSuspensionStateBuilder includeProcessInstances(boolean includeProcessInstances);

  /**
   * Specify when the suspension state should be updated. Note that the <b>job
   * executor</b> needs to be active to use this.
   *
   * @param executionDate
   *          the date on which the process definition will be activated /
   *          suspended. If <code>null</code>, the process definition is
   *          activated / suspended immediately.
   *
   * @return the builder
   */
  UpdateProcessDefinitionSuspensionStateBuilder executionDate(Date executionDate);

  /**
   * Activates the provided process definitions.
   *
   * @throws ProcessEngineException
   *           If no such processDefinition can be found.
   * @throws AuthorizationException
   *           <li>if the user has none of the following:</li>
   *           <ul>
   *           <li>{@link ProcessDefinitionPermissions#SUSPEND} permission on {@link Resources#PROCESS_DEFINITION}</li>
   *           <li>{@link Permissions#UPDATE} permission on {@link Resources#PROCESS_DEFINITION}</li>
   *           </ul>
   *           <li>if {@link #includeProcessInstances(boolean)} is set to <code>true</code> and the user has none of the following:</li>
   *           <ul>
   *           <li>{@link ProcessInstancePermissions#SUSPEND} permission on {@link Resources#PROCESS_INSTANCE}</li>
   *           <li>{@link ProcessDefinitionPermissions#SUSPEND_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}</li>
   *           <li>{@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}</li>
   *           <li>{@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}</li>
   *           </ul>
   */
  void activate();

  /**
   * Suspends the provided process definitions. If a process definition is in
   * state suspended, it will not be possible to start new process instances
   * based on this process definition.
   *
   * @throws ProcessEngineException
   *           If no such processDefinition can be found.
   * @throws AuthorizationException
   *           <li>if the user has none of the following:</li>
   *           <ul>
   *           <li>{@link ProcessDefinitionPermissions#SUSPEND} permission on {@link Resources#PROCESS_DEFINITION}</li>
   *           <li>{@link Permissions#UPDATE} permission on {@link Resources#PROCESS_DEFINITION}</li>
   *           </ul>
   *           <li>if {@link #includeProcessInstances(boolean)} is set to <code>true</code> and the user has none of the following:</li>
   *           <ul>
   *           <li>{@link ProcessInstancePermissions#SUSPEND} permission on {@link Resources#PROCESS_INSTANCE}</li>
   *           <li>{@link ProcessDefinitionPermissions#SUSPEND_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}</li>
   *           <li>{@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}</li>
   *           <li>{@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}</li>
   *           </ul>
   */
  void suspend();

}
