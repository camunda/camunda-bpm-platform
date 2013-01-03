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

import java.util.Map;
import java.util.logging.Logger;

import org.activiti.engine.impl.interceptor.CommandExecutor;

import com.camunda.fox.platform.impl.deployment.spi.ProcessArchiveScanner;
import com.camunda.fox.platform.impl.util.PropertyHelper;
import com.camunda.fox.platform.impl.util.Services;
import com.camunda.fox.platform.spi.ProcessArchive;

/**
 * 
 * @author Daniel Meyer
 */
public class ActivitiDeployer extends AbstractActivitiDeployer {
  
  private static Logger log = Logger.getLogger(ActivitiDeployer.class.getName());
  
  protected CommandExecutor commandExecutor;
  protected ProcessArchiveScanner processArchiveScanner;
  
  public ActivitiDeployer(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
    this.processArchiveScanner = Services.getService(ProcessArchiveScanner.class);
  }
  
  public String processArchiveDeployed(ProcessArchive processArchive) {
    final ClassLoader classLoader = processArchive.getClassLoader();
    final String paName = processArchive.getName();
    
    final Map<String, byte[]> resources = getDeployableResources(processArchive);
   
    if(resources.size()>0) {
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("Process archive '"+paName+"' contains deployables: \n\n");
      for (String resourceName : resources.keySet()) {
        stringBuilder.append("         "+resourceName+"\n");
      }
      log.info(stringBuilder.toString());
      // perform the deployment    
      return deploy(paName, resources, classLoader);
    }else {
      log.info("No deployables for process archive '"+paName+"'");
      return null;
    }  
  }
  
  protected Map<String, byte[]> getDeployableResources(ProcessArchive processArchive) {
    if(PropertyHelper.getProperty(processArchive.getProperties(), ProcessArchive.PROP_IS_SCAN_FOR_PROCESS_DEFINITIONS, false)) {
      return processArchiveScanner.findResources(processArchive);      
    } else {
      return processArchive.getProcessResources();
    }    
  }

  public void processArchiveUndeployed(ProcessArchive processArchive) {
    final boolean deleteUponUndeploy = PropertyHelper.getProperty(processArchive.getProperties(), ProcessArchive.PROP_IS_DELETE_UPON_UNDEPLOY, false);
    final String paName = processArchive.getName();
    unDeploy(paName, deleteUponUndeploy);    
  }
    
  protected CommandExecutor getCommandExecutor() {
    return commandExecutor;
  }
    
  public void setCommandExecutor(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }

}
