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
package org.camunda.bpm.engine.cdi;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;

import java.util.Date;

/**
 * Signifies an event that is happening / has happened during the execution of a
 * business process.
 * 
 * @author Daniel Meyer
 */
public interface BusinessProcessEvent {

  /**
   * @return the process definition in which the event is happening / has
   *         happened or null the event was not related to a process definition
   */
  public ProcessDefinition getProcessDefinition();

  /**
   * @return the id of the activity the process is currently in / was in at the
   *         moment the event was fired.
   */
  public String getActivityId();

  /**
   * @return the name of the transition being taken / that was taken. (null, if
   *         this event is not of type {@link BusinessProcessEventType#TAKE}
   */
  public String getTransitionName();

  /**
   * @return the id of the {@link ProcessInstance} this event corresponds to
   */
  public String getProcessInstanceId();

  /**
   * @return the id of the {@link Execution} this event corresponds to
   */
  public String getExecutionId();

  /**
   * @return the type of the event
   */
  public BusinessProcessEventType getType();

  /**
   * @return the timestamp indicating the local time at which the event was
   *         fired.
   */
  public Date getTimeStamp();

  /**
   * @return the delegate task if this is a task event.
   */
  public DelegateTask getTask();

  /**
   * @return the task id of the current task or null if this is not a task event.
   */
  public String getTaskId();

  /**
   * @return the id of the task in the process definition (BPMN XML) or null if this is not a task event.
   */
  public String getTaskDefinitionKey();
}
