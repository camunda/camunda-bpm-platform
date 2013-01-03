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
package com.camunda.fox.platform.impl.deployment;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.DeploymentQueryImpl;
import org.activiti.engine.impl.ProcessDefinitionQueryImpl;
import org.activiti.engine.impl.ProcessInstanceQueryImpl;
import org.activiti.engine.impl.cmd.SuspendProcessDefinitionCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;

/**
 * Command for undeploying a deployment and updating the {@link ProcessArchiveClassLoader}
 * 
 * @author Daniel Meyer
 */
public class UndeployCmd implements Command<String>, Serializable {
  
  private static Logger log = Logger.getLogger(UndeployCmd.class.getName());
  
  private static final long serialVersionUID = 1L;
  
  protected String name;

  private final boolean delete;
  
  protected transient CommandContext commandContext;

  
  public UndeployCmd(String name, boolean delete) {
    this.name = name;
    this.delete = delete; 
  }
  
  public String execute(CommandContext commandContext) {  
    this.commandContext = commandContext;
    
    // lookup the deployment for this job:
    List<Deployment> deployments = new DeploymentQueryImpl(commandContext)
      .deploymentName(name)
      .orderByDeploymenTime()
      .desc()
      .listPage(0, 1);
      

    if (deployments == null || deployments.isEmpty()) {
      log.info("Could not locate deployment for process-archive '" + name + "'.");
      return null;
    }

    Deployment deployment = deployments.get(0);
    List<ProcessDefinition> processDefinitionsForThisDeployment = new ProcessDefinitionQueryImpl(commandContext)      
      .deploymentId(deployment.getId())
      .list();
    
    log.info("Process engine undeployment: delete=" + delete);
    logRuningProcessDefinitionsSummary(processDefinitionsForThisDeployment);
    
    if (delete) {
      commandContext
        .getDeploymentManager()
        .deleteDeployment(deployment.getId(), true);
    } 
    
    // even if we delete this deployment, process definitions with the same key form older deployments need to be suspended
    suspendProcessDefinitions(processDefinitionsForThisDeployment);
      
    return deployment.getId();
  }

  
  protected void logRuningProcessDefinitionsSummary(List<ProcessDefinition> processDefinitionsForThisDeployment) {
    for (ProcessDefinition processDefinition : processDefinitionsForThisDeployment) {
      // load all process definitions with the same key:
      List<ProcessDefinition> definitionsToSuspend = new ProcessDefinitionQueryImpl(commandContext)
        .processDefinitionKey(processDefinition.getKey())      
        .list();
      
      for (ProcessDefinition definitionToSuspend : definitionsToSuspend) {
        
        // list number of running instances for that process definition:
        Long number =  new ProcessInstanceQueryImpl(commandContext)
          .processDefinitionId(definitionToSuspend.getId())
          .count();
      
        log.log(Level.INFO, 
            "Running processes instances for {0}[definitionId={1}, version={2}] : {3}{4}", 
            new Object[]{processDefinition.getKey(), definitionToSuspend.getId(), definitionToSuspend.getVersion(), number, delete && processDefinitionsForThisDeployment.contains(definitionToSuspend) ? " (will be deleted)" : " (will be suspended)"});     
      }        
    }    
  }

  /**
   * For each process definition part of this deployment: 
   *  - get all process definitions with the same key
   *  - remove that key from the classloader
   *  - then, for each process definition with that key:
   *    - list the number of running instances for that definition
   *    - suspend the process definition (prevents job execution) 
   *    - remove the process definition from the deployment cache 
   * 
   * @param processDefinitionsForThisDeployment
   */
  protected void suspendProcessDefinitions(List<ProcessDefinition> processDefinitionsForThisDeployment) {
    
    ClassLoader classLoader = Context.getProcessEngineConfiguration().getClassLoader();
    
    if ((classLoader == null) || ! (classLoader instanceof ProcessArchiveClassLoader) ) {
      throw new ActivitiException("Custom classloader cannot be null and must be a DeploymentClassLoader");
    }
    
    ProcessArchiveClassLoader deploymentClassLoader = (ProcessArchiveClassLoader) classLoader;  
    deploymentClassLoader = new ProcessArchiveClassLoader(deploymentClassLoader.getDeploymentClassloaderMap());
    
    for (ProcessDefinition processDefinition : processDefinitionsForThisDeployment) {      
      // load all process definitions with the same key:
      List<ProcessDefinition> definitionsToSuspend = new ProcessDefinitionQueryImpl(commandContext)
        .processDefinitionKey(processDefinition.getKey())      
        .list();
      
      for (ProcessDefinition definitionToSuspend : definitionsToSuspend) {
       
        // remove from deployment cache:
        Context.getProcessEngineConfiguration()
          .getDeploymentCache()
          .removeProcessDefinition(definitionToSuspend.getId());
        
        if (!definitionToSuspend.isSuspended() && ((!delete) || !processDefinitionsForThisDeployment.contains(definitionToSuspend))) {
          // suspend:
          log.info("Suspending processDefinition " + processDefinition.getKey() + "[definitionId=" + definitionToSuspend.getId() + ", version="+definitionToSuspend.getVersion()+"]");
          
          new SuspendProcessDefinitionCmd(definitionToSuspend.getId(), null).execute(commandContext);           
        }        
      }
      
      deploymentClassLoader.unregisterProcessDefinition(processDefinition.getKey());
    }    
    
    Context.getProcessEngineConfiguration().setClassLoader(deploymentClassLoader);
  }
}
