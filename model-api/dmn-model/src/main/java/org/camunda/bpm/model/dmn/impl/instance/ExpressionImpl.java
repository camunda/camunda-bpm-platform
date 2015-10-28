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

package org.camunda.bpm.model.dmn.impl.instance;

import static org.camunda.bpm.model.dmn.impl.DmnModelConstants.DMN10_NS;
import static org.camunda.bpm.model.dmn.impl.DmnModelConstants.DMN_ELEMENT_EXPRESSION;

import java.util.Collection;

import org.camunda.bpm.model.dmn.instance.DmnElement;
import org.camunda.bpm.model.dmn.instance.Expression;
import org.camunda.bpm.model.dmn.instance.InformationItem;
import org.camunda.bpm.model.dmn.instance.InputVariableReference;
import org.camunda.bpm.model.dmn.instance.ItemDefinition;
import org.camunda.bpm.model.dmn.instance.ItemDefinitionReference;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;
import org.camunda.bpm.model.xml.type.reference.ElementReference;
import org.camunda.bpm.model.xml.type.reference.ElementReferenceCollection;

public abstract class ExpressionImpl extends DmnElementImpl implements Expression {

  protected static ElementReferenceCollection<InformationItem, InputVariableReference> inputVariableRefCollection;
  protected static ElementReference<ItemDefinition, ItemDefinitionReference> itemDefinitionRef;

  public ExpressionImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public Collection<InformationItem> getInputVariables() {
    return inputVariableRefCollection.getReferenceTargetElements(this);
  }

  public ItemDefinition getItemDefinition() {
    return itemDefinitionRef.getReferenceTargetElement(this);
  }

  public void setItemDefinition(ItemDefinition itemDefinition) {
    itemDefinitionRef.setReferenceTargetElement(this, itemDefinition);
  }

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(Expression.class, DMN_ELEMENT_EXPRESSION)
      .namespaceUri(DMN10_NS)
      .extendsType(DmnElement.class)
      .abstractType();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    inputVariableRefCollection = sequenceBuilder.elementCollection(InputVariableReference.class)
      .idElementReferenceCollection(InformationItem.class)
      .build();

    itemDefinitionRef = sequenceBuilder.element(ItemDefinitionReference.class)
      .uriElementReference(ItemDefinition.class)
      .build();

    typeBuilder.build();
  }

}
