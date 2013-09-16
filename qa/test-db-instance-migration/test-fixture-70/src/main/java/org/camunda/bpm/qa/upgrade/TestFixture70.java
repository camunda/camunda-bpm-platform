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
package org.camunda.bpm.qa.upgrade;


import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.ProcessInstance;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Daniel Meyer
 * 
 */
public class TestFixture70 {
  
  private final static Logger LOGG = Logger.getLogger(TestFixture70.class.getName());

  public static void main(String[] args) {

    ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
      .createProcessEngineConfigurationFromResource("process-engine-config70.xml");
    ProcessEngine processEngine = processEngineConfiguration.buildProcessEngine();
    
    dropCreateDatabase(processEngine);
    
    TestFixture70 fixture = new TestFixture70(processEngine);
    
    // single task
    fixture.startSingleTaskProcess();
    fixture.startNestedSingleTaskProcess();
    
    // concurrency
    fixture.startConcurrentTaskProcess();
    fixture.startNestedConcurrentTaskProcess();
    fixture.startJoinOneExecutionProcess();
    fixture.startNestedJoinOneExecutionProcess();
    fixture.startJoinTwoExecutionsProcess();
    fixture.startNestedJoinTwoExecutionsProcess();
    
    // embedded subprocess
    fixture.startSingleEmbeddedSubprocessTaskProcess();
    fixture.startNestedSingleEmbeddedSubprocessTaskProcess();
    fixture.startConcurrentEmbeddedSubprocessTaskProcess();
    fixture.startNestedConcurrentEmbeddedSubprocessTaskProcess();
    
    // multi instance
    fixture.startMultiInstanceSequentialTask();
    fixture.startNestedMultiInstanceSequentialTask();
    fixture.startMultiInstanceSequentialSubprocess();
    fixture.startNestedMultiInstanceSequentialSubprocess();
    fixture.startMultiInstanceSequentialSubprocessConcurrent();
    fixture.startNestedMultiInstanceSequentialSubprocessConcurrent();
    fixture.startMultiInstanceParallelTask();
    fixture.startNestedMultiInstanceParallelTask();
    fixture.startMultiInstanceParallelSubprocess();
    fixture.startNestedMultiInstanceParallelSubprocess();
    fixture.startMultiInstanceParallelSubprocessConcurrent();
    fixture.startNestedMultiInstanceParallelSubprocessConcurrent();
    
    // suspended process instance
    fixture.startSuspendedAndActiveSingleTaskProcess();
    
    // async continuatuion
    fixture.startAsyncContinutationProcess();
    
    processEngine.close();

  }


