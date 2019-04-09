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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.event.EventType;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.ActivityInstanceState;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;
import org.camunda.bpm.engine.impl.tree.TreeVisitor;
import org.camunda.bpm.engine.impl.tree.FlowScopeWalker;
import org.camunda.bpm.engine.impl.tree.ReferenceWalker;

/**
 * @author Daniel Meyer
 */
public class CompensationUtil {

  /**
   * name of the signal that is thrown when a compensation handler completed
   */
  public final static String SIGNAL_COMPENSATION_DONE = "compensationDone";

  /**
   * we create a separate execution for each compensation handler invocation.
   */
  public static void throwCompensationEvent(List<EventSubscriptionEntity> eventSubscriptions, ActivityExecution execution, boolean async) {

    // first spawn the compensating executions
    for (EventSubscriptionEntity eventSubscription : eventSubscriptions) {
      // check whether compensating execution is already created
      // (which is the case when compensating an embedded subprocess,
      // where the compensating execution is created when leaving the subprocess
      // and holds snapshot data).
      ExecutionEntity compensatingExecution = getCompensatingExecution(eventSubscription);
      if (compensatingExecution != null) {
        if (compensatingExecution.getParent() != execution) {
          // move the compensating execution under this execution if this is not the case yet
          compensatingExecution.setParent((PvmExecutionImpl) execution);
        }

        compensatingExecution.setEventScope(false);
      } else {
        compensatingExecution = (ExecutionEntity) execution.createExecution();
        eventSubscription.setConfiguration(compensatingExecution.getId());
      }
      compensatingExecution.setConcurrent(true);
    }

    // signal compensation events in REVERSE order of their 'created' timestamp
    Collections.sort(eventSubscriptions, new Comparator<EventSubscriptionEntity>() {
      @Override
      public int compare(EventSubscriptionEntity o1, EventSubscriptionEntity o2) {
        return o2.getCreated().compareTo(o1.getCreated());
      }
    });

    for (EventSubscriptionEntity compensateEventSubscriptionEntity : eventSubscriptions) {
      compensateEventSubscriptionEntity.eventReceived(null, async);
    }
  }

  /**
   * creates an event scope for the given execution:
   *
   * create a new event scope execution under the parent of the given execution
   * and move all event subscriptions to that execution.
   *
   * this allows us to "remember" the event subscriptions after finishing a
   * scope
   */
  public static void createEventScopeExecution(ExecutionEntity execution) {

    // parent execution is a subprocess or a miBody
    ActivityImpl activity = execution.getActivity();
    ExecutionEntity scopeExecution = (ExecutionEntity) execution.findExecutionForFlowScope(activity.getFlowScope());

    List<EventSubscriptionEntity> eventSubscriptions = execution.getCompensateEventSubscriptions();

    if (eventSubscriptions.size() > 0 || hasCompensationEventSubprocess(activity)) {

      ExecutionEntity eventScopeExecution = scopeExecution.createExecution();
      eventScopeExecution.setActivity(execution.getActivity());
      eventScopeExecution.activityInstanceStarting();
      eventScopeExecution.enterActivityInstance();
      eventScopeExecution.setActive(false);
      eventScopeExecution.setConcurrent(false);
      eventScopeExecution.setEventScope(true);

      // copy local variables to eventScopeExecution by value. This way,
      // the eventScopeExecution references a 'snapshot' of the local variables
      Map<String, Object> variables = execution.getVariablesLocal();
      for (Entry<String, Object> variable : variables.entrySet()) {
        eventScopeExecution.setVariableLocal(variable.getKey(), variable.getValue());
      }

      // set event subscriptions to the event scope execution:
      for (EventSubscriptionEntity eventSubscriptionEntity : eventSubscriptions) {
        EventSubscriptionEntity newSubscription =
                EventSubscriptionEntity.createAndInsert(
                        eventScopeExecution,
                        EventType.COMPENSATE,
                        eventSubscriptionEntity.getActivity());
        newSubscription.setConfiguration(eventSubscriptionEntity.getConfiguration());
        // use the original date
        newSubscription.setCreated(eventSubscriptionEntity.getCreated());
      }

      // set existing event scope executions as children of new event scope execution
      // (ensuring they don't get removed when 'execution' gets removed)
      for (PvmExecutionImpl childEventScopeExecution : execution.getEventScopeExecutions()) {
        childEventScopeExecution.setParent(eventScopeExecution);
      }

      ActivityImpl compensationHandler = getEventScopeCompensationHandler(execution);
      EventSubscriptionEntity eventSubscription = EventSubscriptionEntity
              .createAndInsert(
                scopeExecution,
                EventType.COMPENSATE,
                compensationHandler
              );
      eventSubscription.setConfiguration(eventScopeExecution.getId());

    }
  }

