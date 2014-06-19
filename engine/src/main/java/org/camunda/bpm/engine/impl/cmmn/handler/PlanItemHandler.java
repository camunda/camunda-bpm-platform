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

import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.model.cmmn.instance.PlanItem;
import org.camunda.bpm.model.cmmn.instance.PlanItemDefinition;

/**
 * @author Roman Smirnov
 *
 */
public abstract class PlanItemHandler extends CmmnElementHandler<PlanItem> {

  public CmmnActivity handleElement(PlanItem planItem, CmmnHandlerContext context) {
    // create a new activity
    CmmnActivity newActivity = createActivity(planItem, context);

    // initialize activity
    initializeActivity(planItem, newActivity, context);

    return newActivity;
  }

  protected void initializeActivity(PlanItem planItem, CmmnActivity activity, CmmnHandlerContext context) {
    String name = planItem.getName();
    if (name == null) {
      PlanItemDefinition definition = planItem.getDefinition();
      name = definition.getName();
    }
    activity.setName(name);

    // TODO: set properties: itemControl (vs.) defaultControl
  }

}
