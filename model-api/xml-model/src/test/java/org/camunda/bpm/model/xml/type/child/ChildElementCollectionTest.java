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

package org.camunda.bpm.model.xml.type.child;

import org.camunda.bpm.model.xml.Model;
import org.camunda.bpm.model.xml.ModelInstance;
import org.camunda.bpm.model.xml.UnsupportedModelOperationException;
import org.camunda.bpm.model.xml.impl.parser.AbstractModelParser;
import org.camunda.bpm.model.xml.impl.type.child.ChildElementCollectionImpl;
import org.camunda.bpm.model.xml.impl.type.child.ChildElementImpl;
import org.camunda.bpm.model.xml.testmodel.Gender;
import org.camunda.bpm.model.xml.testmodel.TestModelParser;
import org.camunda.bpm.model.xml.testmodel.TestModelTest;
import org.camunda.bpm.model.xml.testmodel.instance.*;
import org.camunda.bpm.model.xml.type.ModelElementType;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.junit.runners.Parameterized.Parameters;

/**
 * @author Sebastian Menski
 */
public class ChildElementCollectionTest extends TestModelTest {

  private Bird tweety;
  private Bird daffy;
  private Bird daisy;
  private Bird plucky;
  private Bird birdo;
  private ChildElement<FlightInstructor> flightInstructorChild;
  private ChildElementCollection<FlightPartnerRef> flightPartnerRefCollection;

  public ChildElementCollectionTest(ModelInstance testModelInstance, AbstractModelParser modelParser) {
    super(testModelInstance, modelParser);
  }

  @Parameters
  public static Collection<Object[]> models() {
    Object[][] models = {createModel(), parseModel(ChildElementCollectionTest.class)};
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
    Bird plucky = createBird(modelInstance, "plucky", Gender.Male);
    createBird(modelInstance, "birdo", Gender.Female);

    tweety.setFlightInstructor(daffy);
    tweety.getFlightPartnerRefs().add(daisy);
    tweety.getFlightPartnerRefs().add(plucky);

    return new Object[]{modelInstance, modelParser};
  }

  @Before
  public void copyModelInstance() {
    modelInstance = cloneModelInstance();

    tweety = (Bird) modelInstance.getModelElementById("tweety");
    daffy = (Bird) modelInstance.getModelElementById("daffy");
    daisy = (Bird) modelInstance.getModelElementById("daisy");
    plucky = (Bird) modelInstance.getModelElementById("plucky");
    birdo = (Bird) modelInstance.getModelElementById("birdo");

    flightInstructorChild = (ChildElement<FlightInstructor>) FlyingAnimal.flightInstructorChild.getReferenceSourceCollection();
    flightPartnerRefCollection = FlyingAnimal.flightPartnerRefsColl.getReferenceSourceCollection();
  }

  @Test
  public void testImmutable() {
    assertThat(flightInstructorChild.isImmutable()).isFalse();
    assertThat(flightPartnerRefCollection.isImmutable()).isFalse();

    ((ChildElementImpl<FlightInstructor>) flightInstructorChild).setImmutable();
    ((ChildElementCollectionImpl<FlightPartnerRef>) flightPartnerRefCollection).setImmutable();
    assertThat(flightInstructorChild.isImmutable()).isTrue();
    assertThat(flightPartnerRefCollection.isImmutable()).isTrue();

    ((ChildElementImpl<FlightInstructor>) flightInstructorChild).setMutable(true);
    ((ChildElementCollectionImpl<FlightPartnerRef>) flightPartnerRefCollection).setMutable(true);
    assertThat(flightInstructorChild.isImmutable()).isFalse();
    assertThat(flightPartnerRefCollection.isImmutable()).isFalse();
  }

  @Test
  public void testMinOccurs() {
    assertThat(flightInstructorChild.getMinOccurs()).isEqualTo(0);
    assertThat(flightPartnerRefCollection.getMinOccurs()).isEqualTo(0);
  }

  @Test
  public void testMaxOccurs() {
    assertThat(flightInstructorChild.getMaxOccurs()).isEqualTo(1);
    assertThat(flightPartnerRefCollection.getMaxOccurs()).isEqualTo(-1);
  }

