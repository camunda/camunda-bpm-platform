/*
 * Copyright 2016 camunda services GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.runtime;

/**
 * <p>The result of a message correlation. A message may be correlated to either
 * a waiting execution (BPMN receive message event) or a process definition
 * (BPMN message start event). The type of the correlation (execution vs.
 * processDefinition) can be obtained using {@link #getResultType()}</p>
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 * @since 7.6
 */
public interface MessageCorrelationResult {

  /**
   * Returns the execution entity on which the message was correlated to.
   *
   * @return the execution
   */
  Execution getExecution();

  /**
   * Returns the process instance id on which the message was correlated to.
   *
   * @return the process instance id
   */
  ProcessInstance getProcessInstance();

  /**
   * Returns the result type of the message correlation result.
   * Indicates if either the message was correlated to a waiting execution
   * or to a process definition like a start event.
   *
   * @return the result type of the message correlation result
   * @see {@link MessageCorrelationResultType}
   */
  MessageCorrelationResultType getResultType();
}
