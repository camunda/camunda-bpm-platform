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

package org.camunda.bpm.engine.impl.dmn.transformer;

import org.camunda.bpm.dmn.engine.impl.DmnDecisionImpl;
import org.camunda.bpm.dmn.engine.impl.spi.transform.DmnElementTransformContext;
import org.camunda.bpm.dmn.engine.impl.transform.DmnDecisionTransformHandler;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity;
import org.camunda.bpm.model.dmn.instance.Decision;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureGreaterThanOrEqual;

public class DecisionDefinitionHandler extends DmnDecisionTransformHandler {

  @Override
  protected DmnDecisionImpl createDmnElement() {
    return new DecisionDefinitionEntity();
  }

  @Override
  protected DmnDecisionImpl createFromDecision(DmnElementTransformContext context, Decision decision) {
    DecisionDefinitionEntity decisionDefinition = (DecisionDefinitionEntity) super.createFromDecision(context, decision);

    String category = context.getModelInstance().getDefinitions().getNamespace();
    decisionDefinition.setCategory(category);

    final Integer historyTimeToLive = decision.getCamundaHistoryTimeToLive();
    validateHistoryTimeToLive(historyTimeToLive);
    decisionDefinition.setHistoryTimeToLive(historyTimeToLive);
    decisionDefinition.setVersionTag(decision.getVersionTag());
    
    return decisionDefinition;
  }

  private void validateHistoryTimeToLive(Integer historyTimeToLive) {
    if (historyTimeToLive != null) {
      ensureGreaterThanOrEqual("", "historyTimeToLive", historyTimeToLive, 0);
    }
  }

}
