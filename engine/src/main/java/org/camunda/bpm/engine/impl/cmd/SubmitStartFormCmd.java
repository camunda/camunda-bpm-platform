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
import java.util.Map;

import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.form.FormPropertyHelper;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.cache.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.runtime.ProcessInstanceStartContext;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class SubmitStartFormCmd implements Command<ProcessInstance>, Serializable {

  private static final long serialVersionUID = 1L;

  protected final String processDefinitionId;
  protected final String businessKey;
  protected VariableMap variables;

  public SubmitStartFormCmd(String processDefinitionId, String businessKey, Map<String, Object> properties) {
    this.processDefinitionId = processDefinitionId;
    this.businessKey = businessKey;
    this.variables = Variables.fromMap(properties);
  }

  @Override
  public ProcessInstance execute(CommandContext commandContext) {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    DeploymentCache deploymentCache = processEngineConfiguration.getDeploymentCache();
    ProcessDefinitionEntity processDefinition = deploymentCache.findDeployedProcessDefinitionById(processDefinitionId);
    ensureNotNull("No process definition found for id = '" + processDefinitionId + "'", "processDefinition", processDefinition);

    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkCreateProcessInstance(processDefinition);
    }

    ExecutionEntity processInstance = null;
    if (businessKey != null) {
      processInstance = processDefinition.createProcessInstance(businessKey);
    } else {
      processInstance = processDefinition.createProcessInstance();
    }

    // if the start event is async, we have to set the variables already here
    // since they are lost after the async continuation otherwise
    // see CAM-2828
    if (processDefinition.getInitial().isAsyncBefore()) {
      // avoid firing history events
      processInstance.setStartContext(new ProcessInstanceStartContext(processInstance.getActivity()));
      FormPropertyHelper.initFormPropertiesOnScope(variables, processInstance);
      processInstance.start();

    } else {
      processInstance.startWithFormProperties(variables);

    }


    return processInstance;
  }
}
