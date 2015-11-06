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

package org.camunda.bpm.model.cmmn;

import org.camunda.bpm.model.cmmn.instance.Case;
import org.camunda.bpm.model.cmmn.instance.CasePlanModel;
import org.camunda.bpm.model.cmmn.instance.CmmnModelElementInstance;
import org.camunda.bpm.model.cmmn.instance.Definitions;
import org.camunda.bpm.model.cmmn.instance.HumanTask;
import org.camunda.bpm.model.cmmn.instance.PlanItem;
import org.camunda.bpm.model.cmmn.instance.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Sebastian Menski
 */
public class CreateModelTest {

  public CmmnModelInstance modelInstance;
  public Definitions definitions;
  public Process process;

  @Before
  public void createEmptyModel() {
    modelInstance = Cmmn.createEmptyModel();
    definitions = modelInstance.newInstance(Definitions.class);
    definitions.setTargetNamespace("http://camunda.org/examples");
    modelInstance.setDefinitions(definitions);
  }

  protected <T extends CmmnModelElementInstance> T createElement(CmmnModelElementInstance parentElement, String id, Class<T> elementClass) {
    T element = modelInstance.newInstance(elementClass);
    element.setAttributeValue("id", id, true);
    parentElement.addChildElement(element);
    return element;
  }

  @Test
  public void createCaseWithOneHumanTask() {
    // create process
    Case caseInstance = createElement(definitions, "case-with-one-human-task", Case.class);

    // create case plan model
    CasePlanModel casePlanModel = createElement(caseInstance, "casePlanModel_1", CasePlanModel.class);

    // create elements
    HumanTask humanTask = createElement(casePlanModel, "HumanTask_1", HumanTask.class);

    // create a plan item
    PlanItem planItem = createElement(casePlanModel, "PlanItem_1", PlanItem.class);

    // set definition to human task
    planItem.setDefinition(humanTask);
  }

  @Test
  public void createCaseWithOneStageAndNestedHumanTask() {
    // create process
    Case caseInstance = createElement(definitions, "case-with-one-human-task", Case.class);

    // create case plan model
    CasePlanModel casePlanModel = createElement(caseInstance, "casePlanModel_1", CasePlanModel.class);

    // create a stage
    Stage stage = createElement(casePlanModel, "Stage_1", Stage.class);

    // create elements
    HumanTask humanTask = createElement(stage, "HumanTask_1", HumanTask.class);

    // create a plan item
    PlanItem planItem = createElement(stage, "PlanItem_1", PlanItem.class);

    // set definition to human task
    planItem.setDefinition(humanTask);
  }

  @After
  public void validateModel() {
    Cmmn.validateModel(modelInstance);
  }

}
