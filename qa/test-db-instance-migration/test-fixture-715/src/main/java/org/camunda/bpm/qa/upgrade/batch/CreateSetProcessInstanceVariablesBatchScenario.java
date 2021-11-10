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
package org.camunda.bpm.qa.upgrade.batch;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.qa.upgrade.DescribesScenario;
import org.camunda.bpm.qa.upgrade.ScenarioSetup;

public class CreateSetProcessInstanceVariablesBatchScenario {

  @Deployment
  public static String modelDeployment() {
    return "org/camunda/bpm/qa/upgrade/batch/oneTaskProcess.bpmn20.xml";
  }

  @DescribesScenario("createSeedCreatedScenario")
  public static ScenarioSetup createSeedCreatedScenario() {
    return (engine, scenarioName) -> {
      RuntimeService runtimeService = engine.getRuntimeService();
      String processInstanceIdOne = runtimeService.startProcessInstanceByKey("oneTaskProcess-batch-715", scenarioName)
          .getId();
      String processInstanceIdTwo = runtimeService.startProcessInstanceByKey("oneTaskProcess-batch-715", scenarioName)
          .getId();

      String batchId = runtimeService.setVariablesAsync(
          Arrays.asList(processInstanceIdOne, processInstanceIdTwo),
          Variables.createVariables().putValue("foo", "bar")).getId();

      engine.getManagementService().setProperty("createSeedCreatedScenario.batch.id", batchId);
      engine.getManagementService().setProperty("createSeedCreatedScenario.processInstanceId.one",
          processInstanceIdOne);
      engine.getManagementService().setProperty("createSeedCreatedScenario.processInstanceId.two",
          processInstanceIdTwo);
    };
  }

  @DescribesScenario("createSeedExecutedScenario")
  public static ScenarioSetup createSeedExecutedScenario() {
    return (engine, scenarioName) -> {
      RuntimeService runtimeService = engine.getRuntimeService();
      ManagementService managementService = engine.getManagementService();

      String processInstanceIdOne = runtimeService.startProcessInstanceByKey("oneTaskProcess-batch-715", scenarioName)
          .getId();
      String processInstanceIdTwo = runtimeService.startProcessInstanceByKey("oneTaskProcess-batch-715", scenarioName)
          .getId();

      Batch batch = runtimeService.setVariablesAsync(
          Arrays.asList(processInstanceIdOne, processInstanceIdTwo),
          Variables.createVariables().putValue("foo", "bar"));

      List<Job> jobs = managementService.createJobQuery()
          .jobDefinitionId(batch.getSeedJobDefinitionId())
          .list();

      jobs.forEach(job -> managementService.executeJob(job.getId()));

      managementService.setProperty("createSeedExecutedScenario.batch.id", batch.getId());
      engine.getManagementService().setProperty("createSeedExecutedScenario.processInstanceId.one",
          processInstanceIdOne);
      engine.getManagementService().setProperty("createSeedExecutedScenario.processInstanceId.two",
          processInstanceIdTwo);
    };
  }

}
