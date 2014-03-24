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


import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ExecutionQuery;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;

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

    // job definitions & job suspension
    fixture.startTestCascadingSuspensionOfJobs();

    fixture.startTestJobDefinitionsCreated();

    // variable migration
    fixture.startTestVariableMigration();

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
  protected ManagementService managementService;
  protected TaskService taskService;

  public TestFixture70(ProcessEngine processEngine) {
    this.processEngine = processEngine;
    repositoryService = processEngine.getRepositoryService();
    runtimeService = processEngine.getRuntimeService();
    managementService = processEngine.getManagementService();
    taskService = processEngine.getTaskService();
  }

  public void startTestCascadingSuspensionOfJobs() {
    repositoryService
      .createDeployment()
      .addClasspathResource("org/camunda/bpm/qa/upgrade/TestFixture70.testCascadingSuspensionOfJobs.bpmn20.xml")
      .deploy();

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("TestFixture70.testCascadingSuspensionOfJobs");
    runtimeService.suspendProcessInstanceById(processInstance.getId());
  }

  public void startTestJobDefinitionsCreated() {
    repositoryService
      .createDeployment()
      .addClasspathResource("org/camunda/bpm/qa/upgrade/TestFixture70.testJobDefinitionsCreated.bpmn20.xml")
      .deploy();

    runtimeService.startProcessInstanceByKey("TestFixture70.testJobDefinitionsCreated");
  }

  public void startTestVariableMigration() {
    repositoryService
      .createDeployment()
      .addClasspathResource("org/camunda/bpm/qa/upgrade/TestFixture70.testVariableMigration.bpmn20.xml")
      .deploy();

    Map<String, Object> params = Collections.<String, Object>singletonMap("aStartVariableName", "aStartVariableValue");
    ProcessInstance pi = runtimeService
        .startProcessInstanceByKey("TestFixture70.testVariableMigration", params);

    ExecutionQuery executionQuery = runtimeService.createExecutionQuery().processInstanceId(pi.getId());
    JobQuery jobQuery = managementService.createJobQuery().processInstanceId(pi.getId());
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(pi.getId());

    Execution execution = executionQuery.singleResult();
    Job job = jobQuery.singleResult();

    // add a variable
    runtimeService.setVariableLocal(execution.getId(), "aVariableName", "aVariableValue");

    // execute available job
    managementService.executeJob(job.getId());

    // update execution reference
    execution = executionQuery.singleResult();

    // update variable
    runtimeService.setVariableLocal(execution.getId(), "aVariableName", "newVariableValue");

    // add another new variable
    runtimeService.setVariableLocal(execution.getId(), "anotherVariableName", "anotherVariableValue");

    // update job reference
    job = jobQuery.singleResult();
    // execute available job
    managementService.executeJob(job.getId());

    // get current task
    Task task = taskQuery.singleResult();

    // add a new task variable
    taskService.setVariableLocal(task.getId(), "aLocalTaskVariableName", "aLocalTaskVariableValue");

    // add "aVariableName" variable as local task variable
    taskService.setVariableLocal(task.getId(), "aVariableName", "newVariableValue");
  }

}
