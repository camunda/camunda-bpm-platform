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

import org.camunda.bpm.engine.repository.ProcessDefinition;

/**
 * Represents one execution of a  {@link ProcessDefinition}.
 *
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Daniel Meyer
 */
public interface ProcessInstance extends Execution {

  /**
   * The id of the process definition of the process instance.
   */
  String getProcessDefinitionId();

  /**
   * The business key of this process instance.
   */
  String getBusinessKey();

  /**
   * The id of the root process instance associated with this process instance.
   */
  String getRootProcessInstanceId();

  /**
   * The id of the case instance associated with this process instance.
   */
  String getCaseInstanceId();

  /**
   * returns true if the process instance is suspended
   */
  boolean isSuspended();

}
