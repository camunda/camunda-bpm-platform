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
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity;
import org.camunda.bpm.model.cmmn.instance.Case;
import org.camunda.bpm.model.dmn.instance.Decision;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureGreaterThanOrEqual;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DecisionDefinitionHandler extends DmnDecisionTransformHandler {

  protected static final Pattern REGEX_ISO = Pattern.compile("^P(\\d+)D$");

  @Override
  protected DmnDecisionImpl createDmnElement() {
    return new DecisionDefinitionEntity();
  }

  @Override
  protected DmnDecisionImpl createFromDecision(DmnElementTransformContext context, Decision decision) {
    DecisionDefinitionEntity decisionDefinition = (DecisionDefinitionEntity) super.createFromDecision(context, decision);

    String category = context.getModelInstance().getDefinitions().getNamespace();
    decisionDefinition.setCategory(category);
    parseHistoryTimeToLive(decision, decisionDefinition);
    decisionDefinition.setVersionTag(decision.getVersionTag());

    return decisionDefinition;
  }

  protected void parseHistoryTimeToLive(Decision element, DecisionDefinitionEntity decisionDefinition) {
    Integer historyTimeToLive = null;

    String historyTTL = element.getCamundaHistoryTimeToLiveString();
    if (historyTTL != null && !historyTTL.isEmpty()) {
      Matcher matISO = REGEX_ISO.matcher(historyTTL);
      if (matISO.find()) {
        historyTTL = matISO.group(1);
      }

      try {
        historyTimeToLive = Integer.parseInt(historyTTL);
      } catch (NumberFormatException e) {
        throw new ProcessEngineException("Cannot parse historyTimeToLive: " + e.getMessage() + "| Resource: " + element);
      }
    }

    if (historyTimeToLive == null || historyTimeToLive >= 0) {
      decisionDefinition.setHistoryTimeToLive(historyTimeToLive);
    } else {
      throw new NotValidException("Cannot parse historyTimeToLive: negative value is not allowed. Resource: " + element);
    }
  }

}
