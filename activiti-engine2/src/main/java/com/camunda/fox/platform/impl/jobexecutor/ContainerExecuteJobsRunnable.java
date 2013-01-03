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
package com.camunda.fox.platform.impl.jobexecutor;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.impl.jobexecutor.ExecuteJobsRunnable;
import org.activiti.engine.impl.jobexecutor.JobExecutor;

import com.camunda.fox.platform.impl.context.ProcessArchiveContext;
import com.camunda.fox.platform.impl.context.spi.ProcessArchiveServices;
import com.camunda.fox.platform.spi.ProcessArchiveCallback;

/**
 * <p>This implementation makes sure, that (1) jobs are executed "within" the process archive, 
 * (2) we do not execute jobs from archives that are not currently deployed</p>
 *  
 * @author Daniel Meyer
 */
public class ContainerExecuteJobsRunnable extends ExecuteJobsRunnable {
  
  private static Logger log = Logger.getLogger(ContainerExecuteJobsRunnable.class.getName());
  
  private final List<String> jobIds;
  private final ProcessArchiveServices processArchiveServices;
  private final JobExecutor jobExecutor;

  public ContainerExecuteJobsRunnable(JobExecutor jobExecutor, List<String> jobIds,ProcessArchiveServices processArchiveServices) {
    super(jobExecutor, jobIds);    
    this.jobIds = jobIds;
    this.processArchiveServices = processArchiveServices;
    this.jobExecutor = jobExecutor;
  }
  
  public void run() {    
    // all jobs in the list are from the same 
    // process instance -> same deployment
    ProcessArchiveContext processArchiveContext = null;    
    try {
      String processDefinitionKey = jobExecutor.getCommandExecutor()
              .execute(new GetProcessDefintionKeyForJobId(jobIds.get(0)));
      processArchiveContext = processArchiveServices.getProcessArchiveContext(processDefinitionKey);
    }catch (Exception e) {
      log.log(Level.WARNING, "Could not determine process archive for job '"+jobIds+"'", e);
      return;
    }
    
    if (processArchiveContext != null) {
      ProcessArchiveContext.executeWithinContext(getCallback(), processArchiveContext);
    } else {
      getCallback().execute();
    }
  }

  protected ProcessArchiveCallback<?> getCallback() {
    return new ProcessArchiveCallback<Void>() {
      public Void execute() {
        ContainerExecuteJobsRunnable.super.run();
        return null;
      }
    };
  }

}
