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
import org.camunda.bpm.model.xml.testmodel.TestModelConstants;
import org.camunda.bpm.model.xml.testmodel.TestModelParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.XMLConstants;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
  private Animal hedwig;
  private Animal birdo;
  private Animal plucky;
  private Animal fiffy;
  private Animal timmy;
  private Animal daisy;
  private RelationshipDefinition hedwigRelationship;
  private RelationshipDefinition birdoRelationship;
  private RelationshipDefinition pluckyRelationship;
  private RelationshipDefinition fiffyRelationship;
  private RelationshipDefinition timmyRelationship;
  private RelationshipDefinition daisyRelationship;


  public Bird createBird(String id, Gender gender) {
    Bird bird = modelInstance.newInstance(Bird.class);
    bird.setId(id);
    bird.setGender(gender);
    animals.getAnimals().add(bird);
    return bird;
  }

  private RelationshipDefinition createRelationshipDefinition(final Animal animalInRelationshipWith, final Class<? extends RelationshipDefinition> relationshipDefinitionClass) {
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

  @Before
  public void createModel() {
    modelParser = new TestModelParser();
    modelInstance = modelParser.getEmptyModel();

    animals = modelInstance.newInstance(Animals.class);
    modelInstance.setDocumentElement(animals);

    // add a tns namespace prefix for QName testing
    animals.setAttributeValueNs("xmlns:tns", XMLConstants.XMLNS_ATTRIBUTE_NS_URI, TestModelConstants.MODEL_NAMESPACE, false);

    // create the test animal
    animal = createBird("tweety", Gender.Female);

    // create some childs and friends
    hedwig = createBird("hedwig", Gender.Male);
    birdo = createBird("birdo", Gender.Female);
    plucky = createBird("plucky", Gender.Unknown);
    fiffy = createBird("fiffy", Gender.Female);
    timmy = createBird("timmy", Gender.Male);
    daisy = createBird("daisy", Gender.Female);

    // create and add some relationships
    hedwigRelationship = createRelationshipDefinition(hedwig, ChildRelationshipDefinition.class);
    addRelationshipDefinition(animal, hedwigRelationship);
    birdoRelationship = createRelationshipDefinition(birdo, ChildRelationshipDefinition.class);
    addRelationshipDefinition(animal, birdoRelationship);
    pluckyRelationship = createRelationshipDefinition(plucky, FriendRelationshipDefinition.class);
    addRelationshipDefinition(animal, pluckyRelationship);
    fiffyRelationship = createRelationshipDefinition(fiffy, FriendRelationshipDefinition.class);
    addRelationshipDefinition(animal, fiffyRelationship);
    timmyRelationship = createRelationshipDefinition(timmy, FriendRelationshipDefinition.class);
    daisyRelationship = createRelationshipDefinition(daisy, ChildRelationshipDefinition.class);

    animal.getRelationshipDefinitionRefs().add(hedwigRelationship);
    animal.getRelationshipDefinitionRefs().add(birdoRelationship);
    animal.getRelationshipDefinitionRefs().add(pluckyRelationship);
    animal.getRelationshipDefinitionRefs().add(fiffyRelationship);
  }

  @After
  public void validateModel() {
    Document document = ((ModelInstanceImpl) modelInstance).getDocument();
    modelParser.validateModel(document);
  }

  @Test
  public void testSetIdAttributeByHelper() {
    animal.setId("new-animal-id");
    assertThat(animal.getId()).isEqualTo("new-animal-id");
  }

  @Test
  public void testSetIdAttributeByAttributeName() {
    animal.setAttributeValue("id", "duffy", true);
    assertThat(animal.getId()).isEqualTo("duffy");
  }

  @Test
  public void testRemoveIdAttribute() {
    animal.removeAttribute("id");
    assertThat(animal.getId()).isNull();
  }

  @Test
  public void testSetNameAttributeByHelper() {
    animal.setName("tweety");
    assertThat(animal.getName()).isEqualTo("tweety");
  }

  @Test
  public void testSetNameAttributeByAttributeName() {
    animal.setAttributeValue("name", "daisy", false);
    assertThat(animal.getName()).isEqualTo("daisy");
  }

  @Test
  public void testRemoveNameAttribute() {
    animal.removeAttribute("name");
    assertThat(animal.getName()).isNull();
  }

  @Test
  public void testSetFatherAttributeByHelper() {
    animal.setFather(timmy);
    assertThat(animal.getFather()).isEqualTo(timmy);
  }

  @Test
  public void testSetFatherAttributeByAttributeName() {
    animal.setAttributeValue("father", timmy.getId(), false);
    assertThat(animal.getFather()).isEqualTo(timmy);
  }

  @Test
  public void testSetFatherAttributeByAttributeNameWithNamespace() {
    animal.setAttributeValue("father", "tns:hedwig", false);
    assertThat(animal.getFather()).isEqualTo(hedwig);
  }

  @Test
  public void testRemoveFatherAttribute() {
    animal.setFather(timmy);
    assertThat(animal.getFather()).isEqualTo(timmy);
    animal.removeAttribute("father");
    assertThat(animal.getFather()).isNull();
  }

  @Test
  public void testChangeIdAttributeOfFatherReference() {
    animal.setFather(timmy);
    assertThat(animal.getFather()).isEqualTo(timmy);
    timmy.setId("new-" + timmy.getId());
    assertThat(animal.getFather()).isEqualTo(timmy);
  }

  @Test
  public void testReplaceFatherReferenceWithNewAnimal() {
    animal.setFather(timmy);
    assertThat(animal.getFather()).isEqualTo(timmy);
    timmy.replaceWithElement(plucky);
    assertThat(animal.getFather()).isEqualTo(plucky);
  }

  @Test
  public void testSetMotherAttributeByHelper() {
    animal.setMother(daisy);
    assertThat(animal.getMother()).isEqualTo(daisy);
  }

  @Test
  public void testSetMotherAttributeByAttributeName() {
    animal.setAttributeValue("mother", fiffy.getId(), false);
    assertThat(animal.getMother()).isEqualTo(fiffy);
  }

  @Test
  public void testRemoveMotherAttribute() {
    animal.setMother(daisy);
    assertThat(animal.getMother()).isEqualTo(daisy);
    animal.removeAttribute("mother");
    assertThat(animal.getMother()).isNull();
  }

  @Test
  public void testReplaceMotherReferenceWithNewAnimal() {
    animal.setMother(daisy);
    assertThat(animal.getMother()).isEqualTo(daisy);
    daisy.replaceWithElement(birdo);
    assertThat(animal.getMother()).isEqualTo(birdo);
  }

  @Test
  public void testChangeIdAttributeOfMotherReference() {
    animal.setMother(daisy);
    assertThat(animal.getMother()).isEqualTo(daisy);
    daisy.setId("new-" + daisy.getId());
    assertThat(animal.getMother()).isEqualTo(daisy);
  }

  @Test
  public void testSetIsEndangeredAttributeByHelper() {
    animal.setIsEndangered(true);
    assertThat(animal.isEndangered()).isTrue();
  }

  @Test
  public void testSetIsEndangeredAttributeByAttributeName() {
    animal.setAttributeValue("isEndangered", "false", false);
    assertThat(animal.isEndangered()).isFalse();
  }

  @Test
  public void testRemoveIsEndangeredAttribute() {
    animal.removeAttribute("isEndangered");
    // default value of isEndangered: false
    assertThat(animal.isEndangered()).isFalse();
  }

  @Test
  public void testSetGenderAttributeByHelper() {
    animal.setGender(Gender.Male);
    assertThat(animal.getGender()).isEqualTo(Gender.Male);
  }

  @Test
  public void testSetGenderAttributeByAttributeName() {
    animal.setAttributeValue("gender", Gender.Unknown.toString(), false);
    assertThat(animal.getGender()).isEqualTo(Gender.Unknown);
  }

  @Test
  public void testRemoveGenderAttribute() {
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
  public void testSetAgeAttributeByHelper() {
    animal.setAge(13);
    assertThat(animal.getAge()).isEqualTo(13);
  }

  @Test
  public void testSetAgeAttributeByAttributeName() {
    animal.setAttributeValue("age", "23", false);
    assertThat(animal.getAge()).isEqualTo(23);
  }

  @Test
  public void testRemoveAgeAttribute() {
    animal.removeAttribute("age");
    assertThat(animal.getAge()).isNull();
  }

  @Test
  public void testAddRelationshipDefinitionsByHelper() {
    assertThat(animal.getRelationshipDefinitions())
      .isNotEmpty()
      .hasSize(4)
      .containsOnly(hedwigRelationship, birdoRelationship, pluckyRelationship, fiffyRelationship);

    animal.getRelationshipDefinitions().add(timmyRelationship);
    animal.getRelationshipDefinitions().add(daisyRelationship);

    assertThat(animal.getRelationshipDefinitions())
      .hasSize(6)
      .containsOnly(hedwigRelationship, birdoRelationship, pluckyRelationship, fiffyRelationship, timmyRelationship, daisyRelationship);
  }

  @Test
  public void testUpdateRelationshipDefinitionsByIdByHelper() {
    hedwigRelationship.setId("new-" + hedwigRelationship.getId());
    pluckyRelationship.setId("new-" + pluckyRelationship.getId());
    assertThat(animal.getRelationshipDefinitions())
      .hasSize(4)
      .containsOnly(hedwigRelationship, birdoRelationship, pluckyRelationship, fiffyRelationship);
  }

  @Test
  public void testUpdateRelationshipDefinitionsByIdByAttributeName() {
    birdoRelationship.setAttributeValue("id", "new-" + birdoRelationship.getId(), true);
    fiffyRelationship.setAttributeValue("id", "new-" + fiffyRelationship.getId(), true);
    assertThat(animal.getRelationshipDefinitions())
      .hasSize(4)
      .containsOnly(hedwigRelationship, birdoRelationship, pluckyRelationship, fiffyRelationship);
  }

  @Test
  public void testUpdateRelationshipDefinitionsByReplaceElements() {
    hedwigRelationship.replaceWithElement(timmyRelationship);
    pluckyRelationship.replaceWithElement(daisyRelationship);
    assertThat(animal.getRelationshipDefinitions())
      .hasSize(4)
      .containsOnly(birdoRelationship, fiffyRelationship, timmyRelationship, daisyRelationship);
  }

  @Test
  public void testUpdateRelationshipDefinitionsByRemoveElements() {
    animal.getRelationshipDefinitions().remove(birdoRelationship);
    animal.getRelationshipDefinitions().remove(fiffyRelationship);
    assertThat(animal.getRelationshipDefinitions())
      .hasSize(2)
      .containsOnly(hedwigRelationship, pluckyRelationship);
  }

  @Test
  public void testClearRelationshipDefinitions() {
    animal.getRelationshipDefinitions().clear();
    assertThat(animal.getRelationshipDefinitions()).isEmpty();
  }

  @Test
  public void testAddRelationsDefinitionRefsByHelper() {
    addRelationshipDefinition(animal, timmyRelationship);
    addRelationshipDefinition(animal, daisyRelationship);
    animal.getRelationshipDefinitionRefs().add(timmyRelationship);
    animal.getRelationshipDefinitionRefs().add(daisyRelationship);
    assertThat(animal.getRelationshipDefinitionRefs())
      .isNotEmpty()
      .hasSize(6)
      .containsOnly(hedwigRelationship, birdoRelationship, pluckyRelationship, fiffyRelationship, timmyRelationship, daisyRelationship);
  }

  @Test
  public void testUpdateRelationshipDefinitionRefsByIdByHelper() {
    hedwigRelationship.setId("child-relationship");
    pluckyRelationship.setId("friend-relationship");
    assertThat(animal.getRelationshipDefinitionRefs())
      .hasSize(4)
      .containsOnly(hedwigRelationship, birdoRelationship, pluckyRelationship, fiffyRelationship);
  }

  @Test
  public void testUpdateRelationshipDefinitionRefsByIdByAttributeName() {
    birdoRelationship.setAttributeValue("id", "birdo-relationship", true);
    fiffyRelationship.setAttributeValue("id", "fiffy-relationship", true);
    assertThat(animal.getRelationshipDefinitionRefs())
      .hasSize(4)
      .containsOnly(hedwigRelationship, birdoRelationship, pluckyRelationship, fiffyRelationship);
  }

  @Test
  public void testUpdateRelationshipDefinitionRefsByReplaceElements() {
    hedwigRelationship.replaceWithElement(timmyRelationship);
    pluckyRelationship.replaceWithElement(daisyRelationship);
    assertThat(animal.getRelationshipDefinitionRefs())
      .hasSize(4)
      .containsOnly(birdoRelationship, fiffyRelationship, timmyRelationship, daisyRelationship);
  }

  @Test
  public void testUpdateRelationshipDefinitionRefsByRemoveElements() {
    animal.getRelationshipDefinitions().remove(birdoRelationship);
    animal.getRelationshipDefinitions().remove(fiffyRelationship);
    assertThat(animal.getRelationshipDefinitionRefs())
      .hasSize(2)
      .containsOnly(hedwigRelationship, pluckyRelationship);
  }

  @Test
  public void testUpdateRelationshipDefinitionRefsByRemoveIdAttribute() {
    birdoRelationship.removeAttribute("id");
    pluckyRelationship.removeAttribute("id");
    assertThat(animal.getRelationshipDefinitionRefs())
      .hasSize(2)
      .containsOnly(hedwigRelationship, fiffyRelationship);
  }

  @Test
  public void testClearRelationshipDefinitionsRefs() {
    animal.getRelationshipDefinitionRefs().clear();
    assertThat(animal.getRelationshipDefinitionRefs()).isEmpty();
    // should not affect animal relationship definitions
    assertThat(animal.getRelationshipDefinitions()).hasSize(4);
  }

  @Test
  public void testClearRelationshipDefinitionRefsByClearRelationshipDefinitions() {
    assertThat(animal.getRelationshipDefinitionRefs()).isNotEmpty();
    animal.getRelationshipDefinitions().clear();
    assertThat(animal.getRelationshipDefinitions()).isEmpty();
    // should affect animal relationship definition refs
    assertThat(animal.getRelationshipDefinitionRefs()).isEmpty();
  }

  @Test
  public void testAddRelationshipDefinitionRefElementsByHelper() {
    addRelationshipDefinition(animal, timmyRelationship);
    RelationshipDefinitionRef relationshipDefinitionRef = modelInstance.newInstance(RelationshipDefinitionRef.class);
    relationshipDefinitionRef.setTextContent(timmyRelationship.getId());
    animal.getRelationshipDefinitionRefElements().add(relationshipDefinitionRef);

    assertThat(animal.getRelationshipDefinitionRefElements())
      .isNotEmpty()
      .hasSize(5);
  }

  @Test
  public void testRelationshipDefinitionRefElementsByTextContent() {
    Collection<RelationshipDefinitionRef> relationshipDefinitionRefElements = animal.getRelationshipDefinitionRefElements();
    Collection<String> textContents = new ArrayList<String>();
    for (RelationshipDefinitionRef relationshipDefinitionRef : relationshipDefinitionRefElements) {
      String textContent = relationshipDefinitionRef.getTextContent();
      assertThat(textContent).isNotEmpty();
      textContents.add(textContent);
    }
    assertThat(textContents)
      .isNotEmpty()
      .hasSize(4)
      .containsOnly(hedwigRelationship.getId(), birdoRelationship.getId(), pluckyRelationship.getId(), fiffyRelationship.getId());
  }

  @Test
  public void testUpdateRelationshipDefinitionRefElementsByTextContent() {
    addRelationshipDefinition(animal, timmyRelationship);
    RelationshipDefinitionRef relationshipDefinitionRef = (RelationshipDefinitionRef) animal.getRelationshipDefinitionRefElements().toArray()[0];
    relationshipDefinitionRef.setTextContent(timmyRelationship.getId());
    assertThat(animal.getRelationshipDefinitionRefs())
      .hasSize(4)
      .containsOnly(birdoRelationship, pluckyRelationship, fiffyRelationship, timmyRelationship);
  }

  @Test
  public void testUpdateRelationshipDefinitionRefElementsByTextContentWithNamespace() {
    addRelationshipDefinition(animal, timmyRelationship);
    RelationshipDefinitionRef relationshipDefinitionRef = (RelationshipDefinitionRef) animal.getRelationshipDefinitionRefElements().toArray()[0];
    relationshipDefinitionRef.setTextContent("tns:" + timmyRelationship.getId());
    assertThat(animal.getRelationshipDefinitionRefs())
      .hasSize(4)
      .containsOnly(birdoRelationship, pluckyRelationship, fiffyRelationship, timmyRelationship);
  }

  @Test
  public void testUpdateRelationshipDefinitionRefElementsByRemoveElements() {
    List<RelationshipDefinitionRef> relationshipDefinitionRefElements = new ArrayList<RelationshipDefinitionRef>(animal.getRelationshipDefinitionRefElements());
    animal.getRelationshipDefinitionRefElements().remove(relationshipDefinitionRefElements.get(0));
    animal.getRelationshipDefinitionRefElements().remove(relationshipDefinitionRefElements.get(2));
    assertThat(animal.getRelationshipDefinitionRefs())
      .hasSize(2)
      .containsOnly(birdoRelationship, fiffyRelationship);
  }

  @Test
  public void testClearRelationshipDefinitionRefElements() {
    animal.getRelationshipDefinitionRefElements().clear();
    assertThat(animal.getRelationshipDefinitionRefElements()).isEmpty();
    assertThat(animal.getRelationshipDefinitionRefs()).isEmpty();
    // should not affect animal relationship definitions
    assertThat(animal.getRelationshipDefinitions())
      .isNotEmpty()
      .hasSize(4);
  }

  @Test
  public void testClearRelationshipDefinitionRefElementsByClearRelationshipDefinitionRefs() {
    animal.getRelationshipDefinitionRefs().clear();
    assertThat(animal.getRelationshipDefinitionRefs()).isEmpty();
    assertThat(animal.getRelationshipDefinitionRefElements()).isEmpty();
    // should not affect animal relationship definitions
    assertThat(animal.getRelationshipDefinitions())
      .isNotEmpty()
      .hasSize(4);
  }

  @Test
  public void testClearRelationshipDefinitionRefElementsByClearRelationshipDefinitions() {
    animal.getRelationshipDefinitions().clear();
    assertThat(animal.getRelationshipDefinitionRefs()).isEmpty();
    assertThat(animal.getRelationshipDefinitionRefElements()).isEmpty();
    // should affect animal relationship definitions
    assertThat(animal.getRelationshipDefinitions()).isEmpty();
  }
}
