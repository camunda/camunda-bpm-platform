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

import java.util.List;

import org.camunda.bpm.engine.impl.cmmn.behavior.CmmnActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.behavior.StageActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.model.cmmn.instance.CmmnElement;
import org.camunda.bpm.model.cmmn.instance.PlanItemDefinition;
import org.camunda.bpm.model.cmmn.instance.Stage;

/**
 * @author Roman Smirnov
 *
 */
public class StageItemHandler extends ItemHandler {

  protected void initializeAutoComplete(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
    PlanItemDefinition definition = getDefinition(element);
    if (definition instanceof Stage) {
      Stage stage = (Stage) definition;
      activity.setProperty(PROPERTY_AUTO_COMPLETE, stage.isAutoComplete());
    }
  }

  protected CmmnActivityBehavior getActivityBehavior() {
    return new StageActivityBehavior();
  }

  protected List<String> getStandardEvents(CmmnElement element) {
    return TASK_OR_STAGE_EVENTS;
  }

}
