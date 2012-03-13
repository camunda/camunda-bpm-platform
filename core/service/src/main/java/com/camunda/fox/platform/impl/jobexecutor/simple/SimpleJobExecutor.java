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
package com.camunda.fox.platform.impl.jobexecutor.simple;

import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.activiti.engine.impl.jobexecutor.DefaultJobExecutor;

import com.camunda.fox.platform.impl.context.ProcessArchiveServicesSupport;
import com.camunda.fox.platform.impl.context.spi.ProcessArchiveServices;
import com.camunda.fox.platform.impl.jobexecutor.ContainerExecuteJobsRunnable;

/**
 * 
 * @author Daniel Meyer
 */
public class SimpleJobExecutor extends DefaultJobExecutor implements ProcessArchiveServicesSupport {
  
  protected ProcessArchiveServices processArchiveServices;
      
  @Override
  public void executeJobs(List<String> jobIds) {
    try {
      threadPoolExecutor.execute(new ContainerExecuteJobsRunnable(this, jobIds, processArchiveServices));
    }catch (RejectedExecutionException e) {
      rejectedJobsHandler.jobsRejected(this, jobIds);
    }
  }
  
  @Override
  public void setProcessArchiveServices(ProcessArchiveServices processArchiveServices) {
    this.processArchiveServices = processArchiveServices;
  }

}
