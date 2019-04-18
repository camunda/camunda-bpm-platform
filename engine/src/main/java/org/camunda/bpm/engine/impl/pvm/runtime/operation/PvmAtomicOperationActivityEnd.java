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
package org.camunda.bpm.engine.impl.pvm.runtime.operation;

import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmScope;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.CompositeActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.LegacyBehavior;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 * @author Thorben Lindhauer
 */
public class PvmAtomicOperationActivityEnd implements PvmAtomicOperation {

  protected PvmScope getScope(PvmExecutionImpl execution) {
    return execution.getActivity();
  }

  public boolean isAsync(PvmExecutionImpl execution) {
    return execution.getActivity().isAsyncAfter();
  }

  public boolean isAsyncCapable() {
    return false;
  }

  public void execute(PvmExecutionImpl execution) {
    // restore activity instance id
    if (execution.getActivityInstanceId() == null) {
      execution.setActivityInstanceId(execution.getParentActivityInstanceId());
    }

    PvmActivity activity = execution.getActivity();
    Map<ScopeImpl, PvmExecutionImpl> activityExecutionMapping = execution.createActivityExecutionMapping();

    PvmExecutionImpl propagatingExecution = execution;

    if(execution.isScope() && activity.isScope()) {
      if (!LegacyBehavior.destroySecondNonScope(execution)) {
        execution.destroy();
        if(!execution.isConcurrent()) {
          execution.remove();
          propagatingExecution = execution.getParent();
          propagatingExecution.setActivity(execution.getActivity());
        }
      }
    }

    propagatingExecution = LegacyBehavior.determinePropagatingExecutionOnEnd(propagatingExecution, activityExecutionMapping);
    PvmScope flowScope = activity.getFlowScope();

    // 1. flow scope = Process Definition
    if(flowScope == activity.getProcessDefinition()) {

      // 1.1 concurrent execution => end + tryPrune()
      if(propagatingExecution.isConcurrent()) {
        propagatingExecution.remove();
        propagatingExecution.getParent().tryPruneLastConcurrentChild();
        propagatingExecution.getParent().forceUpdate();
      }
      else {
        // 1.2 Process End
        propagatingExecution.setEnded(true);
        if (!propagatingExecution.isPreserveScope()) {
          propagatingExecution.performOperation(PROCESS_END);
        }
      }
    }
    else {
      // 2. flowScope != process definition
      PvmActivity flowScopeActivity = (PvmActivity) flowScope;

      ActivityBehavior activityBehavior = flowScopeActivity.getActivityBehavior();
      if (activityBehavior instanceof CompositeActivityBehavior) {
        CompositeActivityBehavior compositeActivityBehavior = (CompositeActivityBehavior) activityBehavior;
        // 2.1 Concurrent execution => composite behavior.concurrentExecutionEnded()
        if(propagatingExecution.isConcurrent() && !LegacyBehavior.isConcurrentScope(propagatingExecution)) {
          compositeActivityBehavior.concurrentChildExecutionEnded(propagatingExecution.getParent(), propagatingExecution);
        }
        else {
          // 2.2 Scope Execution => composite behavior.complete()
          propagatingExecution.setActivity(flowScopeActivity);
          compositeActivityBehavior.complete(propagatingExecution);
        }

      }
      else {
        // activity behavior is not composite => this is unexpected
        throw new ProcessEngineException("Expected behavior of composite scope "+activity
            +" to be a CompositeActivityBehavior but got "+activityBehavior);
      }
    }
  }

  public String getCanonicalName() {
    return "activity-end";
  }

}
