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

package org.camunda.bpm.model.xml.type.reference;

import org.camunda.bpm.model.xml.ModelInstance;
import org.camunda.bpm.model.xml.UnsupportedModelOperationException;
import org.camunda.bpm.model.xml.impl.parser.AbstractModelParser;
import org.camunda.bpm.model.xml.impl.type.reference.AttributeReferenceImpl;
import org.camunda.bpm.model.xml.impl.type.reference.QNameAttributeReferenceImpl;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.testmodel.Gender;
import org.camunda.bpm.model.xml.testmodel.TestModelParser;
import org.camunda.bpm.model.xml.testmodel.TestModelTest;
import org.camunda.bpm.model.xml.testmodel.instance.*;
import org.camunda.bpm.model.xml.type.ModelElementType;
import org.camunda.bpm.model.xml.type.attribute.Attribute;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.junit.runners.Parameterized.Parameters;

/**
 * @author Sebastian Menski
 */
public class ReferenceTest extends TestModelTest {

  private Bird tweety;
  private Bird daffy;
  private Bird daisy;
  private Bird plucky;
  private Bird birdo;
  private FlightPartnerRef flightPartnerRef;

  private ModelElementType animalType;
  private QNameAttributeReferenceImpl<Animal> fatherReference;
  private AttributeReferenceImpl<Animal> motherReference;
  private ElementReferenceCollection<FlyingAnimal, FlightPartnerRef> flightPartnerRefsColl;

  public ReferenceTest(final ModelInstance testModelInstance, final AbstractModelParser modelParser) {
    super(testModelInstance, modelParser);
  }

  @Parameters
  public static Collection<Object[]> models() {
    Object[][] models = new Object[][]{createModel(), parseModel(ReferenceTest.class)};
    return Arrays.asList(models);
  }

  public static Object[] createModel() {
    TestModelParser modelParser = new TestModelParser();
    ModelInstance modelInstance = modelParser.getEmptyModel();

    Animals animals = modelInstance.newInstance(Animals.class);
    modelInstance.setDocumentElement(animals);

    Bird tweety = createBird(modelInstance, "tweety", Gender.Female);
    Bird daffy = createBird(modelInstance, "daffy", Gender.Male);
    Bird daisy = createBird(modelInstance, "daisy", Gender.Female);
    createBird(modelInstance, "plucky", Gender.Male);
    createBird(modelInstance, "birdo", Gender.Female);
    tweety.setFather(daffy);
    tweety.setMother(daisy);

    tweety.getFlightPartnerRefs().add(daffy);

    return new Object[]{modelInstance, modelParser};
  }

  @Before
  @SuppressWarnings("unchecked")
  public void copyModelInstance() {
    modelInstance = cloneModelInstance();

    tweety = (Bird) modelInstance.getModelElementById("tweety");
    daffy = (Bird) modelInstance.getModelElementById("daffy");
    daisy = (Bird) modelInstance.getModelElementById("daisy");
    plucky = (Bird) modelInstance.getModelElementById("plucky");
    birdo = (Bird) modelInstance.getModelElementById("birdo");

    animalType = modelInstance.getModel().getType(Animal.class);

    // QName attribute reference
    fatherReference = (QNameAttributeReferenceImpl<Animal>) animalType.getAttribute("father").getOutgoingReferences().iterator().next();

    // ID attribute reference
    motherReference = (AttributeReferenceImpl<Animal>) animalType.getAttribute("mother").getOutgoingReferences().iterator().next();

    // ID element reference
    flightPartnerRefsColl = FlyingAnimal.flightPartnerRefsColl;

    ModelElementType flightPartnerRefType = modelInstance.getModel().getType(FlightPartnerRef.class);
    flightPartnerRef = (FlightPartnerRef) modelInstance.getModelElementsByType(flightPartnerRefType).iterator().next();
  }

  @Test
  public void testReferenceIdentifier() {
    assertThat(fatherReference.getReferenceIdentifier(tweety)).isEqualTo(daffy.getId());
    assertThat(motherReference.getReferenceIdentifier(tweety)).isEqualTo(daisy.getId());
    assertThat(flightPartnerRefsColl.getReferenceIdentifier(flightPartnerRef)).isEqualTo(daffy.getId());
  }

