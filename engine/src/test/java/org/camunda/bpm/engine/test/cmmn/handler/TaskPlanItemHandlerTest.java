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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.camunda.bpm.engine.impl.cmmn.behavior.TaskActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.handler.CmmnHandlerContext;
import org.camunda.bpm.engine.impl.cmmn.handler.TaskPlanItemHandler;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.model.cmmn.instance.PlanItem;
import org.camunda.bpm.model.cmmn.instance.Task;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class TaskPlanItemHandlerTest extends CmmnElementHandlerTest {

  protected Task task;
  protected PlanItem planItem;
  protected TaskPlanItemHandler handler = new TaskPlanItemHandler();
  protected CmmnHandlerContext context;

  @Before
  public void setUp() {
    task = createElement(casePlanModel, "aTask", Task.class);

    planItem = createElement(casePlanModel, "PI_aTask", PlanItem.class);
    planItem.setDefinition(task);

    context = new CmmnHandlerContext();
  }

  @Test
  public void testTaskActivityName() {
    // given:
    // the task has a name "A Task"
    String name = "A Task";
    task.setName(name);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(name, activity.getName());
  }

  @Test
  public void testPlanItemActivityName() {
    // given:
    // the task has a name "A Task"
    String taskName = "A Task";
    task.setName(taskName);

    // the planItem has an own name "My LocalName"
    String planItemName = "My LocalName";
    planItem.setName(planItemName);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertNotEquals(taskName, activity.getName());
    assertEquals(planItemName, activity.getName());
  }

  @Test
  public void testActivityBehavior() {
    // given: a planItem

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    CmmnActivityBehavior behavior = activity.getActivityBehavior();
    assertTrue(behavior instanceof TaskActivityBehavior);
  }

  @Test
  public void testIsBlockingEqualsTrueProperty() {
    // given: a task with isBlocking = true (defaultValue)

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    Boolean isBlocking = (Boolean) activity.getProperty("isBlocking");
    assertTrue(isBlocking);
  }

  @Test
  public void testIsBlockingEqualsFalseProperty() {
    // given:
    // a task with isBlocking = false
    task.setIsBlocking(false);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    Boolean isBlocking = (Boolean) activity.getProperty("isBlocking");
    assertFalse(isBlocking);
  }

  @Test
  public void testWithoutParent() {
    // given: a planItem

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

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
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(parent, activity.getParent());
    assertTrue(parent.getActivities().contains(activity));
  }

}
