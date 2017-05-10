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

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_NS;

import java.util.Arrays;
import java.util.Collection;
import org.camunda.bpm.model.bpmn.BpmnTestConstants;

import org.camunda.bpm.model.bpmn.ProcessType;
import org.camunda.bpm.model.bpmn.impl.instance.Supports;
import org.junit.Test;

/**
 * @author Sebastian Menski
 */
public class ProcessTest extends BpmnModelElementInstanceTest {

  public TypeAssumption getTypeAssumption() {
    return new TypeAssumption(CallableElement.class, false);
  }

  public Collection<ChildElementAssumption> getChildElementAssumptions() {
    return Arrays.asList(
      new ChildElementAssumption(Auditing.class, 0, 1),
      new ChildElementAssumption(Monitoring.class, 0, 1),
      new ChildElementAssumption(Property.class),
      new ChildElementAssumption(LaneSet.class),
      new ChildElementAssumption(FlowElement.class),
      new ChildElementAssumption(Artifact.class),
      new ChildElementAssumption(ResourceRole.class),
      new ChildElementAssumption(CorrelationSubscription.class),
      new ChildElementAssumption(Supports.class)
    );
  }

  @Override
  public Collection<AttributeAssumption> getAttributesAssumptions() {
    return Arrays.asList(
      new AttributeAssumption("processType", false, false, ProcessType.None),
      new AttributeAssumption("isClosed", false, false, false),
      new AttributeAssumption("isExecutable"),
      // TODO: definitionalCollaborationRef
      /** camunda extensions */
      new AttributeAssumption(CAMUNDA_NS, "candidateStarterGroups"),
      new AttributeAssumption(CAMUNDA_NS, "candidateStarterUsers"),
      new AttributeAssumption(CAMUNDA_NS, "jobPriority"),
      new AttributeAssumption(CAMUNDA_NS, "taskPriority"),
      new AttributeAssumption(CAMUNDA_NS, "historyTimeToLive")
    );
  }

  @Test
  public void testCamundaJobPriority() {
    Process process = modelInstance.newInstance(Process.class);
    assertThat(process.getCamundaJobPriority()).isNull();

    process.setCamundaJobPriority("15");

    assertThat(process.getCamundaJobPriority()).isEqualTo("15");
  }
  
  @Test
  public void testCamundaTaskPriority() {
    //given
    Process proc = modelInstance.newInstance(Process.class);
    assertThat(proc.getCamundaTaskPriority()).isNull();
    //when
    proc.setCamundaTaskPriority(BpmnTestConstants.TEST_PROCESS_TASK_PRIORITY);
    //then
    assertThat(proc.getCamundaTaskPriority()).isEqualTo(BpmnTestConstants.TEST_PROCESS_TASK_PRIORITY);    
  }

  @Test
  public void testCamundaHistoryTimeToLive() {
    //given
    Process proc = modelInstance.newInstance(Process.class);
    assertThat(proc.getCamundaHistoryTimeToLive()).isNull();
    //when
    proc.setCamundaHistoryTimeToLive(BpmnTestConstants.TEST_HISTORY_TIME_TO_LIVE);
    //then
    assertThat(proc.getCamundaHistoryTimeToLive()).isEqualTo(BpmnTestConstants.TEST_HISTORY_TIME_TO_LIVE);
  }
}
