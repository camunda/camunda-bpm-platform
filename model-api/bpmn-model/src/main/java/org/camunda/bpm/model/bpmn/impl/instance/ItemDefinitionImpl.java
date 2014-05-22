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

package org.camunda.bpm.model.bpmn.impl.instance;

import org.camunda.bpm.model.bpmn.ItemKind;
import org.camunda.bpm.model.bpmn.impl.BpmnModelConstants;
import org.camunda.bpm.model.bpmn.instance.ItemDefinition;
import org.camunda.bpm.model.bpmn.instance.RootElement;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.attribute.Attribute;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.*;
import static org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * @author Sebastian Menski
 */
public class ItemDefinitionImpl extends RootElementImpl implements ItemDefinition {

  protected static Attribute<String> structureRefAttribute;
  protected static Attribute<Boolean> isCollectionAttribute;
  protected static Attribute<ItemKind> itemKindAttribute;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(ItemDefinition.class,BpmnModelConstants.BPMN_ELEMENT_ITEM_DEFINITION)
      .namespaceUri(BpmnModelConstants.BPMN20_NS)
      .extendsType(RootElement.class)
      .instanceProvider(new ModelTypeInstanceProvider<ItemDefinition>() {
        public ItemDefinition newInstance(ModelTypeInstanceContext instanceContext) {
          return new ItemDefinitionImpl(instanceContext);
        }
      });

    structureRefAttribute = typeBuilder.stringAttribute(BPMN_ATTRIBUTE_STRUCTURE_REF)
      .build();

    isCollectionAttribute = typeBuilder.booleanAttribute(BPMN_ATTRIBUTE_IS_COLLECTION)
      .defaultValue(false)
      .build();

    itemKindAttribute = typeBuilder.enumAttribute(BPMN_ATTRIBUTE_ITEM_KIND, ItemKind.class)
      .defaultValue(ItemKind.Information)
      .build();

    typeBuilder.build();
  }
  
  public ItemDefinitionImpl(ModelTypeInstanceContext context) {
    super(context);
  }

  public String getStructureRef() {
    return structureRefAttribute.getValue(this);
  }

  public void setStructureRef(String structureRef) {
    structureRefAttribute.setValue(this, structureRef);
  }

  public boolean isCollection() {
    return isCollectionAttribute.getValue(this);
  }

  public void setCollection(boolean isCollection) {
    isCollectionAttribute.setValue(this, isCollection);
  }

  public ItemKind getItemKind() {
    return itemKindAttribute.getValue(this);
  }

  public void setItemKind(ItemKind itemKind) {
    itemKindAttribute.setValue(this, itemKind);
  }
}
