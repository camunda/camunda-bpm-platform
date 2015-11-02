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

package org.camunda.bpm.dmn.engine.impl.transform;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionModel;
import org.camunda.bpm.dmn.engine.DmnInput;
import org.camunda.bpm.dmn.engine.DmnItemDefinition;
import org.camunda.bpm.dmn.engine.DmnOutput;
import org.camunda.bpm.dmn.engine.DmnRule;
import org.camunda.bpm.dmn.engine.transform.DmnTransformListener;
import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.model.dmn.instance.Definitions;
import org.camunda.bpm.model.dmn.instance.Input;
import org.camunda.bpm.model.dmn.instance.ItemDefinition;
import org.camunda.bpm.model.dmn.instance.Output;
import org.camunda.bpm.model.dmn.instance.Rule;

/**
 * Abstract base class for implementing a {@link DmnTransformListener} without being forced to implement
 * all methods provided, which make the implementation more robust to future changes.
 *
 */
public abstract class AbstractDmnTransformListener implements DmnTransformListener {

  public void transformDefinitions(Definitions definitions, DmnDecisionModel decisionModel) {
  }

  public void transformDecision(Decision decision, DmnDecision dmnDecision) {
  }

  public void transformItemDefinition(ItemDefinition itemDefinition, DmnItemDefinition dmnItemDefinition) {
  }

  public void transformInput(Input input, DmnInput dmnInput) {
  }

  public void transformOutput(Output output, DmnOutput dmnOutput) {
  }

  public void transformRule(Rule rule, DmnRule dmnRule) {
  }

}
