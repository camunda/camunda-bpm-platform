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

package org.camunda.bpm.engine.impl.bpmn.helper;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.List;
import java.util.logging.Logger;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.bpmn.behavior.EventSubProcessStartEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.bpmn.parser.ErrorEventDefinition;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmProcessDefinition;
import org.camunda.bpm.engine.impl.pvm.PvmScope;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.delegate.SubProcessActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;


/**
 * This class is responsible for finding and executing error handlers for BPMN
 * Errors.
 *
 * Possible error handlers include Error Intermediate Events and Error Event
 * Sub-Processes.
 *
 * @author Falko Menge
 * @author Daniel Meyer
 */
public class ErrorPropagation {

  private static final Logger LOG = Logger.getLogger(ErrorPropagation.class.getName());

  public static void propagateException(Exception ex, ActivityExecution execution) throws Exception {
    if (ex instanceof ProcessEngineException && ex.getCause() == null) {
      throw ex;
    } else {
      propagateError(null, ex, execution);
    }
  }

  public static void propagateError(BpmnError error, ActivityExecution execution) throws Exception {
    propagateError(error.getErrorCode(), null, execution);
  }

  public static void propagateError(String errorCode, Exception origException, ActivityExecution execution) throws Exception {
    // find local error handler
    ErrorEventDefinition eventHandler = findLocalErrorEventHandler(execution, errorCode, origException);

    // TODO: merge two approaches (super process / regular process approach)
    if(eventHandler != null) {
      executeCatch(eventHandler, execution, errorCode);
    }else {
      ActivityExecution superExecution = getSuperExecution(execution);
      if (superExecution != null) {
        executeCatchInSuperProcess(errorCode, origException, superExecution, execution);
      } else {
        if (origException==null) {
          LOG.info(execution.getActivity().getId() + " throws error event with errorCode '"
                  + errorCode + "', but no catching boundary event was defined. "
                  +   "Execution will simply be ended (none end event semantics).");
          execution.end(true);
        } else
          // throw original exception
          throw origException;
      }
    }
  }

  private static ErrorEventDefinition findLocalErrorEventHandler(ActivityExecution execution, String errorCode, Exception origException) {
    PvmScope scope = execution.getActivity();
    while (scope != null) {

      @SuppressWarnings("unchecked")
      List<ErrorEventDefinition> definitions = (List<ErrorEventDefinition>) scope.getProperty(BpmnParse.PROPERTYNAME_ERROR_EVENT_DEFINITIONS);
      if(definitions != null) {
        // definitions are sorted by precedence, ie. event subprocesses first.
        for (ErrorEventDefinition errorEventDefinition : definitions) {
          if(origException != null) {
            if(errorEventDefinition.catchesException(origException)) {
              return errorEventDefinition;
            }
          } else if(errorEventDefinition.catchesError(errorCode)) {
            return errorEventDefinition;
          }
        }
      }

      // search for error handlers in parent scopes
      if (scope instanceof PvmActivity) {
        scope = ((PvmActivity) scope).getParent();
      } else {
        scope = null;
      }
    }
    return null;
  }

  private static void executeCatchInSuperProcess(String errorCode, Exception origException, ActivityExecution superExecution, ActivityExecution execution) throws Exception {
    completeActivity(superExecution, execution);
    ErrorEventDefinition errorHandler = findLocalErrorEventHandler(superExecution, errorCode, origException);
    if (errorHandler != null) {
      executeCatch(errorHandler, superExecution, errorCode);
    } else { // no matching catch found, going one level up in process hierarchy
      ActivityExecution superSuperExecution = getSuperExecution(superExecution);
      if (superSuperExecution != null) {
        executeCatchInSuperProcess(errorCode, origException, superSuperExecution, superExecution);
      } else {
        if (origException == null) {
          throw new BpmnError(errorCode, "No catching boundary event found for error with errorCode '"
                  + errorCode + "', neither in same process nor in parent process");
        } else {
          throw origException;
        }
      }
    }
  }

  /**
   * To pass the out mapping to the parent execution we call {@link SubProcessActivityBehavior#completing(VariableScope, VariableScope)} on
   * the activity.
   * @param superExecution the parent of the current execution
   * @param execution the current execution
   * @throws Exception if something goes wrong inside completing()
   */
  private static void completeActivity(ActivityExecution superExecution, ActivityExecution execution) throws Exception {
    ActivityImpl activity = ((PvmExecutionImpl)superExecution).getActivity();
    SubProcessActivityBehavior subProcessActivityBehavior = (SubProcessActivityBehavior) activity.getActivityBehavior();
    subProcessActivityBehavior.completing(superExecution, execution);
  }

