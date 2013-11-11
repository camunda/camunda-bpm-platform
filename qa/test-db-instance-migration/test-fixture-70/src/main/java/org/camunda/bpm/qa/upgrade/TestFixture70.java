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

    // job definitions & job suspension
    fixture.startTestCascadingSuspensionOfJobs();

    fixture.startTestJobDefinitionsCreated();

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

}
