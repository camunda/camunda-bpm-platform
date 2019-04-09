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
package org.camunda.bpm.engine.management;

import java.util.Date;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;

/**
 * Fluent builder to update the suspension state of job definitions.
 */
public interface UpdateJobDefinitionSuspensionStateBuilder {

  /**
   * Specify if the suspension states of the jobs of the provided job
   * definitions should also be updated. Default is <code>false</code>.
   *
   * @param includeJobs
   *          if <code>true</code>, all related jobs will be activated /
   *          suspended too.
   * @return the builder
   */
  UpdateJobDefinitionSuspensionStateBuilder includeJobs(boolean includeJobs);

  /**
   * Specify when the suspension state should be updated. Note that the <b>job
   * executor</b> needs to be active to use this.
   *
   * @param executionDate
   *          the date on which the job definition will be activated /
   *          suspended. If <code>null</code>, the job definition is activated /
   *          suspended immediately.
   *
   * @return the builder
   */
  UpdateJobDefinitionSuspensionStateBuilder executionDate(Date executionDate);

  /**
   * Activates the provided job definitions.
   *
   * @throws AuthorizationException
   *           <li>if the current user has no {@link Permissions#UPDATE}
   *           permission on {@link Resources#PROCESS_DEFINITION}</li>
   *           <li>If {@link #includeJobs(boolean)} is set to <code>true</code>
   *           and the user have no {@link Permissions#UPDATE_INSTANCE}
   *           permission on {@link Resources#PROCESS_DEFINITION}
   *           {@link Permissions#UPDATE} permission on any
   *           {@link Resources#PROCESS_INSTANCE}</li>
   */
  void activate();

  /**
   * Suspends the provided job definitions. If a job definition is in state
   * suspended, it will be ignored by the job executor.
   *
   * @throws AuthorizationException
   *           <li>if the current user has no {@link Permissions#UPDATE}
   *           permission on {@link Resources#PROCESS_DEFINITION}</li>
   *           <li>If {@link #includeJobs(boolean)} is set to <code>true</code>
   *           and the user have no {@link Permissions#UPDATE_INSTANCE}
   *           permission on {@link Resources#PROCESS_DEFINITION}
   *           {@link Permissions#UPDATE} permission on any
   *           {@link Resources#PROCESS_INSTANCE}</li>
   */
  void suspend();

}
