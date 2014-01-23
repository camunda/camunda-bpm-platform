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
import org.camunda.bpm.model.xml.impl.parser.AbstractModelParser;
import org.camunda.bpm.model.xml.testmodel.Gender;
import org.camunda.bpm.model.xml.testmodel.TestModelConstants;
import org.camunda.bpm.model.xml.testmodel.TestModelParser;
import org.camunda.bpm.model.xml.testmodel.TestModelTest;
import org.junit.Before;
import org.junit.Test;

import javax.xml.XMLConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.runners.Parameterized.Parameters;

/**
 * @author Sebastian Menski
 */
public class FlyingAnimalTest extends TestModelTest {

  private FlyingAnimal tweety;
  private FlyingAnimal hedwig;
  private FlyingAnimal birdo;
  private FlyingAnimal plucky;
  private FlyingAnimal fiffy;
  private FlyingAnimal timmy;
  private FlyingAnimal daisy;


  public FlyingAnimalTest(ModelInstance modelInstance, AbstractModelParser modelParser) {
    super(modelInstance, modelParser);
  }

  @Parameters
  public static Collection<Object[]> models() {
    Object[][] models = new Object[][]{createModel(), parseModel(FlyingAnimalTest.class)};
    return Arrays.asList(models);
  }

  public static Object[] createModel() {
    TestModelParser modelParser = new TestModelParser();
    ModelInstance modelInstance = modelParser.getEmptyModel();

    Animals animals = modelInstance.newInstance(Animals.class);
    modelInstance.setDocumentElement(animals);

    // add a tns namespace prefix for QName testing
    animals.setAttributeValueNs("xmlns:tns", XMLConstants.XMLNS_ATTRIBUTE_NS_URI, TestModelConstants.MODEL_NAMESPACE, false);

    FlyingAnimal tweety = createBird(modelInstance, "tweety", Gender.Female);
    FlyingAnimal hedwig = createBird(modelInstance, "hedwig", Gender.Male);
    FlyingAnimal birdo = createBird(modelInstance, "birdo", Gender.Female);
    FlyingAnimal plucky = createBird(modelInstance, "plucky", Gender.Unknown);
    FlyingAnimal fiffy = createBird(modelInstance, "fiffy", Gender.Female);
    createBird(modelInstance, "timmy", Gender.Male);
    createBird(modelInstance, "daisy", Gender.Female);

    tweety.getFlightPartnerRefs().add(hedwig);
    tweety.getFlightPartnerRefs().add(birdo);
    tweety.getFlightPartnerRefs().add(plucky);
    tweety.getFlightPartnerRefs().add(fiffy);

    return new Object[]{modelInstance, modelParser};
  }

  @Before
  public void copyModelInstance() {
    modelInstance = cloneModelInstance();
    tweety = (FlyingAnimal) modelInstance.getModelElementById("tweety");
    hedwig = (FlyingAnimal) modelInstance.getModelElementById("hedwig");
    birdo = (FlyingAnimal) modelInstance.getModelElementById("birdo");
    plucky = (FlyingAnimal) modelInstance.getModelElementById("plucky");
    fiffy = (FlyingAnimal) modelInstance.getModelElementById("fiffy");
    timmy = (FlyingAnimal) modelInstance.getModelElementById("timmy");
    daisy = (FlyingAnimal) modelInstance.getModelElementById("daisy");
  }

  @Test
  public void testAddFlightPartnerRefsByHelper() {
    assertThat(tweety.getFlightPartnerRefs())
      .isNotEmpty()
      .hasSize(4)
      .containsOnly(hedwig, birdo, plucky, fiffy);

    tweety.getFlightPartnerRefs().add(timmy);
    tweety.getFlightPartnerRefs().add(daisy);

    assertThat(tweety.getFlightPartnerRefs())
      .isNotEmpty()
      .hasSize(6)
      .containsOnly(hedwig, birdo, plucky, fiffy, timmy, daisy);
  }

  @Test
  public void testUpdateFlightPartnerRefsByIdByHelper() {
    hedwig.setId("new-" + hedwig.getId());
    plucky.setId("new-" + plucky.getId());
    assertThat(tweety.getFlightPartnerRefs())
      .hasSize(4)
      .containsOnly(hedwig, birdo, plucky, fiffy);
  }