  @Test
  public void testReferenceTargetElement() {
    assertThat(fatherReference.getReferenceTargetElement(tweety)).isEqualTo(daffy);
    assertThat(motherReference.getReferenceTargetElement(tweety)).isEqualTo(daisy);
    assertThat(flightPartnerRefsColl.getReferenceTargetElement(flightPartnerRef)).isEqualTo(daffy);

    fatherReference.setReferenceTargetElement(tweety, plucky);
    motherReference.setReferenceTargetElement(tweety, birdo);
    flightPartnerRefsColl.setReferenceTargetElement(flightPartnerRef, daisy);

    assertThat(fatherReference.getReferenceTargetElement(tweety)).isEqualTo(plucky);
    assertThat(motherReference.getReferenceTargetElement(tweety)).isEqualTo(birdo);
    assertThat(flightPartnerRefsColl.getReferenceTargetElement(flightPartnerRef)).isEqualTo(daisy);
  }

  @Test
  public void testReferenceTargetAttribute() {
    Attribute<?> idAttribute = animalType.getAttribute("id");
    assertThat(idAttribute.getIncomingReferences()).contains(fatherReference, motherReference);

    assertThat(fatherReference.getReferenceTargetAttribute()).isEqualTo(idAttribute);
    assertThat(motherReference.getReferenceTargetAttribute()).isEqualTo(idAttribute);
    assertThat(flightPartnerRefsColl.getReferenceTargetAttribute()).isEqualTo(idAttribute);
  }

  @Test
  public void testReferenceSourceAttribute() {
    Attribute<?> fatherAttribute = animalType.getAttribute("father");
    Attribute<?> motherAttribute = animalType.getAttribute("mother");

    assertThat(fatherReference.getReferenceSourceAttribute()).isEqualTo(fatherAttribute);
    assertThat(motherReference.getReferenceSourceAttribute()).isEqualTo(motherAttribute);
  }

  @Test
  public void testRemoveReference() {
    fatherReference.referencedElementRemoved(daffy, daffy.getId());
    assertThat(fatherReference.getReferenceTargetElement(tweety)).isNull();
    assertThat(tweety.getFather()).isNull();
    motherReference.referencedElementRemoved(daisy, daisy.getId());
    assertThat(motherReference.getReferenceTargetElement(tweety)).isNull();
    assertThat(tweety.getMother()).isNull();
  }

  @Test
  public void testTargetElementsCollection() {
    Collection<FlyingAnimal> referenceTargetElements = flightPartnerRefsColl.getReferenceTargetElements(tweety);
    Collection<FlyingAnimal> flightPartners = Arrays.asList(new FlyingAnimal[]{birdo, daffy, daisy, plucky});

    // directly test collection methods and not use the	appropriate assertion methods
    assertThat(referenceTargetElements.size()).isEqualTo(1);
    assertThat(referenceTargetElements.isEmpty()).isFalse();
    assertThat(referenceTargetElements.contains(daffy)).isTrue();
    assertThat(referenceTargetElements.toArray()).isEqualTo(new Object[]{daffy});
    assertThat(referenceTargetElements.toArray(new FlyingAnimal[1])).isEqualTo(new FlyingAnimal[]{daffy});

    assertThat(referenceTargetElements.add(daisy)).isTrue();
    assertThat(referenceTargetElements)
      .hasSize(2)
      .containsOnly(daffy, daisy);

    assertThat(referenceTargetElements.remove(daisy)).isTrue();
    assertThat(referenceTargetElements)
      .hasSize(1)
      .containsOnly(daffy);

    assertThat(referenceTargetElements.addAll(flightPartners)).isTrue();
    assertThat(referenceTargetElements.containsAll(flightPartners)).isTrue();
    assertThat(referenceTargetElements)
      .hasSize(4)
      .containsOnly(daffy, daisy, plucky, birdo);

    assertThat(referenceTargetElements.removeAll(flightPartners)).isTrue();
    assertThat(referenceTargetElements).isEmpty();

    try {
      referenceTargetElements.retainAll(flightPartners);
      fail("retainAll method is not implemented");
    }
    catch (Exception e) {
      assertThat(e).isInstanceOf(UnsupportedModelOperationException.class);
    }

    referenceTargetElements.addAll(flightPartners);
    assertThat(referenceTargetElements).isNotEmpty();
    referenceTargetElements.clear();
    assertThat(referenceTargetElements).isEmpty();
  }

}
