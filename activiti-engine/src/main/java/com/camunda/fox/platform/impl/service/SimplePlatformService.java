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
package com.camunda.fox.platform.impl.service;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.camunda.fox.platform.FoxPlatformException;
import com.camunda.fox.platform.spi.ProcessEngineConfiguration;

/**
 * <p>Simple implementation of the fox platform services using {@link FutureTask}.</p>
 * 
 * <p><strong>NOTE</strong>: Do not use this implementation in an environment where 
 * self-management of threads is undesirable. This class is present primarily for 
 * illustrative and testing purposes. In most container environment we want to delegate 
 * the asynchronous processing to container infrastructure.</p> 
 *  
 * @author Daniel Meyer
 */
public class SimplePlatformService extends PlatformService {
  
  final private static Logger log = Logger.getLogger(SimplePlatformService.class.getName());

  public Future<ProcessEngineStartOperation> startProcessEngine(final ProcessEngineConfiguration processEngineConfiguration) {
    
    processEngineRegistry.startInstallingNewProcessEngine(processEngineConfiguration);
        
    FutureTask<ProcessEngineStartOperation> task = new FutureTask<ProcessEngineStartOperation>(new Callable<ProcessEngineStartOperation>() {
      public ProcessEngineStartOperation call() throws Exception {
        try {
          ProcessEngineController processEngineController = new ProcessEngineController(processEngineConfiguration);
          processEngineController.setProcessEngineRegistry(processEngineRegistry);
          processEngineController.start();      
          return new ProcessEngineStartOperationImpl(processEngineController.getProcessEngine());
        } catch (Exception e) {
          log.log(Level.SEVERE,"Caught exception while trying to start process engine", e);
          return new ProcessEngineStartOperationImpl(e);
        }
      }
    });
    task.run();
    return task;
  }

  @Override
  public void stopProcessEngine(String name) {
    ProcessEngineController processEngineController = processEngineRegistry.getProcessEngineController(name);
    if(processEngineController == null) {
      throw new FoxPlatformException("Cannot stop process engine with name '"+name+"': no such process engine found.");
    }
    processEngineController.stop();
  }

}
