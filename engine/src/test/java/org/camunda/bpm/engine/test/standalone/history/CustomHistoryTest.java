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
public class CustomHistoryTest extends ResourceProcessEngineTestCase {

  public CustomHistoryTest() {
    super("org/camunda/bpm/engine/test/standalone/history/customhistory.camunda.cfg.xml");
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testReceivesVariableUpdates() {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    String value = "a Variable Value";
    runtimeService.setVariable(instance.getId(), "aStringVariable", value);
    runtimeService.setVariable(instance.getId(), "aBytesVariable", value.getBytes());

    // then the historic variable instances and their values exist
    assertEquals(2, historyService.createHistoricVariableInstanceQuery().count());

    HistoricVariableInstance historicStringVariable =
        historyService.createHistoricVariableInstanceQuery().variableName("aStringVariable").singleResult();
    assertNotNull(historicStringVariable);
    assertEquals(value, historicStringVariable.getValue());

    HistoricVariableInstance historicBytesVariable =
        historyService.createHistoricVariableInstanceQuery().variableName("aBytesVariable").singleResult();
    assertNotNull(historicBytesVariable);
    assertTrue(Arrays.equals(value.getBytes(), (byte[]) historicBytesVariable.getValue()));

    // then the historic variable updates and their values exist
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().count());

    HistoricVariableUpdate historicStringVariableUpdate =
        (HistoricVariableUpdate) historyService.createHistoricDetailQuery()
          .variableUpdates()
          .variableInstanceId(historicStringVariable.getId())
          .singleResult();

    assertNotNull(historicStringVariableUpdate);
    assertEquals(value, historicStringVariableUpdate.getValue());

    HistoricVariableUpdate historicByteVariableUpdate =
        (HistoricVariableUpdate) historyService.createHistoricDetailQuery()
          .variableUpdates()
          .variableInstanceId(historicBytesVariable.getId())
          .singleResult();
    assertNotNull(historicByteVariableUpdate);
    assertTrue(Arrays.equals(value.getBytes(), (byte[]) historicByteVariableUpdate.getValue()));

  }
}
