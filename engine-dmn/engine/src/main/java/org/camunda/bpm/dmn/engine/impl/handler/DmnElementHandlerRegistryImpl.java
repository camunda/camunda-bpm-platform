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

package org.camunda.bpm.dmn.engine.impl.handler;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.model.dmn.instance.AllowedValue;
import org.camunda.bpm.model.dmn.instance.Clause;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Definitions;
import org.camunda.bpm.model.dmn.instance.DmnModelElementInstance;
import org.camunda.bpm.model.dmn.instance.InputEntry;
import org.camunda.bpm.model.dmn.instance.InputExpression;
import org.camunda.bpm.model.dmn.instance.ItemDefinition;
import org.camunda.bpm.model.dmn.instance.OutputEntry;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.camunda.bpm.model.dmn.instance.TypeDefinition;
import org.camunda.bpm.dmn.engine.handler.DmnElementHandler;
import org.camunda.bpm.dmn.engine.handler.DmnElementHandlerRegistry;

public class DmnElementHandlerRegistryImpl implements DmnElementHandlerRegistry {

  protected Map<Class<? extends DmnModelElementInstance>, DmnElementHandler> elementHandlers = new HashMap<Class<? extends DmnModelElementInstance>, DmnElementHandler>();

  public DmnElementHandlerRegistryImpl() {
    elementHandlers.put(Definitions.class, new DmnDefinitionsHandler());
    elementHandlers.put(ItemDefinition.class, new DmnItemDefinitionHandler());
    elementHandlers.put(TypeDefinition.class, new DmnTypeDefinitionHandler());
    elementHandlers.put(AllowedValue.class, new DmnAllowValueHandler());
    elementHandlers.put(DecisionTable.class, new DmnDecisionTableHandler());
    elementHandlers.put(Clause.class, new DmnClauseHandler());
    elementHandlers.put(InputExpression.class, new DmnInputExpressionHandler());
    elementHandlers.put(InputEntry.class, new DmnInputEntryHandler());
    elementHandlers.put(OutputEntry.class, new DmnOutputEntryHandler());
    elementHandlers.put(Rule.class, new DmnRuleHandler());
  }

  public Map<Class<? extends DmnModelElementInstance>, DmnElementHandler> getElementHandlers() {
    return elementHandlers;
  }

  public void setElementHandlers(Map<Class<? extends DmnModelElementInstance>, DmnElementHandler> elementHandlers) {
    this.elementHandlers = elementHandlers;
  }

  @SuppressWarnings("unchecked")
  public <Target extends DmnModelElementInstance, Source> DmnElementHandler<Target, Source> getElementHandler(Class<Target> elementClass) {
    return elementHandlers.get(elementClass);
  }

}
