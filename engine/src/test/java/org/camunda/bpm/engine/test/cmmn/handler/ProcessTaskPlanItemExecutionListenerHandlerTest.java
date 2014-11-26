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

import org.camunda.bpm.engine.impl.cmmn.handler.ProcessTaskItemHandler;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.test.cmmn.handler.specification.AbstractExecutionListenerSpec;
import org.camunda.bpm.model.cmmn.instance.PlanItem;
import org.camunda.bpm.model.cmmn.instance.ProcessTask;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author Thorben Lindhauer
 *
 */
@RunWith(Parameterized.class)
public class ProcessTaskPlanItemExecutionListenerHandlerTest extends CmmnElementHandlerTest {

  @Parameters(name = "testListener: {0}")
  public static Iterable<Object[]> data() {
    return ExecutionListenerCases.TASK_OR_STAGE_CASES;
  }

  protected ProcessTask processTask;
  protected PlanItem planItem;
  protected ProcessTaskItemHandler handler = new ProcessTaskItemHandler();

  protected AbstractExecutionListenerSpec testSpecification;

  public ProcessTaskPlanItemExecutionListenerHandlerTest(AbstractExecutionListenerSpec testSpecification) {
    this.testSpecification = testSpecification;
  }

  @Before
  public void setUp() {
    processTask = createElement(casePlanModel, "aProcessTask", ProcessTask.class);

    planItem = createElement(casePlanModel, "PI_aProcessTask", PlanItem.class);
    planItem.setDefinition(processTask);

  }

  @Test
  public void testCaseExecutionListener() {
    // given:
    testSpecification.addListenerToElement(modelInstance, processTask);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    testSpecification.verify(activity);
  }

}
