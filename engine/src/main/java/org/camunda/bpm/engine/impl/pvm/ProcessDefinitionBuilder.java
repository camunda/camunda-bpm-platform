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
package org.camunda.bpm.engine.impl.pvm;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.core.model.CoreModelElement;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ActivityStartBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;



/**
 * @author Tom Baeyens
 */
@SuppressWarnings("deprecation")
public class ProcessDefinitionBuilder {

  protected ProcessDefinitionImpl processDefinition;
  protected Deque<ScopeImpl> scopeStack = new ArrayDeque<>();
  protected CoreModelElement processElement = processDefinition;
  protected TransitionImpl transition;
  protected List<Object[]> unresolvedTransitions = new ArrayList<>();

  public ProcessDefinitionBuilder() {
    this(null);
  }

  public ProcessDefinitionBuilder(String processDefinitionId) {
    processDefinition = new ProcessDefinitionImpl(processDefinitionId);
    scopeStack.push(processDefinition);
  }

  public ProcessDefinitionBuilder createActivity(String id) {
    ActivityImpl activity = scopeStack.peek().createActivity(id);
    scopeStack.push(activity);
    processElement = activity;

    transition = null;

    return this;
  }

  public ProcessDefinitionBuilder attachedTo(String id, boolean isInterrupting) {
    ActivityImpl activity = getActivity();
    activity.setEventScope(processDefinition.findActivity(id));

    if(isInterrupting) {
      activity.setActivityStartBehavior(ActivityStartBehavior.INTERRUPT_EVENT_SCOPE);
    }
    else {
      activity.setActivityStartBehavior(ActivityStartBehavior.CONCURRENT_IN_FLOW_SCOPE);
    }

    return this;
  }


  public ProcessDefinitionBuilder endActivity() {
    scopeStack.pop();
    processElement = scopeStack.peek();

    transition = null;

    return this;
  }

  public ProcessDefinitionBuilder initial() {
    processDefinition.setInitial(getActivity());
    return this;
  }

  public ProcessDefinitionBuilder startTransition(String destinationActivityId) {
    return startTransition(destinationActivityId, null);
  }

  public ProcessDefinitionBuilder startTransition(String destinationActivityId, String transitionId) {
    if (destinationActivityId==null) {
      throw new PvmException("destinationActivityId is null");
    }
    ActivityImpl activity = getActivity();
    transition = activity.createOutgoingTransition(transitionId);
    unresolvedTransitions.add(new Object[]{transition, destinationActivityId});
    processElement = transition;
    return this;
  }

  public ProcessDefinitionBuilder endTransition() {
    processElement = scopeStack.peek();
    transition = null;
    return this;
  }

  public ProcessDefinitionBuilder transition(String destinationActivityId) {
    return transition(destinationActivityId, null);
  }

  public ProcessDefinitionBuilder transition(String destinationActivityId, String transitionId) {
    startTransition(destinationActivityId, transitionId);
    endTransition();
    return this;
  }

  public ProcessDefinitionBuilder behavior(ActivityBehavior activityBehaviour) {
    getActivity().setActivityBehavior(activityBehaviour);
    return this;
  }

  public ProcessDefinitionBuilder property(String name, Object value) {
    processElement.setProperty(name, value);
    return this;
  }

  public PvmProcessDefinition buildProcessDefinition() {
    for (Object[] unresolvedTransition: unresolvedTransitions) {
      TransitionImpl transition = (TransitionImpl) unresolvedTransition[0];
      String destinationActivityName = (String) unresolvedTransition[1];
      ActivityImpl destination = processDefinition.findActivity(destinationActivityName);
      if (destination == null) {
        throw new RuntimeException("destination '"+destinationActivityName+"' not found.  (referenced from transition in '"+transition.getSource().getId()+"')");
      }
      transition.setDestination(destination);
    }
    return processDefinition;
  }

  protected ActivityImpl getActivity() {
    return (ActivityImpl) scopeStack.peek();
  }

  public ProcessDefinitionBuilder scope() {
    getActivity().setScope(true);
    return this;
  }

  public ProcessDefinitionBuilder executionListener(ExecutionListener executionListener) {
    if (transition!=null) {
      transition.addExecutionListener(executionListener);
    } else {
      throw new PvmException("not in a transition scope");
    }
    return this;
  }

  public ProcessDefinitionBuilder executionListener(String eventName, ExecutionListener executionListener) {
    if (transition==null) {
      scopeStack.peek().addExecutionListener(eventName, executionListener);
    } else {
      transition.addExecutionListener(executionListener);
    }
    return this;
  }
}
