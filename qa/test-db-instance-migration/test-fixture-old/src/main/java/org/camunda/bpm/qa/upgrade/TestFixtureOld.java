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


import java.util.List;
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
import org.camunda.bpm.qa.upgrade.scenarios.sentry.SentryScenario;

/**
 * @author Daniel Meyer
 *
 * Drops and creates the old database.
 *
 */
public class TestFixtureOld {

  private final static Logger LOGGER = Logger.getLogger(TestFixtureOld.class.getName());

  protected ProcessEngine processEngine;
  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;
  protected ManagementService managementService;
  protected TaskService taskService;

  public TestFixtureOld(ProcessEngine processEngine) {
    this.processEngine = processEngine;
    repositoryService = processEngine.getRepositoryService();
    runtimeService = processEngine.getRuntimeService();
    managementService = processEngine.getManagementService();
    taskService = processEngine.getTaskService();
  }

  public static void main(String[] args) {
    ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
      .createProcessEngineConfigurationFromResource("process-engine-config-old.xml");
    ProcessEngine processEngine = processEngineConfiguration.buildProcessEngine();

    dropCreateDatabase(processEngine);

    // register test scenarios
    ScenarioRunner runner = new ScenarioRunner(processEngine);
    
    // event subprocesses   
    runner.setupScenarios(SentryScenario.class);

    processEngine.close();
  }

  protected static void dropCreateDatabase(ProcessEngine processEngine) {
    // delete all deployments
    RepositoryService repositoryService = processEngine.getRepositoryService();
    List<Deployment> deployments = repositoryService
      .createDeploymentQuery()
      .list();
    for (Deployment deployment : deployments) {
      LOGGER.info("deleting deployment " + deployment.getId());
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
      LOGGER.log(Level.WARNING, "Could not drop schema: " + e.getMessage());
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

}
