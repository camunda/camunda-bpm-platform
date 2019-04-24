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
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;

/**
 * Fluent builder to update the suspension state of process instances.
 */
public interface UpdateProcessInstanceSuspensionStateSelectBuilder extends UpdateProcessInstancesRequest{

  /**
   * Selects the process instance with the given id.
   *
   * @param processInstanceId
   *          id of the process instance
   * @return the builder
   */
  UpdateProcessInstanceSuspensionStateBuilder byProcessInstanceId(String processInstanceId);

  /**
   * Selects the instances of the process definition with the given id.
   *
   * @param processDefinitionId
   *          id of the process definition
   * @return the builder
   */
  UpdateProcessInstanceSuspensionStateBuilder byProcessDefinitionId(String processDefinitionId);

  /**
   * Selects the instances of the process definitions with the given key.
   *
   * @param processDefinitionKey
   *          key of the process definition
   * @return the builder
   */
  UpdateProcessInstanceSuspensionStateTenantBuilder byProcessDefinitionKey(String processDefinitionKey);

}
