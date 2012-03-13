/**
 * Copyright (C) 2011, 2012 camunda services GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.camunda.fox.platform.deployer.impl;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.ProcessDefinitionQueryImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cmd.ActivateProcessDefinitionCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.deploy.DeploymentCache;
import org.activiti.engine.repository.ProcessDefinition;

/**
 * For each process definition part of this deployment: 
 *  - add the key of the process definition to the classloader
 *  - get all process definitions with the same key
 *  - then, for each process definition with that key:
 *    - activate the process definition (resumes job execution) 
 *    - add the process definition to the activiti deployment cache 
 * 
 * @author Daniel Meyer
 * @author nico.rehwaldt@camunda.com
 */
public class ActivateDeploymentCmd implements Command<Void> {

  private static Logger log = Logger.getLogger(ActivateDeploymentCmd.class.getName());
  
  protected final String deploymentId;
  protected final ClassLoader classloader;

  public ActivateDeploymentCmd(String deploymentId, ClassLoader classloader) {
    this.deploymentId = deploymentId;
    this.classloader = classloader;
  }

  public Void execute(CommandContext commandContext) {

    final ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    final DeploymentCache deploymentCache = processEngineConfiguration.getDeploymentCache();

    final List<ProcessDefinition> processDefinitions = new ProcessDefinitionQueryImpl(commandContext).deploymentId(deploymentId).list();

    ClassLoader customClassLoader = processEngineConfiguration.getClassLoader();

    if (customClassLoader == null) {
      customClassLoader = new ProcessArchiveClassLoader();
      processEngineConfiguration.setClassLoader(customClassLoader);
    }

    if (!(customClassLoader instanceof ProcessArchiveClassLoader)) {
      throw new ActivitiException("the custom classloader set on the process engine configuration is not a DeploymentClassLoader");
    }

    ProcessArchiveClassLoader deploymentClassLoader = (ProcessArchiveClassLoader) customClassLoader;

    for (ProcessDefinition processDefinition : processDefinitions) {
      // activate all process definitions with the same key
      List<ProcessDefinition> processDefinitionsToActivate = new ProcessDefinitionQueryImpl(commandContext).processDefinitionKey(processDefinition.getKey()).list();

      for (ProcessDefinition definitionToActivate : processDefinitionsToActivate) {

        log.log(
          Level.INFO, "Deploying process definition  ''{0}[definitionId={1}, version={2}] to the cache.", 
          new Object[]{ processDefinition.getKey(), definitionToActivate.getId(), definitionToActivate.getVersion() });
        
        //  assert that processes are parsed and deployed to the cache:
        deploymentCache.findDeployedProcessDefinitionById(processDefinition.getId());

        if (definitionToActivate.isSuspended()) {
          log.log(
            Level.INFO, "Activating process definition ''{0}[definitionId={1}, version={2}]", 
            new Object[]{ processDefinition.getKey(), definitionToActivate.getId(), definitionToActivate.getVersion() });
          
          new ActivateProcessDefinitionCmd(definitionToActivate.getId(), null).execute(commandContext);
        }
      }

      deploymentClassLoader.registerProcessDefinition(processDefinition.getKey(), classloader);
    }
    
    return null;
  }
}
