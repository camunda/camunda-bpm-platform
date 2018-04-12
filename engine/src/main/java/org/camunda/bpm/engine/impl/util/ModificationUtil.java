package org.camunda.bpm.engine.impl.util;

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.delegate.ModificationObserverBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

/**
 * @author Svetlana Dorokhova.
 */
public abstract class ModificationUtil {

  public static void handleChildRemovalInScope(ExecutionEntity removedExecution) {
    ActivityImpl activity = removedExecution.getActivity();
    if (activity == null) {
      if (removedExecution.getSuperExecution() != null) {
        removedExecution = removedExecution.getSuperExecution();
        activity = removedExecution.getActivity();
        if (activity == null) {
          return;
        }
      } else {
        return;
      }
    }
    ScopeImpl flowScope = activity.getFlowScope();

    PvmExecutionImpl scopeExecution = removedExecution.getParentScopeExecution(false);
    PvmExecutionImpl executionInParentScope = removedExecution.isConcurrent() ? removedExecution : removedExecution.getParent();

    if (flowScope.getActivityBehavior() != null && flowScope.getActivityBehavior() instanceof ModificationObserverBehavior) {
      // let child removal be handled by the scope itself
      ModificationObserverBehavior behavior = (ModificationObserverBehavior) flowScope.getActivityBehavior();
      behavior.destroyInnerInstance(executionInParentScope);
    }
    else {
      if (executionInParentScope.isConcurrent()) {
        executionInParentScope.remove();
        scopeExecution.tryPruneLastConcurrentChild();
        scopeExecution.forceUpdate();
      }
    }
  }
}
