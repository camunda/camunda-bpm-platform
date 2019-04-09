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
package org.camunda.bpm.qa.upgrade;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.repository.Deployment;

import java.util.List;

/**
 * @author Thorben Lindhauer
 */
public class UpgradedDBDropper {

  public static void main(String[] args) {
    ProcessEngine engine = ProcessEngineConfiguration
        .createProcessEngineConfigurationFromResource("camunda.cfg.xml").buildProcessEngine();

    UpgradedDBDropper fixture = new UpgradedDBDropper();
    fixture.cleanDatabase(engine);
  }

  public UpgradedDBDropper() {
  }

  public void cleanDatabase(ProcessEngine engine) {

    // delete all deployments
    RepositoryService repositoryService = engine.getRepositoryService();
    List<Deployment> deployments = repositoryService
      .createDeploymentQuery()
      .list();
    for (Deployment deployment : deployments) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }

    // drop DB
    ((ProcessEngineImpl)engine).getProcessEngineConfiguration()
      .getCommandExecutorTxRequired()
      .execute(new Command<Void>() {
        public Void execute(CommandContext commandContext) {

          commandContext.getDbSqlSession().dbSchemaDrop();

          return null;
        }
      });

    engine.close();
  }
}
