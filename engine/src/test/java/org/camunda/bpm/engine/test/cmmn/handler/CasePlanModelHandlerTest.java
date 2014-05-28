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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.camunda.bpm.engine.impl.cmmn.behavior.StageActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.handler.CasePlanModelHandler;
import org.camunda.bpm.engine.impl.cmmn.handler.CmmnHandlerContext;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class CasePlanModelHandlerTest extends CmmnElementHandlerTest {

  protected CasePlanModelHandler handler = new CasePlanModelHandler();
  protected CmmnHandlerContext context;

  @Before
  public void setUp() {
    context = new CmmnHandlerContext();
  }

  @Test
  public void testCasePlanModelActivityName() {
    // given:
    // the case plan model has a name "A CasePlanModel"
    String name = "A CasePlanModel";
    casePlanModel.setName(name);

    // when
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

    // then
    assertEquals(name, activity.getName());
  }

  @Test
  public void testActivityBehavior() {
    // given: a case plan model

    // when
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

    // then
    CmmnActivityBehavior behavior = activity.getActivityBehavior();
    assertTrue(behavior instanceof StageActivityBehavior);
  }

  @Test
  public void testWithoutParent() {
    // given: a casePlanModel

    // when
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

    // then
    assertNull(activity.getParent());
  }

  @Test
  public void testWithParent() {
    // given:
    // a new activity as parent
    CmmnCaseDefinition parent = new CmmnCaseDefinition("aParentActivity");
    context.setParent(parent);

    // when
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

    // then
    assertEquals(parent, activity.getParent());
    assertTrue(parent.getActivities().contains(activity));
  }

}
