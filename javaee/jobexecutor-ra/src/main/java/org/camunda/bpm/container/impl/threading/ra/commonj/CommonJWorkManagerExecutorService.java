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
package org.camunda.bpm.container.impl.threading.ra.commonj;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;

import org.camunda.bpm.container.ExecutorService;
import org.camunda.bpm.container.impl.threading.ra.JcaExecutorServiceConnector;
import org.camunda.bpm.container.impl.threading.ra.inflow.JcaInflowExecuteJobsRunnable;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;

import commonj.work.WorkException;
import commonj.work.WorkManager;
import commonj.work.WorkRejectedException;

/**
 * {@link AbstractPlatformJobExecutor} implementation delegating to a CommonJ {@link WorkManager}.
 * 
 * @author Christian Lipphardt
 * 
 */
public class CommonJWorkManagerExecutorService implements ExecutorService {

  private static Logger logger = Logger.getLogger(CommonJWorkManagerExecutorService.class.getName());
  
  protected WorkManager workManager;

  protected JcaExecutorServiceConnector ra;

  protected String commonJWorkManagerName;
  
  protected WorkManager lookupWorkMananger() {
    try {
      InitialContext initialContext = new InitialContext();
      return (WorkManager) initialContext.lookup(commonJWorkManagerName);
    } catch (Exception e) {
      throw new RuntimeException("Error while starting JobExecutor: could not look up CommonJ WorkManager in Jndi: "+e.getMessage(), e);
    }   
  }
    
  public CommonJWorkManagerExecutorService(JcaExecutorServiceConnector ra, String commonJWorkManagerName) {
    this.ra = ra;
    this.commonJWorkManagerName = commonJWorkManagerName;
  }
  
  public boolean schedule(Runnable runnable, boolean isLongRunning) {
    if(isLongRunning) {
      return scheduleLongRunning(runnable);
      
    } else {
      return executeShortRunning(runnable);
      
    }
  }

  protected boolean executeShortRunning(Runnable runnable) {
    try {
      workManager.schedule(new CommonjWorkRunnableAdapter(runnable));
      return true;
      
    } catch (WorkRejectedException e) {
      logger.log(Level.FINE, "Work rejected", e);      
      
    } catch (WorkException e) {
      logger.log(Level.WARNING, "WorkException while scheduling jobs for execution", e);
      
    }
    return false;
  }

  protected boolean scheduleLongRunning(Runnable acquisitionRunnable) {
    // initialize the workManager here, because we have access to the initial context
    // of the calling thread (application), so the jndi lookup is working -> see JCA 1.6 specification
    if(workManager == null) {
      workManager = lookupWorkMananger();
    }
      
    try {
      workManager.schedule(new CommonjDeamonWorkRunnableAdapter(acquisitionRunnable));
      return true;
      
    } catch (WorkException e) {
      logger.log(Level.WARNING, "Could not schedule Job Acquisition Runnable: "+e.getMessage(), e);
      return false;
      
    }
  }
  
  public Runnable getExecuteJobsRunnable(List<String> jobIds, ProcessEngineImpl processEngine) {
    return new JcaInflowExecuteJobsRunnable(jobIds, processEngine, ra);
  }
  
  // getters / setters ////////////////////////////////////

  public WorkManager getWorkManager() {
    return workManager;
  }
 
}