  @Test
  public void testChildElementType() {
    Model model = modelInstance.getModel();
    ModelElementType flightInstructorType = modelInstance.getModel().getType(FlightInstructor.class);
    ModelElementType flightPartnerRefType = modelInstance.getModel().getType(FlightPartnerRef.class);

    assertThat(flightInstructorChild.getChildElementType(model)).isEqualTo(flightInstructorType);
    assertThat(flightPartnerRefCollection.getChildElementType(model)).isEqualTo(flightPartnerRefType);
  }

  @Test
  public void testParentElementType() {
    ModelElementType flyingAnimalType = modelInstance.getModel().getType(FlyingAnimal.class);

    assertThat(flightInstructorChild.getParentElementType()).isEqualTo(flyingAnimalType);
    assertThat(flightPartnerRefCollection.getParentElementType()).isEqualTo(flyingAnimalType);
  }

  @Test
  public void testGetChildElements() {
    assertThat(flightInstructorChild.get(tweety)).hasSize(1);
    assertThat(flightPartnerRefCollection.get(tweety)).hasSize(2);

    FlightInstructor flightInstructor = flightInstructorChild.getChild(tweety);
    assertThat(flightInstructor.getTextContent()).isEqualTo(daffy.getId());

    for (FlightPartnerRef flightPartnerRef : flightPartnerRefCollection.get(tweety)) {
      assertThat(flightPartnerRef.getTextContent()).isIn(daisy.getId(), plucky.getId());
    }
  }

  @Test
  public void testRemoveChildElements() {
    assertThat(flightInstructorChild.getChild(tweety)).isNotNull();
    assertThat(flightPartnerRefCollection.get(tweety)).isNotEmpty();

    flightInstructorChild.removeChild(tweety);
    flightPartnerRefCollection.get(tweety).clear();

    assertThat(flightInstructorChild.getChild(tweety)).isNull();
    assertThat(flightPartnerRefCollection.get(tweety)).isEmpty();
  }

  @Test
  public void testChildElementsCollection() {
    Collection<FlightPartnerRef> flightPartnerRefs = flightPartnerRefCollection.get(tweety);

    Iterator<FlightPartnerRef> iterator = flightPartnerRefs.iterator();
    FlightPartnerRef daisyRef = iterator.next();
    FlightPartnerRef pluckyRef = iterator.next();
    assertThat(daisyRef.getTextContent()).isEqualTo(daisy.getId());
    assertThat(pluckyRef.getTextContent()).isEqualTo(plucky.getId());

    FlightPartnerRef birdoRef = modelInstance.newInstance(FlightPartnerRef.class);
    birdoRef.setTextContent(birdo.getId());

    Collection<FlightPartnerRef> flightPartners = Arrays.asList(birdoRef, daisyRef, pluckyRef);

    // directly test collection methods and not use the appropriate assertion methods
    assertThat(flightPartnerRefs.size()).isEqualTo(2);
    assertThat(flightPartnerRefs.isEmpty()).isFalse();
    assertThat(flightPartnerRefs.contains(daisyRef));
    assertThat(flightPartnerRefs.toArray()).isEqualTo(new Object[]{daisyRef, pluckyRef});
    assertThat(flightPartnerRefs.toArray(new FlightPartnerRef[1])).isEqualTo(new FlightPartnerRef[]{daisyRef, pluckyRef});

    assertThat(flightPartnerRefs.add(birdoRef)).isTrue();
    assertThat(flightPartnerRefs)
      .hasSize(3)
      .containsOnly(birdoRef, daisyRef, pluckyRef);

    assertThat(flightPartnerRefs.remove(daisyRef)).isTrue();
    assertThat(flightPartnerRefs)
      .hasSize(2)
      .containsOnly(birdoRef, pluckyRef);

    assertThat(flightPartnerRefs.addAll(flightPartners)).isTrue();
    assertThat(flightPartnerRefs.containsAll(flightPartners)).isTrue();
    assertThat(flightPartnerRefs)
      .hasSize(3)
      .containsOnly(birdoRef, daisyRef, pluckyRef);

    assertThat(flightPartnerRefs.removeAll(flightPartners)).isTrue();
    assertThat(flightPartnerRefs).isEmpty();

    try {
      flightPartnerRefs.retainAll(flightPartners);
      fail("retainAll method is not implemented");
    }
    catch (Exception e) {
      assertThat(e).isInstanceOf(UnsupportedModelOperationException.class);
    }

    flightPartnerRefs.addAll(flightPartners);
    assertThat(flightPartnerRefs).isNotEmpty();
    flightPartnerRefs.clear();
    assertThat(flightPartnerRefs).isEmpty();
  }
}
