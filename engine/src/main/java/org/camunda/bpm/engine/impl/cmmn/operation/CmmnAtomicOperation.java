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

  // lifecycle
  CmmnAtomicOperation CASE_INSTANCE_CREATE = new AtomicOperationCaseInstanceCreate();
  CmmnAtomicOperation CASE_INSTANCE_CLOSE = new AtomicOperationCaseInstanceClose();
  CmmnAtomicOperation CASE_EXECUTION_CREATE = new AtomicOperationCaseExecutionCreate();
  CmmnAtomicOperation CASE_EXECUTION_CREATED = new AtomicOperationCaseExecutionCreated();
  CmmnAtomicOperation CASE_EXECUTION_ENABLE = new AtomicOperationCaseExecutionEnable();
  CmmnAtomicOperation CASE_EXECUTION_RE_ENABLE = new AtomicOperationCaseExecutionReenable();
  CmmnAtomicOperation CASE_EXECUTION_DISABLE = new AtomicOperationCaseExecutionDisable();
  CmmnAtomicOperation CASE_EXECUTION_START = new AtomicOperationCaseExecutionStart();
  CmmnAtomicOperation CASE_EXECUTION_MANUAL_START = new AtomicOperationCaseExecutionManualStart();
  CmmnAtomicOperation CASE_EXECUTION_COMPLETE = new AtomicOperationCaseExecutionComplete();
  CmmnAtomicOperation CASE_EXECUTION_MANUAL_COMPLETE = new AtomicOperationCaseExecutionManualComplete();
  CmmnAtomicOperation CASE_EXECUTION_OCCUR = new AtomicOperationCaseExecutionOccur();
  CmmnAtomicOperation CASE_EXECUTION_TERMINATE = new AtomicOperationCaseExecutionTerminate();
  CmmnAtomicOperation CASE_EXECUTION_PARENT_TERMINATE = new AtomicOperationCaseExecutionParentTerminate();
  CmmnAtomicOperation CASE_EXECUTION_EXIT = new AtomicOperationCaseExecutionExit();
  CmmnAtomicOperation CASE_EXECUTION_SUSPEND = new AtomicOperationCaseExecutionSuspend();
  CmmnAtomicOperation CASE_EXECUTION_PARENT_SUSPEND = new AtomicOperationCaseExecutionParentSuspend();
  CmmnAtomicOperation CASE_EXECUTION_RESUME = new AtomicOperationCaseExecutionResume();
  CmmnAtomicOperation CASE_EXECUTION_PARENT_RESUME = new AtomicOperationCaseExecutionParentResume();
  CmmnAtomicOperation CASE_EXECUTION_RE_ACTIVATE = new AtomicOperationCaseExecutionReactivate();

  // terminating
  CmmnAtomicOperation CASE_EXECUTION_TERMINATING_ON_TERMINATION = new AtomicOperationCaseExecutionTerminatingOnTermination();
  CmmnAtomicOperation CASE_EXECUTION_TERMINATING_ON_PARENT_TERMINATION = new AtomicOperationCaseExecutionTerminatingOnParentTermination();
  CmmnAtomicOperation CASE_EXECUTION_TERMINATING_ON_EXIT = new AtomicOperationCaseExecutionTerminatingOnExit();
  CmmnAtomicOperation CASE_EXECUTION_PARENT_COMPLETE = new AtomicOperationCaseExecutionParentComplete();

  // suspending
  CmmnAtomicOperation CASE_EXECUTION_SUSPENDING_ON_SUSPENSION = new AtomicOperationCaseExecutionSuspendingOnSuspension();
  CmmnAtomicOperation CASE_EXECUTION_SUSPENDING_ON_PARENT_SUSPENSION = new AtomicOperationCaseExecutionSuspendingOnParentSuspension();

  // sentry
  CmmnAtomicOperation CASE_EXECUTION_FIRE_ENTRY_CRITERIA = new AtomicOperationCaseExecutionFireEntryCriteria();
  CmmnAtomicOperation CASE_EXECUTION_FIRE_EXIT_CRITERIA = new AtomicOperationCaseExecutionFireExitCriteria();


  // delete cascade a case execution
  CmmnAtomicOperation CASE_EXECUTION_DELETE_CASCADE = new AtomicOperationCaseExecutionDeleteCascade();

  public void execute(CmmnExecution execution);

  public boolean isAsync(CmmnExecution execution);

}
