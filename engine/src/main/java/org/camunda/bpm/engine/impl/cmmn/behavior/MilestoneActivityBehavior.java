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
package org.camunda.bpm.engine.impl.cmmn.behavior;

import org.camunda.bpm.engine.impl.cmmn.execution.CmmnActivityExecution;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;

/**
 * @author Roman Smirnov
 *
 */
public class MilestoneActivityBehavior extends EventListenerOrMilestoneActivityBehavior {

  protected void creating(CmmnActivityExecution execution) {
    evaluateRequiredRule(execution);
    evaluateRepetitionRule(execution);
  }

  public void created(CmmnActivityExecution execution) {
    // TODO: Check Entry Sentries, if the entryCriterias
    // are not fulfilled then stay in state AVAILABLE.
    // But if the entryCriterias are already fulfilled
    // then perform transition "occur" on given case execution.

    CmmnActivity activity = execution.getActivity();

    // NOTE: this is only a temporally implementation!!! This will
    // be exchanged with a proper implementation of sentries!!!
    boolean hasEntryCriterias = false;
    Object hasEntryCriteriasProperty = activity.getProperty("hasEntryCriterias");
    if (hasEntryCriteriasProperty != null && hasEntryCriteriasProperty instanceof Boolean) {
      hasEntryCriterias = (Boolean) hasEntryCriteriasProperty;
    }

    if (!hasEntryCriterias) {
      // A missing entry criteria (Sentry) is considered "true" => occurred
      execution.occur();
    }

  }

  protected String getTypeName() {
    return "milestone";
  }

}
