/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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

import org.camunda.bpm.dmn.engine.DmnDecisionLogic;
import org.camunda.bpm.dmn.engine.impl.hitpolicy.DefaultHitPolicyHandlerRegistry;
import org.camunda.bpm.dmn.engine.impl.spi.hitpolicy.DmnHitPolicyHandler;
import org.camunda.bpm.model.dmn.BuiltinAggregator;
import org.camunda.bpm.model.dmn.HitPolicy;

public class DmnDecisionTableImpl implements DmnDecisionLogic {

  protected DmnHitPolicyHandler hitPolicyHandler;

  protected List<DmnDecisionTableInputImpl> inputs = new ArrayList<DmnDecisionTableInputImpl>();
  protected List<DmnDecisionTableOutputImpl> outputs = new ArrayList<DmnDecisionTableOutputImpl>();
  protected List<DmnDecisionTableRuleImpl> rules = new ArrayList<DmnDecisionTableRuleImpl>();

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
      " hitPolicyHandler=" + hitPolicyHandler +
      ", inputs=" + inputs +
      ", outputs=" + outputs +
      ", rules=" + rules +
      '}';
  }
}
