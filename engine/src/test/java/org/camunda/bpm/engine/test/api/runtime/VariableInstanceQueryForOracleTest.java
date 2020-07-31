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
package org.camunda.bpm.engine.test.api.runtime;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class VariableInstanceQueryForOracleTest {

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);
  
  @Test
  public void testQueryWhen0InstancesActive() {
    // given
    Assume.assumeTrue(engineRule.getProcessEngineConfiguration().getDatabaseType().equals("oracle"));

    // then
    List<VariableInstance> variables = engineRule.getRuntimeService().createVariableInstanceQuery().list();
    assertEquals(0, variables.size());
  }

  @Test
  public void testQueryWhen1InstanceActive() {
    // given
    Assume.assumeTrue(engineRule.getProcessEngineConfiguration().getDatabaseType().equals("oracle"));
    RuntimeService runtimeService = engineRule.getRuntimeService();
    testRule.deploy(ProcessModels.TWO_TASKS_PROCESS);

    // when
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Process",
        Variables.createVariables().putValue("foo", "bar"));
    String activityInstanceId = runtimeService.getActivityInstance(processInstance.getId()).getId();

    // then
    List<VariableInstance> variables = engineRule.getRuntimeService().createVariableInstanceQuery()
        .activityInstanceIdIn(activityInstanceId).list();
    assertEquals(1, variables.size());
  }

  @Test
  public void testQueryWhen1000InstancesActive() {
    // given
    Assume.assumeTrue(engineRule.getProcessEngineConfiguration().getDatabaseType().equals("oracle"));
    RuntimeService runtimeService = engineRule.getRuntimeService();
    testRule.deploy(ProcessModels.TWO_TASKS_PROCESS);
    String[] ids = new String[1000];

    // when
    for (int i = 0; i < 1000; i++) {
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Process",
          Variables.createVariables().putValue("foo", "bar"));
      String activityInstanceId = runtimeService.getActivityInstance(processInstance.getId()).getId();
      ids[i] = activityInstanceId;
    }

    // then
    List<VariableInstance> variables = engineRule.getRuntimeService().createVariableInstanceQuery()
        .activityInstanceIdIn(ids).list();
    assertEquals(1000, variables.size());
  }

  @Test
  public void testQueryWhen1001InstancesActive() {
    // given
    Assume.assumeTrue(engineRule.getProcessEngineConfiguration().getDatabaseType().equals("oracle"));
    RuntimeService runtimeService = engineRule.getRuntimeService();
    testRule.deploy(ProcessModels.TWO_TASKS_PROCESS);
    String[] ids = new String[1001];

    // when
    for (int i = 0; i < 1001; i++) {
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Process",
          Variables.createVariables().putValue("foo", "bar"));
      String activityInstanceId = runtimeService.getActivityInstance(processInstance.getId()).getId();
      ids[i] = activityInstanceId;
    }

    // then
    List<VariableInstance> variables = engineRule.getRuntimeService().createVariableInstanceQuery()
        .activityInstanceIdIn(ids).list();
    assertEquals(1001, variables.size());
  }

  @Test
  public void testQueryWhen2001InstancesActive() {
    // given
    Assume.assumeTrue(engineRule.getProcessEngineConfiguration().getDatabaseType().equals("oracle"));
    RuntimeService runtimeService = engineRule.getRuntimeService();
    testRule.deploy(ProcessModels.TWO_TASKS_PROCESS);
    String[] ids = new String[2001];

    // when
    for (int i = 0; i < 2001; i++) {
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Process",
          Variables.createVariables().putValue("foo", "bar"));
      String activityInstanceId = runtimeService.getActivityInstance(processInstance.getId()).getId();
      ids[i] = activityInstanceId;
    }

    // then
    List<VariableInstance> variables = engineRule.getRuntimeService().createVariableInstanceQuery()
        .activityInstanceIdIn(ids).list();
    assertEquals(2001, variables.size());
  }
}
