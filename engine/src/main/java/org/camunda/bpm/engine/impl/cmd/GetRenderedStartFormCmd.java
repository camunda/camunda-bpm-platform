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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.Serializable;

import org.camunda.bpm.engine.ScriptEvaluationException;
import org.camunda.bpm.engine.form.StartFormData;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.form.engine.FormEngine;
import org.camunda.bpm.engine.impl.form.handler.StartFormHandler;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.cache.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class GetRenderedStartFormCmd implements Command<Object>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String processDefinitionId;
  protected String formEngineName;
  private static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  public GetRenderedStartFormCmd(String processDefinitionId, String formEngineName) {
    this.processDefinitionId = processDefinitionId;
    this.formEngineName = formEngineName;
  }

  public Object execute(CommandContext commandContext) {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    DeploymentCache deploymentCache = processEngineConfiguration.getDeploymentCache();
    ProcessDefinitionEntity processDefinition = deploymentCache.findDeployedProcessDefinitionById(processDefinitionId);
    ensureNotNull("Process Definition '" + processDefinitionId + "' not found", "processDefinition", processDefinition);

    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkReadProcessDefinition(processDefinition);
    }

    StartFormHandler startFormHandler = processDefinition.getStartFormHandler();
    if (startFormHandler == null) {
      return null;
    }

    FormEngine formEngine = Context
      .getProcessEngineConfiguration()
      .getFormEngines()
      .get(formEngineName);

    ensureNotNull("No formEngine '" + formEngineName + "' defined process engine configuration", "formEngine", formEngine);

    StartFormData startForm = startFormHandler.createStartFormData(processDefinition);

    Object renderedStartForm = null;
    try {
      renderedStartForm = formEngine.renderStartForm(startForm);
    } catch (ScriptEvaluationException e) {
      LOG.exceptionWhenStartFormScriptEvaluation(processDefinitionId, e);
    }
    return renderedStartForm;
  }
}
