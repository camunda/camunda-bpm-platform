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

package org.camunda.bpm.model.bpmn.instance;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.impl.instance.Incoming;
import org.camunda.bpm.model.bpmn.impl.instance.Outgoing;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_NS;

/**
 * @author Sebastian Menski
 */
public class FlowNodeTest extends BpmnModelElementInstanceTest {

  public TypeAssumption getTypeAssumption() {
    return new TypeAssumption(FlowElement.class, true);
  }

  public Collection<ChildElementAssumption> getChildElementAssumptions() {
    return Arrays.asList(
      new ChildElementAssumption(Incoming.class),
      new ChildElementAssumption(Outgoing.class)
    );
  }

  public Collection<AttributeAssumption> getAttributesAssumptions() {
    return Arrays.asList(
      new AttributeAssumption(CAMUNDA_NS, "asyncAfter", false, false, false),
      new AttributeAssumption(CAMUNDA_NS, "asyncBefore", false, false, false),
      new AttributeAssumption(CAMUNDA_NS, "exclusive", false, false, true),
      new AttributeAssumption(CAMUNDA_NS, "jobPriority")
    );
  }

  @Test
  public void testUpdateIncomingOutgoingChildElements() {
    BpmnModelInstance modelInstance = Bpmn.createProcess()
      .startEvent()
      .userTask("test")
      .endEvent()
      .done();

    // save current incoming and outgoing sequence flows
    UserTask userTask = modelInstance.getModelElementById("test");
    Collection<SequenceFlow> incoming = userTask.getIncoming();
    Collection<SequenceFlow> outgoing = userTask.getOutgoing();

    // create a new service task
    ServiceTask serviceTask = modelInstance.newInstance(ServiceTask.class);
    serviceTask.setId("new");

    // replace the user task with the new service task
    userTask.replaceWithElement(serviceTask);

    // assert that the new service task has the same incoming and outgoing sequence flows
    assertThat(serviceTask.getIncoming()).containsExactlyElementsOf(incoming);
    assertThat(serviceTask.getOutgoing()).containsExactlyElementsOf(outgoing);
  }

  @Test
    public void testCamundaAsyncBefore() {
    Task task = modelInstance.newInstance(Task.class);
    assertThat(task.isCamundaAsyncBefore()).isFalse();

    task.setCamundaAsyncBefore(true);
    assertThat(task.isCamundaAsyncBefore()).isTrue();
  }

  @Test
  public void testCamundaAsyncAfter() {
    Task task = modelInstance.newInstance(Task.class);
    assertThat(task.isCamundaAsyncAfter()).isFalse();

    task.setCamundaAsyncAfter(true);
    assertThat(task.isCamundaAsyncAfter()).isTrue();
  }

  @Test
  public void testCamundaAsyncAfterAndBefore() {
    Task task = modelInstance.newInstance(Task.class);

    assertThat(task.isCamundaAsyncAfter()).isFalse();
    assertThat(task.isCamundaAsyncBefore()).isFalse();

    task.setCamundaAsyncBefore(true);

    assertThat(task.isCamundaAsyncAfter()).isFalse();
    assertThat(task.isCamundaAsyncBefore()).isTrue();

    task.setCamundaAsyncAfter(true);

    assertThat(task.isCamundaAsyncAfter()).isTrue();
    assertThat(task.isCamundaAsyncBefore()).isTrue();

    task.setCamundaAsyncBefore(false);

    assertThat(task.isCamundaAsyncAfter()).isTrue();
    assertThat(task.isCamundaAsyncBefore()).isFalse();
  }

  @Test
  public void testCamundaExclusive() {
    Task task = modelInstance.newInstance(Task.class);

    assertThat(task.isCamundaExclusive()).isTrue();

    task.setCamundaExclusive(false);

    assertThat(task.isCamundaExclusive()).isFalse();
  }

  @Test
  public void testCamundaJobPriority() {
    Task task = modelInstance.newInstance(Task.class);
    assertThat(task.getCamundaJobPriority()).isNull();

    task.setCamundaJobPriority("15");

    assertThat(task.getCamundaJobPriority()).isEqualTo("15");
  }
}
