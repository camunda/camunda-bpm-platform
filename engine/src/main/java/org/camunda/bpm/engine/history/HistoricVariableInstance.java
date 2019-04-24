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

import java.util.Date;

import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * A single process variable containing the last value when its process instance has finished.
 * It is only available when HISTORY_LEVEL is set >= AUDIT
 *
 * @author Christian Lipphardt (camunda)
 * @author ruecker
 */
public interface HistoricVariableInstance {

  String STATE_CREATED = "CREATED";
  String STATE_DELETED = "DELETED";

  /**
   * @return the Id of this variable instance
   */
  String getId();

  /**
   * Returns the name of this variable instance.
   */
  String getName();

  /**
   * Returns the name of the type of this variable instance
   *
   * @return the type name of the variable
   */
  String getTypeName();

  /**
   * Returns the value of this variable instance.
   */
  Object getValue();

  /**
   * Returns the {@link TypedValue} of this variable instance.
   */
  TypedValue getTypedValue();

  /**
   * Returns the name of this variable instance.
   *
   * <p>Deprecated since 7.2: use {@link #getName()} instead.</p>
   *
   */
   @Deprecated
  String getVariableName();

  /**
   * <p>Returns the name of the type of this variable instance</p>
   *
   * <p>Deprecated since 7.2: use {@link #getTypeName()} instead.</p>
   *
   */
  @Deprecated
  String getVariableTypeName();

  /**
   * The process definition key reference.
   */
  String getProcessDefinitionKey();

  /**
   * The process definition reference.
   */
  String getProcessDefinitionId();

  /**
   * The root process instance reference.
   */
  String getRootProcessInstanceId();

  /**
   * The process instance reference.
   */
  String getProcessInstanceId();

  /**
   * Return the corresponding execution id.
   */
  String getExecutionId();

  /**
   * Returns the corresponding activity instance id.
   */
  @Deprecated
  String getActivtyInstanceId();

  /**
   * Returns the corresponding activity instance id.
   */
  String getActivityInstanceId();

  /**
   * The case definition key reference.
   */
  String getCaseDefinitionKey();

  /**
   * The case definition reference.
   */
  String getCaseDefinitionId();

  /**
   * The case instance reference.
   */
  String getCaseInstanceId();

  /**
   * Return the corresponding case execution id.
   */
  String getCaseExecutionId();

  /**
   * Return the corresponding task id.
   */
  String getTaskId();

  /**
   * If the variable value could not be loaded, this returns the error message.
   * @return an error message indicating why the variable value could not be loaded.
   */
  String getErrorMessage();

  /**
   * The id of the tenant this variable belongs to. Can be <code>null</code>
   * if the variable belongs to no single tenant.
   */
  String getTenantId();

  /**
   * The current state of the variable. Can be 'CREATED' or 'DELETED'
   */
  String getState();

  /**
   * The time when the variable was created.
   */
  Date getCreateTime();

  /** The time when the historic variable instance will be removed. */
  Date getRemovalTime();
}
