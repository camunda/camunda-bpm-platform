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
package org.camunda.bpm.engine.test.api.repository;

import java.util.Collection;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.model.cmmn.CmmnModelInstance;
import org.camunda.bpm.model.cmmn.instance.Case;
import org.camunda.bpm.model.cmmn.instance.HumanTask;
import org.camunda.bpm.model.cmmn.instance.PlanItem;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

/**
 * @author Roman Smirnov
 *
 */
public class CmmnModelElementInstanceCmdTest extends PluggableProcessEngineTestCase {

  private final static String CASE_KEY = "oneTaskCase";

  @Deployment(resources = "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn")
  public void testRepositoryService() {
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .caseDefinitionKey(CASE_KEY)
        .singleResult()
        .getId();

    CmmnModelInstance modelInstance = repositoryService.getCmmnModelInstance(caseDefinitionId);
    assertNotNull(modelInstance);

    Collection<ModelElementInstance> humanTasks = modelInstance.getModelElementsByType(modelInstance.getModel().getType(HumanTask.class));
    assertEquals(1, humanTasks.size());

    Collection<ModelElementInstance> planItems = modelInstance.getModelElementsByType(modelInstance.getModel().getType(PlanItem.class));
    assertEquals(1, planItems.size());

    Collection<ModelElementInstance> cases = modelInstance.getModelElementsByType(modelInstance.getModel().getType(Case.class));
    assertEquals(1, cases.size());

  }

}
