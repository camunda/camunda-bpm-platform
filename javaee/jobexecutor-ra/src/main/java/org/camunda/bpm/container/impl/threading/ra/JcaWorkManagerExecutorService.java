/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.container.impl.threading.ra;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.Referenceable;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkManager;
import javax.resource.spi.work.WorkRejectedException;

import org.camunda.bpm.container.ExecutorService;
import org.camunda.bpm.container.impl.threading.ra.inflow.JcaInflowExecuteJobsRunnable;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;



/**
 * {@link AbstractPlatformJobExecutor} implementation delegating to a JCA {@link WorkManager}.
 * 
 * @author Daniel Meyer
 * 
 */
public class JcaWorkManagerExecutorService implements Referenceable, ExecutorService {
  
  public static int START_WORK_TIMEOUT = 1500;

  private static Logger logger = Logger.getLogger(JcaWorkManagerExecutorService.class.getName());
  
  protected final JcaExecutorServiceConnector ra;
  protected WorkManager workManager;
  
  public JcaWorkManagerExecutorService(JcaExecutorServiceConnector connector, WorkManager workManager) {
    this.workManager = workManager;
    this.ra = connector;
  }
  
  public boolean schedule(Runnable runnable, boolean isLongRunning) {
    if(isLongRunning) {
      return scheduleLongRunning(runnable);
      
    } else {
      return executeShortRunning(runnable);
      
    }
  }

  protected boolean scheduleLongRunning(Runnable runnable) {
    try {
      workManager.scheduleWork(new JcaWorkRunnableAdapter(runnable));
      return true;
      
    } catch (WorkException e) {
      logger.log(Level.WARNING, "Could not schedule : "+e.getMessage(), e);
      return false;
      
    }
  }
  
  protected boolean executeShortRunning(Runnable runnable) {
   
    try {      
      workManager.startWork(new JcaWorkRunnableAdapter(runnable), START_WORK_TIMEOUT, null, null);
      return true;
      
    } catch (WorkRejectedException e) {
      logger.log(Level.FINE, "WorkRejectedException while scheduling jobs for execution", e);      
      
    } catch (WorkException e) {
      logger.log(Level.WARNING, "WorkException while scheduling jobs for execution", e);
    }
    
    return false;
  }
  
  public Runnable getExecuteJobsRunnable(List<String> jobIds, ProcessEngineImpl processEngine) {
    return new JcaInflowExecuteJobsRunnable(jobIds, processEngine, ra);
  }
    
  // javax.resource.Referenceable /////////////////////////

  protected Reference reference;
  
  public Reference getReference() throws NamingException {    
    return reference;
  }

  public void setReference(Reference reference) {
    this.reference = reference;        
  }
  
  // getters / setters ////////////////////////////////////
  
  public WorkManager getWorkManager() {
    return workManager;
  }

  public JcaExecutorServiceConnector getPlatformJobExecutorConnector() {
    return ra;
  }

}
