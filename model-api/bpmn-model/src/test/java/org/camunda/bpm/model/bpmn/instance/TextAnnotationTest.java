/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.bpm.model.bpmn.instance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Filip Hrisafov
 */
public class TextAnnotationTest extends BpmnModelElementInstanceTest {

  protected static BpmnModelInstance modelInstance;

  public TypeAssumption getTypeAssumption() {
    return new TypeAssumption(Artifact.class, false);
  }

  public Collection<ChildElementAssumption> getChildElementAssumptions() {
    return Arrays.asList(
      new ChildElementAssumption(Text.class, 0, 1)
    );
  }

  public Collection<AttributeAssumption> getAttributesAssumptions() {
    return Arrays.asList(
      new AttributeAssumption("textFormat", false, false, "text/plain")
    );
  }

  @BeforeClass
  public static void parseModel() {
    modelInstance = Bpmn.readModelFromStream(TextAnnotationTest.class
      .getResourceAsStream("TextAnnotationTest.bpmn"));
  }

  @Test
  public void testGetTextAnnotationsByType() {
    Collection<TextAnnotation> textAnnotations = modelInstance.getModelElementsByType(TextAnnotation.class);
    assertThat(textAnnotations)
      .isNotNull()
      .hasSize(2);
  }

  @Test
  public void testGetTextAnnotationById() {
    TextAnnotation textAnnotation = modelInstance.getModelElementById("textAnnotation2");
    assertThat(textAnnotation).isNotNull();
    assertThat(textAnnotation.getTextFormat()).isEqualTo("text/plain");
    Text text = textAnnotation.getText();
    assertThat(text.getTextContent()).isEqualTo("Attached text annotation");
  }

  @Test
  public void testTextAnnotationAsAssociationSource() {
    Association association = modelInstance.getModelElementById("Association_1");
    BaseElement source = association.getSource();
    assertThat(source).isInstanceOf(TextAnnotation.class);
    assertThat(source.getId()).isEqualTo("textAnnotation2");
  }

  @Test
  public void testTextAnnotationAsAssociationTarget() {
    Association association = modelInstance.getModelElementById("Association_2");
    BaseElement target = association.getTarget();
    assertThat(target).isInstanceOf(TextAnnotation.class);
    assertThat(target.getId()).isEqualTo("textAnnotation1");
  }

}
