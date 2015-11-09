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

import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN10_NS;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN11_NS;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_DEFINITION_REF;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_MULTIPLICITY;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_NAME;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_SOURCE_REF;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_SOURCE_REFS;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_TARGET_REFS;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ELEMENT_CASE_FILE_ITEM;

import java.util.Collection;

import org.camunda.bpm.model.cmmn.MultiplicityEnum;
import org.camunda.bpm.model.cmmn.instance.CaseFileItem;
import org.camunda.bpm.model.cmmn.instance.CaseFileItemDefinition;
import org.camunda.bpm.model.cmmn.instance.Children;
import org.camunda.bpm.model.cmmn.instance.CmmnElement;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;
import org.camunda.bpm.model.xml.type.attribute.Attribute;
import org.camunda.bpm.model.xml.type.child.ChildElement;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;
import org.camunda.bpm.model.xml.type.reference.AttributeReference;
import org.camunda.bpm.model.xml.type.reference.AttributeReferenceCollection;

/**
 * @author Roman Smirnov
 *
 */
public class CaseFileItemImpl extends CmmnElementImpl implements CaseFileItem {

  protected static Attribute<String> nameAttribute;
  protected static Attribute<MultiplicityEnum> multiplicityAttribute;
  protected static AttributeReference<CaseFileItemDefinition> definitionRefAttribute;
  protected static AttributeReferenceCollection<CaseFileItem> targetRefCollection;
  protected static ChildElement<Children> childrenChild;

  // cmmn 1.0
  @Deprecated
  protected static AttributeReference<CaseFileItem> sourceRefAttribute;

//  cmmn 1.1
  protected static AttributeReferenceCollection<CaseFileItem> sourceRefCollection;



  public CaseFileItemImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public String getName() {
    return nameAttribute.getValue(this);
  }

  public void setName(String name) {
    nameAttribute.setValue(this, name);
  }

  public MultiplicityEnum getMultiplicity() {
    return multiplicityAttribute.getValue(this);
  }

  public void setMultiplicity(MultiplicityEnum multiplicity) {
    multiplicityAttribute.setValue(this, multiplicity);
  }

  public CaseFileItemDefinition getDefinitionRef() {
    return definitionRefAttribute.getReferenceTargetElement(this);
  }

  public void setDefinitionRef(CaseFileItemDefinition caseFileItemDefinition) {
    definitionRefAttribute.setReferenceTargetElement(this, caseFileItemDefinition);
  }

  public CaseFileItem getSourceRef() {
    return sourceRefAttribute.getReferenceTargetElement(this);
  }

  public void setSourceRef(CaseFileItem sourceRef) {
    sourceRefAttribute.setReferenceTargetElement(this, sourceRef);
  }

  public Collection<CaseFileItem> getSourceRefs() {
    return sourceRefCollection.getReferenceTargetElements(this);
  }

  public Collection<CaseFileItem> getTargetRefs() {
    return targetRefCollection.getReferenceTargetElements(this);
  }

  public Children getChildren() {
    return childrenChild.getChild(this);
  }

  public void setChildren(Children children) {
    childrenChild.setChild(this, children);
  }

  public static void registerType(ModelBuilder modelBuilder) {

    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(CaseFileItem.class, CMMN_ELEMENT_CASE_FILE_ITEM)
        .namespaceUri(CMMN11_NS)
        .extendsType(CmmnElement.class)
        .instanceProvider(new ModelTypeInstanceProvider<CaseFileItem>() {
          public CaseFileItem newInstance(ModelTypeInstanceContext instanceContext) {
            return new CaseFileItemImpl(instanceContext);
          }
        });

    nameAttribute = typeBuilder.stringAttribute(CMMN_ATTRIBUTE_NAME)
        .build();

    multiplicityAttribute = typeBuilder.enumAttribute(CMMN_ATTRIBUTE_MULTIPLICITY, MultiplicityEnum.class)
        .defaultValue(MultiplicityEnum.Unspecified)
        .build();

    definitionRefAttribute = typeBuilder.stringAttribute(CMMN_ATTRIBUTE_DEFINITION_REF)
        .qNameAttributeReference(CaseFileItemDefinition.class)
        .build();

    sourceRefAttribute = typeBuilder.stringAttribute(CMMN_ATTRIBUTE_SOURCE_REF)
        .namespace(CMMN10_NS)
        .idAttributeReference(CaseFileItem.class)
        .build();

    sourceRefCollection = typeBuilder.stringAttribute(CMMN_ATTRIBUTE_SOURCE_REFS)
        .idAttributeReferenceCollection(CaseFileItem.class, CmmnAttributeElementReferenceCollection.class)
        .build();

    targetRefCollection = typeBuilder.stringAttribute(CMMN_ATTRIBUTE_TARGET_REFS)
        .idAttributeReferenceCollection(CaseFileItem.class, CmmnAttributeElementReferenceCollection.class)
        .build();

    SequenceBuilder sequence = typeBuilder.sequence();

    childrenChild = sequence.element(Children.class)
      .build();

    typeBuilder.build();
  }

}
