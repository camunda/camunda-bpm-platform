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

import java.util.Arrays;
import java.util.Collection;
import static org.assertj.core.api.Assertions.assertThat;
import org.camunda.bpm.model.dmn.impl.DmnModelInstanceImpl;

import org.camunda.bpm.model.dmn.instance.Input;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CamundaExtensionsTest {

  private Input input;
  
  private final DmnModelInstance originalModelInstance;
  private DmnModelInstance modelInstance;
  
   @Parameters(name="Namespace: {0}")
   public static Collection<Object[]> parameters(){
     return Arrays.asList(new Object[][]{
         {Dmn.readModelFromStream(CamundaExtensionsTest.class.getResourceAsStream("CamundaExtensionsTest.dmn"))},
         //for compatability reasons we gotta check the old namespace, too
         {Dmn.readModelFromStream(CamundaExtensionsTest.class.getResourceAsStream("CamundaExtensionsCompatibilityTest.dmn"))}
     });
   }

  public CamundaExtensionsTest(DmnModelInstance originalModelInstance) {
    this.originalModelInstance = originalModelInstance;
  }

  @Before
  public void parseModel() {  
    modelInstance = ((DmnModelInstanceImpl)originalModelInstance).clone();
    input = modelInstance.getModelElementById("input");
  }

  @Test
  public void testCamundaClauseOutput() {
    assertThat(input.getCamundaInputVariable()).isEqualTo("myVariable");
    input.setCamundaInputVariable("foo");
    assertThat(input.getCamundaInputVariable()).isEqualTo("foo");
  }

  @After
  public void validateModel() {
    Dmn.validateModel(modelInstance);
  }

}
