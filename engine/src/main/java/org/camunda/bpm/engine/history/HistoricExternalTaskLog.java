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

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.externaltask.ExternalTask;

import java.util.Date;

/**
 * <p>The {@link HistoricExternalTaskLog} is used to have a log containing
 * information about {@link ExternalTask task} execution. The log provides
 * details about the last lifecycle state of a {@link ExternalTask task}:</p>
 *
 * An instance of {@link HistoricExternalTaskLog} represents the latest historic
 * state in the lifecycle of a {@link ExternalTask task}.
 *
 * @since 7.7
 */
public interface HistoricExternalTaskLog {

  /**
   * Returns the unique identifier for <code>this</code> historic external task log.
   */
  String getId();

  /**
   * Returns the time when <code>this</code> log occurred.
   */
  Date getTimestamp();

  /**
   * Returns the id of the associated external task.
   */
  String getExternalTaskId();

  /**
   * Returns the retries of the associated external task before the associated external task has
   * been executed and when <code>this</code> log occurred.
   */
  Integer getRetries();

  /**
   * Returns the priority of the associated external task when <code>this</code> log entry was created.
   */
  long getPriority();

  /**
   * Returns the topic name of the associated external task.
   */
  String getTopicName();

  /**
   * Returns the id of the worker that fetched the external task most recently.
   */
  String getWorkerId();

  /**
   * Returns the message of the error that occurred by executing the associated external task.
   *
   * To get the full error details,
   * use {@link HistoryService#getHistoricExternalTaskLogErrorDetails(String)}
   */
  String getErrorMessage();

  /**
   * Returns the id of the activity which the external task associated with.
   */
  String getActivityId();

  /**
   * Returns the id of the activity instance on which the associated external task was created.
   */
  String getActivityInstanceId();

  /**
   * Returns the specific execution id on which the associated external task was created.
   */
  String getExecutionId();

  /**
   * Returns the specific root process instance id of the process instance
   * on which the associated external task was created.
   */
  String getRootProcessInstanceId();

  /**
   * Returns the specific process instance id on which the associated external task was created.
   */
  String getProcessInstanceId();

  /**
   * Returns the specific process definition id on which the associated external task was created.
   */
  String getProcessDefinitionId();

  /**
   * Returns the specific process definition key on which the associated external task was created.
   */
  String getProcessDefinitionKey();

  /**
   * Returns the id of the tenant this external task log entry belongs to. Can be <code>null</code>
   * if the external task log entry belongs to no single tenant.
   */
  String getTenantId();

  /**
   * Returns <code>true</code> when <code>this</code> log represents
   * the creation of the associated external task.
   */
  boolean isCreationLog();

  /**
   * Returns <code>true</code> when <code>this</code> log represents
   * the failed execution of the associated external task.
   */
  boolean isFailureLog();

  /**
   * Returns <code>true</code> when <code>this</code> log represents
   * the successful execution of the associated external task.
   */
  boolean isSuccessLog();

  /**
   * Returns <code>true</code> when <code>this</code> log represents
   * the deletion of the associated external task.
   */
  boolean isDeletionLog();

  /** The time the historic external task log will be removed. */
  Date getRemovalTime();

}
