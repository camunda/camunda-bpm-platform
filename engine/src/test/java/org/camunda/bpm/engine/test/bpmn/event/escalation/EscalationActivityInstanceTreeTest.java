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

package org.camunda.bpm.engine.test.bpmn.event.escalation;

import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Philipp Ossler
 */
public class EscalationActivityInstanceTreeTest extends PluggableProcessEngineTestCase {

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/escalation/EscalationEventTest.testThrowEscalationEventFromEmbeddedSubprocess.bpmn20.xml")
  public void testNonInterruptingEscalationBoundaryEvent(){
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("escalationProcess");
    // an escalation event is thrown from embedded subprocess and caught by non-interrupting boundary event on subprocess

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .activity("taskAfterCatchedEscalation")
          .beginScope("subProcess")
            .activity("taskInSubprocess")
        .done());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/escalation/EscalationEventTest.testInterruptingEscalationBoundaryEvent.bpmn20.xml")
  public void testInterruptingEscalationBoundaryEvent(){
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("escalationProcess");
    // an escalation event is thrown from embedded subprocess and caught by interrupting boundary event on subprocess

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .activity("taskAfterCatchedEscalation")
        .done());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/escalation/EscalationEventSubprocessTest.testCatchEscalationEventInsideSubprocess.bpmn20.xml")
  public void testNonInterruptingEscalationEventSubprocessInsideSubprocess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("escalationProcess");
    // an escalation event is thrown from embedded subprocess and caught by non-interrupting event subprocess inside the subprocess

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .beginScope("subProcess")
            .activity("taskInSubprocess")
            .beginScope("escalationEventSubprocess")
              .activity("taskAfterCatchedEscalation")
        .done());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/escalation/EscalationEventSubprocessTest.testCatchEscalationEventFromEmbeddedSubprocess.bpmn20.xml")
  public void testNonInterruptingEscalationEventSubprocessOutsideSubprocess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("escalationProcess");
    // an escalation event is thrown from embedded subprocess and caught by non-interrupting event subprocess outside the subprocess

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .beginScope("subProcess")
            .activity("taskInSubprocess")
            .endScope()
          .beginScope("escalationEventSubprocess")
            .activity("taskAfterCatchedEscalation")
        .done());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/escalation/EscalationEventSubprocessTest.testInterruptionEscalationEventSubprocess.bpmn20.xml")
  public void testInterruptingEscalationEventSubprocessInsideSubprocess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("escalationProcess");
    // an escalation event is thrown from embedded subprocess and caught by interrupting event subprocess inside the subprocess

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .beginScope("subProcess")
            .beginScope("escalationEventSubprocess")
              .activity("taskAfterCatchedEscalation")
        .done());
  }

}
