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
package org.camunda.bpm.model.bpmn.instance.camunda;

import java.util.Arrays;
import java.util.Collection;
import org.camunda.bpm.model.bpmn.instance.BpmnModelElementInstanceTest;
import org.junit.Ignore;
import org.junit.Test;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_NS;
import static org.junit.Assert.fail;

/**
 * @author Sebastian Menski
 */
public class CamundaInputParameterTest extends BpmnModelElementInstanceTest {

  public TypeAssumption getTypeAssumption() {
    return new TypeAssumption(CAMUNDA_NS, false);
  }

  public Collection<ChildElementAssumption> getChildElementAssumptions() {
    return null;
  }

  public Collection<AttributeAssumption> getAttributesAssumptions() {
    return Arrays.asList(
      new AttributeAssumption(CAMUNDA_NS, "name", false, true)
    );
  }

  @Ignore("Test ignored. CAM-9441: Bug fix needed")
  @Test
  public void testIntputParameterScriptChildAssignment() {
    try {
      CamundaInputParameter inputParamElement = modelInstance.newInstance(CamundaInputParameter.class);
      inputParamElement.setCamundaName("aVariable");

      CamundaScript scriptElement = modelInstance.newInstance(CamundaScript.class);
      scriptElement.setCamundaScriptFormat("juel");
      scriptElement.setTextContent("${'a script'}");

      inputParamElement.addChildElement(scriptElement);
    } catch (Exception e) {
      fail("CamundaScript should be accepted as a child element of CamundaInputParameter. Error: " + e.getMessage());
    }
  }

  @Ignore("Test ignored. CAM-9441: Bug fix needed")
  @Test
  public void testInputParameterListChildAssignment() {
    try {
      CamundaInputParameter inputParamElement = modelInstance.newInstance(CamundaInputParameter.class);
      inputParamElement.setCamundaName("aVariable");

      CamundaList listElement = modelInstance.newInstance(CamundaList.class);

      inputParamElement.addChildElement(listElement);
    } catch (Exception e) {
      fail("CamundaList should be accepted as a child element of CamundaInputParameter. Error: " + e.getMessage());
    }
  }

  @Ignore("Test ignored. CAM-9441: Bug fix needed")
  @Test
  public void testInputParameterMapChildAssignment() {
    try {
      CamundaInputParameter inputParamElement = modelInstance.newInstance(CamundaInputParameter.class);
      inputParamElement.setCamundaName("aVariable");

      CamundaMap listElement = modelInstance.newInstance(CamundaMap.class);

      inputParamElement.addChildElement(listElement);
    } catch (Exception e) {
      fail("CamundaMap should be accepted as a child element of CamundaInputParameter. Error: " + e.getMessage());
    }
  }
}
