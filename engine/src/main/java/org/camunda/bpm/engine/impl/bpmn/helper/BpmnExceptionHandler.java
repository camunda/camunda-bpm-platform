/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.bpmn.helper;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.bpmn.behavior.BpmnBehaviorLogger;
import org.camunda.bpm.engine.impl.bpmn.parser.ErrorEventDefinition;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;
import org.camunda.bpm.engine.impl.tree.ActivityExecutionHierarchyWalker;
import org.camunda.bpm.engine.impl.tree.ActivityExecutionMappingCollector;
import org.camunda.bpm.engine.impl.tree.ActivityExecutionTuple;
import org.camunda.bpm.engine.impl.tree.OutputVariablesPropagator;
import org.camunda.bpm.engine.impl.tree.ReferenceWalker;

/**
 * Helper class handling the propagation of BPMN Errors.
 */
public class BpmnExceptionHandler {

  private final static BpmnBehaviorLogger LOG = ProcessEngineLogger.BPMN_BEHAVIOR_LOGGER;

  /**
   * Decides how to propagate the exception properly, e.g. as bpmn error or "normal" error.
   * @param execution the current execution
   * @param ex the exception to propagate
   * @throws Exception if no error handler could be found
   */
  public static void propagateException(ActivityExecution execution, Exception ex) throws Exception {
    BpmnError bpmnError = checkIfCauseOfExceptionIsBpmnError(ex);
    if (bpmnError != null) {
      propagateBpmnError(bpmnError, execution);
    } else {
      propagateExceptionAsError(ex, execution);
    }
  }


  protected static void propagateExceptionAsError(Exception exception, ActivityExecution execution) throws Exception {
    if (isProcessEngineExceptionWithoutCause(exception) || isTransactionNotActive()) {
      throw exception;
    }
    else {
      propagateError(null, exception.getMessage(),exception, execution);
    }
  }

  protected static boolean isTransactionNotActive() {
    return !Context.getCommandContext().getTransactionContext().isTransactionActive();
  }

  protected static boolean isProcessEngineExceptionWithoutCause(Exception exception) {
    return exception instanceof ProcessEngineException && exception.getCause() == null;
  }

  /**
   * Searches recursively through the exception to see if the exception itself
   * or one of its causes is a {@link BpmnError}.
   *
   * @param e
   *          the exception to check
   * @return the BpmnError that was the cause of this exception or null if no
   *         BpmnError was found
   */
  protected static BpmnError checkIfCauseOfExceptionIsBpmnError(Throwable e) {
    if (e instanceof BpmnError) {
      return (BpmnError) e;
    } else if (e.getCause() == null) {
      return null;
    }
    return checkIfCauseOfExceptionIsBpmnError(e.getCause());
  }


  public static void propagateBpmnError(BpmnError error, ActivityExecution execution) throws Exception {
    propagateError(error.getErrorCode(), error.getMessage(), null, execution);
  }

  public static void propagateError(String errorCode, String errorMessage, Exception origException, ActivityExecution execution) throws Exception {

    ActivityExecutionHierarchyWalker walker = new ActivityExecutionHierarchyWalker(execution);

    final ErrorDeclarationForProcessInstanceFinder errorDeclarationFinder = new ErrorDeclarationForProcessInstanceFinder(origException, errorCode, execution.getActivity());
    ActivityExecutionMappingCollector activityExecutionMappingCollector = new ActivityExecutionMappingCollector(execution);

    walker.addScopePreVisitor(errorDeclarationFinder);
    walker.addExecutionPreVisitor(activityExecutionMappingCollector);
    // map variables to super executions in the hierarchy of called process instances
    walker.addExecutionPreVisitor(new OutputVariablesPropagator());

    try {

      walker.walkUntil(new ReferenceWalker.WalkCondition<ActivityExecutionTuple>() {

        @Override
        public boolean isFulfilled(ActivityExecutionTuple element) {
          return errorDeclarationFinder.getErrorEventDefinition() != null || element == null;
        }
      });

    } catch(Exception e) {
      LOG.errorPropagationException(execution.getActivityInstanceId(), e);

      // separate the exception handling to support a fail-safe error propagation
      throw new ErrorPropagationException(e.getCause());
    }

    PvmActivity errorHandlingActivity = errorDeclarationFinder.getErrorHandlerActivity();

    // process the error
    if (errorHandlingActivity == null) {
      if (origException == null) {

        if (Context.getCommandContext().getProcessEngineConfiguration().isEnableExceptionsAfterUnhandledBpmnError()) {
          throw LOG.missingBoundaryCatchEventError(execution.getActivity().getId(), errorCode);
        } else {
          LOG.missingBoundaryCatchEvent(execution.getActivity().getId(), errorCode);
          execution.end(true);
        }
      } else {
        // throw original exception
        throw origException;
      }
    }
    else {

      ErrorEventDefinition errorDefinition = errorDeclarationFinder.getErrorEventDefinition();
      PvmExecutionImpl errorHandlingExecution = activityExecutionMappingCollector.getExecutionForScope(errorHandlingActivity.getEventScope());

      if(errorDefinition.getErrorCodeVariable() != null) {
        errorHandlingExecution.setVariable(errorDefinition.getErrorCodeVariable(), errorCode);
      }
      if(errorDefinition.getErrorMessageVariable() != null) {
        errorHandlingExecution.setVariable(errorDefinition.getErrorMessageVariable(), errorMessage);
      }
      errorHandlingExecution.executeActivity(errorHandlingActivity);
    }
  }


}