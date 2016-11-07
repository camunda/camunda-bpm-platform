/*
 * Copyright 2016 camunda services GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
package org.camunda.bpm.engine.impl.bpmn.parser;

import java.io.Serializable;
import java.util.Set;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.Condition;
import org.camunda.bpm.engine.impl.core.variable.event.VariableEvent;
import org.camunda.bpm.engine.impl.event.EventType;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;

/**
 * Represents the conditional event definition corresponding to the
 * ConditionalEvent defined by the BPMN 2.0 spec.
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class ConditionalEventDefinition extends EventSubscriptionDeclaration implements Serializable {

  private static final long serialVersionUID = 1L;

  protected final Condition condition;
  protected boolean interrupting;
  protected String variableName;
  protected Set<String> variableEvents;
  protected ActivityImpl activity;

  public ConditionalEventDefinition(Condition condition, ActivityImpl activity) {
    super(null, EventType.CONDITONAL);
    this.activityId = activity.getActivityId();
    this.activity = activity;
    this.condition = condition;
  }

  public ActivityImpl getActivity() {
    return activity;
  }

  public void setActivity(ActivityImpl activity) {
    this.activity = activity;
  }

  public boolean isInterrupting() {
    return interrupting;
  }

  public void setInterrupting(boolean interrupting) {
    this.interrupting = interrupting;
  }

  public String getVariableName() {
    return variableName;
  }

  public void setVariableName(String variableName) {
    this.variableName = variableName;
  }

  public Set<String> getVariableEvents() {
    return variableEvents;
  }

  public void setVariableEvents(Set<String> variableEvents) {
    this.variableEvents = variableEvents;
  }

  public boolean shouldEvaluateForVariableEvent(VariableEvent event) {
    return
    ((variableName == null || event.getVariableInstance().getName().equals(variableName))
                                          &&
    ((variableEvents == null || variableEvents.isEmpty()) || variableEvents.contains(event.getEventName())));
  }

  public boolean evaluate(DelegateExecution execution) {
    if (condition != null) {
      return condition.evaluate(execution, execution);
    }
    throw new IllegalStateException("Condtional event must have a condition!");
  }

  public boolean tryEvaluate(DelegateExecution execution) {
    if (condition != null) {
      return condition.tryEvaluate(execution, execution);
    }
    throw new IllegalStateException("Condtional event must have a condition!");
  }

  public boolean tryEvaluate(VariableEvent variableEvent, DelegateExecution execution) {
    return shouldEvaluateForVariableEvent(variableEvent) && tryEvaluate(execution);
  }
}
