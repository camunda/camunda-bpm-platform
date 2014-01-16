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
package org.camunda.bpm.model.xml.testmodel;

import org.camunda.bpm.model.xml.ModelInstance;
import org.camunda.bpm.model.xml.impl.ModelInstanceImpl;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.testmodel.instance.Animals;
import org.camunda.bpm.model.xml.testmodel.instance.Bird;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Daniel Meyer
 *
 */
public class ModelElementInstanceTest {

  @Test
  public void shouldReturnTextContent() {
    ModelInstance modelInstance = new TestModelParser()
      .parseModelFromStream(getClass().getResourceAsStream("ModelElementInstanceTest.textContent.xml"));

    ModelElementInstance tweety = modelInstance.getModelElementById("tweety");
    assertThat(tweety.getTextContent()).isEqualTo("");

    ModelElementInstance donald = modelInstance.getModelElementById("donald");
    assertThat(donald.getTextContent()).isEqualTo("some text content");

    ModelElementInstance daisy = modelInstance.getModelElementById("daisy");
    assertThat(daisy.getTextContent()).isEqualTo("some text content with outer line breaks");

    ModelElementInstance hedwig = modelInstance.getModelElementById("hedwig");
    assertThat(hedwig.getTextContent()).isEqualTo("some text content with inner\n        line breaks");
  }

  @Test
  public void shouldReturnRawTextContent() {
    ModelInstance modelInstance = new TestModelParser()
      .parseModelFromStream(getClass().getResourceAsStream("ModelElementInstanceTest.textContent.xml"));

    ModelElementInstance tweety = modelInstance.getModelElementById("tweety");
    assertThat(tweety.getRawTextContent()).isEqualTo("");

    ModelElementInstance donald = modelInstance.getModelElementById("donald");
    assertThat(donald.getRawTextContent()).isEqualTo("some text content");

    ModelElementInstance daisy = modelInstance.getModelElementById("daisy");
    assertThat(daisy.getRawTextContent()).isEqualTo("\n        some text content with outer line breaks\n    ");

    ModelElementInstance hedwig = modelInstance.getModelElementById("hedwig");
    assertThat(hedwig.getRawTextContent()).isEqualTo("\n        some text content with inner\n        line breaks\n    ");
  }

  private static Bird createBird(ModelInstance modelInstance, String id, Gender gender) {
    Bird bird = modelInstance.newInstance(Bird.class);
    bird.setId(id);
    bird.setGender(gender);
    ((Animals) modelInstance.getDocumentElement()).getAnimals().add(bird);
    return bird;
  }

  @Test
  public void shouldUpdateReference() {
    TestModelParser modelParser = new TestModelParser();
    ModelInstance modelInstance = modelParser.getEmptyModel();
    Animals animals = modelInstance.newInstance(Animals.class);
    modelInstance.setDocumentElement(animals);

    // create some birds
    Bird tweety = createBird(modelInstance, "tweety", Gender.Female);
    Bird fiffy = createBird(modelInstance, "fiffy", Gender.Female);
    Bird daffy = createBird(modelInstance, "daffy", Gender.Male);
    Bird timmy = createBird(modelInstance, "timmy", Gender.Unknown);

    // set references
    assertThat(tweety.getMother()).isNull();
    tweety.setMother(fiffy);
    assertThat(tweety.getMother()).isNotNull();
    assertThat(tweety.getMother()).isEqualTo(fiffy);

    assertThat(tweety.getFather()).isNull();
    tweety.setFather(daffy);
    assertThat(tweety.getFather()).isNotNull();
    assertThat(tweety.getFather()).isEqualTo(daffy);

    // change ids
    fiffy.setId("mother");
    assertThat(tweety.getMother()).isEqualTo(fiffy);

    daffy.setId("father");
    assertThat(tweety.getFather()).isEqualTo(daffy);

    // replace birds
    fiffy.replaceElement(timmy);
    assertThat(tweety.getMother()).isEqualTo(timmy);
    timmy.replaceElement(fiffy);
    assertThat(tweety.getMother()).isEqualTo(fiffy);

    daffy.replaceElement(timmy);
    assertThat(tweety.getFather()).isEqualTo(timmy);
    timmy.replaceElement(daffy);
    assertThat(tweety.getFather()).isEqualTo(daffy);

    // remove id
    fiffy.removeAttribute("id");
    assertThat(tweety.getMother()).isNull();

    daffy.removeAttribute("id");
    assertThat(tweety.getFather()).isNull();

    // validate model
    modelParser.validateModel(((ModelInstanceImpl) modelInstance).getDocument());
  }

}
