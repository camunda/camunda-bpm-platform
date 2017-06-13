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
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.python.google.common.collect.Sets;


import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class UpdateProcessInstancesSuspendStateAsyncTest {

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);
  protected BatchSuspensionHelper helper = new BatchSuspensionHelper(engineRule);

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

  @After
  public void removeBatches() {
    helper.removeAllRunningAndHistoricBatches();
  }



  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml"})
  public void testBatchSuspensionById() {
    // given
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("twoExternalTaskProcess");

    // when
    Batch suspendprocess = runtimeService.updateProcessInstanceSuspensionState().byProcessInstanceIds(Arrays.asList(processInstance1.getId(), processInstance2.getId())).suspendAsync();
    helper.executeSeedJob(suspendprocess);
    helper.executeJobs(suspendprocess);


    // then
    ProcessInstance p1c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance1.getId()).singleResult();
    assertTrue(p1c.isSuspended());
    ProcessInstance p2c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance2.getId()).singleResult();
    assertTrue(p2c.isSuspended());

  }


  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml"})
  public void testBatchActivationById() {
    // given
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("twoExternalTaskProcess");

    // when
    Batch suspendprocess = runtimeService.updateProcessInstanceSuspensionState().byProcessInstanceIds(Arrays.asList(processInstance1.getId(), processInstance2.getId())).suspendAsync();
    helper.executeSeedJob(suspendprocess);
    helper.executeJobs(suspendprocess);
    Batch activateprocess = runtimeService.updateProcessInstanceSuspensionState().byProcessInstanceIds(Arrays.asList(processInstance1.getId(), processInstance2.getId())).activateAsync();
    helper.executeSeedJob(activateprocess);
    helper.executeJobs(activateprocess);


    // then
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

    // when
    Batch suspendprocess = runtimeService.updateProcessInstanceSuspensionState().byProcessInstanceQuery(runtimeService.createProcessInstanceQuery().active()).suspendAsync();
    helper.executeSeedJob(suspendprocess);
    helper.executeJobs(suspendprocess);


    // then
    ProcessInstance p1c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance1.getId()).singleResult();
    assertTrue(p1c.isSuspended());
    ProcessInstance p2c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance2.getId()).singleResult();
    assertTrue(p2c.isSuspended());

  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml"})
  public void testBatchActivationByProcessInstanceQuery() {
    // given
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("twoExternalTaskProcess");

    // when
    Batch suspendprocess = runtimeService.updateProcessInstanceSuspensionState().byProcessInstanceQuery(runtimeService.createProcessInstanceQuery().active()).suspendAsync();
    helper.executeSeedJob(suspendprocess);
    helper.executeJobs(suspendprocess);
    Batch activateprocess = runtimeService.updateProcessInstanceSuspensionState().byProcessInstanceQuery(runtimeService.createProcessInstanceQuery().suspended()).activateAsync();
    helper.executeSeedJob(activateprocess);
    helper.executeJobs(activateprocess);


    // then
    ProcessInstance p1c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance1.getId()).singleResult();
    assertFalse(p1c.isSuspended());
    ProcessInstance p2c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance2.getId()).singleResult();
    assertFalse(p2c.isSuspended());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml"})
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
  public void testBatchSuspensionByHistoricProcessInstanceQuery() {
    // given
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("twoExternalTaskProcess");

    // when
    Batch suspendprocess = runtimeService.updateProcessInstanceSuspensionState().byHistoricProcessInstanceQuery(historyService.createHistoricProcessInstanceQuery().processInstanceIds(Sets.newHashSet(processInstance1.getId(), processInstance2.getId()))).suspendAsync();
    helper.executeSeedJob(suspendprocess);
    helper.executeJobs(suspendprocess);


    // then
    ProcessInstance p1c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance1.getId()).singleResult();
    assertTrue(p1c.isSuspended());
    ProcessInstance p2c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance2.getId()).singleResult();
    assertTrue(p2c.isSuspended());

  }


  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml"})
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
  public void testBatchActivationByHistoricProcessInstanceQuery() {
    // given
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("twoExternalTaskProcess");

    // when
    Batch suspendprocess = runtimeService.updateProcessInstanceSuspensionState().byHistoricProcessInstanceQuery(historyService.createHistoricProcessInstanceQuery().processInstanceIds(Sets.newHashSet(processInstance1.getId(), processInstance2.getId()))).suspendAsync();
    helper.executeSeedJob(suspendprocess);
    helper.executeJobs(suspendprocess);
    Batch activateprocess = runtimeService.updateProcessInstanceSuspensionState().byHistoricProcessInstanceQuery(historyService.createHistoricProcessInstanceQuery().processInstanceIds(Sets.newHashSet(processInstance1.getId(), processInstance2.getId()))).activateAsync();
    helper.executeSeedJob(activateprocess);
    helper.executeJobs(activateprocess);


    // then
    ProcessInstance p1c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance1.getId()).singleResult();
    assertFalse(p1c.isSuspended());
    ProcessInstance p2c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance2.getId()).singleResult();
    assertFalse(p2c.isSuspended());
  }

  @Test
  public void testEmptyProcessInstanceListSuspendAsync() {
    // given
    // nothing

    thrown.expect(BadUserRequestException.class);
    thrown.expectMessage("No process instance ids given");

    // when
    runtimeService.updateProcessInstanceSuspensionState().byProcessInstanceIds().suspendAsync();

  }

  @Test
  public void testEmptyProcessInstanceListActivateAsync() {
    // given
    // nothing

    thrown.expect(BadUserRequestException.class);
    thrown.expectMessage("No process instance ids given");

    // when
    runtimeService.updateProcessInstanceSuspensionState().byProcessInstanceIds().activateAsync();


  }


  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml"})
  public void testNullProcessInstanceListActivateAsync() {
    // given
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("twoExternalTaskProcess");

    thrown.expect(BadUserRequestException.class);
    thrown.expectMessage("Cannot be null");

    // when
    runtimeService.updateProcessInstanceSuspensionState().byProcessInstanceIds(Arrays.asList(processInstance1.getId(), processInstance2.getId(), null)).activateAsync();

  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml"})
  public void testNullProcessInstanceListSuspendAsync() {
    // given
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("twoExternalTaskProcess");

    thrown.expect(BadUserRequestException.class);
    thrown.expectMessage("Cannot be null");

    // when
    runtimeService.updateProcessInstanceSuspensionState().byProcessInstanceIds(Arrays.asList(processInstance1.getId(), processInstance2.getId(), null)).suspendAsync();

  }
}
