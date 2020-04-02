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
package org.camunda.bpm.engine.impl.cmd;

import org.camunda.bpm.engine.impl.MessageCorrelationBuilderImpl;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionVariableSnapshotObserver;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.runtime.CorrelationHandlerResult;
import org.camunda.bpm.engine.impl.runtime.MessageCorrelationResultImpl;
import org.camunda.bpm.engine.runtime.MessageCorrelationResultType;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;

/**
 * @author Thorben Lindhauer
 * @author Daniel Meyer
 * @author Michael Scholz
 * @author Christopher Zell
 */
public abstract class AbstractCorrelateMessageCmd {

  protected final String messageName;

  protected final MessageCorrelationBuilderImpl builder;

  protected ExecutionVariableSnapshotObserver variablesListener;
  protected boolean variablesEnabled = false;
  protected boolean deserializeVariableValues = false;

  /**
   * Initialize the command with a builder
   *
   * @param builder
   */
  protected AbstractCorrelateMessageCmd(MessageCorrelationBuilderImpl builder) {
    this.builder = builder;
    this.messageName = builder.getMessageName();
  }

  protected AbstractCorrelateMessageCmd(MessageCorrelationBuilderImpl builder, boolean variablesEnabled, boolean deserializeVariableValues) {
    this(builder);
    this.variablesEnabled = variablesEnabled;
    this.deserializeVariableValues = deserializeVariableValues;
  }

  protected void triggerExecution(CommandContext commandContext, CorrelationHandlerResult correlationResult) {
    String executionId = correlationResult.getExecutionEntity().getId();

    MessageEventReceivedCmd command = new MessageEventReceivedCmd(messageName, executionId, builder.getPayloadProcessInstanceVariables(), builder.getPayloadProcessInstanceVariablesLocal(), builder.isExclusiveCorrelation());
    command.execute(commandContext);
  }

  protected ProcessInstance instantiateProcess(CommandContext commandContext, CorrelationHandlerResult correlationResult) {
    ProcessDefinitionEntity processDefinitionEntity = correlationResult.getProcessDefinitionEntity();

    ActivityImpl messageStartEvent = processDefinitionEntity.findActivity(correlationResult.getStartEventActivityId());
    ExecutionEntity processInstance = processDefinitionEntity.createProcessInstance(builder.getBusinessKey(), messageStartEvent);

    if (variablesEnabled) {
      variablesListener = new ExecutionVariableSnapshotObserver(processInstance, false, deserializeVariableValues);
    }

    VariableMap startVariables = resolveStartVariables();

    processInstance.start(startVariables);

    return processInstance;
  }

  protected void checkAuthorization(CorrelationHandlerResult correlation) {
    CommandContext commandContext = Context.getCommandContext();

    for (CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      if (MessageCorrelationResultType.Execution.equals(correlation.getResultType())) {
        ExecutionEntity execution = correlation.getExecutionEntity();
        checker.checkUpdateProcessInstanceById(execution.getProcessInstanceId());

      } else {
        ProcessDefinitionEntity definition = correlation.getProcessDefinitionEntity();

        checker.checkCreateProcessInstance(definition);
      }
    }
  }

  protected MessageCorrelationResultImpl createMessageCorrelationResult(final CommandContext commandContext, final CorrelationHandlerResult handlerResult) {
    MessageCorrelationResultImpl resultWithVariables = new MessageCorrelationResultImpl(handlerResult);
    if (MessageCorrelationResultType.Execution.equals(handlerResult.getResultType())) {
      ExecutionEntity execution = findProcessInstanceExecution(commandContext, handlerResult);
      if (variablesEnabled && execution != null) {
        variablesListener = new ExecutionVariableSnapshotObserver(execution, false, deserializeVariableValues);
      }
      triggerExecution(commandContext, handlerResult);
    } else {
      ProcessInstance instance = instantiateProcess(commandContext, handlerResult);
      resultWithVariables.setProcessInstance(instance);
    }

    if (variablesListener != null) {
      resultWithVariables.setVariables(variablesListener.getVariables());
    }

    return resultWithVariables;
  }

  protected ExecutionEntity findProcessInstanceExecution(final CommandContext commandContext, final CorrelationHandlerResult handlerResult) {
    ExecutionEntity execution = commandContext.getExecutionManager().findExecutionById(handlerResult.getExecution().getProcessInstanceId());
    return execution;
  }

  protected VariableMap resolveStartVariables() {
    VariableMap mergedVariables = Variables.createVariables();
    mergedVariables.putAll(builder.getPayloadProcessInstanceVariables());
    mergedVariables.putAll(builder.getPayloadProcessInstanceVariablesLocal());
    return mergedVariables;
  }

}
