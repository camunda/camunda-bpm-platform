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
package org.camunda.bpm.engine.impl.cmmn.handler;

import org.camunda.bpm.engine.impl.cmmn.execution.CmmnActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.model.cmmn.instance.DiscretionaryItem;
import org.camunda.bpm.model.cmmn.instance.PlanItemDefinition;

/**
 * @author Roman Smirnov
 *
 */
public abstract class DiscretionaryItemHandler extends CmmnElementHandler<DiscretionaryItem> {

  public CmmnActivity handleElement(DiscretionaryItem discretionaryItem, CmmnHandlerContext context) {
    // create a new activity
    CmmnActivity newActivity = createActivity(discretionaryItem, context);

    // initialize activity
    initializeActivity(discretionaryItem, newActivity, context);

    // perform custom transformation
    handleElementProperties(discretionaryItem, newActivity, context);

    return newActivity;
  }

  protected void initializeActivity(DiscretionaryItem discretionaryItem, CmmnActivity activity, CmmnHandlerContext context) {
    // set name of activity
    PlanItemDefinition definition = discretionaryItem.getDefinition();
    activity.setName(definition.getName());

    // set activity to discretionary
    activity.setProperty("discretionary", Boolean.TRUE);

    // TODO: set properties: itemControl (vs.) defaultControl

    CmmnActivityBehavior behavior = getActivityBehavior();
    activity.setActivityBehavior(behavior);
  }

  protected void handleElementProperties(DiscretionaryItem discretionaryItem, CmmnActivity activity, CmmnHandlerContext context) {
    // noop
  }

}
