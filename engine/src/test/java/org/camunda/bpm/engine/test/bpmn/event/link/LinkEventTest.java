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
package org.camunda.bpm.engine.test.bpmn.event.link;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;


/**
 * @author Bernd Ruecker
 */
public class LinkEventTest extends PluggableProcessEngineTestCase {

  @Deployment
  public void testValidEventLink() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("linkEventValid");

    List<String> activeActivities = runtimeService.getActiveActivityIds(pi.getId());
    // assert that now the first receive task is active
    assertEquals(Arrays.asList(new String []{"waitAfterLink1"}), activeActivities);

    runtimeService.signal(pi.getId());

    activeActivities = runtimeService.getActiveActivityIds(pi.getId());
    // assert that now the second receive task is active
    assertEquals(Arrays.asList(new String []{"waitAfterLink2"}), activeActivities);

    runtimeService.signal(pi.getId());
    assertProcessEnded(pi.getId());

    // validate history
    if(processEngineConfiguration.getHistoryLevel().getId() >= ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {
      List<HistoricActivityInstance> activities = historyService.createHistoricActivityInstanceQuery().processInstanceId(pi.getId()).orderByActivityId().asc().list();
      assertEquals(4, activities.size());
      assertEquals("EndEvent_1", activities.get(0).getActivityId());
      assertEquals("StartEvent_1", activities.get(1).getActivityId());
      assertEquals("waitAfterLink1", activities.get(2).getActivityId());
      assertEquals("waitAfterLink2", activities.get(3).getActivityId());
    }

  }

  @Deployment
  public void testEventLinkMultipleSources() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("linkEventValid");
    List<String> activeActivities = runtimeService.getActiveActivityIds(pi.getId());

    // assert that the link event was triggered and that we are
    assertEquals(Arrays.asList(new String []{"WaitAfterLink", "WaitAfterLink"}), activeActivities);

    runtimeService.deleteProcessInstance(pi.getId(), "test done");

    // validate history
    if(processEngineConfiguration.getHistoryLevel().getId() >= ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {
      List<HistoricActivityInstance> activities = historyService.createHistoricActivityInstanceQuery().processInstanceId(pi.getId()).orderByActivityId().asc().list();
      assertEquals(5, activities.size());
      assertEquals("ManualTask_1", activities.get(0).getActivityId());
      assertEquals("ParallelGateway_1", activities.get(1).getActivityId());
      assertEquals("StartEvent_1", activities.get(2).getActivityId());
      assertEquals("WaitAfterLink", activities.get(3).getActivityId());
      assertEquals("WaitAfterLink", activities.get(4).getActivityId());
    }

  }

  public void testInvalidEventLinkMultipleTargets() {
    try {
      repositoryService.createDeployment().addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/link/LinkEventTest.testInvalidEventLinkMultipleTargets.bpmn20.xml").deploy();
      fail("process should not deploy because it contains multiple event link targets which is invalid in the BPMN 2.0 spec");
    }
    catch (Exception ex) {
      assertTrue(ex.getMessage().contains("Multiple Intermediate Catch Events with the same link event name ('LinkA') are not allowed"));
    }
  }

  public void testCatchLinkEventAfterEventBasedGatewayNotAllowed() {
    try {
      repositoryService.createDeployment().addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/link/LinkEventTest.testCatchLinkEventAfterEventBasedGatewayNotAllowed.bpmn20.xml").deploy();
      fail("process should not deploy because it contains multiple event link targets which is invalid in the BPMN 2.0 spec");
    }
    catch (Exception ex) {
      assertTrue(ex.getMessage().contains("IntermediateCatchLinkEvent is not allowed after an EventBasedGateway."));
    }
  }
}