  protected static void dropCreateDatabase(ProcessEngine processEngine) {
    // delete all deployments
    RepositoryService repositoryService = processEngine.getRepositoryService();
    List<Deployment> deployments = repositoryService
      .createDeploymentQuery()
      .list();
    for (Deployment deployment : deployments) {
      LOGG.info("deleting deployment "+deployment.getId());
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
    
    try {
      ((ProcessEngineImpl)processEngine).getProcessEngineConfiguration()
        .getCommandExecutorTxRequired()
        .execute(new Command<Void>() {
          public Void execute(CommandContext commandContext) {
            commandContext.getDbSqlSession().dbSchemaDrop();
            return null;
          }          
        });
    } catch(Exception e) {
      LOGG.log(Level.WARNING, "Could not drop schema: " +e.getMessage());
    }
    
    ((ProcessEngineImpl)processEngine).getProcessEngineConfiguration()
      .getCommandExecutorTxRequired()
      .execute(new Command<Void>() {  
        public Void execute(CommandContext commandContext) {
          commandContext.getDbSqlSession().dbSchemaCreate();
          return null;
        }          
      });
  }

  protected ProcessEngine processEngine;
  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;

  public TestFixture70(ProcessEngine processEngine) {
    this.processEngine = processEngine;
    repositoryService = processEngine.getRepositoryService();
    runtimeService = processEngine.getRuntimeService();
  }

  public void startSingleTaskProcess() {
    repositoryService
      .createDeployment() 
      .addClasspathResource("org/camunda/bpm/qa/upgrade/TestFixture70.singleTaskProcess.bpmn20.xml")
      .deploy();
    
    runtimeService.startProcessInstanceByKey("TestFixture70.singleTaskProcess");    
  }
  

  public void startNestedSingleTaskProcess() {
    repositoryService
      .createDeployment() 
      .addClasspathResource("org/camunda/bpm/qa/upgrade/TestFixture70.nestedSingleTaskProcess.bpmn20.xml")
      .deploy();
    
    runtimeService.startProcessInstanceByKey("TestFixture70.nestedSingleTaskProcess");    
  }
  
  public void startConcurrentTaskProcess() {
    repositoryService
      .createDeployment() 
      .addClasspathResource("org/camunda/bpm/qa/upgrade/TestFixture70.concurrentTaskProcess.bpmn20.xml")
      .deploy();
    
    runtimeService.startProcessInstanceByKey("TestFixture70.concurrentTaskProcess");    
  }
  
  public void startNestedConcurrentTaskProcess() {
    repositoryService
      .createDeployment() 
      .addClasspathResource("org/camunda/bpm/qa/upgrade/TestFixture70.nestedConcurrentTaskProcess.bpmn20.xml")
      .deploy();
    
    runtimeService.startProcessInstanceByKey("TestFixture70.nestedConcurrentTaskProcess");    
  }
  
  public void startJoinOneExecutionProcess() {
    repositoryService
      .createDeployment() 
      .addClasspathResource("org/camunda/bpm/qa/upgrade/TestFixture70.joinOneExecutionProcess.bpmn20.xml")
      .deploy();
    
    runtimeService.startProcessInstanceByKey("TestFixture70.joinOneExecutionProcess");    
  }
  
  public void startNestedJoinOneExecutionProcess() {
    repositoryService
      .createDeployment() 
      .addClasspathResource("org/camunda/bpm/qa/upgrade/TestFixture70.nestedJoinOneExecutionProcess.bpmn20.xml")
      .deploy();
    
    runtimeService.startProcessInstanceByKey("TestFixture70.nestedJoinOneExecutionProcess");    
  }
  
  public void startJoinTwoExecutionsProcess() {
    repositoryService
      .createDeployment() 
      .addClasspathResource("org/camunda/bpm/qa/upgrade/TestFixture70.joinTwoExecutionsProcess.bpmn20.xml")
      .deploy();
    
    runtimeService.startProcessInstanceByKey("TestFixture70.joinTwoExecutionsProcess");    
  }
  
  public void startNestedJoinTwoExecutionsProcess() {
    repositoryService
      .createDeployment() 
      .addClasspathResource("org/camunda/bpm/qa/upgrade/TestFixture70.nestedJoinTwoExecutionsProcess.bpmn20.xml")
      .deploy();
    
    runtimeService.startProcessInstanceByKey("TestFixture70.nestedJoinTwoExecutionsProcess");    
  }
  
  public void startSingleEmbeddedSubprocessTaskProcess() {
    repositoryService
      .createDeployment() 
      .addClasspathResource("org/camunda/bpm/qa/upgrade/TestFixture70.singleEmbeddedSubprocess.bpmn20.xml")
      .deploy();
    
    runtimeService.startProcessInstanceByKey("TestFixture70.singleEmbeddedSubprocess");    
  }
  
  public void startNestedSingleEmbeddedSubprocessTaskProcess() {
    repositoryService
      .createDeployment() 
      .addClasspathResource("org/camunda/bpm/qa/upgrade/TestFixture70.nestedSingleEmbeddedSubprocess.bpmn20.xml")
      .deploy();
    
    runtimeService.startProcessInstanceByKey("TestFixture70.nestedSingleEmbeddedSubprocess");    
  }
  
  public void startConcurrentEmbeddedSubprocessTaskProcess() {
    repositoryService
      .createDeployment() 
      .addClasspathResource("org/camunda/bpm/qa/upgrade/TestFixture70.concurrentEmbeddedSubprocess.bpmn20.xml")
      .deploy();
    
    runtimeService.startProcessInstanceByKey("TestFixture70.concurrentEmbeddedSubprocess");    
  }
  
  public void startNestedConcurrentEmbeddedSubprocessTaskProcess() {
    repositoryService
      .createDeployment() 
      .addClasspathResource("org/camunda/bpm/qa/upgrade/TestFixture70.nestedConcurrentEmbeddedSubprocess.bpmn20.xml")
      .deploy();
    
    runtimeService.startProcessInstanceByKey("TestFixture70.nestedConcurrentEmbeddedSubprocess");    
  }
  
  public void startMultiInstanceSequentialTask() {
    repositoryService
      .createDeployment() 
      .addClasspathResource("org/camunda/bpm/qa/upgrade/TestFixture70.multiInstanceSequentialTask.bpmn20.xml")
      .deploy();
    
    runtimeService.startProcessInstanceByKey("TestFixture70.multiInstanceSequentialTask");    
  }
  
  public void startNestedMultiInstanceSequentialTask() {
    repositoryService
      .createDeployment() 
      .addClasspathResource("org/camunda/bpm/qa/upgrade/TestFixture70.nestedMultiInstanceSequentialTask.bpmn20.xml")
      .deploy();
    
    runtimeService.startProcessInstanceByKey("TestFixture70.nestedMultiInstanceSequentialTask");    
  }
  
  public void startMultiInstanceSequentialSubprocess() {
    repositoryService
      .createDeployment() 
      .addClasspathResource("org/camunda/bpm/qa/upgrade/TestFixture70.multiInstanceSequentialSubprocess.bpmn20.xml")
      .deploy();
    
    runtimeService.startProcessInstanceByKey("TestFixture70.multiInstanceSequentialSubprocess");    
  }
  
  public void startNestedMultiInstanceSequentialSubprocess() {
    repositoryService
      .createDeployment() 
      .addClasspathResource("org/camunda/bpm/qa/upgrade/TestFixture70.nestedMultiInstanceSequentialSubprocess.bpmn20.xml")
      .deploy();
    
    runtimeService.startProcessInstanceByKey("TestFixture70.nestedMultiInstanceSequentialSubprocess");    
  }
  
  public void startMultiInstanceSequentialSubprocessConcurrent() {
    repositoryService
      .createDeployment() 
      .addClasspathResource("org/camunda/bpm/qa/upgrade/TestFixture70.multiInstanceSequentialSubprocessConcurrent.bpmn20.xml")
      .deploy();
    
    runtimeService.startProcessInstanceByKey("TestFixture70.multiInstanceSequentialSubprocessConcurrent");    
  }
  
  public void startNestedMultiInstanceSequentialSubprocessConcurrent() {
    repositoryService
      .createDeployment() 
      .addClasspathResource("org/camunda/bpm/qa/upgrade/TestFixture70.nestedMultiInstanceSequentialSubprocessConcurrent.bpmn20.xml")
      .deploy();
    
    runtimeService.startProcessInstanceByKey("TestFixture70.nestedMultiInstanceSequentialSubprocessConcurrent");    
  }
  
  public void startMultiInstanceParallelTask() {
    repositoryService
      .createDeployment() 
      .addClasspathResource("org/camunda/bpm/qa/upgrade/TestFixture70.multiInstanceParallelTask.bpmn20.xml")
      .deploy();
    
    runtimeService.startProcessInstanceByKey("TestFixture70.multiInstanceParallelTask");    
  }
  
  public void startNestedMultiInstanceParallelTask() {
    repositoryService
      .createDeployment() 
      .addClasspathResource("org/camunda/bpm/qa/upgrade/TestFixture70.nestedMultiInstanceParallelTask.bpmn20.xml")
      .deploy();
    
    runtimeService.startProcessInstanceByKey("TestFixture70.nestedMultiInstanceParallelTask");    
  }
  
  public void startMultiInstanceParallelSubprocess() {
    repositoryService
      .createDeployment() 
      .addClasspathResource("org/camunda/bpm/qa/upgrade/TestFixture70.multiInstanceParallelSubprocess.bpmn20.xml")
      .deploy();
    
    runtimeService.startProcessInstanceByKey("TestFixture70.multiInstanceParallelSubprocess");    
  }
  
  public void startNestedMultiInstanceParallelSubprocess() {
    repositoryService
      .createDeployment() 
      .addClasspathResource("org/camunda/bpm/qa/upgrade/TestFixture70.nestedMultiInstanceParallelSubprocess.bpmn20.xml")
      .deploy();
    
    runtimeService.startProcessInstanceByKey("TestFixture70.nestedMultiInstanceParallelSubprocess");    
  }
  
  public void startMultiInstanceParallelSubprocessConcurrent() {
    repositoryService
      .createDeployment() 
      .addClasspathResource("org/camunda/bpm/qa/upgrade/TestFixture70.multiInstanceParallelSubprocessConcurrent.bpmn20.xml")
      .deploy();
    
    runtimeService.startProcessInstanceByKey("TestFixture70.multiInstanceParallelSubprocessConcurrent");    
  }
  
  public void startNestedMultiInstanceParallelSubprocessConcurrent() {
    repositoryService
      .createDeployment() 
      .addClasspathResource("org/camunda/bpm/qa/upgrade/TestFixture70.nestedMultiInstanceParallelSubprocessConcurrent.bpmn20.xml")
      .deploy();
    
    runtimeService.startProcessInstanceByKey("TestFixture70.nestedMultiInstanceParallelSubprocessConcurrent");    
  }
  
  public void startSuspendedAndActiveSingleTaskProcess() {
    repositoryService
    .createDeployment() 
    .addClasspathResource("org/camunda/bpm/qa/upgrade/TestFixture70.suspensionStateSingleTaskProcess.bpmn20.xml")
    .deploy();
  
    runtimeService.startProcessInstanceByKey("TestFixture70.suspensionStateSingleTaskProcess", "activeInstance");  
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("TestFixture70.suspensionStateSingleTaskProcess", "suspendedInstance");
    runtimeService.suspendProcessInstanceById(instance.getId());
  }
  
  public void startAsyncContinutationProcess() {
    repositoryService
      .createDeployment() 
      .addClasspathResource("org/camunda/bpm/qa/upgrade/TestFixture70.asyncContinuationProcess.bpmn20.xml")
      .deploy();
    
    runtimeService.startProcessInstanceByKey("TestFixture70.asyncContinuationProcess");    
  }
      
}
