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
package org.camunda.bpm.engine.test.cmmn.handler;

import org.camunda.bpm.model.cmmn.Cmmn;
import org.camunda.bpm.model.cmmn.CmmnModelInstance;
import org.camunda.bpm.model.cmmn.impl.instance.CasePlanModel;
import org.camunda.bpm.model.cmmn.instance.Case;
import org.camunda.bpm.model.cmmn.instance.CmmnModelElementInstance;
import org.camunda.bpm.model.cmmn.instance.Definitions;
import org.junit.Before;

/**
 * @author Roman Smirnov
 *
 */
public abstract class CmmnElementHandlerTest {

  protected CmmnModelInstance modelInstance;
  protected Definitions definitions;
  protected Case caseDefinition;
  protected CasePlanModel casePlanModel;

  @Before
  public void setup() {
    modelInstance = Cmmn.createEmptyModel();
    definitions = modelInstance.newInstance(Definitions.class);
    definitions.setTargetNamespace("http://camunda.org/examples");
    modelInstance.setDefinitions(definitions);

    caseDefinition = createElement(definitions, "aCaseDefinition", Case.class);
    casePlanModel = createElement(caseDefinition, "aCasePlanModel", CasePlanModel.class);
  }

  protected <T extends CmmnModelElementInstance> T createElement(CmmnModelElementInstance parentElement, String id, Class<T> elementClass) {
    T element = modelInstance.newInstance(elementClass);
    element.setAttributeValue("id", id, true);
    parentElement.addChildElement(element);
    return element;
  }

}
