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

import java.util.List;

import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;


/**
 * @author Bernd Ruecker
 */
public class LinkEventTest extends PluggableProcessEngineTestCase {
  
  @Deployment(resources="org/camunda/bpm/engine/test/bpmn/event/link/linkEventValid.bpmn")
  public void testValidEventLink() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("linkEventValid");    
    assertProcessEnded(pi.getId());
    
    List<HistoricActivityInstance> activities = historyService.createHistoricActivityInstanceQuery().processInstanceId(pi.getId()).orderByActivityId().asc().list();
    assertEquals(5, activities.size());
    assertEquals("EndEvent_1", activities.get(0).getActivityId());
//    assertEquals("IntermediateCatchEvent_1", activities.get(1).getActivityId());
//    assertEquals("IntermediateCatchEvent_2", activities.get(2).getActivityId());
    assertEquals("ManualTask_1", activities.get(1).getActivityId());
    assertEquals("ManualTask_2", activities.get(2).getActivityId());
    assertEquals("ManualTask_3", activities.get(3).getActivityId());
    assertEquals("StartEvent_1", activities.get(4).getActivityId());
    
  }

  @Deployment(resources="org/camunda/bpm/engine/test/bpmn/event/link/linkEventDuplicateSourceValid.bpmn")
  public void testEventLinkMultipleSources() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("linkEventValid");    
    assertProcessEnded(pi.getId());
    
    List<HistoricActivityInstance> activities = historyService.createHistoricActivityInstanceQuery().processInstanceId(pi.getId()).orderByActivityId().asc().list();
    assertEquals(7, activities.size());
    assertEquals("EndEvent_1", activities.get(0).getActivityId());
    assertEquals("EndEvent_1", activities.get(1).getActivityId());
//    assertEquals("IntermediateCatchEvent_1", activities.get(1).getActivityId());
//    assertEquals("IntermediateCatchEvent_2", activities.get(2).getActivityId());
    assertEquals("ManualTask_1", activities.get(2).getActivityId());
    assertEquals("ManualTask_2", activities.get(3).getActivityId());
    assertEquals("ManualTask_2", activities.get(4).getActivityId());
    assertEquals("ParallelGateway_1", activities.get(5).getActivityId());
    assertEquals("StartEvent_1", activities.get(6).getActivityId());
    
  }

  public void testInvalidEventLinkMultipleTargets() {
    try {
      repositoryService.createDeployment().addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/link/linkEventInvalid.bpmn").deploy();
      fail("process should not deploy because it contains multiple event link targets which is invalid in the BPMN 2.0 spec");
    }
    catch (Exception ex) {
      assertTrue(ex.getMessage().contains("Multiple Intermediate Catch Events with the same link event name ('LinkA') are not allowed"));
    }   
  }
}