  @Test
  public void testUpdateFlightPartnerRefsByIdByAttributeName() {
    birdo.setAttributeValue("id", "new-" + birdo.getId(), true);
    fiffy.setAttributeValue("id", "new-" + fiffy.getId(), true);
    assertThat(tweety.getFlightPartnerRefs())
      .hasSize(4)
      .containsOnly(hedwig, birdo, plucky, fiffy);
  }

  @Test
  public void testUpdateFlightPartnerRefsByReplaceElements() {
    hedwig.replaceWithElement(timmy);
    plucky.replaceWithElement(daisy);
    assertThat(tweety.getFlightPartnerRefs())
      .hasSize(4)
      .containsOnly(birdo, fiffy, timmy ,daisy);
  }

  @Test
  public void testUpdateFlightPartnerRefsByRemoveElements() {
    tweety.getFlightPartnerRefs().remove(birdo);
    tweety.getFlightPartnerRefs().remove(fiffy);
    assertThat(tweety.getFlightPartnerRefs())
      .hasSize(2)
      .containsOnly(hedwig, plucky);
  }

  @Test
  public void testClearFlightPartnerRefs() {
    tweety.getFlightPartnerRefs().clear();
    assertThat(tweety.getFlightPartnerRefs()).isEmpty();
  }

  @Test
  public void testAddFlightPartnerRefElementsByHelper() {
    assertThat(tweety.getFlightPartnerRefElements())
      .isNotEmpty()
      .hasSize(4);

    FlightPartnerRef timmyFlightPartnerRef = modelInstance.newInstance(FlightPartnerRef.class);
    timmyFlightPartnerRef.setTextContent(timmy.getId());
    tweety.getFlightPartnerRefElements().add(timmyFlightPartnerRef);

    FlightPartnerRef daisyFlightPartnerRef = modelInstance.newInstance(FlightPartnerRef.class);
    daisyFlightPartnerRef.setTextContent(daisy.getId());
    tweety.getFlightPartnerRefElements().add(daisyFlightPartnerRef);

    assertThat(tweety.getFlightPartnerRefElements())
      .isNotEmpty()
      .hasSize(6)
      .contains(timmyFlightPartnerRef, daisyFlightPartnerRef);
  }

  @Test
  public void testFlightPartnerRefElementsByTextContent() {
    Collection<FlightPartnerRef> flightPartnerRefElements = tweety.getFlightPartnerRefElements();
    Collection<String> textContents = new ArrayList<String>();
    for (FlightPartnerRef flightPartnerRefElement : flightPartnerRefElements) {
      String textContent = flightPartnerRefElement.getTextContent();
      assertThat(textContent).isNotEmpty();
      textContents.add(textContent);
    }
    assertThat(textContents)
      .isNotEmpty()
      .hasSize(4)
      .containsOnly(hedwig.getId(), birdo.getId(), plucky.getId(), fiffy.getId());
  }

  @Test
  public void testUpdateFlightPartnerRefElementsByTextContent() {
    List<FlightPartnerRef> flightPartnerRefs = new ArrayList<FlightPartnerRef>(tweety.getFlightPartnerRefElements());

    flightPartnerRefs.get(0).setTextContent(timmy.getId());
    flightPartnerRefs.get(2).setTextContent(daisy.getId());

    assertThat(tweety.getFlightPartnerRefs())
      .hasSize(4)
      .containsOnly(birdo, fiffy, timmy, daisy);
  }

  @Test
  public void testUpdateFlightPartnerRefElementsByRemoveElements() {
    List<FlightPartnerRef> flightPartnerRefs = new ArrayList<FlightPartnerRef>(tweety.getFlightPartnerRefElements());
    tweety.getFlightPartnerRefElements().remove(flightPartnerRefs.get(1));
    tweety.getFlightPartnerRefElements().remove(flightPartnerRefs.get(3));
    assertThat(tweety.getFlightPartnerRefs())
      .hasSize(2)
      .containsOnly(hedwig, plucky);
  }

  @Test
  public void testClearFlightPartnerRefElements() {
    tweety.getFlightPartnerRefElements().clear();
    assertThat(tweety.getFlightPartnerRefElements()).isEmpty();

    // should not affect animals collection
    Animals animals = (Animals) modelInstance.getDocumentElement();
    assertThat(animals.getAnimals())
      .isNotEmpty()
      .hasSize(7);
  }

}
