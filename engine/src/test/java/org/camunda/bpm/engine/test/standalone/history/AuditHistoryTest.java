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
package org.camunda.bpm.engine.test.standalone.history;

import java.util.Arrays;

import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableUpdate;
import org.camunda.bpm.engine.impl.test.ResourceProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Thorben Lindhauer
 *
 */
public class AuditHistoryTest extends ResourceProcessEngineTestCase {

  public AuditHistoryTest() {
    super("org/camunda/bpm/engine/test/standalone/history/audithistory.camunda.cfg.xml");
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testReceivesNoHistoricVariableUpdatesAsDetails() {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    String value = "a Variable Value";
    runtimeService.setVariable(instance.getId(), "aStringVariable", value);
    runtimeService.setVariable(instance.getId(), "aBytesVariable", value.getBytes());

    String newValue = "a new Variable Value";
    runtimeService.setVariable(instance.getId(), "aStringVariable", newValue);
    runtimeService.setVariable(instance.getId(), "aBytesVariable", newValue.getBytes());

    // then the historic variable instances exist and they have the latest values
    assertEquals(2, historyService.createHistoricVariableInstanceQuery().count());

    HistoricVariableInstance historicStringVariable =
        historyService.createHistoricVariableInstanceQuery().variableName("aStringVariable").singleResult();
    assertNotNull(historicStringVariable);
    assertEquals(newValue, historicStringVariable.getValue());

    HistoricVariableInstance historicBytesVariable =
        historyService.createHistoricVariableInstanceQuery().variableName("aBytesVariable").singleResult();
    assertNotNull(historicBytesVariable);
    assertTrue(Arrays.equals(newValue.getBytes(), (byte[]) historicBytesVariable.getValue()));

    // and no historic details exist
    assertEquals(0, historyService.createHistoricDetailQuery().variableUpdates().count());

  }
}
