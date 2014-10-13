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
import org.camunda.bpm.engine.impl.cmmn.behavior.MilestoneActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.model.cmmn.instance.CmmnElement;

/**
 * @author Roman Smirnov
 *
 */
public class MilestoneItemHandler extends ItemHandler {

  protected List<String> getStandardEvents(CmmnElement element) {
    return EVENT_LISTENER_OR_MILESTONE_EVENTS;
  }

  protected CmmnActivityBehavior getActivityBehavior() {
    return new MilestoneActivityBehavior();
  }

  protected void initializeManualActivationRule(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
    // manual activation rule is not applicable on milestones
  }

  protected void initializeExitCriterias(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
    // exit criteria is not applicable on milestones
  }

}
