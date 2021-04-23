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
package org.camunda.bpm.qa.upgrade.migration;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.qa.upgrade.DescribesScenario;
import org.camunda.bpm.qa.upgrade.ScenarioSetup;

public class CreateSetVariablesMigrationBatchScenario {

  @Deployment
  public static String sourceDeployment() {
    return "org/camunda/bpm/qa/upgrade/migration/oneTaskProcess-source.bpmn20.xml";
  }

  @Deployment
  public static String targetDeployment() {
    return "org/camunda/bpm/qa/upgrade/migration/oneTaskProcess-target.bpmn20.xml";
  }

  @DescribesScenario("createSetVariablesMigrationBatchScenario")
  public static ScenarioSetup createSetVariablesMigrationBatchScenario() {
    return (engine, scenarioName) -> {
      RuntimeService runtimeService = engine.getRuntimeService();
      runtimeService.startProcessInstanceByKey("oneTaskProcess-source-715", scenarioName);

      String sourceDefinition = engine.getRepositoryService().createProcessDefinitionQuery()
          .processDefinitionKey("oneTaskProcess-source-715")
          .singleResult()
          .getId();

      String targetDefinition = engine.getRepositoryService().createProcessDefinitionQuery()
          .processDefinitionKey("oneTaskProcess-target-715")
          .singleResult()
          .getId();

      String processInstanceId =
          runtimeService.startProcessInstanceByKey("oneTaskProcess-source-715").getId();

      MigrationPlan migrationPlan =
          runtimeService.createMigrationPlan(sourceDefinition, targetDefinition)
              .mapEqualActivities()
              .build();

      String batchId = runtimeService.newMigration(migrationPlan)
          .processInstanceIds(processInstanceId)
          .executeAsync()
          .getId();

      engine.getManagementService()
          .setProperty("CreateSetVariablesMigrationBatchScenario.batch.id", batchId);
    };
  }

}
