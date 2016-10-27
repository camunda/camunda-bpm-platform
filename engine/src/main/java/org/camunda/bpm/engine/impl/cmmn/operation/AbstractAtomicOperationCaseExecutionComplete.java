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

import static org.camunda.bpm.engine.delegate.CaseExecutionListener.COMPLETE;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.COMPLETED;
import static org.camunda.bpm.engine.impl.util.ActivityBehaviorUtil.getActivityBehavior;

import java.util.List;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cmmn.behavior.CmmnActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.behavior.CmmnCompositeActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.behavior.TransferVariablesActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution;
import org.camunda.bpm.engine.impl.pvm.delegate.SubProcessActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

/**
 * @author Roman Smirnov
 *
 */
public abstract class AbstractAtomicOperationCaseExecutionComplete extends AbstractCmmnEventAtomicOperation {

  protected static final CmmnOperationLogger LOG = ProcessEngineLogger.CMMN_OPERATION_LOGGER;

  protected String getEventName() {
    return COMPLETE;
  }

  protected CmmnExecution eventNotificationsStarted(CmmnExecution execution) {
    CmmnActivityBehavior behavior = getActivityBehavior(execution);
    triggerBehavior(behavior, execution);

    execution.setCurrentState(COMPLETED);

    return execution;
  }

  protected void postTransitionNotification(CmmnExecution execution) {
    if (!execution.isCaseInstanceExecution()) {
      execution.remove();

    } else {
      CmmnExecution superCaseExecution = execution.getSuperCaseExecution();
      PvmExecutionImpl superExecution = execution.getSuperExecution();

      if (superCaseExecution != null) {
        TransferVariablesActivityBehavior behavior = (TransferVariablesActivityBehavior) getActivityBehavior(superCaseExecution);
        behavior.transferVariables(execution, superCaseExecution);
        superCaseExecution.complete();

      } else if (superExecution != null) {
        SubProcessActivityBehavior behavior = (SubProcessActivityBehavior) getActivityBehavior(superExecution);

        try {
          behavior.passOutputVariables(superExecution, execution);
        } catch (RuntimeException e) {
          LOG.completingSubCaseError(execution, e);
          throw e;
        } catch (Exception e) {
          LOG.completingSubCaseError(execution, e);
          throw LOG.completingSubCaseErrorException(execution, e);
        }

        // set sub case instance to null
        superExecution.setSubCaseInstance(null);

        try {
          behavior.completed(superExecution);
        } catch (RuntimeException e) {
          LOG.completingSubCaseError(execution, e);
          throw e;
        } catch (Exception e) {
          LOG.completingSubCaseError(execution, e);
          throw LOG.completingSubCaseErrorException(execution, e);
        }
      }

      execution.setSuperCaseExecution(null);
      execution.setSuperExecution(null);
    }

    CmmnExecution parent = execution.getParent();
    if (parent != null) {
      CmmnActivityBehavior behavior = getActivityBehavior(parent);
      if (behavior instanceof CmmnCompositeActivityBehavior) {
        CmmnCompositeActivityBehavior compositeBehavior = (CmmnCompositeActivityBehavior) behavior;
        compositeBehavior.handleChildCompletion(parent, execution);
      }
    }

  }

  protected abstract void triggerBehavior(CmmnActivityBehavior behavior, CmmnExecution execution);

}
