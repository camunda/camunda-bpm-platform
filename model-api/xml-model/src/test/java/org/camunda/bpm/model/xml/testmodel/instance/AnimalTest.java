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

package org.camunda.bpm.model.xml.testmodel.instance;

import org.camunda.bpm.model.xml.ModelInstance;
import org.camunda.bpm.model.xml.ModelValidationException;
import org.camunda.bpm.model.xml.impl.ModelInstanceImpl;
import org.camunda.bpm.model.xml.testmodel.Gender;
import org.camunda.bpm.model.xml.testmodel.TestModelParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import java.util.Collection;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Sebastian Menski
 */
public class AnimalTest {

  private TestModelParser modelParser;
  private ModelInstance modelInstance;
  private Animals animals;
  private Animal animal;

  public Bird createBird(String id, Gender gender) {
    Bird bird = modelInstance.newInstance(Bird.class);
    bird.setId(id);
    bird.setGender(gender);
    animals.getAnimals().add(bird);
    return bird;
  }

  public RelationshipDefinition addFriendRelationshipDefinition(Animal animalWithRelationship, Animal animalInRelationshipWith) {
    RelationshipDefinition friendRelationshipDefinition = createFriendRelationshipDefinition(animalInRelationshipWith);
    friendRelationshipDefinition.setId(animalWithRelationship.getId() + "-" + animalInRelationshipWith.getId());
    animalWithRelationship.getRelationshipDefinitions().add(friendRelationshipDefinition);
    return friendRelationshipDefinition;
  }

  public RelationshipDefinition createFriendRelationshipDefinition(Animal animalInRelationshipWith) {
    FriendRelationshipDefinition friendRelationshipDefinition = modelInstance.newInstance(FriendRelationshipDefinition.class);
    friendRelationshipDefinition.setId("friend-" + animalInRelationshipWith.getId());
    friendRelationshipDefinition.setAnimal(animalInRelationshipWith);
    return friendRelationshipDefinition;
  }

  public RelationshipDefinition addChildRelationshipDefinition(Animal animalWithRelationship, Animal animalInRelationshipWith) {
    ChildRelationshipDefinition childRelationshipDefinition = createChildRelationshipDefinition(animalInRelationshipWith);
    childRelationshipDefinition.setId(animalWithRelationship.getId() + "-" + animalInRelationshipWith.getId());
    animalWithRelationship.getRelationshipDefinitions().add(childRelationshipDefinition);
    return childRelationshipDefinition;
  }

  public ChildRelationshipDefinition createChildRelationshipDefinition(Animal animalInRelationshipWith) {
    ChildRelationshipDefinition childRelationshipDefinition = modelInstance.newInstance(ChildRelationshipDefinition.class);
    childRelationshipDefinition.setAnimal(animalInRelationshipWith);
    childRelationshipDefinition.setId("child-" + animalInRelationshipWith.getId());
    return childRelationshipDefinition;
  }

  @Before
  public void createModelWithOneBird() {
    modelParser = new TestModelParser();
    modelInstance = modelParser.getEmptyModel();

    animals = modelInstance.newInstance(Animals.class);
    modelInstance.setDocumentElement(animals);

    animal = createBird("tweety", Gender.Female);
  }

  @After
  public void validateModel() {
    Document document = ((ModelInstanceImpl) modelInstance).getDocument();
    modelParser.validateModel(document);
  }

  @Test
  public void testId() {
    assertThat(animal.getId()).isEqualTo("tweety");

    // set id by helper
    animal.setId("daisy");
    assertThat(animal.getId()).isEqualTo("daisy");

    // set id by name
    animal.setAttributeValue("id", "duffy", true);
    assertThat(animal.getId()).isEqualTo("duffy");

    // remove id
    animal.removeAttribute("id");
    assertThat(animal.getId()).isNull();
  }

  @Test
  public void testName() {
    assertThat(animal.getName()).isNull();

    // set name by helper
    animal.setName("tweety");
    assertThat(animal.getName()).isEqualTo("tweety");

    // set name by name
    animal.setAttributeValue("name", "daisy", false);
    assertThat(animal.getName()).isEqualTo("daisy");

    // remove name
    animal.removeAttribute("name");
    assertThat(animal.getName()).isNull();
  }

