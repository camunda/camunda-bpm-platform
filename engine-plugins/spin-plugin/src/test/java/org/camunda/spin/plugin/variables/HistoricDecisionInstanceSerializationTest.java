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

package org.camunda.spin.plugin.variables;

import java.util.List;

import org.camunda.bpm.engine.history.HistoricDecisionInputInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionOutputInstance;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.spin.DataFormats;

public class HistoricDecisionInstanceSerializationTest extends PluggableProcessEngineTestCase {

  @Deployment(resources = {"org/camunda/spin/plugin/DecisionSingleOutput.dmn11.xml"})
  public void testListJsonProperty() {
    JsonListSerializable<String> list = new JsonListSerializable<String>();
    list.addElement("foo");

    ObjectValue objectValue = Variables.objectValue(list).serializationDataFormat(DataFormats.JSON_DATAFORMAT_NAME).create();

    VariableMap variables = Variables.createVariables()
      .putValueTyped("input1", objectValue);

    decisionService.evaluateDecisionTableByKey("testDecision", variables);

    HistoricDecisionInstance testDecision = historyService.createHistoricDecisionInstanceQuery().decisionDefinitionKey("testDecision").includeInputs().includeOutputs().singleResult();
    assertNotNull(testDecision);

    List<HistoricDecisionInputInstance> inputs = testDecision.getInputs();
    assertEquals(1, inputs.size());

    HistoricDecisionInputInstance inputInstance = inputs.get(0);
    assertEquals(list.getListProperty(), inputInstance.getValue());

    List<HistoricDecisionOutputInstance> outputs = testDecision.getOutputs();
    assertEquals(1, outputs.size());

    HistoricDecisionOutputInstance outputInstance = outputs.get(0);
    assertEquals(list.getListProperty(), outputInstance.getValue());

  }

}
