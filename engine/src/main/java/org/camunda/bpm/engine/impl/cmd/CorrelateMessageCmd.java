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
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.runtime.CorrelationHandler;
import org.camunda.bpm.engine.impl.runtime.CorrelationSet;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Execution;

/**
 * @author Thorben Lindhauer
 */
public class CorrelateMessageCmd implements Command<Void> {

  protected final String messageName;
  protected final String businessKey;
  protected final Map<String, Object> correlationKeys;
  protected final Map<String, Object> processVariables;
  
  public CorrelateMessageCmd(String messageName, String businessKey,
      Map<String, Object> correlationKeys, Map<String, Object> processVariables) {
    this.messageName = messageName;
    this.businessKey = businessKey;
    this.correlationKeys = correlationKeys;
    this.processVariables = processVariables;
  }

  public Void execute(CommandContext commandContext) {
    if(messageName == null) {
      throw new ProcessEngineException("messageName cannot be null");
    }

    CorrelationHandler correlationHandler = Context.getProcessEngineConfiguration().getCorrelationHandler();
    
    CorrelationSet correlationSet = new CorrelationSet(businessKey, correlationKeys);
    Execution matchingExecution = correlationHandler.correlateMessageToExecution(commandContext, messageName, correlationSet);
    ProcessDefinition matchingDefinition = correlationHandler.correlateMessageToProcessDefinition(commandContext, messageName);
    
    if (matchingExecution != null && matchingDefinition != null) {
      throw new MismatchingMessageCorrelationException(messageName, businessKey, 
          correlationKeys, "An execution and a process definition match the correlation.");
    }
    
    if (matchingExecution != null) {
      triggerExecution(commandContext, matchingExecution);
      return null;
    }
    
    if (matchingDefinition != null) {
      instantiateProcess(commandContext, matchingDefinition);
      return null;
    }
    
    throw new MismatchingMessageCorrelationException(messageName, "No process definition or execution matches the parameters");
  }

  private void triggerExecution(CommandContext commandContext, Execution matchingExecution) {
    new MessageEventReceivedCmd(messageName, matchingExecution.getId(), processVariables).execute(commandContext);
  }
  
  private void instantiateProcess(CommandContext commandContext,
      ProcessDefinition matchingDefinition) {
    new StartProcessInstanceCmd(null, matchingDefinition.getId(), null, processVariables).execute(commandContext);
  }
  
}
