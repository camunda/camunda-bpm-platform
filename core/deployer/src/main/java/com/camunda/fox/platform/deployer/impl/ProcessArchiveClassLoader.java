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

import java.net.URL;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.context.ExecutionContext;
import org.activiti.engine.impl.persistence.deploy.DeploymentCache;

/**
 * Classloader holding a map of ProcessDefinition Keys and Classloaders.
 * 
 * There should be only one instance of this classloader on which the deployments
 * are registered.
 * 
 * @author Daniel Meyer
 */
public class ProcessArchiveClassLoader extends ClassLoader {

  protected Map<String, ClassLoader> deploymentClassloaderMap = new HashMap<String, ClassLoader>();
    
  public ProcessArchiveClassLoader(Map<String, ClassLoader> map) {
    deploymentClassloaderMap.putAll(map); 
  }
    
  public ProcessArchiveClassLoader() {    
    
  }

  public void registerProcessDefinition(String processDefinitionKey, ClassLoader cl) {
    if (processDefinitionKey == null || cl == null) {
      throw new ActivitiException("processDefinitionKey and classloader must not be null.");
    }
    deploymentClassloaderMap.put(processDefinitionKey, cl);
  }

  public void unregisterProcessDefinition(String processDefinitionKey) {
    deploymentClassloaderMap.remove(processDefinitionKey);
  }

  @Override
  public Class< ? > loadClass(String name) throws ClassNotFoundException {    
    ClassLoader classloader = resolveProcessArchiveClassLoader();
    if(classloader == null) {
      throw new ClassNotFoundException("Clould not determine classoader from activiti context.");
    }

    // delegate to the deploymentUnitClassLoader
    return classloader.loadClass(name);
  }

  @Override
  protected synchronized Class< ? > loadClass(String name, boolean resolve) throws ClassNotFoundException {
    Class<?> clazz;
    try {
      clazz = findClass(name);
    } catch (ClassNotFoundException e) {
      try {
        clazz = loadClass(name);
      } catch (ClassNotFoundException ex) {
        throw ex;
      }
    }
    if (resolve) {
      resolveClass(clazz);
    }
    return clazz;
  }

  @Override
  protected URL findResource(String name) {    
    ClassLoader classLoader = resolveProcessArchiveClassLoader();
    if (classLoader == null) {
      return null;
    }
    // delegate to the deploymentUnitClassLoader
    return classLoader.getResource(name);
  }
  
  protected ClassLoader resolveProcessArchiveClassLoader() {
    
    final DeploymentCache deploymentCache = Context.getProcessEngineConfiguration().getDeploymentCache();
    final ExecutionContext executionContext = getExecutionContext();
    
    if (executionContext != null) {

      String processDefinitionId = executionContext.getProcessInstance().getProcessDefinitionId();
  
      // check whether the process definition is deployed to the cache.
      // if the process definition is not deployed to the cache, we do not attempt
      // to resolve the deploymentClassloader since the process archive is undeployed.
      
      if (deploymentCache.getProcessDefinitionCache().containsKey(processDefinitionId)) {
        
        String processDefinitionKey = executionContext.getProcessDefinition().getKey();      
  
        // look up the classloader for that processDefinition:
        return deploymentClassloaderMap.get(processDefinitionKey);
       
      } 
    }
    
    return null;   
  }

  protected ExecutionContext getExecutionContext() {
    try {
      return Context.getExecutionContext();
    } catch (EmptyStackException e) {
      // hack for bug in activiti
      return null;
    }
  }
  
  
  public Map<String, ClassLoader> getDeploymentClassloaderMap() {
    return deploymentClassloaderMap;
  }

}
