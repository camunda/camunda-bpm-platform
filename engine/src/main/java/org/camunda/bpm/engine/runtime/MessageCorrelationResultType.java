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
 * The message correlation result type indicates which type
 * of message correlation result is returned after a message correlation.
 * A message may be correlated to either
 * a waiting execution (BPMN receive message event) or a process definition
 * (BPMN message start event). The result type indicates which correlation was performed.
 * 
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public enum MessageCorrelationResultType {

  /** signifies a message correlated to an execution */
  Execution,

  /** signifies a message correlated to a process definition */
  ProcessDefinition
}
