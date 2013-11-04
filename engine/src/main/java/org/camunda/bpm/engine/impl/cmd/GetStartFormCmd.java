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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.form.StartFormData;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.form.handler.StartFormHandler;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;


/**
 * @author Tom Baeyens
 */
public class GetStartFormCmd implements Command<StartFormData>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String processDefinitionId;

  public GetStartFormCmd(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public StartFormData execute(CommandContext commandContext) {
    ProcessDefinitionEntity processDefinition = Context
      .getProcessEngineConfiguration()
      .getDeploymentCache()
      .findDeployedProcessDefinitionById(processDefinitionId);
    if (processDefinition == null) {
      throw new ProcessEngineException("No process definition found for id '" + processDefinitionId +"'");
    }
    
    StartFormHandler startFormHandler = processDefinition.getStartFormHandler();
    if (startFormHandler == null) {
      throw new ProcessEngineException("No startFormHandler defined in process '" + processDefinitionId +"'");
    }
    
    
    return startFormHandler.createStartFormData(processDefinition);
  }
}
