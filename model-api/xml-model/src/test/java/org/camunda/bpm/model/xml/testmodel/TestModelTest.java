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

package org.camunda.bpm.model.xml.testmodel;

import org.camunda.bpm.model.xml.ModelInstance;
import org.camunda.bpm.model.xml.impl.ModelInstanceImpl;
import org.camunda.bpm.model.xml.testmodel.instance.Animal;
import org.camunda.bpm.model.xml.testmodel.instance.Animals;
import org.camunda.bpm.model.xml.testmodel.instance.Bird;
import org.camunda.bpm.model.xml.testmodel.instance.RelationshipDefinition;
import org.junit.After;
import org.w3c.dom.Document;

import javax.xml.XMLConstants;

/**
 * @author Sebastian Menski
 */
public abstract class TestModelTest {

  protected TestModelParser modelParser;
  protected ModelInstance modelInstance;
  protected Animals animals;
  protected Animal animal;

  public Bird createBird(String id, Gender gender) {
    Bird bird = modelInstance.newInstance(Bird.class);
    bird.setId(id);
    bird.setGender(gender);
    animals.getAnimals().add(bird);
    return bird;
  }

  protected RelationshipDefinition createRelationshipDefinition(final Animal animalInRelationshipWith, final Class<? extends RelationshipDefinition> relationshipDefinitionClass) {
    RelationshipDefinition relationshipDefinition = modelInstance.newInstance(relationshipDefinitionClass);
    relationshipDefinition.setId("relationship-" + animalInRelationshipWith.getId());
    relationshipDefinition.setAnimal(animalInRelationshipWith);
    return relationshipDefinition;
  }

  public void addRelationshipDefinition(final Animal animalWithRelationship, final RelationshipDefinition relationshipDefinition) {
    Animal animalInRelationshipWith = relationshipDefinition.getAnimal();
    relationshipDefinition.setId(animalWithRelationship.getId() + "-" + animalInRelationshipWith.getId());
    animalWithRelationship.getRelationshipDefinitions().add(relationshipDefinition);
  }

  public void createTestModel() {
    modelParser = new TestModelParser();
    modelInstance = modelParser.getEmptyModel();

    animals = modelInstance.newInstance(Animals.class);
    modelInstance.setDocumentElement(animals);

    // add a tns namespace prefix for QName testing
    animals.setAttributeValueNs("xmlns:tns", XMLConstants.XMLNS_ATTRIBUTE_NS_URI, TestModelConstants.MODEL_NAMESPACE, false);

    // create the test animal
    animal = createBird("tweety", Gender.Female);
  }

  @After
  public void validateModel() {
    Document document = ((ModelInstanceImpl) modelInstance).getDocument();
    modelParser.validateModel(document);
  }
}
