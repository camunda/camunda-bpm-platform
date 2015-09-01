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

package org.camunda.bpm.dmn.engine.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.DmnDecisionTableRule;
import org.camunda.bpm.dmn.engine.DmnDecisionTableValue;

public class DmnDecisionTableResultImpl implements DmnDecisionTableResult {

  protected Map<String, DmnDecisionTableValue> inputs = new HashMap<String, DmnDecisionTableValue>();
  protected List<DmnDecisionTableRule> matchingRules = new ArrayList<DmnDecisionTableRule>();
  protected String collectResultName;
  protected Object collectResultValue;
  protected long evaluationMetric = 0;

  public Map<String, DmnDecisionTableValue> getInputs() {
    return inputs;
  }

  public void setInputs(Map<String, DmnDecisionTableValue> inputs) {
    this.inputs = inputs;
  }

  public List<DmnDecisionTableRule> getMatchingRules() {
    return matchingRules;
  }

  public void setMatchingRules(List<DmnDecisionTableRule> matchingRules) {
    this.matchingRules = matchingRules;
  }

  public String getCollectResultName() {
    return collectResultName;
  }

  public void setCollectResultName(String collectResultName) {
    this.collectResultName = collectResultName;
  }

  public Object getCollectResultValue() {
    return collectResultValue;
  }

  public void setCollectResultValue(Object collectResultValue) {
    this.collectResultValue = collectResultValue;
  }

  public long getEvaluationMetric() {
    return evaluationMetric;
  }

  public void setEvaluationMetric(long evaluationMetric) {
    this.evaluationMetric = evaluationMetric;
  }

  public String toString() {
    return "DmnDecisionTableResultImpl{" +
      "inputs=" + inputs +
      ", matchingRules=" + matchingRules +
      ", collectResult=" + collectResultValue +
      ", evaluationMetric=" + evaluationMetric +
      '}';
  }

}
