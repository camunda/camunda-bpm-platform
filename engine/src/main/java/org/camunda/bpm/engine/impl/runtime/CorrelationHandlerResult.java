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
package org.camunda.bpm.engine.impl.runtime;

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.MessageCorrelationResultType;

/**
 * <p>The result of a message correlation. A message may be correlated to either
 * a waiting execution (BPMN receive message event) or a process definition
 * (BPMN message start event). The type of the correlation (execution vs.
 * processDefinition) can be obtained using {@link #getResultType()}</p>
 *
 * <p>Correlation is performed by a {@link CorrelationHandler}.</p>
 *
 * @author Daniel Meyer
 *
 */
public class CorrelationHandlerResult {

  /**
   * @see MessageCorrelationResultType#Execution
   * @see MessageCorrelationResultType#ProcessDefinition
   */
  protected MessageCorrelationResultType resultType;

  protected ExecutionEntity executionEntity;
  protected ProcessDefinitionEntity processDefinitionEntity;
  protected String startEventActivityId;

  public static CorrelationHandlerResult matchedExecution(ExecutionEntity executionEntity) {
    CorrelationHandlerResult messageCorrelationResult = new CorrelationHandlerResult();
    messageCorrelationResult.resultType = MessageCorrelationResultType.Execution;
    messageCorrelationResult.executionEntity = executionEntity;
    return messageCorrelationResult;
  }

  public static CorrelationHandlerResult matchedProcessDefinition(ProcessDefinitionEntity processDefinitionEntity, String startEventActivityId) {
    CorrelationHandlerResult messageCorrelationResult = new CorrelationHandlerResult();
    messageCorrelationResult.processDefinitionEntity = processDefinitionEntity;
    messageCorrelationResult.startEventActivityId = startEventActivityId;
    messageCorrelationResult.resultType = MessageCorrelationResultType.ProcessDefinition;
    return messageCorrelationResult;
  }

  // getters ////////////////////////////////////////////

  public ExecutionEntity getExecutionEntity() {
    return executionEntity;
  }

  public ProcessDefinitionEntity getProcessDefinitionEntity() {
    return processDefinitionEntity;
  }

  public String getStartEventActivityId() {
    return startEventActivityId;
  }

  public MessageCorrelationResultType getResultType() {
    return resultType;
  }

  public Execution getExecution() {
    return executionEntity;
  }

  public ProcessDefinition getProcessDefinition() {
    return processDefinitionEntity;
  }
}
