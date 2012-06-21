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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.ProcessEventJobHandler;
import org.activiti.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.activiti.engine.impl.persistence.entity.JobEntity;

/**
 * 
 * @author Daniel Meyer
 */
public class GetProcessDefintionKeyForJobId implements Command<String> {
  
  public static String IS_TIMER_START;
    
  protected final String jobId;

  public GetProcessDefintionKeyForJobId(String jobId) {
    this.jobId = jobId;
  }

  @Override
  public String execute(CommandContext commandContext) {
    JobEntity jobEntity = commandContext.getJobManager()
      .findJobById(jobId);
    
    if(jobEntity == null) {
      return null;
    }
    
    String executionId = jobEntity.getExecutionId();
    
    if(executionId == null) {
      if (jobEntity.getJobHandlerType().equals(TimerStartEventJobHandler.TYPE)) {
        return jobEntity.getJobHandlerConfiguration();
      }
      else if (jobEntity.getJobHandlerType().equals(ProcessEventJobHandler.TYPE)) {
        return jobEntity.getJobHandlerConfiguration();
      }
      throw new ActivitiException("Job [id="+jobEntity+"] of type '"+jobEntity.getJobHandlerType()+"' does not reference any execution, which is an invalid state");
    }
    
    String processDefinitionId = commandContext.getExecutionManager()
      .findExecutionById(executionId)
      .getProcessDefinitionId();
    
    return commandContext.getProcessDefinitionManager()
      .findLatestProcessDefinitionById(processDefinitionId)
      .getKey();
      
  }

}
