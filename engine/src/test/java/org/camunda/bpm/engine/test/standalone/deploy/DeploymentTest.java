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
package org.camunda.bpm.engine.test.standalone.deploy;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class DeploymentTest {

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(configuration -> {
    configuration.setJdbcUrl("jdbc:h2:mem:DeploymentTest-HistoryLevelNone;DB_CLOSE_DELAY=1000");
    configuration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP);
    configuration.setHistoryLevel(HistoryLevel.HISTORY_LEVEL_NONE);
    configuration.setDbHistoryUsed(false);
  });
  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testHelper);

  @Test
  public void shouldDeleteDeployment() {
     BpmnModelInstance instance = Bpmn.createExecutableProcess("process").startEvent().endEvent().done();

     DeploymentWithDefinitions deployment = engineRule.getRepositoryService()
         .createDeployment()
         .addModelInstance("foo.bpmn", instance)
         .deployWithResult();

     engineRule.getRepositoryService().deleteDeployment(deployment.getId(), true);

     long count = engineRule.getRepositoryService().createDeploymentQuery().count();
     assertThat(count).isEqualTo(0L);
  }

  @Test
  public void shouldDeleteDeploymentWithRunningInstance() {
     BpmnModelInstance instance = Bpmn.createExecutableProcess("process")
         .startEvent()
         .userTask("testTask")
         .endEvent()
         .done();

     DeploymentWithDefinitions deployment = engineRule.getRepositoryService()
         .createDeployment()
         .addModelInstance("foo.bpmn", instance)
         .deployWithResult();

     engineRule.getRuntimeService().startProcessInstanceByKey("process");
     assertThat(engineRule.getRuntimeService().createProcessInstanceQuery().count()).isEqualTo(1L);

     engineRule.getRepositoryService().deleteDeployment(deployment.getId(), true);

     long count = engineRule.getRepositoryService().createDeploymentQuery().count();
     assertThat(count).isEqualTo(0L);
  }
}