  protected static boolean hasCompensationEventSubprocess(ActivityImpl activity) {
    ActivityImpl compensationHandler = activity.findCompensationHandler();

    return compensationHandler != null && compensationHandler.isSubProcessScope() && compensationHandler.isTriggeredByEvent();
  }

  /**
   * In the context when an event scope execution is created (i.e. a scope such as a subprocess has completed),
   * this method returns the compensation handler activity that is going to be executed when by the event scope execution.
   *
   * This method is not relevant when the scope has a boundary compensation handler.
   */
  protected static ActivityImpl getEventScopeCompensationHandler(ExecutionEntity execution) {
    ActivityImpl activity = execution.getActivity();

    ActivityImpl compensationHandler = activity.findCompensationHandler();
    if (compensationHandler != null && compensationHandler.isSubProcessScope()) {
      // subprocess with inner compensation event subprocess
      return compensationHandler;
    } else {
      // subprocess without compensation handler or
      // multi instance activity
      return activity;
    }
  }

  /**
   * Collect all compensate event subscriptions for scope of given execution.
   */
  public static List<EventSubscriptionEntity> collectCompensateEventSubscriptionsForScope(ActivityExecution execution) {

    final Map<ScopeImpl, PvmExecutionImpl> scopeExecutionMapping = execution.createActivityExecutionMapping();
    ScopeImpl activity = (ScopeImpl) execution.getActivity();

    // <LEGACY>: different flow scopes may have the same scope execution =>
    // collect subscriptions in a set
    final Set<EventSubscriptionEntity> subscriptions = new HashSet<EventSubscriptionEntity>();
    TreeVisitor<ScopeImpl> eventSubscriptionCollector = new TreeVisitor<ScopeImpl>() {
      @Override
      public void visit(ScopeImpl obj) {
        PvmExecutionImpl execution = scopeExecutionMapping.get(obj);
        subscriptions.addAll(((ExecutionEntity) execution).getCompensateEventSubscriptions());
      }
    };

    new FlowScopeWalker(activity).addPostVisitor(eventSubscriptionCollector).walkUntil(new ReferenceWalker.WalkCondition<ScopeImpl>() {
      @Override
      public boolean isFulfilled(ScopeImpl element) {
        Boolean consumesCompensationProperty = (Boolean) element.getProperty(BpmnParse.PROPERTYNAME_CONSUMES_COMPENSATION);
        return consumesCompensationProperty == null || consumesCompensationProperty == Boolean.TRUE;
      }
    });

    return new ArrayList<EventSubscriptionEntity>(subscriptions);
  }

  /**
   * Collect all compensate event subscriptions for activity on the scope of
   * given execution.
   */
  public static List<EventSubscriptionEntity> collectCompensateEventSubscriptionsForActivity(ActivityExecution execution, String activityRef) {

    final List<EventSubscriptionEntity> eventSubscriptions = collectCompensateEventSubscriptionsForScope(execution);
    final String subscriptionActivityId = getSubscriptionActivityId(execution, activityRef);

    List<EventSubscriptionEntity> eventSubscriptionsForActivity = new ArrayList<EventSubscriptionEntity>();
    for (EventSubscriptionEntity subscription : eventSubscriptions) {
      if (subscriptionActivityId.equals(subscription.getActivityId())) {
        eventSubscriptionsForActivity.add(subscription);
      }
    }
    return eventSubscriptionsForActivity;
  }

  public static ExecutionEntity getCompensatingExecution(EventSubscriptionEntity eventSubscription) {
    String configuration = eventSubscription.getConfiguration();
    if (configuration != null) {
      return Context.getCommandContext().getExecutionManager().findExecutionById(configuration);
    }
    else {
      return null;
    }
  }

  private static String getSubscriptionActivityId(ActivityExecution execution, String activityRef) {
    ActivityImpl activityToCompensate = ((ExecutionEntity) execution).getProcessDefinition().findActivity(activityRef);

    if (activityToCompensate.isMultiInstance()) {

      ActivityImpl flowScope = (ActivityImpl) activityToCompensate.getFlowScope();
      return flowScope.getActivityId();
    } else {

      ActivityImpl compensationHandler = activityToCompensate.findCompensationHandler();
      if (compensationHandler != null) {
        return compensationHandler.getActivityId();
      } else {
        // if activityRef = subprocess and subprocess has no compensation handler
        return activityRef;
      }
    }
  }

}
