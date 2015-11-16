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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;

import org.camunda.bpm.model.cmmn.impl.instance.CaseFileItemTransitionStandardEventImpl;
import org.camunda.bpm.model.cmmn.instance.Case;
import org.camunda.bpm.model.cmmn.instance.CasePlanModel;
import org.camunda.bpm.model.cmmn.instance.DefaultControl;
import org.camunda.bpm.model.cmmn.instance.Definitions;
import org.camunda.bpm.model.cmmn.instance.HumanTask;
import org.junit.Test;

public class GenerateIdTest {

  @Test
  public void shouldNotGenerateIdsOnRead() {
    CmmnModelInstance modelInstance = Cmmn.readModelFromStream(GenerateIdTest.class.getResourceAsStream("GenerateIdTest.cmmn"));
    Definitions definitions = modelInstance.getDefinitions();
    assertThat(definitions.getId()).isNull();

    Case caseElement = modelInstance.getModelElementsByType(Case.class).iterator().next();
    assertThat(caseElement.getId()).isNull();

    CasePlanModel casePlanModel = modelInstance.getModelElementsByType(CasePlanModel.class).iterator().next();
    assertThat(casePlanModel.getId()).isNull();

    HumanTask humanTask = modelInstance.getModelElementsByType(HumanTask.class).iterator().next();
    assertThat(humanTask.getId()).isNull();
  }

  @Test
  public void shouldGenerateIdsOnCreate() {
    CmmnModelInstance modelInstance = Cmmn.createEmptyModel();
    Definitions definitions = modelInstance.newInstance(Definitions.class);
    assertThat(definitions.getId()).isNotNull();

    Case caseElement = modelInstance.newInstance(Case.class);
    assertThat(caseElement.getId()).isNotNull();

    CasePlanModel casePlanModel = modelInstance.newInstance(CasePlanModel.class);
    assertThat(casePlanModel.getId()).isNotNull();

    HumanTask humanTask = modelInstance.newInstance(HumanTask.class);
    assertThat(humanTask.getId()).isNotNull();
  }

}
