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
package org.camunda.bpm.qa.upgrade.batch.deploymentaware;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.qa.upgrade.DescribesScenario;
import org.camunda.bpm.qa.upgrade.ScenarioSetup;

public class DeploymentAwareBatchesScenario {

  @Deployment
  public static String deployOneTask() {
    return "org/camunda/bpm/qa/upgrade/batch/deploymentaware/oneTaskProcess.bpmn20.xml";
  }

  @Deployment
  public static String deployTwoTasks() {
    return "org/camunda/bpm/qa/upgrade/batch/deploymentaware/twoTasksProcess.bpmn20.xml";
  }

  @DescribesScenario("createDeleteInstancesBatch")
  public static ScenarioSetup createDeleteInstancesBatch() {
    return (engine, scenarioName) -> {
      // definitions
      ProcessDefinition definitionOne = engine.getRepositoryService().createProcessDefinitionQuery()
          .processDefinitionKey("oneTaskProcess_712").singleResult();
      ProcessDefinition definitionTwo = engine.getRepositoryService().createProcessDefinitionQuery()
          .processDefinitionKey("twoTasksProcess_712").singleResult();

      // delete instances batch
      List<String> instanceIds = new ArrayList<>();
      instanceIds.add(engine.getRuntimeService().startProcessInstanceByKey(definitionOne.getKey()).getId());
      instanceIds.add(engine.getRuntimeService().startProcessInstanceByKey(definitionOne.getKey()).getId());
      instanceIds.add(engine.getRuntimeService().startProcessInstanceByKey(definitionTwo.getKey()).getId());
      instanceIds.add(engine.getRuntimeService().startProcessInstanceByKey(definitionTwo.getKey()).getId());

      Batch batch = engine.getRuntimeService().deleteProcessInstancesAsync(instanceIds, "DeploymentAwareBatches");
      engine.getManagementService().setProperty(getPropertyName("delete.batchId"), batch.getId());
      engine.getManagementService().setProperty(getPropertyName("deploymentId1"), definitionOne.getDeploymentId());
      engine.getManagementService().setProperty(getPropertyName("deploymentId2"), definitionTwo.getDeploymentId());
    };
  }

  @DescribesScenario("createRestartInstancesBatch")
  public static ScenarioSetup createRestartInstancesBatch() {
    return (engine, scenarioName) -> {
      ProcessDefinition definitionTwo = engine.getRepositoryService().createProcessDefinitionQuery()
          .processDefinitionKey("twoTasksProcess_712").singleResult();

      ProcessInstance restartInstance1 = engine.getRuntimeService().startProcessInstanceByKey(definitionTwo.getKey());
      ProcessInstance restartInstance2 = engine.getRuntimeService().startProcessInstanceByKey(definitionTwo.getKey());

      engine.getRuntimeService().deleteProcessInstance(restartInstance1.getId(), "DeploymentAwareBatches");
      engine.getRuntimeService().deleteProcessInstance(restartInstance2.getId(), "DeploymentAwareBatches");

      Batch batch = engine.getRuntimeService().restartProcessInstances(definitionTwo.getId())
          .startBeforeActivity("userTask1")
          .processInstanceIds(restartInstance1.getId(), restartInstance2.getId())
          .executeAsync();
      engine.getManagementService().setProperty(getPropertyName("restart.batchId"), batch.getId());
    };
  }

  @DescribesScenario("createModificationInstancesBatch")
  public static ScenarioSetup createModificationInstancesBatch() {
    return (engine, scenarioName) -> {
      ProcessDefinition definitionTwo = engine.getRepositoryService().createProcessDefinitionQuery()
          .processDefinitionKey("twoTasksProcess_712").singleResult();

      ProcessInstance instance = engine.getRuntimeService().startProcessInstanceByKey(definitionTwo.getKey());

      Batch batch = engine.getRuntimeService().createProcessInstanceModification(instance.getId())
          .startBeforeActivity("secondTask")
          .executeAsync();
      engine.getManagementService().setProperty(getPropertyName("modify.batchId"), batch.getId());
    };
  }

  @DescribesScenario("createMigrationInstancesBatch")
  public static ScenarioSetup createMigrationInstancesBatch() {
    return (engine, scenarioName) -> {
      ProcessDefinition definitionOne = engine.getRepositoryService().createProcessDefinitionQuery()
          .processDefinitionKey("oneTaskProcess_712").singleResult();
      ProcessDefinition definitionTwo = engine.getRepositoryService().createProcessDefinitionQuery()
          .processDefinitionKey("twoTasksProcess_712").singleResult();

      ProcessInstance instance = engine.getRuntimeService().startProcessInstanceByKey(definitionOne.getKey());

      MigrationPlan migrationPlan = engine.getRuntimeService()
          .createMigrationPlan(definitionOne.getId(), definitionTwo.getId())
          .mapEqualActivities()
          .build();

      Batch batch = engine.getRuntimeService().newMigration(migrationPlan)
          .processInstanceIds(Arrays.asList(instance.getId()))
          .executeAsync();
      engine.getManagementService().setProperty(getPropertyName("migrate.batchId"), batch.getId());
    };
  }

  protected static String getDeploymentIdFromDefinitionByKey(String processDefinitionKey, RepositoryService repositoryService) {
    return repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey).singleResult().getDeploymentId();
  }

  protected static String getPropertyName(String suffix) {
    return "DeploymentAwareBatches." + suffix;
  }
}
