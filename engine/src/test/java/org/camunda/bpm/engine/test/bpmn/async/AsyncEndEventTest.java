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
package org.camunda.bpm.engine.test.bpmn.async;

import java.util.List;

import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Assert;

/**
 * @author Stefan Hentschel
 */
public class AsyncEndEventTest extends PluggableProcessEngineTestCase {

  @Deployment
  public void testAsyncEndEvent() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("asyncEndEvent");
    long count = runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).active().count();

    Assert.assertEquals(1, runtimeService.createExecutionQuery().activityId("endEvent").count());
    Assert.assertEquals(1, count);

    executeAvailableJobs();
    count = runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).count();

    Assert.assertEquals(0, runtimeService.createExecutionQuery().activityId("endEvent").active().count());
    Assert.assertEquals(0, count);
  }

  @Deployment
  public void testAsyncEndEventListeners() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("asyncEndEvent");
    long count = runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).active().count();

    Assert.assertNull(runtimeService.getVariable(pi.getId(), "listener"));
    Assert.assertEquals(1, runtimeService.createExecutionQuery().activityId("endEvent").count());
    Assert.assertEquals(1, count);

    // as we are standing at the end event, we execute it.
    executeAvailableJobs();

    count = runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).active().count();
    Assert.assertEquals(0, count);

    if(processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {

      // after the end event we have a event listener
      HistoricVariableInstanceQuery name = historyService.createHistoricVariableInstanceQuery()
                                                          .processInstanceId(pi.getId())
                                                          .variableName("listener");
      Assert.assertNotNull(name);
      Assert.assertEquals("listener invoked", name.singleResult().getValue());
    }
  }

  @Deployment
  public void testMultipleAsyncEndEvents() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("multipleAsyncEndEvent");
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    // should stop at both end events
    List<Job> jobs = managementService.createJobQuery().withRetriesLeft().list();
    assertEquals(2, jobs.size());

    // execute one of the end events
    managementService.executeJob(jobs.get(0).getId());
    jobs = managementService.createJobQuery().withRetriesLeft().list();
    assertEquals(1, jobs.size());

    // execute the second one
    managementService.executeJob(jobs.get(0).getId());
    // assert that we have finished our instance now
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());

    if(processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {

      // after the end event we have a event listener
      HistoricVariableInstanceQuery name = historyService.createHistoricVariableInstanceQuery()
        .processInstanceId(pi.getId())
        .variableName("message");
      Assert.assertNotNull(name);
      Assert.assertEquals(true, name.singleResult().getValue());

    }
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/async/AsyncEndEventTest.testCallActivity-super.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/async/AsyncEndEventTest.testCallActivity-sub.bpmn20.xml"
  })
  public void testCallActivity() {
    runtimeService.startProcessInstanceByKey("super");

    ProcessInstance pi = runtimeService
        .createProcessInstanceQuery()
        .processDefinitionKey("sub")
        .singleResult();

    assertTrue(pi instanceof ExecutionEntity);

    assertEquals("theSubEnd", ((ExecutionEntity)pi).getActivityId());

  }

}
