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

package org.camunda.bpm.engine.impl.dmn.result;

import org.camunda.bpm.dmn.engine.DmnDecisionRuleResult;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.DmnEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.dmn.DecisionLogger;
import org.camunda.bpm.engine.variable.Variables;

/**
 * Maps the decision result to a single typed entry.
 *
 * @author Philipp Ossler
 */
public class SingleEntryDecisionTableResultMapper implements DecisionTableResultMapper {

  protected static final DecisionLogger LOG = ProcessEngineLogger.DECISION_LOGGER;

  @Override
  public Object mapDecisionTableResult(DmnDecisionTableResult decisionTableResult) {
    try {
      DmnDecisionRuleResult singleResult = decisionTableResult.getSingleResult();
      if (singleResult != null) {
        return singleResult.getSingleEntryTyped();
      }
      else {
        return Variables.untypedNullValue();
      }
    } catch (DmnEngineException e) {
      throw LOG.decisionResultMappingException(decisionTableResult, this, e);
    }
  }

  @Override
  public String toString() {
    return "SingleEntryDecisionTableResultMapper{}";
  }

}
