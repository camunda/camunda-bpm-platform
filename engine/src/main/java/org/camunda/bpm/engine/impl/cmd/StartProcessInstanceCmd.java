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

import java.io.Serializable;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.runtime.ProcessInstance;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class StartProcessInstanceCmd implements Command<ProcessInstance>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String processDefinitionKey;
  protected String processDefinitionId;
  protected Map<String, Object> variables;
  protected String businessKey;
  
  public StartProcessInstanceCmd(String processDefinitionKey, String processDefinitionId, String businessKey, Map<String, Object> variables) {
    this.processDefinitionKey = processDefinitionKey;
    this.processDefinitionId = processDefinitionId;
    this.businessKey = businessKey;
    this.variables = variables;
  }
  
  public ProcessInstance execute(CommandContext commandContext) {
    DeploymentCache deploymentCache = Context
      .getProcessEngineConfiguration()
      .getDeploymentCache();
    
    // Find the process definition
    ProcessDefinitionEntity processDefinition = null;
    if (processDefinitionId!=null) {
      processDefinition = deploymentCache.findDeployedProcessDefinitionById(processDefinitionId);
      if (processDefinition == null) {
        throw new ProcessEngineException("No process definition found for id = '" + processDefinitionId + "'");
      }
    } else if(processDefinitionKey != null){
      processDefinition = deploymentCache.findDeployedLatestProcessDefinitionByKey(processDefinitionKey);
      if (processDefinition == null) {
        throw new ProcessEngineException("No process definition found for key '" + processDefinitionKey +"'");
      }
    } else {
      throw new ProcessEngineException("processDefinitionKey and processDefinitionId are null");
    }
    
    // Start the process instance
    ExecutionEntity processInstance = processDefinition.createProcessInstance(businessKey);
    processInstance.start(variables);
    return processInstance;
  }
}