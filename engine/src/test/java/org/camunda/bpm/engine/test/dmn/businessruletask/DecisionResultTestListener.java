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

package org.camunda.bpm.engine.test.dmn.businessruletask;

import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.util.DecisionEvaluationUtil;

/**
 * @author Philipp Ossler
 */
public class DecisionResultTestListener implements ExecutionListener {

  public static DmnDecisionResult decisionResult = null;

  @Override
  public void notify(DelegateExecution execution) throws Exception {
    decisionResult = (DmnDecisionResult) execution.getVariable(DecisionEvaluationUtil.DECISION_RESULT_VARIABLE);
  }

  public static DmnDecisionResult getDecisionResult() {
    return decisionResult;
  }

  public static void reset() {
    decisionResult = null;
  }

}
