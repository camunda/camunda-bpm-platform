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
package org.camunda.bpm.engine.impl.cmmn.operation;

import org.camunda.bpm.engine.delegate.CaseExecutionListener;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution;

/**
 * @author Roman Smirnov
 *
 */
public class AtomicOperationCaseInstanceNotifyListenerCreate extends AbstractAtomicOperationNotifyListener {

  public String getCanonicalName() {
    return "case-notify-listener-create";
  }

  protected String getEventName() {
    return CaseExecutionListener.CREATE;
  }

  @Override
  protected void eventNotificationsCompleted(CmmnExecution execution) {
    execution.performOperation(ACTIVITY_EXECUTE);
  }

}
