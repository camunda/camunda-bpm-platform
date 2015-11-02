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

package org.camunda.bpm.dmn.engine.transform;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.List;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionModel;
import org.camunda.bpm.dmn.engine.DmnDecisionTable;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnExpression;
import org.camunda.bpm.dmn.engine.DmnInput;
import org.camunda.bpm.dmn.engine.DmnInputEntry;
import org.camunda.bpm.dmn.engine.DmnOutput;
import org.camunda.bpm.dmn.engine.DmnOutputEntry;
import org.camunda.bpm.dmn.engine.DmnRule;
import org.camunda.bpm.dmn.engine.impl.DefaultTypeDefinition;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableImpl;
import org.camunda.bpm.dmn.engine.impl.DmnEngineConfigurationImpl;
import org.camunda.bpm.dmn.engine.impl.DmnInputImpl;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.camunda.commons.utils.IoUtil;
import org.junit.BeforeClass;
import org.junit.Test;

public class DmnTransformTest {

  protected static DmnEngine engine;
  protected static DmnDecisionModel decisionModel;

  @BeforeClass
  public static void readModelFromFile() {
    InputStream inputStream = IoUtil.fileAsStream("org/camunda/bpm/dmn/engine/transform/DmnTransformTest.dmn");
    DmnEngineConfigurationImpl engineConfiguration = new DmnEngineConfigurationImpl();
    engine = engineConfiguration.buildEngine();
    decisionModel = engine.parseDecisionModel(inputStream);
  }

  @Test
  public void shouldTransformDefinitions() {
    assertThat(decisionModel.getKey()).isEqualTo("definitions");
    assertThat(decisionModel.getName()).isEqualTo("camunda");
    assertThat(decisionModel.getNamespace()).isEqualTo("http://camunda.org/schema/1.0/dmn");
    assertThat(decisionModel.getExpressionLanguage()).isEqualTo(null);
  }


  @Test
  public void shouldTransformDecisions() {
    assertThat(decisionModel.getDecisions()).hasSize(2);

    DmnDecision decision = decisionModel.getDecision("decision1");
    assertThat(decision).isNotNull();
    assertThat(decision.getName()).isEqualTo("camunda");

    // decision2 should be ignored as it isn't supported by the DMN engine
    decision = decisionModel.getDecision("decision2");
    assertThat(decision).isNull();

    decision = decisionModel.getDecision("decision3");
    assertThat(decision).isNotNull();
    assertThat(decision.getName()).isEqualTo("camunda");
  }

  @Test
  public void shouldTransformDecisionTables() {
    DmnDecision decision = decisionModel.getDecision("decision1");
    assertThat(decision).isInstanceOf(DmnDecisionTable.class);

    DmnDecisionTable decisionTable = (DmnDecisionTable) decision;
    assertThat(decisionTable.getHitPolicy()).isEqualTo(DmnDecisionTableImpl.DEFAULT_HIT_POLICY);

    decision = decisionModel.getDecision("decision3");
    assertThat(decision).isInstanceOf(DmnDecisionTable.class);

    decisionTable = (DmnDecisionTable) decision;
    assertThat(decisionTable.getHitPolicy()).isEqualTo(HitPolicy.FIRST);
  }

  @Test
  public void shouldTransformInputs() {
    List<DmnInput> inputs = getInputsForDecision("decision1");
    assertThat(inputs).hasSize(2);

    DmnInput input = inputs.get(0);
    assertThat(input.getKey()).isEqualTo("input");
    assertThat(input.getName()).isEqualTo("camunda");
    assertThat(input.getOutputName()).isEqualTo("camunda");

    DmnExpression inputExpression = input.getInputExpression();
    assertThat(inputExpression).isNotNull();
    assertThat(inputExpression.getKey()).isEqualTo("inputExpression");
    assertThat(inputExpression.getName()).isNull();
    assertThat(inputExpression.getExpressionLanguage()).isEqualTo("camunda");
    assertThat(inputExpression.getExpression()).isEqualTo("camunda");

    assertThat(inputExpression.getTypeDefinition()).isNotNull();
    assertThat(inputExpression.getTypeDefinition().getTypeName()).isEqualTo("string");

    input = inputs.get(1);
    assertThat(input.getKey()).isNull();
    assertThat(input.getName()).isNull();
    assertThat(input.getOutputName()).isEqualTo(DmnInputImpl.DEFAULT_INPUT_VARIABLE_NAME);

    inputExpression = input.getInputExpression();
    assertThat(inputExpression).isNotNull();
    assertThat(inputExpression.getKey()).isNull();
    assertThat(inputExpression.getName()).isNull();
    assertThat(inputExpression.getExpressionLanguage()).isNull();
    assertThat(inputExpression.getExpression()).isNull();

    assertThat(inputExpression.getTypeDefinition()).isNotNull();
    assertThat(inputExpression.getTypeDefinition()).isEqualTo(new DefaultTypeDefinition());
  }

