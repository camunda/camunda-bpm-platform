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
import static org.camunda.bpm.model.dmn.impl.DmnModelConstants.DMN_ATTRIBUTE_NAME;
import static org.camunda.bpm.model.dmn.impl.DmnModelConstants.DMN_ELEMENT_ITEM_COMPONENT;

import org.camunda.bpm.model.dmn.instance.ItemComponent;
import org.camunda.bpm.model.dmn.instance.ItemDefinition;
import org.camunda.bpm.model.dmn.instance.ItemDefinitionReference;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;
import org.camunda.bpm.model.xml.type.attribute.Attribute;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;
import org.camunda.bpm.model.xml.type.reference.ElementReference;

public class ItemComponentImpl extends DmnModelElementInstanceImpl implements ItemComponent {

  protected static Attribute<String> nameAttribute;
  protected static ElementReference<ItemDefinition, ItemDefinitionReference> itemDefinitionRef;

  public ItemComponentImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public String getName() {
    return nameAttribute.getValue(this);
  }

  public void setName(String name) {
    nameAttribute.setValue(this, name);
  }

  public ItemDefinition getItemDefinition() {
    return itemDefinitionRef.getReferenceTargetElement(this);
  }

  public void setItemDefinition(ItemDefinition itemDefinition) {
    itemDefinitionRef.setReferenceTargetElement(this, itemDefinition);
  }

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(ItemComponent.class, DMN_ELEMENT_ITEM_COMPONENT)
      .namespaceUri(DMN10_NS)
      .instanceProvider(new ModelTypeInstanceProvider<ItemComponent>() {
        public ItemComponent newInstance(ModelTypeInstanceContext instanceContext) {
          return new ItemComponentImpl(instanceContext);
        }
      });

    nameAttribute = typeBuilder.stringAttribute(DMN_ATTRIBUTE_NAME)
      .required()
      .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    itemDefinitionRef = sequenceBuilder.element(ItemDefinitionReference.class)
      .required()
      .uriElementReference(ItemDefinition.class)
      .build();

    typeBuilder.build();
  }

}