  @Test
  public void testFather() {
    assertThat(animal.getFather()).isNull();

    // create father and stepfather
    Animal father = createBird("daffy", Gender.Male);
    Animal stepfather = createBird("timmy", Gender.Male);

    // set father by helper
    animal.setFather(father);
    assertThat(animal.getFather()).isEqualTo(father);

    father.setId("changed-father");
    assertThat(animal.getFather()).isEqualTo(father);

    // set father by name with namespace
    animal.setAttributeValue("father", "tns:" + stepfather.getId(), false);
    assertThat(animal.getFather()).isEqualTo(stepfather);

    // replace father
    stepfather.replaceElement(father);
    assertThat(animal.getFather()).isEqualTo(father);

    // remove father
    animal.removeAttribute("father");
    assertThat(animal.getFather()).isNull();
  }

  @Test
  public void testMother() {
    assertThat(animal.getMother()).isNull();

    // create mother and stepmother
    Animal mother = createBird("fiffy", Gender.Female);
    Animal stepmother = createBird("birdo", Gender.Female);

    // set mother by helper
    animal.setMother(mother);
    assertThat(animal.getMother()).isEqualTo(mother);

    // set mother by name
    animal.setAttributeValue("mother", stepmother.getId(), false);
    assertThat(animal.getMother()).isEqualTo(stepmother);

    // replace mother
    stepmother.replaceElement(mother);
    assertThat(animal.getMother()).isEqualTo(mother);

    // remove mother
    animal.removeAttribute("mother");
    assertThat(animal.getMother()).isNull();
  }

  @Test
  public void testIsEndangered() {
    // default value of endangered is false
    assertThat(animal.isEndangered()).isFalse();

    // set isEndangered by helper
    animal.setIsEndangered(true);
    assertThat(animal.isEndangered()).isTrue();

    // set isEndangered by name
    animal.setAttributeValue("isEndangered", "false", false);
    assertThat(animal.isEndangered()).isFalse();

    // remove isEndangered
    animal.removeAttribute("isEndangered");
    assertThat(animal.isEndangered()).isFalse();
  }

  @Test
  public void testGender() {
    assertThat(animal.getGender()).isEqualTo(Gender.Female);

    // set gender by helper
    animal.setGender(Gender.Male);
    assertThat(animal.getGender()).isEqualTo(Gender.Male);

    // set gender by name
    animal.setAttributeValue("gender", Gender.Unknown.toString(), false);
    assertThat(animal.getGender()).isEqualTo(Gender.Unknown);

    // remove gender
    animal.removeAttribute("gender");
    assertThat(animal.getGender()).isNull();

    // gender is required, so the model is invalid without
    try {
      validateModel();
      fail("The model is invalid cause the gender of an animal is a required attribute.");
    }
    catch (Exception e) {
      assertThat(e).isInstanceOf(ModelValidationException.class);
    }

    // add gender to make model valid
    animal.setGender(Gender.Female);
  }

  @Test
  public void testAge() {
    assertThat(animal.getAge()).isNull();

    // set age by helper
    animal.setAge(13);
    assertThat(animal.getAge()).isEqualTo(13);

    // set age by name
    animal.setAttributeValue("age", "23", false);
    assertThat(animal.getAge()).isEqualTo(23);

    // remove age
    animal.removeAttribute("age");
    assertThat(animal.getAge()).isNull();
  }

  @Test
  public void testChildRelationshipDefinitions() {
    assertThat(animal.getRelationshipDefinitions()).isEmpty();

    // create some childs and friends
    Bird hedwig = createBird("hedwig", Gender.Male);
    Bird birdo = createBird("birdo", Gender.Female);
    Bird plucky = createBird("plucky", Gender.Unknown);
    Bird fiffy = createBird("fiffy", Gender.Female);
    Bird timmy = createBird("timmy", Gender.Male);
    Bird daisy = createBird("daisy", Gender.Female);

    // create and add some relationships
    RelationshipDefinition hedwigRelationship = addChildRelationshipDefinition(animal, hedwig);
    RelationshipDefinition birdoRelationship = addChildRelationshipDefinition(animal, birdo);
    RelationshipDefinition pluckyRelationship = addFriendRelationshipDefinition(animal, plucky);
    RelationshipDefinition fiffyRelationship = addFriendRelationshipDefinition(animal, fiffy);
    RelationshipDefinition timmyRelationship = createFriendRelationshipDefinition(timmy);
    RelationshipDefinition daisyRelationship = createChildRelationshipDefinition(daisy);

    assertThat(animal.getRelationshipDefinitions()).isNotEmpty();
    assertThat(animal.getRelationshipDefinitions()).hasSize(4);
    assertThat(animal.getRelationshipDefinitions()).containsOnly(hedwigRelationship, birdoRelationship, pluckyRelationship, fiffyRelationship);

    // set new id by setId
    hedwigRelationship.setId("child-relationship");
    pluckyRelationship.setId("friend-relationship");
    assertThat(animal.getRelationshipDefinitions()).hasSize(4);
    assertThat(animal.getRelationshipDefinitions()).containsOnly(hedwigRelationship, birdoRelationship, pluckyRelationship, fiffyRelationship);

    // set new id by setAttributeValue
    birdoRelationship.setAttributeValue("id", "birdo-relationship", true);
    fiffyRelationship.setAttributeValue("id", "fiffy-relationship", true);
    assertThat(animal.getRelationshipDefinitions()).hasSize(4);
    assertThat(animal.getRelationshipDefinitions()).containsOnly(hedwigRelationship, birdoRelationship, pluckyRelationship, fiffyRelationship);

    // replace element
    hedwigRelationship.replaceElement(timmyRelationship);
    pluckyRelationship.replaceElement(daisyRelationship);
    assertThat(animal.getRelationshipDefinitions()).hasSize(4);
    assertThat(animal.getRelationshipDefinitions()).containsOnly(birdoRelationship, fiffyRelationship, timmyRelationship, daisyRelationship);

    // remove element
    animal.getRelationshipDefinitions().remove(birdoRelationship);
    animal.getRelationshipDefinitions().remove(fiffyRelationship);
    assertThat(animal.getRelationshipDefinitions()).hasSize(2);
    assertThat(animal.getRelationshipDefinitions()).containsOnly(timmyRelationship, daisyRelationship);

    // clear collection
    animal.getRelationshipDefinitions().clear();
    assertThat(animal.getRelationshipDefinitions()).isEmpty();
  }

