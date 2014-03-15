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

package org.camunda.bpm.engine.impl.bpmn.behavior;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.impl.bpmn.helper.ErrorPropagation;
import org.camunda.bpm.engine.impl.bpmn.helper.ScopeUtil;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.delegate.ExecutionListenerInvocation;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.delegate.CompositeActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.SubProcessActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;


/**
 * Implementation of the multi-instance functionality as described in the BPMN 2.0 spec.
 *
 * Multi instance functionality is implemented as an {@link ActivityBehavior} that
 * wraps the original {@link ActivityBehavior} of the activity.
 *
 * Only subclasses of {@link AbstractBpmnActivityBehavior} can have multi-instance
 * behavior. As such, special logic is contained in the {@link AbstractBpmnActivityBehavior}
 * to delegate to the {@link MultiInstanceActivityBehavior} if needed.
 *
 * @author Joram Barrez
 * @author Falko Menge
 */
public abstract class MultiInstanceActivityBehavior extends FlowNodeActivityBehavior
  implements CompositeActivityBehavior, SubProcessActivityBehavior {

  protected static final Logger LOGGER = Logger.getLogger(MultiInstanceActivityBehavior.class.getName());

  // Variable names for outer instance(as described in spec)
  protected final String NUMBER_OF_INSTANCES = "nrOfInstances";
  protected final String NUMBER_OF_ACTIVE_INSTANCES = "nrOfActiveInstances";
  protected final String NUMBER_OF_COMPLETED_INSTANCES = "nrOfCompletedInstances";

  // Variable names for inner instances (as described in the spec)
  protected final String LOOP_COUNTER = "loopCounter";

  // Instance members
  protected ActivityImpl activity;
  protected AbstractBpmnActivityBehavior innerActivityBehavior;
  protected Expression loopCardinalityExpression;
  protected Expression completionConditionExpression;
  protected Expression collectionExpression;
  protected String collectionVariable;
  protected String collectionElementVariable;

  /**
   * @param innerActivityBehavior The original {@link ActivityBehavior} of the activity
   *                         that will be wrapped inside this behavior.
   * @param isSequential Indicates whether the multi instance behavior
   *                     must be sequential or parallel
   */
  public MultiInstanceActivityBehavior(ActivityImpl activity, AbstractBpmnActivityBehavior innerActivityBehavior) {
    this.activity = activity;
    setInnerActivityBehavior(innerActivityBehavior);
  }

  public void execute(ActivityExecution execution) throws Exception {
    if (getLocalLoopVariable(execution, LOOP_COUNTER) == null) {
      try {
        if (!createInstancesIfPossible(execution)) {
          // leave the state through the default behavior (so we have to call super.!)
          // no multiple instance needed in this case
          super.leave(execution);
        }
      } catch (BpmnError error) {
        ErrorPropagation.propagateError(error, execution);
      } catch (Exception ex) {
        ErrorPropagation.propagateException(ex, execution);
      }
    } else {
        innerActivityBehavior.execute(execution);
    }
  }

  /**
   * protects the createInstance method - it is only called if the numberOfInstances is valid
   * (negative is invalid -> exception; zero means the sub instances are skipped)
   *
   * Returns true if instances were created
   */
  protected boolean createInstancesIfPossible(ActivityExecution execution) throws Exception {
    int nrOfInstances = resolveNrOfInstances(execution);
    if (nrOfInstances ==0) {
      // we have nothing to do here - but it is valid to hand in empty collections according to the BPMN specification
      return false;
    }
    else if (nrOfInstances < 0) {
      throw new ProcessEngineException("Invalid number of instances: must be positive integer value or zero"
              + ", but was " + nrOfInstances);
    }
    createInstances(execution, nrOfInstances);
    return true;
  }

  protected abstract void createInstances(ActivityExecution execution, int nrOfInstances) throws Exception;

  // Intercepts signals, and delegates it to the wrapped {@link ActivityBehavior}.
  public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
    innerActivityBehavior.signal(execution, signalName, signalData);
  }

  // required for supporting embedded subprocesses
  public void lastExecutionEnded(ActivityExecution execution) {
    ScopeUtil.createEventScopeExecution((ExecutionEntity) execution);
    leave(execution);
  }

  // required for supporting external subprocesses
  public void completing(DelegateExecution execution, DelegateExecution subProcessInstance) throws Exception {
  }

  // required for supporting external subprocesses
  public void completed(ActivityExecution execution) throws Exception {
    leave(execution);
  }

  // Helpers //////////////////////////////////////////////////////////////////////

  @SuppressWarnings("rawtypes")
  protected int resolveNrOfInstances(ActivityExecution execution) {
    int nrOfInstances = -1;
    if (loopCardinalityExpression != null) {
      nrOfInstances = resolveLoopCardinality(execution);
    } else if (collectionExpression != null) {
      Object obj = collectionExpression.getValue(execution);
      if (!(obj instanceof Collection)) {
        throw new ProcessEngineException(collectionExpression.getExpressionText()+"' didn't resolve to a Collection");
      }
      nrOfInstances = ((Collection) obj).size();
    } else if (collectionVariable != null) {
      Object obj = execution.getVariable(collectionVariable);
      if (!(obj instanceof Collection)) {
        throw new ProcessEngineException("Variable " + collectionVariable+"' is not a Collection");
      }
      nrOfInstances = ((Collection) obj).size();
    } else {
      throw new ProcessEngineException("Couldn't resolve collection expression nor variable reference");
    }
    return nrOfInstances;
  }

  @SuppressWarnings("rawtypes")
  protected void executeOriginalBehavior(ActivityExecution execution, int loopCounter) throws Exception {
    if (usesCollection() && collectionElementVariable != null) {
      Collection collection = null;
      if (collectionExpression != null) {
        collection = (Collection) collectionExpression.getValue(execution);
      } else if (collectionVariable != null) {
        collection = (Collection) execution.getVariable(collectionVariable);
      }

      Object value = null;
      int index = 0;
      Iterator it = collection.iterator();
      while (index <= loopCounter) {
        value = it.next();
        index++;
      }
      setLoopVariable(execution, collectionElementVariable, value);
    }

    // If loopcounter == 1, then historic activity instance already created, no need to
    // pass through executeActivity again since it will create a new historic activity
    if (loopCounter == 0) {
      innerActivityBehavior.execute(execution);
    } else {
      execution.executeActivity(activity);
    }
  }

  protected boolean usesCollection() {
    return collectionExpression != null
              || collectionVariable != null;
  }

  public boolean isExtraScopeNeeded() {
    // special care is needed when the behavior is an embedded subprocess (not very clean, but it works)
    return innerActivityBehavior instanceof org.camunda.bpm.engine.impl.bpmn.behavior.SubProcessActivityBehavior;
  }

  protected int resolveLoopCardinality(ActivityExecution execution) {
    // Using Number since expr can evaluate to eg. Long (which is also the default for Juel)
    Object value = loopCardinalityExpression.getValue(execution);
    if (value instanceof Number) {
      return ((Number) value).intValue();
    } else if (value instanceof String) {
      return Integer.valueOf((String) value);
    } else {
      throw new ProcessEngineException("Could not resolve loopCardinality expression '"
              +loopCardinalityExpression.getExpressionText()+"': not a number nor number String");
    }
  }

  protected boolean completionConditionSatisfied(ActivityExecution execution) {
    if (completionConditionExpression != null) {
      Object value = completionConditionExpression.getValue(execution);
      if (! (value instanceof Boolean)) {
        throw new ProcessEngineException("completionCondition '"
                + completionConditionExpression.getExpressionText()
                + "' does not evaluate to a boolean value");
      }
      Boolean booleanValue = (Boolean) value;
      if (LOGGER.isLoggable(Level.FINE)) {
        LOGGER.fine("Completion condition of multi-instance satisfied: " + booleanValue);
      }
      return booleanValue;
    }
    return false;
  }

  protected void setLoopVariable(ActivityExecution execution, String variableName, Object value) {
    execution.setVariableLocal(variableName, value);
  }

  protected Integer getLoopVariable(ActivityExecution execution, String variableName) {
    Object value = execution.getVariableLocal(variableName);
    ActivityExecution parent = execution.getParent();
    while (value == null && parent != null) {
      value = parent.getVariableLocal(variableName);
      parent = parent.getParent();
    }

    if (value == null) {
      throw new ProcessEngineException("The variable \"" + variableName + "\" could not be found in execution with id " + execution.getId());
    }

    return (Integer) value;
  }

  protected Integer getLocalLoopVariable(ActivityExecution execution, String variableName) {
    return (Integer) execution.getVariableLocal(variableName);
  }

  /**
   * Since no transitions are followed when leaving the inner activity,
   * it is needed to call the end listeners yourself.
   */
  protected void callActivityEndListeners(ActivityExecution execution) {
    // TODO: This is currently done without a proper {@link AtomicOperation} causing problems, see http://jira.codehaus.org/browse/ACT-1339
    List<ExecutionListener> listeners = activity.getExecutionListeners(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_END);
    for (ExecutionListener executionListener : listeners) {
      try {
        Context.getProcessEngineConfiguration()
          .getDelegateInterceptor()
          .handleInvocation(new ExecutionListenerInvocation(executionListener, execution));
      } catch (Exception e) {
        throw new ProcessEngineException("Couldn't execute end listener", e);
      }
    }
  }

  protected void logLoopDetails(ActivityExecution execution, String custom, int loopCounter,
          int nrOfCompletedInstances, int nrOfActiveInstances, int nrOfInstances) {
    if (LOGGER.isLoggable(Level.FINE)) {
      StringBuilder strb = new StringBuilder();
      strb.append("Multi-instance '" + execution.getActivity() + "' " + custom + ". ");
      strb.append("Details: loopCounter=" + loopCounter + ", ");
      strb.append("nrOrCompletedInstances=" + nrOfCompletedInstances + ", ");
      strb.append("nrOfActiveInstances=" + nrOfActiveInstances+ ", ");
      strb.append("nrOfInstances=" + nrOfInstances);
      LOGGER.fine(strb.toString());
    }
  }


  // Getters and Setters ///////////////////////////////////////////////////////////

  public Expression getLoopCardinalityExpression() {
    return loopCardinalityExpression;
  }
  public void setLoopCardinalityExpression(Expression loopCardinalityExpression) {
    this.loopCardinalityExpression = loopCardinalityExpression;
  }
  public Expression getCompletionConditionExpression() {
    return completionConditionExpression;
  }
  public void setCompletionConditionExpression(Expression completionConditionExpression) {
    this.completionConditionExpression = completionConditionExpression;
  }
  public Expression getCollectionExpression() {
    return collectionExpression;
  }
  public void setCollectionExpression(Expression collectionExpression) {
    this.collectionExpression = collectionExpression;
  }
  public String getCollectionVariable() {
    return collectionVariable;
  }
  public void setCollectionVariable(String collectionVariable) {
    this.collectionVariable = collectionVariable;
  }
  public String getCollectionElementVariable() {
    return collectionElementVariable;
  }
  public void setCollectionElementVariable(String collectionElementVariable) {
    this.collectionElementVariable = collectionElementVariable;
  }
  public void setInnerActivityBehavior(AbstractBpmnActivityBehavior innerActivityBehavior) {
    this.innerActivityBehavior = innerActivityBehavior;
    this.innerActivityBehavior.setMultiInstanceActivityBehavior(this);
  }

}
