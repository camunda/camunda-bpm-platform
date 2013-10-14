/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.jobexecutor;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cmd.ExecuteJobsCmd;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;


/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class ExecuteJobsRunnable implements Runnable {
  
  private final static Logger LOGG = Logger.getLogger(ExecuteJobsRunnable.class.getName());

  protected final List<String> jobIds;
  protected JobExecutor jobExecutor;
  protected ProcessEngineImpl processEngine;
  
  public ExecuteJobsRunnable(JobExecutor jobExecutor, List<String> jobIds) {
    this.jobExecutor = jobExecutor;
    this.jobIds = jobIds;
  }
  
  public ExecuteJobsRunnable(List<String> jobIds, ProcessEngineImpl processEngine) {
    this.jobIds = jobIds;
    this.processEngine = processEngine;
  }

  public void run() {
    final JobExecutorContext jobExecutorContext = new JobExecutorContext();
    final List<String> currentProcessorJobQueue = jobExecutorContext.getCurrentProcessorJobQueue();
    CommandExecutor commandExecutor = null;
    
    if(processEngine == null) {
      // temporary hack to maintain API compatibility 
      commandExecutor = jobExecutor.getCommandExecutor();
    } else {
      commandExecutor = processEngine.getProcessEngineConfiguration().getCommandExecutorTxRequired();
    }

    currentProcessorJobQueue.addAll(jobIds);
    
    Context.setJobExecutorContext(jobExecutorContext);
    try {
      while (!currentProcessorJobQueue.isEmpty()) {
        
        String nextJobId = currentProcessorJobQueue.remove(0);
        try {
          executeJob(nextJobId, commandExecutor);        
        } catch(Throwable t) {
          LOGG.log(Level.WARNING, "Exception while executing job with id "+nextJobId, t);
        }
        
      }      
    }finally {
      Context.removeJobExecutorContext();
    }
  }
  
  protected void executeJob(String nextJobId, CommandExecutor commandExecutor) {    
    commandExecutor.execute(new ExecuteJobsCmd(nextJobId));
  }
  
}
