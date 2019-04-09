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
package org.camunda.bpm.dmn.engine.impl.delegate;

import java.util.Map;

import org.camunda.bpm.dmn.engine.delegate.DmnEvaluatedDecisionRule;
import org.camunda.bpm.dmn.engine.delegate.DmnEvaluatedOutput;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableRuleImpl;

public class DmnEvaluatedDecisionRuleImpl implements DmnEvaluatedDecisionRule {

  protected String id;
  protected Map<String, DmnEvaluatedOutput> outputEntries;

  public DmnEvaluatedDecisionRuleImpl(DmnDecisionTableRuleImpl matchingRule) {
    this.id = matchingRule.getId();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Map<String, DmnEvaluatedOutput> getOutputEntries() {
    return outputEntries;
  }

  public void setOutputEntries(Map<String, DmnEvaluatedOutput> outputEntries) {
    this.outputEntries = outputEntries;
  }

  @Override
  public String toString() {
    return "DmnEvaluatedDecisionRuleImpl{" +
      "id='" + id + '\'' +
      ", outputEntries=" + outputEntries +
      '}';
  }

}
