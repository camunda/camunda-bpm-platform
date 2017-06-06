/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import java.util.Arrays;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.python.google.common.collect.Sets;


import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

public class SuspendProcessInstanceTest {

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Rule
  public ExpectedException thrown = ExpectedException.none();


  protected RuntimeService runtimeService;
  protected HistoryService historyService;

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    historyService = engineRule.getHistoryService();
  }


  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml"})
  public void testBatchSuspensionById() {
    // given
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("twoExternalTaskProcess");

    // when the process instances are suspended
    runtimeService.suspendProcessInstances()
      .processInstanceIds(processInstance1.getId(), processInstance2.getId()).execute();

    // Update the process instances and they are suspended
    ProcessInstance p1c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance1.getId()).singleResult();
    assertTrue(p1c.isSuspended());
    ProcessInstance p2c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance2.getId()).singleResult();
    assertTrue(p2c.isSuspended());

  }


  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml"})
  public void testBatchActivatationById() {
    // given
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("twoExternalTaskProcess");

    // when the process instances are suspended
    runtimeService.suspendProcessInstances()
      .processInstanceIds(processInstance1.getId(), processInstance2.getId()).execute();

    // when they are activated again
    runtimeService.activateProcessInstances()
      .processInstanceIds(processInstance1.getId(), processInstance2.getId()).execute();

    // Update the process instances and they are active again
    ProcessInstance p1c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance1.getId()).singleResult();
    assertFalse(p1c.isSuspended());
    ProcessInstance p2c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance2.getId()).singleResult();
    assertFalse(p2c.isSuspended());

  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml"})
  public void testBatchSuspensionByIdArray() {
    // given
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("twoExternalTaskProcess");

    // when the process instances are suspended
    runtimeService.suspendProcessInstances()
      .processInstanceIds(Arrays.asList(processInstance1.getId(), processInstance2.getId())).execute();

    // Update the process instances and they are suspended
    ProcessInstance p1c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance1.getId()).singleResult();
    assertTrue(p1c.isSuspended());
    ProcessInstance p2c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance2.getId()).singleResult();
    assertTrue(p2c.isSuspended());

  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml"})
  public void testBatchActivatationByIdArray() {
    // given
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("twoExternalTaskProcess");

    // when the process instances are suspended
    runtimeService.suspendProcessInstances()
      .processInstanceIds(Arrays.asList(processInstance1.getId(), processInstance2.getId())).execute();

    // when they are activated again
    runtimeService.activateProcessInstances()
      .processInstanceIds(Arrays.asList(processInstance1.getId(), processInstance2.getId())).execute();


    // Update the process instances and they are active again
    ProcessInstance p1c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance1.getId()).singleResult();
    assertFalse(p1c.isSuspended());
    ProcessInstance p2c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance2.getId()).singleResult();
    assertFalse(p2c.isSuspended());

  }


  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml"})
  public void testBatchSuspensionByProcessInstanceQuery() {
    // given
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("twoExternalTaskProcess");

    // when the process instances are suspended
    runtimeService.suspendProcessInstances()
      .processInstanceQuery(runtimeService.createProcessInstanceQuery().active()).execute();

    // Update the process instances and they are suspended
    ProcessInstance p1c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance1.getId()).singleResult();
    assertTrue(p1c.isSuspended());
    ProcessInstance p2c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance2.getId()).singleResult();
    assertTrue(p2c.isSuspended());

  }


  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml"})
  public void testBatchActivatationByProcessInstanceQuery() {
    // given
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("twoExternalTaskProcess");

    // when the process instances are suspended
    runtimeService.suspendProcessInstances()
      .processInstanceQuery(runtimeService.createProcessInstanceQuery().active()).execute();


    // when they are activated again
    runtimeService.activateProcessInstances()
      .processInstanceQuery(runtimeService.createProcessInstanceQuery().suspended()).execute();


    // Update the process instances and they are active again
    ProcessInstance p1c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance1.getId()).singleResult();
    assertFalse(p1c.isSuspended());
    ProcessInstance p2c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance2.getId()).singleResult();
    assertFalse(p2c.isSuspended());

  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml"})
  public void testBatchSuspensionByHistoricProcessInstanceQuery() {
    // given
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("twoExternalTaskProcess");


    // when the process instances are suspended
    runtimeService.suspendProcessInstances()
      .historicProcessInstanceQuery(historyService.createHistoricProcessInstanceQuery().processInstanceIds(Sets.newHashSet(processInstance1.getId(), processInstance2.getId()))).execute();

    // Update the process instances and they are suspended
    ProcessInstance p1c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance1.getId()).singleResult();
    assertTrue(p1c.isSuspended());
    ProcessInstance p2c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance2.getId()).singleResult();
    assertTrue(p2c.isSuspended());

  }


  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml"})
  public void testBatchActivatationByHistoricProcessInstanceQuery() {
    // given
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("twoExternalTaskProcess");


    // when the process instances are suspended
    runtimeService.suspendProcessInstances()
      .historicProcessInstanceQuery(historyService.createHistoricProcessInstanceQuery().processInstanceIds(Sets.newHashSet(processInstance1.getId(), processInstance2.getId()))).execute();

    // when they are activated again
    runtimeService.activateProcessInstances()
      .historicProcessInstanceQuery(historyService.createHistoricProcessInstanceQuery().processInstanceIds(Sets.newHashSet(processInstance1.getId(), processInstance2.getId()))).execute();


    // Update the process instances and they are active again
    ProcessInstance p1c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance1.getId()).singleResult();
    assertFalse(p1c.isSuspended());
    ProcessInstance p2c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance2.getId()).singleResult();
    assertFalse(p2c.isSuspended());

  }
}
