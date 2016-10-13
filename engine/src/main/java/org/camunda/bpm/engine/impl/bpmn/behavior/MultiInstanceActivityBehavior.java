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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.bpmn.helper.CompensationUtil;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.delegate.CompositeActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.ModificationObserverBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.variable.value.IntegerValue;


/**
 * Abstract Multi Instance Behavior: used for both parallel and sequential
 * multi instance implementation.
 *
 * @author Daniel Meyer
 * @author Thorben Lindhauer
 */
public abstract class MultiInstanceActivityBehavior extends AbstractBpmnActivityBehavior implements CompositeActivityBehavior, ModificationObserverBehavior {

  protected static final BpmnBehaviorLogger LOG = ProcessEngineLogger.BPMN_BEHAVIOR_LOGGER;

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

  @Override
  public void execute(ActivityExecution execution) throws Exception {
    int nrOfInstances = resolveNrOfInstances(execution);
    if (nrOfInstances == 0) {
      leave(execution);
    }
    else if (nrOfInstances < 0) {
      throw LOG.invalidAmountException("instances", nrOfInstances);
    }
    else {
      createInstances(execution, nrOfInstances);
    }
  }

  protected void performInstance(ActivityExecution execution, PvmActivity activity, int loopCounter) {
    setLoopVariable(execution, LOOP_COUNTER, loopCounter);
    evaluateCollectionVariable(execution, loopCounter);
    execution.setEnded(false);
    execution.setActive(true);
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
        throw LOG.unresolvableExpressionException(collectionExpression.getExpressionText(), "Collection");
      }
      nrOfInstances = ((Collection<?>) obj).size();
    } else if (collectionVariable != null) {
      Object obj = execution.getVariable(collectionVariable);
      if (!(obj instanceof Collection)) {
        throw LOG.invalidVariableTypeException(collectionVariable, "Collection");
      }
      nrOfInstances = ((Collection<?>) obj).size();
    } else {
      throw LOG.resolveCollectionExpressionOrVariableReferenceException();
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
      throw LOG.expressionNotANumberException("loopCardinality", loopCardinalityExpression.getExpressionText());
    }
  }

  protected boolean completionConditionSatisfied(ActivityExecution execution) {
    if (completionConditionExpression != null) {
      Object value = completionConditionExpression.getValue(execution);
      if (! (value instanceof Boolean)) {
        throw LOG.expressionNotBooleanException("completionCondition", completionConditionExpression.getExpressionText());
      }
      Boolean booleanValue = (Boolean) value;

      LOG.multiInstanceCompletionConditionState(booleanValue);
      return booleanValue;
    }
    return false;
  }

  @Override
  public void doLeave(ActivityExecution execution) {
    CompensationUtil.createEventScopeExecution((ExecutionEntity) execution);

    super.doLeave(execution);
  }

  /**
   * Get the inner activity of the multi instance execution.
   *
   * @param execution
   *          of multi instance activity
   * @return inner activity
   */
  public ActivityImpl getInnerActivity(PvmActivity miBodyActivity) {
    for (PvmActivity activity : miBodyActivity.getActivities()) {
      ActivityImpl innerActivity = (ActivityImpl) activity;
      // note that miBody can contains also a compensation handler
      if (!innerActivity.isCompensationHandler()) {
        return innerActivity;
      }
    }
    throw new ProcessEngineException("inner activity of multi instance body activity '" + miBodyActivity.getId() + "' not found");
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

  public boolean hasLoopVariable(ActivityExecution execution, String variableName) {
    return execution.hasVariableLocal(variableName);
  }

  public void removeLoopVariable(ActivityExecution execution, String variableName) {
    execution.removeVariableLocal(variableName);
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
