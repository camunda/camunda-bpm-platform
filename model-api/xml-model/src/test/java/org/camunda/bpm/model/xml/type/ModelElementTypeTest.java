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

package org.camunda.bpm.model.xml.type;

import org.camunda.bpm.model.xml.Model;
import org.camunda.bpm.model.xml.ModelInstance;
import org.camunda.bpm.model.xml.impl.util.ModelTypeException;
import org.camunda.bpm.model.xml.testmodel.TestModelParser;
import org.camunda.bpm.model.xml.testmodel.instance.*;
import org.junit.Before;
import org.junit.Test;

import static org.camunda.bpm.model.xml.testmodel.TestModelConstants.MODEL_NAMESPACE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Sebastian Menski
 */
public class ModelElementTypeTest {

  private ModelInstance modelInstance;
  private Model model;
  private ModelElementType animalsType;
  private ModelElementType animalType;
  private ModelElementType flyingAnimalType;
  private ModelElementType birdType;

  @Before
  public void getTypes() {
    TestModelParser modelParser = new TestModelParser();
    modelInstance = modelParser.getEmptyModel();
    model = modelInstance.getModel();
    animalsType = model.getType(Animals.class);
    animalType = model.getType(Animal.class);
    flyingAnimalType = model.getType(FlyingAnimal.class);
    birdType = model.getType(Bird.class);
  }

  @Test
  public void testTypeName() {
    assertThat(animalsType.getTypeName()).isEqualTo("animals");
    assertThat(animalType.getTypeName()).isEqualTo("animal");
    assertThat(flyingAnimalType.getTypeName()).isEqualTo("flyingAnimal");
    assertThat(birdType.getTypeName()).isEqualTo("bird");
  }

  @Test
  public void testTypeNamespace() {
    assertThat(animalsType.getTypeNamespace()).isEqualTo(MODEL_NAMESPACE);
    assertThat(animalType.getTypeNamespace()).isEqualTo(MODEL_NAMESPACE);
    assertThat(flyingAnimalType.getTypeNamespace()).isEqualTo(MODEL_NAMESPACE);
    assertThat(birdType.getTypeNamespace()).isEqualTo(MODEL_NAMESPACE);
  }

  @Test
  public void testInstanceType() {
    assertThat(animalsType.getInstanceType()).isEqualTo(Animals.class);
    assertThat(animalType.getInstanceType()).isEqualTo(Animal.class);
    assertThat(flyingAnimalType.getInstanceType()).isEqualTo(FlyingAnimal.class);
    assertThat(birdType.getInstanceType()).isEqualTo(Bird.class);
  }

  @Test
  public void testNumberOfAttributes() {
    assertThat(animalsType.getAttributes()).hasSize(0);
    assertThat(animalType.getAttributes()).hasSize(7);
    assertThat(flyingAnimalType.getAttributes()).hasSize(0);
    assertThat(birdType.getAttributes()).hasSize(0);
  }

  @Test
  public void testBaseType() {
    assertThat(animalsType.getBaseType()).isNull();
    assertThat(animalType.getBaseType()).isNull();
    assertThat(flyingAnimalType.getBaseType()).isEqualTo(animalType);
    assertThat(birdType.getBaseType()).isEqualTo(flyingAnimalType);
  }

  @Test
  public void testAbstractType() {
    assertThat(animalsType.isAbstract()).isFalse();
    assertThat(animalType.isAbstract()).isTrue();
    assertThat(flyingAnimalType.isAbstract()).isTrue();
    assertThat(birdType.isAbstract()).isFalse();
  }

  @Test
  public void testExtendingTypes() {
    assertThat(animalsType.getExtendingTypes()).isEmpty();
    assertThat(animalType.getExtendingTypes())
      .contains(flyingAnimalType)
      .doesNotContain(birdType);
    assertThat(flyingAnimalType.getExtendingTypes()).contains(birdType);
    assertThat(birdType.getExtendingTypes()).isEmpty();
  }

  @Test
  public void testModel() {
    assertThat(animalsType.getModel()).isEqualTo(model);
    assertThat(animalType.getModel()).isEqualTo(model);
    assertThat(flyingAnimalType.getModel()).isEqualTo(model);
    assertThat(birdType.getModel()).isEqualTo(model);
  }

  @Test
  public void testInstances() {
    assertThat(animalsType.getInstances(modelInstance)).isEmpty();
    assertThat(animalType.getInstances(modelInstance)).isEmpty();
    assertThat(flyingAnimalType.getInstances(modelInstance)).isEmpty();
    assertThat(birdType.getInstances(modelInstance)).isEmpty();

    Animals animals = (Animals) animalsType.newInstance(modelInstance);
    modelInstance.setDocumentElement(animals);

    try {
      animalType.newInstance(modelInstance);
      fail("Animal is a abstract type and not instance can be created.");
    }
    catch (Exception e) {
      assertThat(e).isInstanceOf(ModelTypeException.class);
    }

    try {
      flyingAnimalType.newInstance(modelInstance);
      fail("Flying animal is a abstract type and not instance can be created.");
    }
    catch (Exception e) {
      assertThat(e).isInstanceOf(ModelTypeException.class);
    }

    animals.getAnimals().add((Animal) birdType.newInstance(modelInstance));
    animals.getAnimals().add((Animal) birdType.newInstance(modelInstance));
    animals.getAnimals().add((Animal) birdType.newInstance(modelInstance));

    assertThat(animalsType.getInstances(modelInstance)).hasSize(1);
    assertThat(animalType.getInstances(modelInstance)).isEmpty();
    assertThat(flyingAnimalType.getInstances(modelInstance)).isEmpty();
    assertThat(birdType.getInstances(modelInstance)).hasSize(3);
  }

  @Test
  public void testChildElementTypes() {
    ModelElementType relationshipDefinitionType = model.getType(RelationshipDefinition.class);
    ModelElementType relationshipDefinitionRefType = model.getType(RelationshipDefinitionRef.class);
    ModelElementType flightPartnerRefType = model.getType(FlightPartnerRef.class);
    ModelElementType eggType = model.getType(Egg.class);
    ModelElementType spouseRefType = model.getType(SpouseRef.class);

    assertThat(animalsType.getChildElementTypes())
      .containsSequence(animalType);
    assertThat(animalType.getChildElementTypes())
      .containsSequence(relationshipDefinitionType, relationshipDefinitionRefType);
    assertThat(flyingAnimalType.getChildElementTypes())
      .containsSequence(flightPartnerRefType);
    assertThat(birdType.getChildElementTypes())
      .containsSequence(eggType, spouseRefType);
  }

}
