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
  CmmnAtomicOperation CASE_INSTANCE_NOTIFY_LISTENER_CREATE = new AtomicOperationCaseInstanceNotifyListenerCreate();

  // lifecycle of a case execution: ////////////////////////////

  // create a new case execution
  CmmnAtomicOperation CASE_EXECUTION_NOTIFY_LISTENER_CREATE = new AtomicOperationCaseExecutionNotifyListenerCreate();

  // start a case execution
  CmmnAtomicOperation CASE_EXECUTION_NOTIFY_LISTENER_START = new AtomicOperationCaseExecutionNotifyListenerStart();

  // enable case execution
  CmmnAtomicOperation CASE_EXECUTION_NOTIFY_LISTENER_ENABLE = new AtomicOperationCaseExecutionNotifyListenerEnable();

  // disable a case execution
  CmmnAtomicOperation CASE_EXECUTION_NOTIFY_LISTENER_DISABLE = new AtomicOperationCaseExecutionNotifyListenerDisable();

  // re-enable a case execution
  CmmnAtomicOperation CASE_EXECUTION_NOTIFY_LISTENER_RE_ENABLE = new AtomicOperationCaseExecutionNotifyListenerReEnable();

  // start manual a case execution
  CmmnAtomicOperation CASE_EXECUTION_NOTIFY_LISTENER_MANUAL_START = new AtomicOperationCaseExecutionNotifyListenerManualStart();

  // fault of a case execution
  CmmnAtomicOperation CASE_EXECUTION_NOTIFY_LISTENER_FAULT = new AtomicOperationCaseExecutionNotifyListenerFault();

  // execute activity behavior
  CmmnAtomicOperation ACTIVITY_EXECUTE = new AtomicOperationActivityExecute();

  // re-activate a case execution
  CmmnAtomicOperation CASE_EXECUTION_NOTIFY_LISTENER_RE_ACTIVATE = new AtomicOperationCaseExecutionNotifyListenerReActivate();

  // complete a case execution
  CmmnAtomicOperation CASE_EXECUTION_NOTIFY_LISTENER_COMPLETE = new AtomicOperationCaseExecutionNotifyListenerComplete();

  // terminate a case execution
  CmmnAtomicOperation CASE_EXECUTION_NOTIFY_LISTENER_TERMINATE = new AtomicOperationCaseExecutionNotifyListenerTerminate();

  // exit a case execution
  CmmnAtomicOperation CASE_EXECUTION_NOTIFY_LISTENER_EXIT = new AtomicOperationCaseExecutionNotifyListenerExit();

  // suspend a case execution
  CmmnAtomicOperation CASE_EXECUTION_NOTIFY_LISTENER_SUSPEND = new AtomicOperationCaseExecutionNotifyListenerSuspend();

  // resume a case execution
  CmmnAtomicOperation CASE_EXECUTION_NOTIFY_LISTENER_RESUME = new AtomicOperationCaseExecutionNotifyListenerResume();

  // parent suspend a case execution
  CmmnAtomicOperation CASE_EXECUTION_NOTIFY_LISTENER_PARENT_SUSPEND = new AtomicOperationCaseExecutionNotifyListenerParentSuspend();

  // parent resume a case execution
  CmmnAtomicOperation CASE_EXECUTION_NOTIFY_LISTENER_PARENT_RESUME = new AtomicOperationCaseExecutionNotifyListenerParentResume();

  public void execute(CmmnExecution execution);

  public boolean isAsync(CmmnExecution execution);

}
