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

import org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution;
import org.camunda.bpm.engine.impl.core.operation.CoreAtomicOperation;

/**
 * @author Roman Smirnov
 *
 */
public interface CmmnAtomicOperation extends CoreAtomicOperation<CmmnExecution> {

  // lifecycle of a case //////////////////////////////////
  CmmnAtomicOperation CASE_NOTIFY_LISTENER_CREATE = new AtomicOperationCaseNotifyListenerCreate();

  // lifecycle of a plan item: ////////////////////////////

  // create a new plan item
  CmmnAtomicOperation PLAN_ITEM_NOTIFY_LISTENER_CREATE = new AtomicOperationPlanItemNotifyListenerCreate();

  // start a plan item
  CmmnAtomicOperation PLAN_ITEM_NOTIFY_LISTENER_START = new AtomicOperationPlanItemNotifyListenerStart();

  // enable plan item
  CmmnAtomicOperation PLAN_ITEM_NOTIFY_LISTENER_ENABLE = new AtomicOperationPlanItemNotifyListenerEnable();

  // disable a plan item
  CmmnAtomicOperation PLAN_ITEM_NOTIFY_LISTENER_DISABLE = new AtomicOperationPlanItemNotifyListenerDisable();

  // re-enable a plan item
  CmmnAtomicOperation PLAN_ITEM_NOTIFY_LISTENER_RE_ENABLE = new AtomicOperationPlanItemNotifyListenerReEnable();

  // start manual a plan item
  CmmnAtomicOperation PLAN_ITEM_NOTIFY_LISTENER_MANUAL_START = new AtomicOperationPlanItemNotifyListenerManualStart();

  // fault of a plan item
  CmmnAtomicOperation PLAN_ITEM_NOTIFY_LISTENER_FAULT = new AtomicOperationPlanItemNotifyListenerFault();

  // execute activity behavior
  CmmnAtomicOperation ACTIVITY_EXECUTE = new AtomicOperationActivityExecute();

  // re-activate a plan item
  CmmnAtomicOperation PLAN_ITEM_NOTIFY_LISTENER_RE_ACTIVATE = new AtomicOperationPlanItemNotifyListenerReActivate();

  // complete a plan item
  CmmnAtomicOperation PLAN_ITEM_NOTIFY_LISTENER_COMPLETE = new AtomicOperationPlanItemNotifyListenerComplete();

  // terminate a plan item
  CmmnAtomicOperation PLAN_ITEM_NOTIFY_LISTENER_TERMINATE = new AtomicOperationPlanItemNotifyListenerTerminate();

  // exit a plan item
  CmmnAtomicOperation PLAN_ITEM_NOTIFY_LISTENER_EXIT = new AtomicOperationPlanItemNotifyListenerExit();

  // suspend a plan item
  CmmnAtomicOperation PLAN_ITEM_NOTIFY_LISTENER_SUSPEND = new AtomicOperationPlanItemNotifyListenerSuspend();

  // resume a plan item
  CmmnAtomicOperation PLAN_ITEM_NOTIFY_LISTENER_RESUME = new AtomicOperationPlanItemNotifyListenerResume();

  // parent suspend a plan item
  CmmnAtomicOperation PLAN_ITEM_NOTIFY_LISTENER_PARENT_SUSPEND = new AtomicOperationPlanItemNotifyListenerParentSuspend();

  // parent resume a plan item
  CmmnAtomicOperation PLAN_ITEM_NOTIFY_LISTENER_PARENT_RESUME = new AtomicOperationPlanItemNotifyListenerParentResume();

  public void execute(CmmnExecution execution);

  public boolean isAsync(CmmnExecution execution);

}
