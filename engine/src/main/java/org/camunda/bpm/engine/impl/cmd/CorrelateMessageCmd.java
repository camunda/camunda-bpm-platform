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

import java.util.Map;

import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.MessageCorrelationBuilderImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.runtime.CorrelationHandler;
import org.camunda.bpm.engine.impl.runtime.CorrelationSet;
import org.camunda.bpm.engine.impl.runtime.MessageCorrelationResult;

/**
 * @author Thorben Lindhauer
 * @author Daniel Meyer
 */
public class CorrelateMessageCmd implements Command<Void> {

  protected final String messageName;
  protected final String businessKey;
  protected final Map<String, Object> correlationKeys;
  protected final Map<String, Object> processVariables;
  protected String processInstanceId;

  public CorrelateMessageCmd(String messageName, String businessKey,
      Map<String, Object> correlationKeys, Map<String, Object> processVariables) {
    this.messageName = messageName;
    this.businessKey = businessKey;
    this.correlationKeys = correlationKeys;
    this.processVariables = processVariables;
  }

  /**
   * Initialize the command with a builder
   *
   * @param messageCorrelationBuilderImpl
   */
  public CorrelateMessageCmd(MessageCorrelationBuilderImpl messageCorrelationBuilderImpl) {
    this.messageName = messageCorrelationBuilderImpl.getMessageName();
    this.processVariables = messageCorrelationBuilderImpl.getPayloadProcessInstanceVariables();
    this.correlationKeys = messageCorrelationBuilderImpl.getCorrelationProcessInstanceVariables();
    this.businessKey = messageCorrelationBuilderImpl.getBusinessKey();
    this.processInstanceId = messageCorrelationBuilderImpl.getProcessInstanceId();
  }

  public Void execute(CommandContext commandContext) {
    if(messageName == null) {
      throw new ProcessEngineException("messageName cannot be null");
    }

    CorrelationHandler correlationHandler = Context.getProcessEngineConfiguration().getCorrelationHandler();

    CorrelationSet correlationSet = new CorrelationSet(businessKey, processInstanceId, correlationKeys);
    MessageCorrelationResult correlationResult = correlationHandler.correlateMessage(commandContext, messageName, correlationSet);

    if(correlationResult == null) {
      throw new MismatchingMessageCorrelationException(messageName, "No process definition or execution matches the parameters");

    } else if(MessageCorrelationResult.TYPE_EXECUTION.equals(correlationResult.getResultType())) {
      triggerExecution(commandContext, correlationResult);

    } else {
      instantiateProcess(commandContext, correlationResult);

    }

    return null;
  }

  protected void triggerExecution(CommandContext commandContext, MessageCorrelationResult correlationResult) {
    new MessageEventReceivedCmd(messageName, correlationResult.getExecutionEntity().getId(), processVariables).execute(commandContext);
  }

  protected void instantiateProcess(CommandContext commandContext, MessageCorrelationResult correlationResult) {
    ProcessDefinitionEntity processDefinitionEntity = correlationResult.getProcessDefinitionEntity();
    ActivityImpl messageStartEvent = processDefinitionEntity.findActivity(correlationResult.getStartEventActivityId());
    ExecutionEntity processInstance = processDefinitionEntity.createProcessInstance(businessKey, messageStartEvent);
    processInstance.start(businessKey, processVariables);
  }

}
