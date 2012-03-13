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
package com.camunda.fox.platform.api;

import java.util.List;
import java.util.concurrent.Future;

import org.activiti.engine.ProcessEngine;

import com.camunda.fox.platform.FoxPlatformException;
import com.camunda.fox.platform.spi.ProcessEngineConfiguration;

/**
 * <p>Returns the process engine service</p>
 * 
 * <p>Users of this class may look up an instance of the service through a lookup strategy
 * appropriate for the platform they are using (Examples: Jndi, OSGi Service Registry ...)</p>
 * 
 * <p>The process engine service can be used to manage {@link ProcessEngine ProcessEngines}.
 * Users may retrieve existing Process Engines using this class as well as start and stop a
 * Process Engine.</p>
 * 
 * @author Daniel Meyer
 */
public interface ProcessEngineService {
  
  /**
   * @throws FoxPlatformException if no such process engine exists.
   * 
   * @return the default process engine.
   */
  public ProcessEngine getDefaultProcessEngine();

  /**
   * @return all {@link ProcessEngine ProcessEngines} managed by the fox platform.
   */
  public List<ProcessEngine> getProcessEngines();

  /**
   * 
   * @return the names of all {@link ProcessEngine ProcessEngines} managed by the fox platform.
   */
  public List<String> getProcessEngineNames();
  
  /**
   * @throws FoxPlatformException if no such process engine exists.
   * 
   * @return the {@link ProcessEngine} for the given name
   */
  public ProcessEngine getProcessEngine(String name);

  /**
   * Starts a {@link ProcessEngine} for the given {@link ProcessEngineConfiguration}.
   * <p />
   * Note that the process engine is started asynchronously. 
   *  
   * @param processEngineConfiguration
   *          the configuration of the ProcessEngine to be started.
   * 
   * @return a {@link Future} to the {@link ProcessEngine} to be 
   */
  public Future<ProcessEngineStartOperation> startProcessEngine(ProcessEngineConfiguration processEngineConfiguration);
  
  /**
   * Stops the {@link ProcessEngine} passed in as a parameter.
   * 
   * @throws FoxPlatformException if the passed in process engine is not managed by the fox platform  
   * 
   * @param processEngine
   *          the process engine to be stopped.
   */
  public Future<ProcessEngineStopOperation> stopProcessEngine(ProcessEngine processEngine);
  
  /**
   * Stops which is named after the parameter passed in to this method.
   * 
   * @throws FoxPlatformException if no such {@link ProcessEngine} exists. 
   * 
   * @param processEngine
   *          the process engine to be stopped.
   */
  public Future<ProcessEngineStopOperation> stopProcessEngine(String name);
  
  
  // operations ////////////////////////////////////
  
  
  public static interface ProcessEngineStartOperation {
    
    /**
     * @return true if the process engine could be started
     */
    public boolean wasSuccessful();
    
    /**
     * @return the started {@link ProcessEngine}
     */
    public ProcessEngine getProcessenEngine();
    
    /**
     * @return the exception that was thrown while attempting to start the {@link ProcessEngine}
     */
    public Throwable getException();
  }
  
  public static interface ProcessEngineStopOperation {
    
    /**
     * @return true if the process engine could successfully be stopped
     */
    public boolean wasSuccessful();
    
    /**
     * @return the exception thrown while attempting to stop the process engine.
     */
    public Throwable getException();
  }

}
