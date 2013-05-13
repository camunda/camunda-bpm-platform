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

package org.camunda.bpm.engine.impl.runtime;

import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.impl.ExecutionQueryImpl;
import org.camunda.bpm.engine.impl.ProcessDefinitionQueryImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Execution;

/**
 * @author Thorben Lindhauer
 */
public class DefaultCorrelationHandler implements CorrelationHandler {

  public Execution correlateMessageToExecution(CommandContext commandContext, String messageName,
      CorrelationSet correlationSet) {
    ExecutionQueryImpl query = new ExecutionQueryImpl();
    
    Map<String, Object> correlationKeys = correlationSet.getCorrelationKeys();
    if (correlationKeys != null) {
      for (Map.Entry<String, Object> correlationKey : correlationKeys.entrySet()) {
        query.processVariableValueEquals(correlationKey.getKey(), correlationKey.getValue());
      }
    }
    
    String businessKey = correlationSet.getBusinessKey();
    if(businessKey != null) {
      query.processInstanceBusinessKey(businessKey);
    }
    
    query.messageEventSubscriptionName(messageName);
    List<Execution> matchingExecutions = query.executeList(commandContext, null);
    
    if (matchingExecutions.size() > 1) {
      throw new MismatchingMessageCorrelationException(messageName, businessKey, correlationKeys, 
          String.valueOf(matchingExecutions.size()) + " executions match the correlation keys. Should be one or zero.");
    }
    
    Execution matchingExecution = null;
    if (!matchingExecutions.isEmpty()) {
      matchingExecution = matchingExecutions.get(0);
    }

    return matchingExecution;
  }

  public ProcessDefinition correlateMessageToProcessDefinition(CommandContext commandContext, String messageName) {
    ProcessDefinitionQueryImpl query = (ProcessDefinitionQueryImpl) new ProcessDefinitionQueryImpl().messageEventSubscriptionName(messageName);
    List<ProcessDefinition> matchingDefinitions = query.executeList(commandContext, null);
    
    if (matchingDefinitions.size() > 1) {
      throw new MismatchingMessageCorrelationException(messageName, 
          String.valueOf(matchingDefinitions.size()) + " process definitions match the message " + 
          messageName + ". Should be one or zero.");
    }
    
    ProcessDefinition matchingDefinition = null;
    if (!matchingDefinitions.isEmpty()) {
      matchingDefinition = matchingDefinitions.get(0);
    }

    return matchingDefinition;
  }

}
