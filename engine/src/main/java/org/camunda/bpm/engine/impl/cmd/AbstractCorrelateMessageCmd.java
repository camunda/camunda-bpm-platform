/* Licensed under the Apache License, Version 2.0 (the "License");
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
package org.camunda.bpm.engine.impl.cmd;

import org.camunda.bpm.engine.impl.MessageCorrelationBuilderImpl;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.runtime.CorrelationHandlerResult;
import org.camunda.bpm.engine.impl.runtime.MessageCorrelationResultImpl;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.camunda.bpm.engine.runtime.MessageCorrelationResultType;
import org.camunda.bpm.engine.runtime.ProcessInstance;

/**
 * @author Thorben Lindhauer
 * @author Daniel Meyer
 * @author Michael Scholz
 * @author Christopher Zell
 */
public abstract class AbstractCorrelateMessageCmd {

  protected final String messageName;

  protected final MessageCorrelationBuilderImpl builder;

  /**
   * Initialize the command with a builder
   *
   * @param builder
   */
  protected AbstractCorrelateMessageCmd(MessageCorrelationBuilderImpl builder) {
    this.builder = builder;
    this.messageName = builder.getMessageName();
  }

  protected void triggerExecution(CommandContext commandContext, CorrelationHandlerResult correlationResult) {
    String executionId = correlationResult.getExecutionEntity().getId();

    MessageEventReceivedCmd command = new MessageEventReceivedCmd(messageName, executionId, builder.getPayloadProcessInstanceVariables(), builder.isExclusiveCorrelation());
    command.execute(commandContext);
  }

  protected ProcessInstance instantiateProcess(CommandContext commandContext, CorrelationHandlerResult correlationResult) {
    ProcessDefinitionEntity processDefinitionEntity = correlationResult.getProcessDefinitionEntity();

    ActivityImpl messageStartEvent = processDefinitionEntity.findActivity(correlationResult.getStartEventActivityId());
    ExecutionEntity processInstance = processDefinitionEntity.createProcessInstance(builder.getBusinessKey(), messageStartEvent);
    processInstance.start(builder.getPayloadProcessInstanceVariables());

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

  protected MessageCorrelationResult createMessageCorrelationResult(final CommandContext commandContext, final CorrelationHandlerResult handlerResult) {
    MessageCorrelationResultImpl result = new MessageCorrelationResultImpl(handlerResult);
    if (MessageCorrelationResultType.Execution.equals(handlerResult.getResultType())) {
      triggerExecution(commandContext, handlerResult);
    } else {
      ProcessInstance instance = instantiateProcess(commandContext, handlerResult);
      result.setProcessInstance(instance);
    }
    return result;
  }

}
