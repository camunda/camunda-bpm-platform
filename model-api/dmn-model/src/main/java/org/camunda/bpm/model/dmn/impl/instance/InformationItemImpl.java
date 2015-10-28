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
import static org.camunda.bpm.model.dmn.impl.DmnModelConstants.DMN_ELEMENT_INFORMATION_ITEM;

import org.camunda.bpm.model.dmn.instance.InformationItem;
import org.camunda.bpm.model.dmn.instance.ItemDefinition;
import org.camunda.bpm.model.dmn.instance.ItemDefinitionReference;
import org.camunda.bpm.model.dmn.instance.NamedDmnElement;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;
import org.camunda.bpm.model.xml.type.reference.ElementReference;

public class InformationItemImpl extends NamedDmnElementImpl implements InformationItem {

  protected static ElementReference<ItemDefinition, ItemDefinitionReference> itemDefinitionRef;

  public InformationItemImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public ItemDefinition getItemDefinition() {
    return itemDefinitionRef.getReferenceTargetElement(this);
  }

  public void setItemDefinition(ItemDefinition itemDefinition) {
    itemDefinitionRef.setReferenceTargetElement(this, itemDefinition);
  }

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(InformationItem.class, DMN_ELEMENT_INFORMATION_ITEM)
      .namespaceUri(DMN10_NS)
      .extendsType(NamedDmnElement.class)
      .instanceProvider(new ModelTypeInstanceProvider<InformationItem>() {
        public InformationItem newInstance(ModelTypeInstanceContext instanceContext) {
          return new InformationItemImpl(instanceContext);
        }
      });

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    itemDefinitionRef = sequenceBuilder.element(ItemDefinitionReference.class)
      .uriElementReference(ItemDefinition.class)
      .build();

    typeBuilder.build();
  }

}