  @Test
  public void shouldTransformOutputs() {
    List<DmnOutput> outputs = getOutputsForDecision("decision1");
    assertThat(outputs).hasSize(2);

    DmnOutput output = outputs.get(0);
    assertThat(output.getKey()).isEqualTo("output");
    assertThat(output.getName()).isEqualTo("camunda");
    assertThat(output.getOutputName()).isEqualTo("camunda");
    assertThat(output.getTypeDefinition()).isNotNull();
    assertThat(output.getTypeDefinition().getTypeName()).isEqualTo("string");

    output = outputs.get(1);
    assertThat(output.getKey()).isNull();
    assertThat(output.getName()).isNull();
    assertThat(output.getOutputName()).isNull();
    assertThat(output.getTypeDefinition()).isNotNull();
    assertThat(output.getTypeDefinition()).isEqualTo(new DefaultTypeDefinition());
  }

  @Test
  public void shouldTransformRules() {
    List<DmnRule> rules = getRulesForDecision("decision1");
    assertThat(rules).hasSize(1);

    DmnRule rule = rules.get(0);

    List<DmnInputEntry> inputEntries = rule.getInputEntries();
    assertThat(inputEntries).hasSize(2);

    DmnInputEntry inputEntry = inputEntries.get(0);
    assertThat(inputEntry.getKey()).isEqualTo("inputEntry");
    assertThat(inputEntry.getName()).isEqualTo("camunda");
    assertThat(inputEntry.getExpressionLanguage()).isEqualTo("camunda");
    assertThat(inputEntry.getExpression()).isEqualTo("camunda");

    inputEntry = inputEntries.get(1);
    assertThat(inputEntry.getKey()).isNull();
    assertThat(inputEntry.getName()).isNull();
    assertThat(inputEntry.getExpressionLanguage()).isNull();
    assertThat(inputEntry.getExpression()).isNull();

    List<DmnOutputEntry> outputEntries = rule.getOutputEntries();
    assertThat(outputEntries).hasSize(2);

    DmnOutputEntry outputEntry = outputEntries.get(0);
    assertThat(outputEntry.getKey()).isEqualTo("outputEntry");
    assertThat(outputEntry.getName()).isEqualTo("camunda");
    assertThat(outputEntry.getExpressionLanguage()).isEqualTo("camunda");
    assertThat(outputEntry.getExpression()).isEqualTo("camunda");

    outputEntry = outputEntries.get(1);
    assertThat(outputEntry.getKey()).isNull();
    assertThat(outputEntry.getName()).isNull();
    assertThat(outputEntry.getExpressionLanguage()).isNull();
    assertThat(outputEntry.getExpression()).isNull();
  }

  // helper ///////////////////////////////////////////////////////////////////////////////////////////////////////

  public List<DmnInput> getInputsForDecision(String decisionKey) {
    DmnDecisionTable decisionTable = decisionModel.getDecision(decisionKey);
    return decisionTable.getInputs();
  }

  public List<DmnOutput> getOutputsForDecision(String decisionKey) {
    DmnDecisionTable decisionTable = decisionModel.getDecision(decisionKey);
    return decisionTable.getOutputs();
  }

  public List<DmnRule> getRulesForDecision(String decisionKey) {
    DmnDecisionTable decisionTable = decisionModel.getDecision(decisionKey);
    return decisionTable.getRules();
  }

}
