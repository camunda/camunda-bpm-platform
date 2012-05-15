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
package com.camunda.fox.platform.spi;

import java.util.Map;

import javax.naming.InitialContext;

/**
 * <p>A process archive represents a deployment to the fox platform</p>
 * 
 * @author Daniel Meyer
 */
public interface ProcessArchive {
  
  /** Indicates whether the undeployment of the process archive should trigger deleting the process engine deployment.
   * If the process engine deployment is deleted, all running and historic process instances are removed as well. */
  public static final String PROP_IS_DELETE_UPON_UNDEPLOY = "isDeleteUponUndeploy";  
  /** Indicates whether the classloader should be scanned for process definitions. */
  public static final String PROP_IS_SCAN_FOR_PROCESS_DEFINITIONS = "isScanForProcessDefinitions";
  
  /**
   * @return the name of the process archive (must be unique for a given process engine)
   */
  public String getName();
  
  /**
   * If this method returns 'null', the default process engine is used. 
   * 
   * @return the name of the process engine this process archive is to be deployed to.
   */
  public String getProcessEngineName();
  
  /**
   * @return the {@link ClassLoader} used to load classes from the process archive. 
   */
  public ClassLoader getClassLoader();
  
  /**
   * Allows the process archive to supply a map of type Map<String, byte[]> of
   * process resources to be deployed.
   */
  public Map<String, byte[]> getProcessResources();
  
  /**
   * <p>Executes a callback inside the context of the process archive</p>
   * 
   * <p>Allows the Process Engine to perform context switching and 
   * execute commands within the context of the process archive.</p>
   * 
   * <p>An implementation must make sure that the {@link ProcessArchiveCallback} passed in to 
   * this method is executed with the same <em>Context</em> as code natively executed by the 
   * process archive. Depending on the platform, an implementation might change the context 
   * {@link ClassLoader} of the current thread, adjust the security context, and provide other
   * environment resources like an {@link InitialContext} which is identical to the context
   * of the process archive.</p> 
   * 
   * @param callback the {@link ProcessArchiveCallback} to execute
   * @return the result of the {@link ProcessArchivallback}
   * @throws Exception 
   */
  public <T> T executeWithinContext(ProcessArchiveCallback<T> callback) throws Exception;
  
  /**
   * @return a map of properties
   * 
   * @see #PROP_IS_DELETE_UPON_UNDEPLOY
   * @see #PROP_PROCESS_RESOURCES
   */
  public Map<String, Object> getProperties();
  
}
