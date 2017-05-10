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
package org.camunda.bpm.model.bpmn.instance.camunda;

import static org.camunda.bpm.model.bpmn.BpmnTestConstants.PROCESS_ID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.CamundaExtensionsTest;
import org.camunda.bpm.model.bpmn.impl.BpmnModelConstants;
import org.camunda.bpm.model.bpmn.impl.instance.ProcessImpl;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.junit.Test;

/**
 * Test to check the interoperability when changing elements and attributes with
 * the {@link BpmnModelConstants#ACTIVITI_NS}. In contrast to
 * {@link CamundaExtensionsTest} this test uses directly the get*Ns() methods to
 * check the expected value.
 *
 * @author Ronny Bräunlich
 *
 */
public class CompatabilityTest {

  @Test
  public void modifyingElementWithActivitiNsKeepsIt() {
    BpmnModelInstance modelInstance = Bpmn.readModelFromStream(CamundaExtensionsTest.class.getResourceAsStream("CamundaExtensionsCompatabilityTest.xml"));
    ProcessImpl process = modelInstance.getModelElementById(PROCESS_ID);
    ExtensionElements extensionElements = process.getExtensionElements();
    Collection<CamundaExecutionListener> listeners = extensionElements.getChildElementsByType(CamundaExecutionListener.class);
    String listenerClass = "org.foo.Bar";
    for (CamundaExecutionListener listener : listeners) {
      listener.setCamundaClass(listenerClass);
    }
    for (CamundaExecutionListener listener : listeners) {
      assertThat(listener.getAttributeValueNs(BpmnModelConstants.ACTIVITI_NS, "class"), is(listenerClass));
    }
  }

  @Test
  public void modifyingAttributeWithActivitiNsKeepsIt() {
    BpmnModelInstance modelInstance = Bpmn.readModelFromStream(CamundaExtensionsTest.class.getResourceAsStream("CamundaExtensionsCompatabilityTest.xml"));
    ProcessImpl process = modelInstance.getModelElementById(PROCESS_ID);
    String priority = "9000";
    process.setCamundaJobPriority(priority);
    process.setCamundaTaskPriority(priority);
    Integer historyTimeToLive = 10;
    process.setCamundaHistoryTimeToLive(historyTimeToLive);
    assertThat(process.getAttributeValueNs(BpmnModelConstants.ACTIVITI_NS, "jobPriority"), is(priority));
    assertThat(process.getAttributeValueNs(BpmnModelConstants.ACTIVITI_NS, "taskPriority"), is(priority));
    assertThat(process.getAttributeValueNs(BpmnModelConstants.ACTIVITI_NS, "historyTimeToLive"), is(historyTimeToLive.toString()));
  }

}
