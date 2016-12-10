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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.form.handler.DefaultStartFormHandler;
import org.camunda.bpm.engine.impl.form.handler.DelegateStartFormHandler;
import org.camunda.bpm.engine.impl.form.handler.FormHandler;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.cache.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.task.TaskDefinition;


/**
 * Command for retrieving start or task form keys.
 *
 * @author Falko Menge (camunda)
 */
public class GetFormKeyCmd implements Command<String> {

  protected String taskDefinitionKey;
  protected String processDefinitionId;

  /**
   * Retrieves a start form key.
   */
  public GetFormKeyCmd(String processDefinitionId) {
    setProcessDefinitionId(processDefinitionId);
  }

  /**
   * Retrieves a task form key.
   */
  public GetFormKeyCmd(String processDefinitionId, String taskDefinitionKey) {
    setProcessDefinitionId(processDefinitionId);
    if (taskDefinitionKey == null || taskDefinitionKey.length() < 1) {
      throw new ProcessEngineException("The task definition key is mandatory, but '" + taskDefinitionKey + "' has been provided.");
    }
    this.taskDefinitionKey = taskDefinitionKey;
  }

  protected void setProcessDefinitionId(String processDefinitionId) {
    if (processDefinitionId == null || processDefinitionId.length() < 1) {
      throw new ProcessEngineException("The process definition id is mandatory, but '" + processDefinitionId + "' has been provided.");
    }
    this.processDefinitionId = processDefinitionId;
  }

  public String execute(CommandContext commandContext) {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    DeploymentCache deploymentCache = processEngineConfiguration.getDeploymentCache();
    ProcessDefinitionEntity processDefinition = deploymentCache.findDeployedProcessDefinitionById(processDefinitionId);

    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkReadProcessDefinition(processDefinition);
    }

    Expression formKeyExpression = null;

    if (taskDefinitionKey == null) {

      // TODO: Maybe add getFormKey() to FormHandler interface to avoid the following cast
      FormHandler formHandler = processDefinition.getStartFormHandler();

      if (formHandler instanceof DelegateStartFormHandler) {
        DelegateStartFormHandler delegateFormHandler = (DelegateStartFormHandler) formHandler;
        formHandler = delegateFormHandler.getFormHandler();
      }

      // Sorry!!! In case of a custom start form handler (which does not extend
      // the DefaultFormHandler) a formKey would never be returned. So a custom
      // form handler (for a startForm) has always to extend the DefaultStartFormHandler!
      if (formHandler instanceof DefaultStartFormHandler) {
        DefaultStartFormHandler startFormHandler = (DefaultStartFormHandler) formHandler;
        formKeyExpression = startFormHandler.getFormKey();
      }

    } else {
      TaskDefinition taskDefinition = processDefinition.getTaskDefinitions().get(taskDefinitionKey);
      formKeyExpression = taskDefinition.getFormKey();
    }

    String formKey = null;
    if (formKeyExpression != null) {
      formKey = formKeyExpression.getExpressionText();
    }
    return formKey;
  }

}
