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

import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.MessageCorrelationResultType;
import org.camunda.bpm.engine.runtime.MessageCorrelationResultWithVariables;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.VariableMap;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class MessageCorrelationResultImpl implements MessageCorrelationResultWithVariables {

  protected final Execution execution;
  protected final MessageCorrelationResultType resultType;
  protected ProcessInstance processInstance;
  protected VariableMap variables;


  public MessageCorrelationResultImpl(CorrelationHandlerResult handlerResult) {
    this.execution = handlerResult.getExecution();
    this.resultType = handlerResult.getResultType();
  }

  @Override
  public Execution getExecution() {
    return execution;
  }

  @Override
  public ProcessInstance getProcessInstance() {
    return processInstance;
  }

  public void setProcessInstance(ProcessInstance processInstance) {
    this.processInstance = processInstance;
  }

  @Override
  public MessageCorrelationResultType getResultType() {
    return resultType;
  }

  @Override
  public VariableMap getVariables() {
    return variables;
  }

  public void setVariables(VariableMap variables) {
    this.variables = variables;
  }
}
