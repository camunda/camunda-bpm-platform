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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionModel;
import org.camunda.bpm.dmn.engine.DmnItemDefinition;

public class DmnDecisionModelImpl extends DmnElementImpl implements DmnDecisionModel {

  protected String expressionLanguage;
  protected String typeLanguage;
  protected String namespace;

  protected Map<String, DmnItemDefinition> itemDefinitions = new HashMap<String, DmnItemDefinition>();
  protected Map<String, DmnDecision> decisions = new HashMap<String, DmnDecision>();

  public String getExpressionLanguage() {
    return expressionLanguage;
  }

  public void setExpressionLanguage(String expressionLanguage) {
    this.expressionLanguage = expressionLanguage;
  }

  public String getTypeLanguage() {
    return typeLanguage;
  }

  public void setTypeLanguage(String typeLanguage) {
    this.typeLanguage = typeLanguage;
  }

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public List<DmnItemDefinition> getItemDefinitions() {
    return new ArrayList<DmnItemDefinition>(itemDefinitions.values());
  }

  public void setItemDefinitions(Collection<DmnItemDefinition> itemDefinitions) {
    this.itemDefinitions.clear();
    for (DmnItemDefinition itemDefinition : itemDefinitions) {
      addItemDefinition(itemDefinition);
    }
  }

  public void addItemDefinition(DmnItemDefinition itemDefinition) {
    itemDefinitions.put(itemDefinition.getKey(), itemDefinition);
  }

  public DmnItemDefinition getItemDefinition(String itemDefinitionKey) {
    return itemDefinitions.get(itemDefinitionKey);
  }

  @SuppressWarnings("unchecked")
  public <T extends DmnDecision> List<T> getDecisions() {
    return new ArrayList<T>((Collection<T>) decisions.values());
  }

  @SuppressWarnings("unchecked")
  public <T extends DmnDecision> T getDecision(String decisionKey) {
    return (T) decisions.get(decisionKey);
  }

  public void setDecisions(Collection<DmnDecision> decisions) {
    this.decisions.clear();
    for (DmnDecision decision : decisions) {
      addDecision(decision);
    }
  }

  public void addDecision(DmnDecision decision) {
    this.decisions.put(decision.getKey(), decision);
  }

  public String toString() {
    return "DmnDecisionModelImpl{" +
      "key='" + key + '\'' +
      ", name='" + name + '\'' +
      ", expressionLanguage='" + expressionLanguage + '\'' +
      ", typeLanguage='" + typeLanguage + '\'' +
      ", namespace='" + namespace + '\'' +
      ", itemDefinitions=" + itemDefinitions +
      ", decisions=" + decisions +
      '}';
  }

}