  @Test
  public void testRelationshipDefinitionRefs() throws Exception {
    assertThat(animal.getRelationshipDefinitionRefs()).isEmpty();

    // create some childs and friends
    Bird hedwig = createBird("hedwig", Gender.Male);
    Bird birdo = createBird("birdo", Gender.Female);
    Bird plucky = createBird("plucky", Gender.Unknown);
    Bird fiffy = createBird("fiffy", Gender.Female);
    Bird timmy = createBird("timmy", Gender.Male);
    Bird daisy = createBird("daisy", Gender.Female);

    // create and add some relationships
    RelationshipDefinition hedwigRelationship = addChildRelationshipDefinition(animal, hedwig);
    RelationshipDefinition birdoRelationship = addChildRelationshipDefinition(animal, birdo);
    RelationshipDefinition pluckyRelationship = addFriendRelationshipDefinition(animal, plucky);
    RelationshipDefinition fiffyRelationship = addFriendRelationshipDefinition(animal, fiffy);
    RelationshipDefinition timmyRelationship = createFriendRelationshipDefinition(timmy);
    RelationshipDefinition daisyRelationship = createChildRelationshipDefinition(daisy);

    animal.getRelationshipDefinitionRefs().add(hedwigRelationship);
    animal.getRelationshipDefinitionRefs().add(birdoRelationship);
    animal.getRelationshipDefinitionRefs().add(pluckyRelationship);
    animal.getRelationshipDefinitionRefs().add(fiffyRelationship);
    assertThat(animal.getRelationshipDefinitionRefs()).isNotEmpty();
    assertThat(animal.getRelationshipDefinitionRefs()).hasSize(4);
    assertThat(animal.getRelationshipDefinitionRefs()).containsOnly(hedwigRelationship, birdoRelationship, pluckyRelationship, fiffyRelationship);

    // set new id by setId
    hedwigRelationship.setId("child-relationship");
    pluckyRelationship.setId("friend-relationship");
    assertThat(animal.getRelationshipDefinitionRefs()).hasSize(4);
    assertThat(animal.getRelationshipDefinitionRefs()).containsOnly(hedwigRelationship, birdoRelationship, pluckyRelationship, fiffyRelationship);

    // set new id by setAttributeValue
    birdoRelationship.setAttributeValue("id", "birdo-relationship", true);
    fiffyRelationship.setAttributeValue("id", "fiffy-relationship", true);
    assertThat(animal.getRelationshipDefinitionRefs()).hasSize(4);
    assertThat(animal.getRelationshipDefinitionRefs()).containsOnly(hedwigRelationship, birdoRelationship, pluckyRelationship, fiffyRelationship);

    // replace element
    hedwigRelationship.replaceElement(timmyRelationship);
    pluckyRelationship.replaceElement(daisyRelationship);
    assertThat(animal.getRelationshipDefinitionRefs()).hasSize(4);
    assertThat(animal.getRelationshipDefinitionRefs()).containsOnly(birdoRelationship, fiffyRelationship, timmyRelationship, daisyRelationship);

    // remove element

    animal.getRelationshipDefinitions().remove(birdoRelationship);
    Collection<RelationshipDefinition> relationshipDefinitionRefs = animal.getRelationshipDefinitionRefs();
    assertThat(relationshipDefinitionRefs).hasSize(3);
    assertThat(animal.getRelationshipDefinitionRefs()).containsOnly(fiffyRelationship, timmyRelationship, daisyRelationship);

    // remove id attribute
    fiffyRelationship.removeAttribute("id");
    assertThat(animal.getRelationshipDefinitionRefs()).hasSize(2);
    assertThat(animal.getRelationshipDefinitionRefs()).containsOnly(timmyRelationship, daisyRelationship);

    // clear definitions collection
    animal.getRelationshipDefinitions().clear();
    assertThat(animal.getRelationshipDefinitionRefs()).isEmpty();

    // add again definitions
    animal.getRelationshipDefinitions().add(hedwigRelationship);
    animal.getRelationshipDefinitionRefs().add(hedwigRelationship);
    animal.getRelationshipDefinitions().add(pluckyRelationship);
    animal.getRelationshipDefinitionRefs().add(pluckyRelationship);
    assertThat(animal.getRelationshipDefinitionRefs()).hasSize(2);
    assertThat(animal.getRelationshipDefinitionRefs()).containsOnly(hedwigRelationship, pluckyRelationship);

    // clear refs collection
    animal.getRelationshipDefinitionRefs().clear();
    assertThat(animal.getRelationshipDefinitionRefs()).isEmpty();
    assertThat(animal.getRelationshipDefinitions()).hasSize(2);
  }

