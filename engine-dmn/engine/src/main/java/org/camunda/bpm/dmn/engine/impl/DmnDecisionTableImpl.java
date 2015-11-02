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

import org.camunda.bpm.dmn.engine.DmnDecisionTable;
import org.camunda.bpm.dmn.engine.DmnInput;
import org.camunda.bpm.dmn.engine.DmnOutput;
import org.camunda.bpm.dmn.engine.DmnRule;
import org.camunda.bpm.model.dmn.BuiltinAggregator;
import org.camunda.bpm.model.dmn.HitPolicy;

public class DmnDecisionTableImpl extends DmnElementImpl implements DmnDecisionTable {

  public static final HitPolicy DEFAULT_HIT_POLICY = HitPolicy.UNIQUE;

  protected HitPolicy hitPolicy = DEFAULT_HIT_POLICY;
  protected BuiltinAggregator aggregation;

  protected List<DmnInput> inputs = new ArrayList<DmnInput>();
  protected List<DmnOutput> outputs = new ArrayList<DmnOutput>();
  protected List<DmnRule> rules = new ArrayList<DmnRule>();

  public HitPolicy getHitPolicy() {
    return hitPolicy;
  }

  public void setHitPolicy(HitPolicy hitPolicy) {
    this.hitPolicy = hitPolicy;
  }

  public BuiltinAggregator getAggregation() {
    return aggregation;
  }

  public void setAggregation(BuiltinAggregator aggregation) {
    this.aggregation = aggregation;
  }

  public List<DmnInput> getInputs() {
    return inputs;
  }

  public void setInputs(List<DmnInput> inputs) {
    this.inputs = inputs;
  }

  public void addInput(DmnInput input) {
    this.inputs.add(input);
  }

  public List<DmnOutput> getOutputs() {
    return outputs;
  }

  public void setOutputs(List<DmnOutput> outputs) {
    this.outputs = outputs;
  }

  public void addOutput(DmnOutput output) {
    this.outputs.add(output);
  }

  public void setRules(List<DmnRule> rules) {
    this.rules = rules;
  }

  public List<DmnRule> getRules() {
    return rules;
  }

  public void addRule(DmnRule rule) {
    rules.add(rule);
  }

  @Override
  public String toString() {
    return "DmnDecisionTableImpl{" +
      "key='" + key + '\'' +
      ", name='" + name + '\'' +
      ", hitPolicy=" + hitPolicy +
      ", aggregation=" + aggregation +
      ", inputs=" + inputs +
      ", outputs=" + outputs +
      ", rules=" + rules +
      '}';
  }

}
