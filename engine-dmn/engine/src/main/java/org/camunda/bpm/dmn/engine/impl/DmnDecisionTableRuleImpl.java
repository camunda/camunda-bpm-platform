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

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.dmn.engine.DmnDecisionTableRule;
import org.camunda.bpm.dmn.engine.DmnDecisionTableValue;

public class DmnDecisionTableRuleImpl implements DmnDecisionTableRule {

  protected String key;
  protected Map<String, DmnDecisionTableValue> outputs = new HashMap<String, DmnDecisionTableValue>();

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public Map<String, DmnDecisionTableValue> getOutputs() {
    return outputs;
  }

  public void setOutputs(Map<String, DmnDecisionTableValue> outputs) {
    this.outputs = outputs;
  }

  public String toString() {
    return "DmnDecisionTableRuleImpl{" +
      "key='" + key + '\'' +
      ", outputs=" + outputs +
      '}';
  }

}
