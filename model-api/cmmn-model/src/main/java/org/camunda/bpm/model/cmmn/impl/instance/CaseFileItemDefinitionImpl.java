/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.model.cmmn.impl.instance;

import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN11_NS;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_DEFINITION_TYPE;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_NAME;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_STRUCTURE_REF;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ELEMENT_CASE_FILE_ITEM_DEFINITION;

import java.util.Collection;

import org.camunda.bpm.model.cmmn.instance.CaseFileItemDefinition;
import org.camunda.bpm.model.cmmn.instance.CmmnElement;
import org.camunda.bpm.model.cmmn.instance.Property;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;
import org.camunda.bpm.model.xml.type.attribute.Attribute;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;

/**
 * @author Roman Smirnov
 *
 */
public class CaseFileItemDefinitionImpl extends CmmnElementImpl implements CaseFileItemDefinition {

  protected static Attribute<String> nameAttribute;
  protected static Attribute<String> definitionTypeAttribute;
  // structureRef should be a QName, but it is not clear
  // what kind of element the attribute value should reference,
  // that's why we use a simple String
  protected static Attribute<String> structureAttribute;

  // TODO: The Import does not have an id attribute!
//  protected static AttributeReference<Import> importRefAttribute;
  protected static ChildElementCollection<Property> propertyCollection;

  public CaseFileItemDefinitionImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public String getName() {
    return nameAttribute.getValue(this);
  }

  public void setName(String name) {
    nameAttribute.setValue(this, name);
  }

  public String getDefinitionType() {
    return definitionTypeAttribute.getValue(this);
  }

  public void setDefinitionType(String definitionType) {
    definitionTypeAttribute.setValue(this, definitionType);
  }

  public String getStructure() {
    return structureAttribute.getValue(this);
  }

  public void setStructure(String structureRef) {
    structureAttribute.setValue(this, structureRef);
  }

//  public Import getImport() {
//    return importRefAttribute.getReferenceTargetElement(this);
//  }
//
//  public void setImport(Import importRef) {
//    importRefAttribute.setReferenceTargetElement(this, importRef);
//  }

  public Collection<Property> getProperties() {
    return propertyCollection.get(this);
  }

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(CaseFileItemDefinition.class, CMMN_ELEMENT_CASE_FILE_ITEM_DEFINITION)
        .namespaceUri(CMMN11_NS)
        .extendsType(CmmnElement.class)
        .instanceProvider(new ModelTypeInstanceProvider<CaseFileItemDefinition>() {
          public CaseFileItemDefinition newInstance(ModelTypeInstanceContext instanceContext) {
            return new CaseFileItemDefinitionImpl(instanceContext);
          }
        });

    nameAttribute = typeBuilder.stringAttribute(CMMN_ATTRIBUTE_NAME)
        .build();

    definitionTypeAttribute = typeBuilder.stringAttribute(CMMN_ATTRIBUTE_DEFINITION_TYPE)
        .defaultValue("http://www.omg.org/spec/CMMN/DefinitionType/Unspecified")
        .build();

    structureAttribute = typeBuilder.stringAttribute(CMMN_ATTRIBUTE_STRUCTURE_REF)
        .build();

    // TODO: The Import does not have an id attribute!
    // importRefAttribute = typeBuilder.stringAttribute(CMMN_ATTRIBUTE_IMPORT_REF)
    //    .qNameAttributeReference(Import.class)
    //    .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    propertyCollection = sequenceBuilder.elementCollection(Property.class)
        .build();

    typeBuilder.build();
  }

}
