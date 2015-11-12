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
import org.camunda.bpm.engine.impl.core.delegate.CoreActivityBehavior;

/**
 * @author Roman Smirnov
 *
 */
public interface CmmnActivityBehavior extends CoreActivityBehavior<CmmnActivityExecution> {

  public void onCreate(CmmnActivityExecution execution);

  public void created(CmmnActivityExecution execution);

  public void onEnable(CmmnActivityExecution execution);

  public void onReenable(CmmnActivityExecution execution);

  public void onDisable(CmmnActivityExecution execution);

  public void onStart(CmmnActivityExecution execution);

  public void onManualStart(CmmnActivityExecution execution);

  public void started(CmmnActivityExecution execution);

  public void onCompletion(CmmnActivityExecution execution);

  public void onManualCompletion(CmmnActivityExecution execution);

  public void onTermination(CmmnActivityExecution execution);

  public void onParentTermination(CmmnActivityExecution execution);

  public void onExit(CmmnActivityExecution execution);

  public void onOccur(CmmnActivityExecution execution);

  public void onSuspension(CmmnActivityExecution execution);

  public void onParentSuspension(CmmnActivityExecution execution);

  public void onResume(CmmnActivityExecution execution);

  public void onParentResume(CmmnActivityExecution execution);

  public void resumed(CmmnActivityExecution execution);

  public void onReactivation(CmmnActivityExecution execution);

  public void reactivated(CmmnActivityExecution execution);

  public void onClose(CmmnActivityExecution execution);

  public void fireEntryCriteria(CmmnActivityExecution execution);

  public void fireExitCriteria(CmmnActivityExecution execution);

  public void repeat(CmmnActivityExecution execution, String standardEvent);

}
