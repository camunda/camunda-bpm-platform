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

package org.camunda.bpm.model.bpmn;

import org.camunda.bpm.model.bpmn.instance.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Dario Campagna
 */
public class DataObjectsTest {

  private static BpmnModelInstance modelInstance;

  @BeforeClass
  public static void parseModel() {
    modelInstance = Bpmn.readModelFromStream(DataObjectsTest.class.getResourceAsStream("DataObjectTest.bpmn"));
  }

  @Test
  public void testGetDataObject() {
    DataObject dataObject = modelInstance.getModelElementById("_21");
    ItemDefinition itemDefinition = modelInstance.getModelElementById("_100");
    assertThat(dataObject).isNotNull();
    assertThat(dataObject.getName()).isEqualTo("DataObject _21");
    assertThat(dataObject.isCollection()).isFalse();
    assertThat(dataObject.getItemSubject()).isEqualTo(itemDefinition);
  }

  @Test
  public void testGetDataObjectReference() {
    DataObjectReference dataObjectReference = modelInstance.getModelElementById("_dataRef_7");
    DataObject dataObject = modelInstance.getModelElementById("_7");
    assertThat(dataObjectReference).isNotNull();
    assertThat(dataObjectReference.getName()).isNull();
    assertThat(dataObjectReference.getDataObject()).isEqualTo(dataObject);
  }

  @Test
  public void testDataObjectReferenceAsDataAssociationSource() {
    ScriptTask scriptTask = modelInstance.getModelElementById("_3");
    DataObjectReference dataObjectReference = modelInstance.getModelElementById("_dataRef_11");
    DataInputAssociation dataInputAssociation = scriptTask.getDataInputAssociations().iterator().next();
    Collection<ItemAwareElement> sources = dataInputAssociation.getSources();
    assertThat(sources.size()).isEqualTo(1);
    assertThat(sources.iterator().next()).isEqualTo(dataObjectReference);
  }

  @Test
  public void testDataObjectReferenceAsDataAssociationTarget() {
    ScriptTask scriptTask = modelInstance.getModelElementById("_3");
    DataObjectReference dataObjectReference = modelInstance.getModelElementById("_dataRef_7");
    DataOutputAssociation dataOutputAssociation = scriptTask.getDataOutputAssociations().iterator().next();
    assertThat(dataOutputAssociation.getTarget()).isEqualTo(dataObjectReference);
  }
}
