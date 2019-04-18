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

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.delegate.ExecutionListener;

/**
 * Fluent builder to delete process definitions by a process definition key or process definition ids.
 *
 * @author Tassilo Weidner
 */
public interface DeleteProcessDefinitionsBuilder {

  /**
   * All process instances of the process definition as well as history data is deleted.
   *
   * @return the builder
   */
  DeleteProcessDefinitionsBuilder cascade();

  /**
   * Only the built-in {@link ExecutionListener}s are notified with the
   * {@link ExecutionListener#EVENTNAME_END} event.
   * Is only applied in conjunction with the cascade method.
   *
   * @return the builder
   */
  DeleteProcessDefinitionsBuilder skipCustomListeners();

  /**
   * Specifies whether input/output mappings for tasks should be invoked
   *
   * @return the builder
   */
  DeleteProcessDefinitionsBuilder skipIoMappings();

  /**
   * Performs the deletion of process definitions.
   *
   * @throws ProcessEngineException
   *           If no such processDefinition can be found.
   * @throws AuthorizationException
   *           <ul><li>if the user has no {@link Permissions#UPDATE} permission on
   *           {@link Resources#PROCESS_DEFINITION}</li>
   *           <li>if {@link #cascade()} is applied and the user has
   *           no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE} or
   *           no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.</li></ul>
   */
  void delete();

}
