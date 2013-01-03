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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.activiti.engine.impl.interceptor.CommandExecutor;


/**
 * Server-independent Deployment / Redeployment Strategy
 * <p>
 * Each deploymentJob (representing a '.jar', or '.bar'),  is identified by its full 
 * path relative to the deploy folder of the server. Using this path as a name, a deployment 
 * is created in activiti. 
 * <br />
 * Upon reception of a deployment job, we check, for each process, whether processes with the same key(id) 
 * are already deployed. <br />
 *   If true, we check whether they are from the same deployment-unit. <br />
 *     If not, the deployment fails.<br />
 *     If true, we check whether the process has changed. <br />
 *       If the process has changed, we create a new activiti deployment, for all the processes in the job.<br />
 *       Otherwise, we set the classloader passed-in by the deployment job for the existing activiti deployment.<br />
 *   If the deployment job contains at least one process the key of which is not yet present in the activiti-db, a new deployment for all the processes is created.<br />
 * </p> 
 * 
 * @author Daniel Meyer
 */
public abstract class AbstractActivitiDeployer {

  private static Logger log = Logger.getLogger(AbstractActivitiDeployer.class.getName());
  
  // /////////////////////////////////////////// Configuration

  protected final Set<String> activeDeploymentIds = new HashSet<String>();
  
  /////////////////////////////////////////// Deployment methods
  
  /**
   * Ceate a new deployment if at least one of the resources have changed.
   * 
   * @param name the name for the deployment
   * @param resources a list of resources
   * @param classLoader the classloader for this deployment
   * @param activeDeployments 
   * 
   */
  public String deploy(String name, Map<String, byte[]> resources, ClassLoader classLoader ) {
    log.fine("===================================================================");
    try {
      CommandExecutor commandExecutor = getCommandExecutor();
      if (commandExecutor == null) {
        log.warning("Found process(es) but not performing deployment, commandExecutor is null. Is the activiti-service.jar deployed?");
        return null;
      } else {
        String deploymentId = getCommandExecutor().execute(new DeployIfChangedCmd(name, resources, classLoader, new HashSet<String>(activeDeploymentIds)));
        
        getCommandExecutor().execute(new ActivateDeploymentCmd(deploymentId, classLoader));
        
        activeDeploymentIds.add(deploymentId);
        return deploymentId;
      }
     
    } finally {
      log.fine("===================================================================");
    }
  }
  
  /**
   * Undeploy a deployment. 
   * 
   * @param name the name of the deployment
   * 
   */
  public String unDeploy(String name, boolean delete) {
    log.fine("===================================================================");
    try {
      CommandExecutor commandExecutor = getCommandExecutor();
      if (commandExecutor == null) {
        log.warning("Not performing undeployment, command executor is null.");
        return null;
      } else {
        String deploymentId = commandExecutor.execute(new UndeployCmd(name, delete));
        activeDeploymentIds.remove(deploymentId);
        return deploymentId;        
      }
    } finally {
      log.fine("===================================================================");
    }
  }
  

  /////////////////////////////////////////// Getters / Setters

  protected abstract CommandExecutor getCommandExecutor();

}
