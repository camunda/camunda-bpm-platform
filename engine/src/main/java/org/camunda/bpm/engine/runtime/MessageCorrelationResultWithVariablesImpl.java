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

import org.camunda.bpm.engine.impl.runtime.CorrelationHandlerResult;
import org.camunda.bpm.engine.impl.runtime.MessageCorrelationResultImpl;
import org.camunda.bpm.engine.variable.VariableMap;

public class MessageCorrelationResultWithVariablesImpl implements MessageCorrelationResultWithVariables {

  protected MessageCorrelationResultImpl result;
  protected VariableMap variables;

  public MessageCorrelationResultWithVariablesImpl(CorrelationHandlerResult handlerResult) {
    this.result = new MessageCorrelationResultImpl(handlerResult);
  }

  @Override
  public Execution getExecution() {
    return result.getExecution();
  }

  @Override
  public ProcessInstance getProcessInstance() {
    return result.getProcessInstance();
  }

  public void setProcessInstance(ProcessInstance processInstance) {
    result.setProcessInstance(processInstance);
  }

  @Override
  public MessageCorrelationResultType getResultType() {
    return result.getResultType();
  }

  public MessageCorrelationResult getMessageCorrelationResult() {
    return result;
  }

  @Override
  public VariableMap getVariables() {
    return variables;
  }

  public void setVariables(VariableMap variables) {
    this.variables = variables;
  }
}
