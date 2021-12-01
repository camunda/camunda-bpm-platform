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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.form.FormDefinition;
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

      FormDefinition formDefinition = processDefinition.getStartFormDefinition();
      formKeyExpression = formDefinition.getFormKey();

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