  private static ActivityExecution getSuperExecution(ActivityExecution execution) {
    ExecutionEntity executionEntity = (ExecutionEntity) execution;
    ExecutionEntity superExecution = executionEntity.getProcessInstance().getSuperExecution();
    if (superExecution != null && !superExecution.isScope()) {
      return superExecution.getParent();
    }
    return superExecution;
  }

  private static void executeCatch(ErrorEventDefinition errorEventDefinition, ActivityExecution execution, String errorCode) {
    ProcessDefinitionImpl processDefinition = ((ExecutionEntity) execution).getProcessDefinition();
    ActivityImpl errorHandler = processDefinition.findActivity(errorEventDefinition.getHandlerActivityId());
    ensureNotNull(errorEventDefinition.getHandlerActivityId() + " not found in process definition", "errorHandler", errorHandler);

    boolean matchingParentFound = false;
    ActivityExecution leavingExecution = execution;
    ActivityImpl currentActivity = (ActivityImpl) execution.getActivity();

    ScopeImpl catchingScope = errorHandler.getParent();
    if (catchingScope instanceof ActivityImpl) {
      ActivityImpl catchingScopeActivity = (ActivityImpl) catchingScope;
      if (!catchingScopeActivity.isScope()) { // event subprocesses
        catchingScope = catchingScopeActivity.getParent();
      }
    }

    if (catchingScope instanceof PvmProcessDefinition) {
      ExecutionEntity processInstance = ((ExecutionEntity) execution).getProcessInstance();
      executeEventHandler(errorHandler, processInstance, errorEventDefinition, errorCode);
    } else {
      if (currentActivity.getId().equals(catchingScope.getId())) {
        matchingParentFound = true;
      } else {
        // boundary events are always defined on scopes, so we have to
        // find the next parent activity that is a scope
        currentActivity = currentActivity.getParentScopeActivity();

        // Traverse parents until one is found that is a scope
        // and matches the activity the boundary event is defined on.
        //
        // This loop attempts attempts to find the execution that matches
        // currentActivity and then checks whether this is the scope that
        // is responsible for catching the error (i.e. currentActivity == catchingScope).
        // If not, search is continued in the parent activities and executions.
        while (!matchingParentFound && leavingExecution != null && currentActivity != null) {
          if (!leavingExecution.isConcurrent() && currentActivity.getId().equals(catchingScope.getId())) {
            matchingParentFound = true;
          } else if (leavingExecution.isConcurrent()) {
            leavingExecution = leavingExecution.getParent();

          } else {
            currentActivity = currentActivity.getParentScopeActivity();
            leavingExecution = leavingExecution.getParent();
          }
        }

        // Follow parents up until matching scope can't be found anymore (needed to support for multi-instance)
        while (leavingExecution != null
          && leavingExecution.getParent() != null
          && leavingExecution.getParent().getActivity() != null
          && leavingExecution.getParent().getActivity().getId().equals(catchingScope.getId())) {
          leavingExecution = leavingExecution.getParent();
        }
      }

      if (matchingParentFound && leavingExecution != null) {
        executeEventHandler(errorHandler, leavingExecution, errorEventDefinition, errorCode);
      } else {
        throw new ProcessEngineException("No matching parent execution for activity " + errorEventDefinition + " found");
      }
    }
  }

  private static void executeEventHandler(ActivityImpl borderEventActivity, ActivityExecution leavingExecution, ErrorEventDefinition errorEventDefinition, String errorCode) {
    setErrorCodeVariable(leavingExecution, errorEventDefinition, errorCode);
    if(borderEventActivity.getActivityBehavior() instanceof EventSubProcessStartEventActivityBehavior) {
      leavingExecution.executeActivity(borderEventActivity.getParentActivity());
    }else {
      leavingExecution.executeActivity(borderEventActivity);
    }
  }

  /**
   * If the attribute camunda:errorCodeVariable is set on the error event definition on the catching boundary event
   * we set the error code under that name in the parent scope.
   * @param leavingExecution
   * @param errorEventDefinition
   * @param errorCode
   */
  private static void setErrorCodeVariable(ActivityExecution leavingExecution, ErrorEventDefinition errorEventDefinition, String errorCode) {
    if(errorEventDefinition.getErrorCodeVariable() != null){
      leavingExecution.setVariable(errorEventDefinition.getErrorCodeVariable(), errorCode);
    }
  }

}
