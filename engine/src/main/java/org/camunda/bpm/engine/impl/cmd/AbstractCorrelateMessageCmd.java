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
import org.camunda.bpm.engine.impl.runtime.MessageCorrelationResult;
import org.camunda.bpm.engine.runtime.ProcessInstance;

/**
 * @author Thorben Lindhauer
 * @author Daniel Meyer
 * @author Michael Scholz
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

  protected void triggerExecution(CommandContext commandContext, MessageCorrelationResult correlationResult) {
    String executionId = correlationResult.getExecutionEntity().getId();

    MessageEventReceivedCmd command = new MessageEventReceivedCmd(messageName, executionId, builder.getPayloadProcessInstanceVariables(), builder.isExclusiveCorrelation());
    command.execute(commandContext);
  }

  protected ProcessInstance instantiateProcess(CommandContext commandContext, MessageCorrelationResult correlationResult) {
    ProcessDefinitionEntity processDefinitionEntity = correlationResult.getProcessDefinitionEntity();

    ActivityImpl messageStartEvent = processDefinitionEntity.findActivity(correlationResult.getStartEventActivityId());
    ExecutionEntity processInstance = processDefinitionEntity.createProcessInstance(builder.getBusinessKey(), messageStartEvent);
    processInstance.start(builder.getPayloadProcessInstanceVariables());

    return processInstance;
  }

  protected void checkAuthorization(MessageCorrelationResult correlation) {
    CommandContext commandContext = Context.getCommandContext();

    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      if (MessageCorrelationResult.TYPE_EXECUTION.equals(correlation.getResultType())) {
        ExecutionEntity execution = correlation.getExecutionEntity();
        checker.checkUpdateProcessInstanceById(execution.getProcessInstanceId());

      } else {
        ProcessDefinitionEntity definition = correlation.getProcessDefinitionEntity();

        checker.checkCreateProcessInstance(definition);
      }
    }
  }

}
