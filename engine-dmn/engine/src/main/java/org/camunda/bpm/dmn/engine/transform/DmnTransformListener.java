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

import org.camunda.bpm.dmn.engine.DmnClause;
import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionModel;
import org.camunda.bpm.dmn.engine.DmnItemDefinition;
import org.camunda.bpm.dmn.engine.DmnRule;
import org.camunda.bpm.model.dmn.instance.Clause;
import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.model.dmn.instance.Definitions;
import org.camunda.bpm.model.dmn.instance.ItemDefinition;
import org.camunda.bpm.model.dmn.instance.Rule;

public interface DmnTransformListener {

  void transformDefinitions(Definitions definitions, DmnDecisionModel decisionModel);

  void transformDecision(Decision decision, DmnDecision dmnDecision);

  void transformItemDefinition(ItemDefinition itemDefinition, DmnItemDefinition dmnItemDefinition);

  void transformClause(Clause clause, DmnClause dmnClause);

  void transformRule(Rule rule, DmnRule dmnRule);

}
