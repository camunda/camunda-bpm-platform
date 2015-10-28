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

package org.camunda.bpm.model.dmn;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.model.dmn.instance.Input;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CamundaExtensionsTest {

  private DmnModelInstance modelInstance;
  private Input clause;

  @Before
  public void parseModel() {
    modelInstance = Dmn.readModelFromStream(getClass().getResourceAsStream(getClass().getSimpleName() + ".dmn"));
    clause = modelInstance.getModelElementById("clause");
  }

  @Test
  public void testCamundaClauseOutput() {
    assertThat(clause.getCamundaOutput()).isEqualTo("clauseOutput");
    clause.setCamundaOutput("foo");
    assertThat(clause.getCamundaOutput()).isEqualTo("foo");
  }

  @After
  public void validateModel() {
    Dmn.validateModel(modelInstance);
  }

}
