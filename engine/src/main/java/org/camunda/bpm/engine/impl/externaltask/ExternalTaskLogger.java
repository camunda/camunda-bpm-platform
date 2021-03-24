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
package org.camunda.bpm.engine.impl.externaltask;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.bpmn.parser.CamundaErrorEventDefinition;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;

/**
 * Represents the logger for the external task.
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class ExternalTaskLogger extends ProcessEngineLogger {

  /**
   * Logs that the priority could not be determined in the given context.
   *
   * @param execution the context that is used for determining the priority
   * @param value the default value
   * @param e the exception which was caught
   */
  public void couldNotDeterminePriority(ExecutionEntity execution, Object value, ProcessEngineException e) {
    logWarn(
        "001",
        "Could not determine priority for external task created in context of execution {}. Using default priority {}",
        execution, value, e);
  }

  /**
   * Logs that the error event definition expression could not be evaluated and will be considered as false
   *
   * @param taskId the context of the definition
   * @param errorEventDefinition the definition whose expression failed
   * @param exception the exception that was caught
   */
  public void errorEventDefinitionEvaluationException(String taskId, CamundaErrorEventDefinition errorEventDefinition, Exception exception) {
    logDebug("002", "Evaluation of error event definition's expression {} on external task {} failed and will be considered as 'false'. "
        + "Received exception: {}", errorEventDefinition.getExpression(), taskId, exception.getMessage());
  }
}