  @Test
  public void testRelationshipDefinitionRefElements() throws Exception {
    assertThat(animal.getRelationshipDefinitionRefElements()).isEmpty();

    // create some childs and friends
    Bird hedwig = createBird("hedwig", Gender.Male);
    Bird birdo = createBird("birdo", Gender.Female);
    Bird plucky = createBird("plucky", Gender.Unknown);
    Bird fiffy = createBird("fiffy", Gender.Female);
    Bird timmy = createBird("timmy", Gender.Male);

    // create and add some relationships
    RelationshipDefinition hedwigRelationship = addChildRelationshipDefinition(animal, hedwig);
    RelationshipDefinition birdoRelationship = addChildRelationshipDefinition(animal, birdo);
    RelationshipDefinition pluckyRelationship = addFriendRelationshipDefinition(animal, plucky);
    RelationshipDefinition fiffyRelationship = addFriendRelationshipDefinition(animal, fiffy);
    RelationshipDefinition timmyRelationship = addFriendRelationshipDefinition(animal, timmy);

    animal.getRelationshipDefinitionRefs().add(hedwigRelationship);
    animal.getRelationshipDefinitionRefs().add(birdoRelationship);
    animal.getRelationshipDefinitionRefs().add(pluckyRelationship);
    animal.getRelationshipDefinitionRefs().add(fiffyRelationship);
    assertThat(animal.getRelationshipDefinitionRefElements()).isNotEmpty();
    assertThat(animal.getRelationshipDefinitionRefElements()).hasSize(4);

    // test text content
    Collection<RelationshipDefinitionRef> relationshipDefinitionRefElements = animal.getRelationshipDefinitionRefElements();
    for (RelationshipDefinitionRef relationshipDefinitionRef : relationshipDefinitionRefElements) {
      assertThat(relationshipDefinitionRef.getTextContent()).isNotEmpty();
      assertThat(relationshipDefinitionRef.getTextContent()).contains(animal.getId());
    }

    // change text-content and add namespace prefix
    RelationshipDefinitionRef relationshipDefinitionRef = (RelationshipDefinitionRef) relationshipDefinitionRefElements.toArray()[0];
    relationshipDefinitionRef.setTextContent("tns:" + animal.getId() + "-" + timmy.getId());
    assertThat(animal.getRelationshipDefinitionRefs()).hasSize(4);
    assertThat(animal.getRelationshipDefinitionRefs()).containsOnly(birdoRelationship, pluckyRelationship, fiffyRelationship, timmyRelationship);

    // remove element
    animal.getRelationshipDefinitionRefElements().remove(relationshipDefinitionRef);
    assertThat(animal.getRelationshipDefinitionRefs()).hasSize(3);
    assertThat(animal.getRelationshipDefinitionRefs()).containsOnly(birdoRelationship, pluckyRelationship, fiffyRelationship);

    // clear elements
    animal.getRelationshipDefinitionRefElements().clear();
    assertThat(animal.getRelationshipDefinitionRefs()).isEmpty();
  }
}
