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
  
  /**
   * The name of the process archive (must be unique for a given process engine)
   */
  public String getName();
  
  /**
   * If this method returns 'null', the default process engine is used. 
   * 
   * @return the name of the process engine this process archive is to be deployed to.
   */
  public String getProcessEngineName();
  
  /**
   * 
   * @return true if the classloader should be scanned for process definitions.
   */
  public boolean scanForProcessDefinitions();
  
  /**
   * This method is only invoked if {@link #scanForProcessDefinitions()} returns false.
   *  
   * @return a collection of named process definitions as byte arrays. 
   */
  public Map<String, byte[]> getProcessResources();
  
  /**
   * If 'true' the process engine deletes both running and historic process instances when performing
   * undeployment. 
   */
  public boolean isDeleteUponUndeploy();
  
  /**
   * <p>Executes a callback inside the context of the process archive</p>
   * 
   * <p>Allows the CMPE (Container-Managed Process Engine) to perform context switching and 
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
   * @return the {@link ClassLoader} used to load classes from the process archive. 
   */
  public ClassLoader getClassLoader();
  
}
