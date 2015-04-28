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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.delegate.CompositeActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.ModificationObserverBehavior;
import org.camunda.bpm.engine.variable.value.IntegerValue;


/**
 * Abstract Multi Instance Behavior: used for both parallel and sequential
 * multi instance implementation.
 *
 * @author Daniel Meyer
 * @author Thorben Lindhauer
 */
public abstract class MultiInstanceActivityBehavior extends FlowNodeActivityBehavior implements CompositeActivityBehavior, ModificationObserverBehavior {

  protected static final Logger LOGGER = Logger.getLogger(MultiInstanceActivityBehavior.class.getName());

  // Variable names for mi-body scoped variables (as described in spec)
  public static final String NUMBER_OF_INSTANCES = "nrOfInstances";
  public static final String NUMBER_OF_ACTIVE_INSTANCES = "nrOfActiveInstances";
  public static final String NUMBER_OF_COMPLETED_INSTANCES = "nrOfCompletedInstances";

  // Variable names for mi-instance scoped variables (as described in the spec)
  public static final String LOOP_COUNTER = "loopCounter";

  protected Expression loopCardinalityExpression;
  protected Expression completionConditionExpression;
  protected Expression collectionExpression;
  protected String collectionVariable;
  protected String collectionElementVariable;

  public void execute(ActivityExecution execution) throws Exception {
    int nrOfInstances = resolveNrOfInstances(execution);
    if (nrOfInstances == 0) {
      leave(execution);
    }
    else if (nrOfInstances < 0) {
      throw new ProcessEngineException("Invalid number of instances: must be positive integer value or zero"
              + ", but was " + nrOfInstances);
    }
    else {
      createInstances(execution, nrOfInstances);
    }
  }

  protected void performInstance(ActivityExecution execution, PvmActivity activity, int loopCounter) {
    setLoopVariable(execution, LOOP_COUNTER, loopCounter);
    evaluateCollectionVariable(execution, loopCounter);
    execution.executeActivity(activity);
  }

  protected void evaluateCollectionVariable(ActivityExecution execution, int loopCounter) {
    if (usesCollection() && collectionElementVariable != null) {
      Collection<?> collection = null;
      if (collectionExpression != null) {
        collection = (Collection<?>) collectionExpression.getValue(execution);
      } else if (collectionVariable != null) {
        collection = (Collection<?>) execution.getVariable(collectionVariable);
      }

      Object value = getElementAtIndex(loopCounter, collection);
      setLoopVariable(execution, collectionElementVariable, value);
    }
  }

  protected abstract void createInstances(ActivityExecution execution, int nrOfInstances) throws Exception;

  // Helpers //////////////////////////////////////////////////////////////////////

  protected int resolveNrOfInstances(ActivityExecution execution) {
    int nrOfInstances = -1;
    if (loopCardinalityExpression != null) {
      nrOfInstances = resolveLoopCardinality(execution);
    } else if (collectionExpression != null) {
      Object obj = collectionExpression.getValue(execution);
      if (!(obj instanceof Collection)) {
        throw new ProcessEngineException(collectionExpression.getExpressionText()+"' didn't resolve to a Collection");
      }
      nrOfInstances = ((Collection<?>) obj).size();
    } else if (collectionVariable != null) {
      Object obj = execution.getVariable(collectionVariable);
      if (!(obj instanceof Collection)) {
        throw new ProcessEngineException("Variable " + collectionVariable+"' is not a Collection");
      }
      nrOfInstances = ((Collection<?>) obj).size();
    } else {
      throw new ProcessEngineException("Couldn't resolve collection expression nor variable reference");
    }
    return nrOfInstances;
  }

  protected Object getElementAtIndex(int i, Collection<?> collection) {
    Object value = null;
    int index = 0;
    Iterator<?> it = collection.iterator();
    while (index <= i) {
      value = it.next();
      index++;
    }
    return value;
  }

  protected boolean usesCollection() {
    return collectionExpression != null
              || collectionVariable != null;
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
    IntegerValue value = execution.getVariableLocalTyped(variableName);
    ensureNotNull("The variable \"" + variableName + "\" could not be found in execution with id " + execution.getId(), "value", value);
    return value.getValue();
  }

  protected Integer getLocalLoopVariable(ActivityExecution execution, String variableName) {
    return (Integer) execution.getVariableLocal(variableName);
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

}
