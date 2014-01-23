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
package org.camunda.bpm.model.xml.testmodel.instance;

import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelElementInstanceImpl;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.testmodel.Gender;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.attribute.Attribute;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;
import org.camunda.bpm.model.xml.type.reference.AttributeReference;
import org.camunda.bpm.model.xml.type.reference.ElementReferenceCollection;

import java.util.Collection;

import static org.camunda.bpm.model.xml.testmodel.TestModelConstants.*;

/**
 * @author Daniel Meyer
 *
 */
public abstract class Animal extends ModelElementInstanceImpl implements ModelElementInstance {

  private static Attribute<String> idAttr;
  private static Attribute<String> nameAttr;
  private static AttributeReference<Animal> fatherRef;
  private static AttributeReference<Animal> motherRef;
  private static Attribute<Boolean> isEndangeredAttr;
  private static Attribute<Gender> genderAttr;
  private static Attribute<Integer> ageAttr;
  private static ChildElementCollection<RelationshipDefinition> relationshipDefinitionsColl;
  private static ElementReferenceCollection<RelationshipDefinition, RelationshipDefinitionRef> relationshipDefinitionRefsColl;

  public static void registerType(ModelBuilder modelBuilder) {

    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(Animal.class, TYPE_NAME_ANIMAL)
      .namespaceUri(MODEL_NAMESPACE)
      .abstractType();

    idAttr = typeBuilder.stringAttribute(ATTRIBUTE_NAME_ID)
      .idAttribute()
      .build();

    nameAttr = typeBuilder.stringAttribute(ATTRIBUTE_NAME_NAME)
      .build();

    fatherRef = typeBuilder.stringAttribute(ATTRIBUTE_NAME_FATHER)
      .qNameAttributeReference(Animal.class)
      .build();

    motherRef = typeBuilder.stringAttribute(ATTRIBUTE_NAME_MOTHER)
      .idAttributeReference(Animal.class)
      .build();

    isEndangeredAttr = typeBuilder.booleanAttribute(ATTRIBUTE_NAME_IS_ENDANGERED)
      .defaultValue(false)
      .build();

    genderAttr = typeBuilder.enumAttribute(ATTRIBUTE_NAME_GENDER, Gender.class)
      .required()
      .build();

    ageAttr = typeBuilder.integerAttribute(ATTRIBUTE_NAME_AGE)
      .build();

    SequenceBuilder sequence = typeBuilder.sequence();

    relationshipDefinitionsColl = sequence.elementCollection(RelationshipDefinition.class)
      .build();

    relationshipDefinitionRefsColl = sequence.elementCollection(RelationshipDefinitionRef.class)
      .qNameElementReferenceCollection(RelationshipDefinition.class)
      .build();

    typeBuilder.build();
  }

  public Animal(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public String getId() {
    return idAttr.getValue(this);
  }

  public void setId(String id) {
    idAttr.setValue(this, id);
  }

  public String getName() {
    return nameAttr.getValue(this);
  }

  public void setName(String name) {
    nameAttr.setValue(this, name);
  }

  public Animal getFather() {
    return fatherRef.getReferenceTargetElement(this);
  }

  public void setFather(Animal father) {
    fatherRef.setReferenceTargetElement(this, father);
  }

  public Animal getMother() {
    return motherRef.getReferenceTargetElement(this);
  }

  public void setMother(Animal mother) {
    motherRef.setReferenceTargetElement(this, mother);
  }

  public Boolean isEndangered() {
    return isEndangeredAttr.getValue(this);
  }

  public void setIsEndangered(boolean isEndangered) {
    isEndangeredAttr.setValue(this, isEndangered);
  }

  public Gender getGender() {
    return genderAttr.getValue(this);
  }

  public void setGender(Gender gender) {
    genderAttr.setValue(this, gender);
  }

  public Integer getAge() {
    return ageAttr.getValue(this);
  }

  public void setAge(int age) {
    ageAttr.setValue(this, age);
  }

  public Collection<RelationshipDefinition> getRelationshipDefinitions() {
    return relationshipDefinitionsColl.get(this);
  }

  public Collection<RelationshipDefinition> getRelationshipDefinitionRefs() {
    return relationshipDefinitionRefsColl.getReferenceTargetElements(this);
  }

  public Collection<RelationshipDefinitionRef> getRelationshipDefinitionRefElements() {
    return relationshipDefinitionRefsColl.getReferenceSourceCollection().get(this);
  }

}
