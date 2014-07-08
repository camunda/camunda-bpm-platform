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

import org.camunda.bpm.engine.impl.cmmn.behavior.CmmnActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.behavior.ProcessTaskActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.model.cmmn.instance.PlanItem;
import org.camunda.bpm.model.cmmn.instance.ProcessTask;

/**
 * @author Roman Smirnov
 *
 */
public class ProcessTaskPlanItemHandler extends ProcessOrCaseTaskPlanItemHandler {

  protected CmmnActivityBehavior getActivityBehavior() {
    return new ProcessTaskActivityBehavior();
  }

  protected ProcessTask getDefinition(PlanItem planItem) {
    return (ProcessTask) planItem.getDefinition();
  }

  protected String getDefinitionKey(PlanItem planItem, CmmnActivity activity, CmmnHandlerContext context) {
    ProcessTask definition = getDefinition(planItem);

    return definition.getProcess();
  }

  protected String getBinding(PlanItem planItem, CmmnActivity activity, CmmnHandlerContext context) {
    ProcessTask definition = getDefinition(planItem);

    return definition.getCamundaProcessBinding();
  }

  protected String getVersion(PlanItem planItem, CmmnActivity activity, CmmnHandlerContext context) {
    ProcessTask definition = getDefinition(planItem);

    return definition.getCamundaProcessVersion();
  }

}
