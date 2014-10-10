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
package org.camunda.bpm.model.cmmn.instance.camunda;

import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CAMUNDA_NS;

import java.util.Arrays;
import java.util.Collection;

import org.camunda.bpm.model.cmmn.instance.CmmnModelElementInstanceTest;

/**
 * @author Thorben Lindhauer
 */
public class CamundaVariableListenerTest extends CmmnModelElementInstanceTest {

  public TypeAssumption getTypeAssumption() {
    return new TypeAssumption(CAMUNDA_NS, false);
  }

  public Collection<ChildElementAssumption> getChildElementAssumptions() {
    return Arrays.asList(
        new ChildElementAssumption(CAMUNDA_NS, CamundaField.class),
        new ChildElementAssumption(CAMUNDA_NS, CamundaScript.class, 0, 1)
    );
  }

  public Collection<AttributeAssumption> getAttributesAssumptions() {
    return Arrays.asList(
      new AttributeAssumption(CAMUNDA_NS, "event"),
      new AttributeAssumption(CAMUNDA_NS, "class"),
      new AttributeAssumption(CAMUNDA_NS, "expression"),
      new AttributeAssumption(CAMUNDA_NS, "delegateExpression")
    );
  }

}
