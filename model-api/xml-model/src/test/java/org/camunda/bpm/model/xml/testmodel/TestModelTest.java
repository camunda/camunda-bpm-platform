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
import org.camunda.bpm.model.xml.impl.parser.AbstractModelParser;
import org.camunda.bpm.model.xml.testmodel.instance.*;
import org.junit.After;
import org.w3c.dom.Document;

import java.io.InputStream;

/**
 * @author Sebastian Menski
 */
public abstract class TestModelTest {

  private final ModelInstance testModelInstance;
  private final AbstractModelParser modelParser;

  // cloned model instance for every test method (see subclasses)
  protected ModelInstance modelInstance;

  public TestModelTest(final ModelInstance testModelInstance, final AbstractModelParser modelParser) {
    this.testModelInstance = testModelInstance;
    this.modelParser = modelParser;
  }

  public ModelInstance cloneModelInstance() {
    return (ModelInstance) ((ModelInstanceImpl) testModelInstance).clone();
  }

  protected static Object[] parseModel(Class<?> test) {
    TestModelParser modelParser = new TestModelParser();
    String testXml = test.getSimpleName() + ".xml";
    InputStream testXmlAsStream = test.getResourceAsStream(testXml);
    ModelInstance modelInstance = modelParser.parseModelFromStream(testXmlAsStream);
    return new Object[]{modelInstance, modelParser};
  }

  public Bird createBird(final String id, final Gender gender) {
    return createBird(testModelInstance, id, gender);
  }

  public static Bird createBird(final ModelInstance modelInstance, final String id, Gender gender) {
    Bird bird = modelInstance.newInstance(Bird.class);
    bird.setId(id);
    bird.setGender(gender);
    Animals animals = (Animals) modelInstance.getDocumentElement();
    animals.getAnimals().add(bird);
    return bird;
  }

  protected static RelationshipDefinition createRelationshipDefinition(final ModelInstance modelInstance, final Animal animalInRelationshipWith, final Class<? extends RelationshipDefinition> relationshipDefinitionClass) {
    RelationshipDefinition relationshipDefinition = modelInstance.newInstance(relationshipDefinitionClass);
    relationshipDefinition.setId("relationship-" + animalInRelationshipWith.getId());
    relationshipDefinition.setAnimal(animalInRelationshipWith);
    return relationshipDefinition;
  }

  public static void addRelationshipDefinition(final Animal animalWithRelationship, final RelationshipDefinition relationshipDefinition) {
    Animal animalInRelationshipWith = relationshipDefinition.getAnimal();
    relationshipDefinition.setId(animalWithRelationship.getId() + "-" + animalInRelationshipWith.getId());
    animalWithRelationship.getRelationshipDefinitions().add(relationshipDefinition);
  }

  @After
  public void validateModel() {
    Document document = ((ModelInstanceImpl) modelInstance).getDocument();
    modelParser.validateModel(document);
  }
}
