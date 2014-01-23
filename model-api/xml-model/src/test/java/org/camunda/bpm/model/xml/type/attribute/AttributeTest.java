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

package org.camunda.bpm.model.xml.type.attribute;

import org.camunda.bpm.model.xml.ModelInstance;
import org.camunda.bpm.model.xml.impl.parser.AbstractModelParser;
import org.camunda.bpm.model.xml.impl.type.attribute.AttributeImpl;
import org.camunda.bpm.model.xml.testmodel.Gender;
import org.camunda.bpm.model.xml.testmodel.TestModelParser;
import org.camunda.bpm.model.xml.testmodel.TestModelTest;
import org.camunda.bpm.model.xml.testmodel.instance.Animal;
import org.camunda.bpm.model.xml.testmodel.instance.AnimalTest;
import org.camunda.bpm.model.xml.testmodel.instance.Animals;
import org.camunda.bpm.model.xml.testmodel.instance.Bird;
import org.camunda.bpm.model.xml.type.ModelElementType;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.runners.Parameterized.Parameters;

/**
 * @author Sebastian Menski
 */
public class AttributeTest extends TestModelTest {

  private Bird tweety;
  private ModelElementType animalType;
  private Attribute<String> idAttribute;
  private Attribute<String> nameAttribute;
  private Attribute<String> fatherAttribute;

  public AttributeTest(final ModelInstance testModelInstance, final AbstractModelParser modelParser) {
    super(testModelInstance, modelParser);
  }

  @Parameters
  public static Collection<Object[]> models() {
    Object[][] models = new Object[][]{createModel(), parseModel(AnimalTest.class)};
    return Arrays.asList(models);
  }

  public static Object[] createModel() {
    TestModelParser modelParser = new TestModelParser();
    ModelInstance modelInstance = modelParser.getEmptyModel();

    Animals animals = modelInstance.newInstance(Animals.class);
    modelInstance.setDocumentElement(animals);

    createBird(modelInstance, "tweety", Gender.Female);

    return new Object[]{modelInstance, modelParser};
  }

  @Before
  @SuppressWarnings("unchecked")
  public void copyModelInstance() {
    modelInstance = cloneModelInstance();

    tweety = (Bird) modelInstance.getModelElementById("tweety");
    animalType = modelInstance.getModel().getType(Animal.class);
    idAttribute = (Attribute<String>) animalType.getAttribute("id");
    nameAttribute = (Attribute<String>) animalType.getAttribute("name");
    fatherAttribute = (Attribute<String>) animalType.getAttribute("father");
  }

  @Test
  public void testOwningElementType() {
    AttributeImpl<String> idAttributeImpl = (AttributeImpl<String>) idAttribute;
    AttributeImpl<String> nameAttributeImpl = (AttributeImpl<String>) nameAttribute;
    AttributeImpl<String> fatherAttributeImpl = (AttributeImpl<String>) fatherAttribute;
    ModelElementType animalType = modelInstance.getModel().getType(Animal.class);

    assertThat(idAttributeImpl.getOwningElementType()).isEqualTo(animalType);
    assertThat(nameAttributeImpl.getOwningElementType()).isEqualTo(animalType);
    assertThat(fatherAttributeImpl.getOwningElementType()).isEqualTo(animalType);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testSetAttributeValue() {
    String identifier = "new-" + tweety.getId();
    idAttribute.setValue(tweety, identifier);
    assertThat(idAttribute.getValue(tweety)).isEqualTo(identifier);
  }

  @Test
  public void testSetDefaultValue() {
    String defaultName = "default-name";
    assertThat(tweety.getName()).isNull();
    assertThat(nameAttribute.getDefaultValue()).isNull();
    ((AttributeImpl<String>) nameAttribute).setDefaultValue(defaultName);
    assertThat(tweety.getName()).isEqualTo(defaultName);
    tweety.setName("not-" + defaultName);
    assertThat(tweety.getName()).isNotEqualTo(defaultName);
    tweety.removeAttribute("name");
    assertThat(tweety.getName()).isEqualTo(defaultName);
    ((AttributeImpl<String>) nameAttribute).setDefaultValue(null);
    assertThat(nameAttribute.getDefaultValue()).isNull();
  }

  @Test
  public void testRequired() {
    tweety.removeAttribute("name");
    assertThat(nameAttribute.isRequired()).isFalse();
    ((AttributeImpl<String>) nameAttribute).setRequired(true);
    assertThat(nameAttribute.isRequired()).isTrue();
    ((AttributeImpl<String>) nameAttribute).setRequired(false);
  }

  @Test
  public void testSetNamespaceUri() {
    String testNamespace = "http://camunda.org/test";
    ((AttributeImpl<String>) idAttribute).setNamespaceUri(testNamespace);
    assertThat(idAttribute.getNamespaceUri()).isEqualTo(testNamespace);
  }

  @Test
  public void testIdAttribute() {
    assertThat(idAttribute.isIdAttribute()).isTrue();
    assertThat(nameAttribute.isIdAttribute()).isFalse();
    assertThat(fatherAttribute.isIdAttribute()).isFalse();
  }

  @Test
  public void testAttributeName() {
    assertThat(idAttribute.getAttributeName()).isEqualTo("id");
    assertThat(nameAttribute.getAttributeName()).isEqualTo("name");
    assertThat(fatherAttribute.getAttributeName()).isEqualTo("father");
  }

  @Test
  public void testRemoveAttribute() {
    tweety.setName("test");
    assertThat(tweety.getName()).isNotNull();
    ((AttributeImpl<String>) nameAttribute).removeAttribute(tweety);
    assertThat(tweety.getName()).isNull();
  }

  @Test
  public void testIncomingReferences() {
    assertThat(idAttribute.getIncomingReferences()).isNotEmpty();
    assertThat(nameAttribute.getIncomingReferences()).isEmpty();
    assertThat(fatherAttribute.getIncomingReferences()).isEmpty();
  }

  @Test
  public void testOutgoingReferences() {
    assertThat(idAttribute.getOutgoingReferences()).isEmpty();
    assertThat(nameAttribute.getOutgoingReferences()).isEmpty();
    assertThat(fatherAttribute.getOutgoingReferences()).isNotEmpty();
  }

}
