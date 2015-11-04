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

import org.camunda.bpm.dmn.engine.DmnDecisionTableInput;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.DmnDecisionTableRule;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class DmnDecisionTableResultImpl implements DmnDecisionTableResult {

  protected Map<String, DmnDecisionTableInput> inputs = new HashMap<String, DmnDecisionTableInput>();
  protected List<DmnDecisionTableRule> matchingRules = new ArrayList<DmnDecisionTableRule>();
  protected String collectResultName;
  protected TypedValue collectResultValue;
  protected long executedDecisionElements = 0;

  public Map<String, DmnDecisionTableInput> getInputs() {
    return inputs;
  }

  public void setInputs(Map<String, DmnDecisionTableInput> inputs) {
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

  public TypedValue getCollectResultValue() {
    return collectResultValue;
  }

  public void setCollectResultValue(TypedValue outputValue) {
    this.collectResultValue = outputValue;
  }

  public long getExecutedDecisionElements() {
    return executedDecisionElements;
  }

  public void setExecutedDecisionElements(long executedDecisionElements) {
    this.executedDecisionElements = executedDecisionElements;
  }

  @Override
  public String toString() {
    return "DmnDecisionTableResultImpl{" +
      "inputs=" + inputs +
      ", matchingRules=" + matchingRules +
      ", collectResultName='" + collectResultName + '\'' +
      ", collectResultValue=" + collectResultValue +
      ", executedDecisionElements=" + executedDecisionElements +
      '}';
  }

}
