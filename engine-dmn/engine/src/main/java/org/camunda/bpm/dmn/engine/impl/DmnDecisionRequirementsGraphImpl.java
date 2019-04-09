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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionRequirementsGraph;

public class DmnDecisionRequirementsGraphImpl implements DmnDecisionRequirementsGraph {

  protected String key;
  protected String name;

  protected Map<String, DmnDecision> decisions = new HashMap<String, DmnDecision>();

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

  public Collection<DmnDecision> getDecisions() {
    return decisions.values();
  }

  public void setDecisions(Map<String, DmnDecision> decisions) {
    this.decisions = decisions;
  }

  public void addDecision(DmnDecision decision) {
    decisions.put(decision.getKey(), decision);
  }

  public DmnDecision getDecision(String key) {
    return decisions.get(key);
  }

  public Set<String> getDecisionKeys() {
    return decisions.keySet();
  }

  @Override
  public String toString() {
    return "DmnDecisionRequirementsGraphImpl [key=" + key + ", name=" + name + ", decisions=" + decisions + "]";
  }



}
