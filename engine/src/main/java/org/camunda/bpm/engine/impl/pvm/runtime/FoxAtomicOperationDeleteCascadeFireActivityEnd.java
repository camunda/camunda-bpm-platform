package org.camunda.bpm.engine.impl.pvm.runtime;

import java.util.List;

import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.pvm.PvmException;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;


public class FoxAtomicOperationDeleteCascadeFireActivityEnd extends AtomicOperationDeleteCascadeFireActivityEnd {
  
  @Override
  protected void eventNotificationsCompleted(InterpretableExecution execution) {
    ActivityImpl activity = (ActivityImpl) execution.getActivity();
    if ( (execution.isScope())
            && (activity!=null)
            && (!activity.isScope())
          )  {
      execution.setActivity(activity.getParentActivity());
      execution.performOperation(this);
      
    } else {
      if (execution.isScope()) {
        execution.destroy();
      }
 
      execution.remove();
    }
  }
  
  @Override
  public void execute(InterpretableExecution execution) {
    ScopeImpl scope = getScope(execution);
    int executionListenerIndex = execution.getExecutionListenerIndex();
    List<ExecutionListener> executionListeners = scope.getExecutionListeners(getEventName());
    for (ExecutionListener listener : executionListeners) {
      execution.setEventName(getEventName());
      execution.setEventSource(scope);
      try {
        listener.notify(execution);
      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        throw new PvmException("couldn't execute event listener : "+e.getMessage(), e);
      }
      executionListenerIndex += 1;
      execution.setExecutionListenerIndex(executionListenerIndex);
    }
    execution.setExecutionListenerIndex(0);
    execution.setEventName(null);
    execution.setEventSource(null);

    eventNotificationsCompleted(execution);
  }


}
