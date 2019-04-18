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

import java.util.Map;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;

/**
 * Fluent builder to notify the process engine that a signal event has been
 * received.
 */
public interface SignalEventReceivedBuilder {

  /**
   * Add the given variables to the triggered executions.
   *
   * @param variables
   *          a map of variables added to the executions
   * @return the builder
   */
  SignalEventReceivedBuilder setVariables(Map<String, Object> variables);

  /**
   * Specify a single execution to deliver the signal to.
   *
   * @param executionId
   *          the id of the process instance or the execution to deliver the
   *          signal to
   * @return the builder
   */
  SignalEventReceivedBuilder executionId(String executionId);

  /**
   * Specify a tenant to deliver the signal to. The signal can only be received
   * on executions or process definitions which belongs to the given tenant.
   * Cannot be used in combination with {@link #executionId(String)}.
   *
   * @param tenantId
   *          the id of the tenant
   * @return the builder
   */
  SignalEventReceivedBuilder tenantId(String tenantId);

  /**
   * Specify that the signal can only be received on executions or process
   * definitions which belongs to no tenant. Cannot be used in combination with
   * {@link #executionId(String)}.
   *
   * @return the builder
   */
  SignalEventReceivedBuilder withoutTenantId();

  /**
   * <p>
   * Delivers the signal to waiting executions and process definitions. The notification and instantiation happen
   * synchronously.
   * </p>
   *
   * <p>
   * Note that the signal delivers to all tenants if no tenant is specified
   * using {@link #tenantId(String)} or {@link #withoutTenantId()}.
   * </p>
   *
   * @throws ProcessEngineException
   *           if a single execution is specified and no such execution exists
   *           or has not subscribed to the signal
   * @throws AuthorizationException
   *           <li>if notify an execution and the user has no
   *           {@link Permissions#UPDATE} permission on
   *           {@link Resources#PROCESS_INSTANCE} or no
   *           {@link Permissions#UPDATE_INSTANCE} permission on
   *           {@link Resources#PROCESS_DEFINITION}.</li>
   *           <li>if start a new process instance and the user has no
   *           {@link Permissions#CREATE} permission on
   *           {@link Resources#PROCESS_INSTANCE} and no
   *           {@link Permissions#CREATE_INSTANCE} permission on
   *           {@link Resources#PROCESS_DEFINITION}.</li>
   */
  void send();

}
