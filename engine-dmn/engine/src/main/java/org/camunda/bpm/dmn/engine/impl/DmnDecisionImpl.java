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
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionLogic;

public class DmnDecisionImpl implements DmnDecision {

  protected String key;
  protected String name;

  protected DmnDecisionLogic decisionLogic;

  protected Collection<DmnDecision> requiredDecision = new ArrayList<DmnDecision>();

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

  public void setDecisionLogic(DmnDecisionLogic decisionLogic) {
    this.decisionLogic = decisionLogic;
  }

  public DmnDecisionLogic getDecisionLogic() {
    return decisionLogic;
  }

  public void setRequiredDecision(List<DmnDecision> requiredDecision) {
    this.requiredDecision = requiredDecision;
  }

  @Override
  public Collection<DmnDecision> getRequiredDecisions() {
    return requiredDecision;
  }

  @Override
  public boolean isDecisionTable() {
    return decisionLogic != null && decisionLogic instanceof DmnDecisionTableImpl;
  }

  @Override
  public String toString() {
    return "DmnDecisionTableImpl{" +
      " key= "+ key +
      ", name= "+ name +
      ", requiredDecision=" + requiredDecision +
      ", decisionLogic=" + decisionLogic +
      '}';
  }
}
