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
import java.util.List;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.impl.spi.hitpolicy.DmnHitPolicyHandler;

public class DmnDecisionTableImpl implements DmnDecision {

  protected String key;
  protected String name;

  protected DmnHitPolicyHandler hitPolicyHandler;

  protected List<DmnDecisionTableInputImpl> inputs = new ArrayList<DmnDecisionTableInputImpl>();
  protected List<DmnDecisionTableOutputImpl> outputs = new ArrayList<DmnDecisionTableOutputImpl>();
  protected List<DmnDecisionTableRuleImpl> rules = new ArrayList<DmnDecisionTableRuleImpl>();

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isDecisionTable() {
    return true;
  }

  public DmnHitPolicyHandler getHitPolicyHandler() {
    return hitPolicyHandler;
  }

  public void setHitPolicyHandler(DmnHitPolicyHandler hitPolicyHandler) {
    this.hitPolicyHandler = hitPolicyHandler;
  }

  public List<DmnDecisionTableInputImpl> getInputs() {
    return inputs;
  }

  public void setInputs(List<DmnDecisionTableInputImpl> inputs) {
    this.inputs = inputs;
  }

  public List<DmnDecisionTableOutputImpl> getOutputs() {
    return outputs;
  }

  public void setOutputs(List<DmnDecisionTableOutputImpl> outputs) {
    this.outputs = outputs;
  }

  public List<DmnDecisionTableRuleImpl> getRules() {
    return rules;
  }

  public void setRules(List<DmnDecisionTableRuleImpl> rules) {
    this.rules = rules;
  }

  @Override
  public String toString() {
    return "DmnDecisionTableImpl{" +
      "key='" + key + '\'' +
      ", name='" + name + '\'' +
      ", hitPolicyHandler=" + hitPolicyHandler +
      ", inputs=" + inputs +
      ", outputs=" + outputs +
      ", rules=" + rules +
      '}';
  }

}
