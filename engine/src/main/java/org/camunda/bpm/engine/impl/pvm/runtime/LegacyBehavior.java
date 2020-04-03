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
package org.camunda.bpm.engine.impl.pvm.runtime;

import static org.camunda.bpm.engine.impl.bpmn.helper.CompensationUtil.SIGNAL_COMPENSATION_DONE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.bpmn.behavior.BpmnBehaviorLogger;
import org.camunda.bpm.engine.impl.bpmn.behavior.CancelBoundaryEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.CancelEndEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.CompensationEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.EventSubProcessActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.MultiInstanceActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ReceiveTaskActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.SequentialMultiInstanceActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.SubProcessActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.cmd.GetActivityInstanceCmd;
import org.camunda.bpm.engine.impl.jobexecutor.AsyncContinuationJobHandler;
import org.camunda.bpm.engine.impl.persistence.entity.ActivityInstanceImpl;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceHistoryListener;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmScope;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.delegate.CompositeActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.tree.ExecutionWalker;
import org.camunda.bpm.engine.impl.tree.ReferenceWalker;

/**
 * This class encapsulates legacy runtime behavior for the process engine.
 *<p>
 * Since 7.3 the behavior of certain bpmn elements has changed slightly.
 *<p>
 *
 * 1. Some elements which did not used to be scopes are now scopes:
 * <ul>
 *  <li>Sequential multi instance Embedded Subprocess: is now a scope, used to be non-scope.</li>
 *  <li>Event subprocess: is now a scope, used to be non-scope.</li>
 * </ul>
 *
 * 2. In certain situations, executions which were both scope and concurrent were created.
 * This used to be the case if a scope execution already had a single scope child execution
 * and then concurrency was introduced (by a on interrupting boundary event or
 * a non-interrupting event subprocess).  In that case the existing scope execution
 * was made concurrent. Starting from 7.3 this behavior is considered legacy.
 * The new behavior is that the existing scope execution will not be made concurrent, instead,
 * a new, concurrent execution will be created and be interleaved between the parent and the
 * existing scope execution.
 *<p>
 *
 * @author Daniel Meyer
 * @since 7.3
 */
public class LegacyBehavior {

  private final static BpmnBehaviorLogger LOG = ProcessEngineLogger.BPMN_BEHAVIOR_LOGGER;

  // concurrent scopes ///////////////////////////////////////////

  /**
   * Prunes a concurrent scope. This can only happen if
   * (a) the process instance has been migrated from a previous version to a new version of the process engine
   *
   * This is an inverse operation to {@link #createConcurrentScope(PvmExecutionImpl)}.
   *
   * See: javadoc of this class for note about concurrent scopes.
   *
   * @param execution
   */
  public static void pruneConcurrentScope(PvmExecutionImpl execution) {
    ensureConcurrentScope(execution);
    LOG.debugConcurrentScopeIsPruned(execution);
    execution.setConcurrent(false);
  }

  /**
   * Cancels an execution which is both concurrent and scope. This can only happen if
   * (a) the process instance has been migrated from a previous version to a new version of the process engine
   *
   * See: javadoc of this class for note about concurrent scopes.
   *
   * @param execution the concurrent scope execution to destroy
   * @param cancelledScopeActivity the activity that cancels the execution; it must hold that
   *   cancellingActivity's event scope is the scope the execution is responsible for
   */
  public static void cancelConcurrentScope(PvmExecutionImpl execution, PvmActivity cancelledScopeActivity) {
    ensureConcurrentScope(execution);
    LOG.debugCancelConcurrentScopeExecution(execution);

    execution.interrupt("Scope "+cancelledScopeActivity+" cancelled.");
    // <!> HACK set to event scope activity and leave activity instance
    execution.setActivity(cancelledScopeActivity);
    execution.leaveActivityInstance();
    execution.interrupt("Scope "+cancelledScopeActivity+" cancelled.");
    execution.destroy();
  }

  /**
   * Destroys a concurrent scope Execution. This can only happen if
   * (a) the process instance has been migrated from a previous version to a 7.3+ version of the process engine
   *
   * See: javadoc of this class for note about concurrent scopes.
   *
   * @param execution the execution to destroy
   */
  public static void destroyConcurrentScope(PvmExecutionImpl execution) {
    ensureConcurrentScope(execution);
    LOG.destroyConcurrentScopeExecution(execution);
    execution.destroy();
  }

  // sequential multi instance /////////////////////////////////

  public static boolean eventSubprocessComplete(ActivityExecution scopeExecution) {
    boolean performLegacyBehavior = isLegacyBehaviorRequired(scopeExecution);

    if(performLegacyBehavior) {
      LOG.completeNonScopeEventSubprocess();
      scopeExecution.end(false);
    }

    return performLegacyBehavior;
  }

  public static boolean eventSubprocessConcurrentChildExecutionEnded(ActivityExecution scopeExecution, ActivityExecution endedExecution) {
    boolean performLegacyBehavior = isLegacyBehaviorRequired(endedExecution);

    if(performLegacyBehavior) {
      LOG.endConcurrentExecutionInEventSubprocess();
      // notify the grandparent flow scope in a similar way PvmAtomicOperationAcitivtyEnd does
      ScopeImpl flowScope = endedExecution.getActivity().getFlowScope();
      if (flowScope != null) {
        flowScope = flowScope.getFlowScope();

        if (flowScope != null) {
          if (flowScope == endedExecution.getActivity().getProcessDefinition()) {
            endedExecution.remove();
            scopeExecution.tryPruneLastConcurrentChild();
            scopeExecution.forceUpdate();
          }
          else {
            PvmActivity flowScopeActivity = (PvmActivity) flowScope;

            ActivityBehavior activityBehavior = flowScopeActivity.getActivityBehavior();
            if (activityBehavior instanceof CompositeActivityBehavior) {
              ((CompositeActivityBehavior) activityBehavior).concurrentChildExecutionEnded(scopeExecution, endedExecution);
            }
          }
        }
      }
    }

    return performLegacyBehavior;
  }

  /**
   * Destroy an execution for an activity that was previously not a scope and now is
   * (e.g. event subprocess)
   */
  public static boolean destroySecondNonScope(PvmExecutionImpl execution) {
    ensureScope(execution);
    boolean performLegacyBehavior = isLegacyBehaviorRequired(execution);

    if(performLegacyBehavior) {
      // legacy behavior is to do nothing
    }

    return performLegacyBehavior;
  }

  /**
   * This method
   * @param scopeExecution
   * @return
   */
  protected static boolean isLegacyBehaviorRequired(ActivityExecution scopeExecution) {
    // legacy behavior is turned off: the current activity was parsed as scope.
    // now we need to check whether a scope execution was correctly created for the
    // event subprocess.

    // first create the mapping:
    Map<ScopeImpl, PvmExecutionImpl> activityExecutionMapping = scopeExecution.createActivityExecutionMapping();
    // if the scope execution for the current activity is the same as for the parent scope
    // -> we need to perform legacy behavior
    PvmScope activity = scopeExecution.getActivity();
    if (!activity.isScope()) {
      activity = activity.getFlowScope();
    }
    return activityExecutionMapping.get(activity) == activityExecutionMapping.get(activity.getFlowScope());
  }

  /**
   * In case the process instance was migrated from a previous version, activities which are now parsed as scopes
   * do not have scope executions. Use the flow scopes of these activities in order to find their execution.
   * - For an event subprocess this is the scope execution of the scope in which the event subprocess is embeded in
   * - For a multi instance sequential subprocess this is the multi instace scope body.
   *
   * @param scope
   * @param activityExecutionMapping
   * @return
   */
  public static PvmExecutionImpl getScopeExecution(ScopeImpl scope, Map<ScopeImpl, PvmExecutionImpl> activityExecutionMapping) {
    ScopeImpl flowScope = scope.getFlowScope();
    return activityExecutionMapping.get(flowScope);
  }

  // helpers ////////////////////////////////////////////////

  protected static void ensureConcurrentScope(PvmExecutionImpl execution) {
    ensureScope(execution);
    ensureConcurrent(execution);
  }

  protected static void ensureConcurrent(PvmExecutionImpl execution) {
    if(!execution.isConcurrent()) {
      throw new ProcessEngineException("Execution must be concurrent.");
    }
  }

  protected static void ensureScope(PvmExecutionImpl execution) {
    if(!execution.isScope()) {
      throw new ProcessEngineException("Execution must be scope.");
    }
  }

  /**
   * Creates an activity execution mapping, when the scope hierarchy and the execution hierarchy are out of sync.
   *
   * @param scopeExecutions
   * @param scopes
   * @return
   */
  public static Map<ScopeImpl, PvmExecutionImpl> createActivityExecutionMapping(List<PvmExecutionImpl> scopeExecutions, List<ScopeImpl> scopes) {
    PvmExecutionImpl deepestExecution = scopeExecutions.get(0);
    if (isLegacyAsyncAtMultiInstance(deepestExecution)) {
      // in case the deepest execution is in fact async at multi-instance, the multi instance body is part
      // of the list of scopes, however it is not instantiated yet or has already ended. Thus it must be removed.
      scopes.remove(0);
    }

    // The trees are out of sync.
    // We are missing executions:
    int numOfMissingExecutions = scopes.size() - scopeExecutions.size();

    // We need to find out which executions are missing.
    // Consider: elements which did not use to be scopes are now scopes.
    // But, this does not mean that all instances of elements which became scopes
    // are missing their executions. We could have created new instances in the
    // lower part of the tree after legacy behavior was turned off while instances of these elements
    // further up the hierarchy miss scopes. So we need to iterate from the top down and skip all scopes which
    // were not scopes before:
    Collections.reverse(scopeExecutions);
    Collections.reverse(scopes);

    Map<ScopeImpl, PvmExecutionImpl> mapping  = new HashMap<ScopeImpl, PvmExecutionImpl>();
    // process definition / process instance.
    mapping.put(scopes.get(0), scopeExecutions.get(0));
    // nested activities
    int executionCounter = 0;
    for(int i = 1; i < scopes.size(); i++) {
      ActivityImpl scope = (ActivityImpl) scopes.get(i);

      PvmExecutionImpl scopeExecutionCandidate = null;
      if (executionCounter + 1 < scopeExecutions.size()) {
        scopeExecutionCandidate = scopeExecutions.get(executionCounter + 1);
      }

      if(numOfMissingExecutions > 0 && wasNoScope(scope, scopeExecutionCandidate)) {
        // found a missing scope
        numOfMissingExecutions--;
      }
      else {
        executionCounter++;
      }

      if (executionCounter >= scopeExecutions.size()) {
        throw new ProcessEngineException("Cannot construct activity-execution mapping: there are "
            + "more scope executions missing than explained by the flow scope hierarchy.");
      }

      PvmExecutionImpl execution = scopeExecutions.get(executionCounter);
      mapping.put(scope, execution);
    }

    return mapping;
  }

  /**
   * Determines whether the given scope was a scope in previous versions
   */
  protected static boolean wasNoScope(ActivityImpl activity, PvmExecutionImpl scopeExecutionCandidate) {
    return wasNoScope72(activity) || wasNoScope73(activity, scopeExecutionCandidate);
  }

  protected static boolean wasNoScope72(ActivityImpl activity) {
    ActivityBehavior activityBehavior = activity.getActivityBehavior();
    ActivityBehavior parentActivityBehavior = (ActivityBehavior) (activity.getFlowScope() != null ? activity.getFlowScope().getActivityBehavior() : null);
    return (activityBehavior instanceof EventSubProcessActivityBehavior)
        || (activityBehavior instanceof SubProcessActivityBehavior
              && parentActivityBehavior instanceof SequentialMultiInstanceActivityBehavior)
        || (activityBehavior instanceof ReceiveTaskActivityBehavior
              && parentActivityBehavior instanceof MultiInstanceActivityBehavior);
  }

  protected static boolean wasNoScope73(ActivityImpl activity, PvmExecutionImpl scopeExecutionCandidate) {
    ActivityBehavior activityBehavior = activity.getActivityBehavior();
    return (activityBehavior instanceof CompensationEventActivityBehavior)
        || (activityBehavior instanceof CancelEndEventActivityBehavior)
        || isMultiInstanceInCompensation(activity, scopeExecutionCandidate);
  }

  protected static boolean isMultiInstanceInCompensation(ActivityImpl activity, PvmExecutionImpl scopeExecutionCandidate) {
    return
        activity.getActivityBehavior() instanceof MultiInstanceActivityBehavior
        && ((scopeExecutionCandidate != null && findCompensationThrowingAncestorExecution(scopeExecutionCandidate) != null)
              || scopeExecutionCandidate == null);
  }

  /**
   * This returns true only if the provided execution has reached its wait state in a legacy engine version, because
   * only in that case, it can be async and waiting at the inner activity wrapped by the miBody. In versions >= 7.3,
   * the execution would reference the multi-instance body instead.
   */
  protected static boolean isLegacyAsyncAtMultiInstance(PvmExecutionImpl execution) {
    ActivityImpl activity = execution.getActivity();

    if (activity != null) {
      boolean isAsync = execution.getActivityInstanceId() == null;
      boolean isAtMultiInstance = activity.getParentFlowScopeActivity() != null
          && activity.getParentFlowScopeActivity().getActivityBehavior() instanceof MultiInstanceActivityBehavior;


      return isAsync && isAtMultiInstance;
    }
    else {
      return false;
    }
  }

  /**
   * Tolerates the broken execution trees fixed with CAM-3727 where there may be more
   * ancestor scope executions than ancestor flow scopes;
   *
   * In that case, the argument execution is removed, the parent execution of the argument
   * is returned such that one level of mismatch is corrected.
   *
   * Note that this does not necessarily skip the correct scope execution, since
   * the broken parent-child relationships may be anywhere in the tree (e.g. consider a non-interrupting
   * boundary event followed by a subprocess (i.e. scope), when the subprocess ends, we would
   * skip the subprocess's execution).
   *
   */
  public static PvmExecutionImpl determinePropagatingExecutionOnEnd(PvmExecutionImpl propagatingExecution, Map<ScopeImpl, PvmExecutionImpl> activityExecutionMapping) {
    if (!propagatingExecution.isScope()) {
      // non-scope executions may end in the "wrong" flow scope
      return propagatingExecution;
    }
    else {
      // superfluous scope executions won't be contained in the activity-execution mapping
      if (activityExecutionMapping.values().contains(propagatingExecution)) {
        return propagatingExecution;
      }
      else {
        // skip one scope
        propagatingExecution.remove();
        PvmExecutionImpl parent = propagatingExecution.getParent();
        parent.setActivity(propagatingExecution.getActivity());
        return propagatingExecution.getParent();
      }
    }
  }

  /**
   * Concurrent + scope executions are legacy and could occur in processes with non-interrupting
   * boundary events or event subprocesses
   */
  public static boolean isConcurrentScope(PvmExecutionImpl propagatingExecution) {
    return propagatingExecution.isConcurrent() && propagatingExecution.isScope();
  }

  /**
   * <p>Required for migrating active sequential MI receive tasks. These activities were formerly not scope,
   * but are now. This has the following implications:
   *
   * <p>Before migration:
   * <ul><li> the event subscription is attached to the miBody scope execution</ul>
   *
   * <p>After migration:
   * <ul><li> a new subscription is created for every instance
   * <li> the new subscription is attached to a dedicated scope execution as a child of the miBody scope
   *   execution</ul>
   *
   * <p>Thus, this method removes the subscription on the miBody scope
   */
  public static void removeLegacySubscriptionOnParent(ExecutionEntity execution, EventSubscriptionEntity eventSubscription) {
    ActivityImpl activity = execution.getActivity();
    if (activity == null) {
      return;
    }

    ActivityBehavior behavior = activity.getActivityBehavior();
    ActivityBehavior parentBehavior = (ActivityBehavior) (activity.getFlowScope() != null ? activity.getFlowScope().getActivityBehavior() : null);

    if (behavior instanceof ReceiveTaskActivityBehavior &&
        parentBehavior instanceof MultiInstanceActivityBehavior) {
      List<EventSubscriptionEntity> parentSubscriptions = execution.getParent().getEventSubscriptions();

      for (EventSubscriptionEntity subscription : parentSubscriptions) {
        // distinguish a boundary event on the mi body with the same message name from the receive task subscription
        if (areEqualEventSubscriptions(subscription, eventSubscription)) {
          subscription.delete();
        }
      }
    }

  }

  /**
   * Checks if the parameters are the same apart from the execution id
   */
  protected static boolean areEqualEventSubscriptions(EventSubscriptionEntity subscription1, EventSubscriptionEntity subscription2) {
    return valuesEqual(subscription1.getEventType(), subscription2.getEventType())
        && valuesEqual(subscription1.getEventName(), subscription2.getEventName())
        && valuesEqual(subscription1.getActivityId(), subscription2.getActivityId());

  }

  protected static <T> boolean valuesEqual(T value1, T value2) {
    return (value1 == null && value2 == null) || (value1 != null && value1.equals(value2));
  }

  /**
   * Remove all entries for legacy non-scopes given that the assigned scope execution is also responsible for another scope
   */
  public static void removeLegacyNonScopesFromMapping(Map<ScopeImpl, PvmExecutionImpl> mapping) {
    Map<PvmExecutionImpl, List<ScopeImpl>> scopesForExecutions = new HashMap<PvmExecutionImpl, List<ScopeImpl>>();

    for (Map.Entry<ScopeImpl, PvmExecutionImpl> mappingEntry : mapping.entrySet()) {
      List<ScopeImpl> scopesForExecution = scopesForExecutions.get(mappingEntry.getValue());
      if (scopesForExecution == null) {
        scopesForExecution = new ArrayList<ScopeImpl>();
        scopesForExecutions.put(mappingEntry.getValue(), scopesForExecution);
      }

      scopesForExecution.add(mappingEntry.getKey());
    }

    for (Map.Entry<PvmExecutionImpl, List<ScopeImpl>> scopesForExecution : scopesForExecutions.entrySet()) {
      List<ScopeImpl> scopes = scopesForExecution.getValue();

      if (scopes.size() > 1) {
        ScopeImpl topMostScope = getTopMostScope(scopes);

        for (ScopeImpl scope : scopes) {
          if (scope != scope.getProcessDefinition() && scope != topMostScope) {
            mapping.remove(scope);
          }
        }
      }
    }
  }

  protected static ScopeImpl getTopMostScope(List<ScopeImpl> scopes) {
    ScopeImpl topMostScope = null;

    for (ScopeImpl candidateScope : scopes) {
      if (topMostScope == null || candidateScope.isAncestorFlowScopeOf(topMostScope)) {
        topMostScope = candidateScope;
      }
    }

    return topMostScope;
  }

  /**
   * This is relevant for {@link GetActivityInstanceCmd} where in case of legacy multi-instance execution trees, the default
   * algorithm omits multi-instance activity instances.
   */
  public static void repairParentRelationships(Collection<ActivityInstanceImpl> values, String processInstanceId) {
    for (ActivityInstanceImpl activityInstance : values) {
      // if the determined activity instance id and the parent activity instance are equal,
      // just put the activity instance under the process instance
      if (valuesEqual(activityInstance.getId(), activityInstance.getParentActivityInstanceId())) {
        activityInstance.setParentActivityInstanceId(processInstanceId);
      }
    }
  }

  /**
   * When deploying an async job definition for an activity wrapped in an miBody, set the activity id to the
   * miBody except the wrapped activity is marked as async.
   *
   * Background: in <= 7.2 async job definitions were created for the inner activity, although the
   * semantics are that they are executed before the miBody is entered
   */
  public static void migrateMultiInstanceJobDefinitions(ProcessDefinitionEntity processDefinition, List<JobDefinitionEntity> jobDefinitions) {
    for (JobDefinitionEntity jobDefinition : jobDefinitions) {

      String activityId = jobDefinition.getActivityId();
      if (activityId != null) {
        ActivityImpl activity = processDefinition.findActivity(jobDefinition.getActivityId());

        if (!isAsync(activity) && isActivityWrappedInMultiInstanceBody(activity) && isAsyncJobDefinition(jobDefinition)) {
          jobDefinition.setActivityId(activity.getFlowScope().getId());
        }
      }
    }
  }

  protected static boolean isAsync(ActivityImpl activity) {
    return activity.isAsyncBefore() || activity.isAsyncAfter();
  }

  protected static boolean isAsyncJobDefinition(JobDefinitionEntity jobDefinition) {
    return AsyncContinuationJobHandler.TYPE.equals(jobDefinition.getJobType());
  }

  protected static boolean isActivityWrappedInMultiInstanceBody(ActivityImpl activity) {
    ScopeImpl flowScope = activity.getFlowScope();

    if (flowScope != activity.getProcessDefinition()) {
      ActivityImpl flowScopeActivity = (ActivityImpl) flowScope;

      return flowScopeActivity.getActivityBehavior() instanceof MultiInstanceActivityBehavior;
    } else {
      return false;
    }
  }

  /**
   * When executing an async job for an activity wrapped in an miBody, set the execution to the
   * miBody except the wrapped activity is marked as async.
   *
   * Background: in <= 7.2 async jobs were created for the inner activity, although the
   * semantics are that they are executed before the miBody is entered
   */
  public static void repairMultiInstanceAsyncJob(ExecutionEntity execution) {
    ActivityImpl activity = execution.getActivity();

    if (!isAsync(activity) && isActivityWrappedInMultiInstanceBody(activity)) {
      execution.setActivity((ActivityImpl) activity.getFlowScope());
    }
  }

  /**
   * With prior versions, the boundary event was already executed when compensation was performed; Thus, after
   * compensation completes, the execution is signalled waiting at the boundary event.
   */
  public static boolean signalCancelBoundaryEvent(String signalName) {
    return SIGNAL_COMPENSATION_DONE.equals(signalName);
  }

  /**
   * @see #signalCancelBoundaryEvent(String)
   */
  public static void parseCancelBoundaryEvent(ActivityImpl activity) {
    activity.setProperty(BpmnParse.PROPERTYNAME_THROWS_COMPENSATION, true);
  }

  /**
   * <p>In general, only leaf executions have activity ids.</p>
   * <p>Exception to that rule: compensation throwing executions.</p>
   * <p>Legacy exception (<= 7.2) to that rule: miBody executions and parallel gateway executions</p>
   *
   * @return true, if the argument is not a leaf and has an invalid (i.e. legacy) non-null activity id
   */
  public static boolean hasInvalidIntermediaryActivityId(PvmExecutionImpl execution) {
    return !execution.getNonEventScopeExecutions().isEmpty() && !CompensationBehavior.isCompensationThrowing(execution);
  }

  /**
   * Returns true if the given execution is in a compensation-throwing activity but there is no dedicated scope execution
   * in the given mapping.
   */
  public static boolean isCompensationThrowing(PvmExecutionImpl execution, Map<ScopeImpl, PvmExecutionImpl> activityExecutionMapping) {
    if (CompensationBehavior.isCompensationThrowing(execution)) {
      ScopeImpl compensationThrowingActivity = execution.getActivity();

      if (compensationThrowingActivity.isScope()) {
        return activityExecutionMapping.get(compensationThrowingActivity) ==
            activityExecutionMapping.get(compensationThrowingActivity.getFlowScope());
      }
      else {
        // for transaction sub processes with cancel end events, the compensation throwing execution waits in the boundary event, not in the end
        // event; cancel boundary events are currently not scope
        return compensationThrowingActivity.getActivityBehavior() instanceof CancelBoundaryEventActivityBehavior;
      }
    }
    else {
      return false;
    }
  }

  public static boolean isCompensationThrowing(PvmExecutionImpl execution) {
    return isCompensationThrowing(execution, execution.createActivityExecutionMapping());
  }

  protected static PvmExecutionImpl findCompensationThrowingAncestorExecution(PvmExecutionImpl execution) {
    ExecutionWalker walker = new ExecutionWalker(execution);
    walker.walkUntil(new ReferenceWalker.WalkCondition<PvmExecutionImpl>() {
      public boolean isFulfilled(PvmExecutionImpl element) {
        return element == null || CompensationBehavior.isCompensationThrowing(element);
      }
    });

    return walker.getCurrentElement();
  }

  /**
   * See #CAM-10978
   * Use case process instance with <code>asyncBefore</code> startEvent
   * After unifying the history variable's creation<br>
   * The following changed:<br>
   *   * variables will receive the <code>processInstanceId</code> as <code>activityInstanceId</code> in such cases (previously was the startEvent id)<br>
   *   * historic details have new <code>initial</code> property to track initial variables that process is started with<br>
   * The jobs created prior <code>7.13</code> and not executed before do not have historic information of variables.
   * This method takes care of that.
   */
  public static void createMissingHistoricVariables(PvmExecutionImpl execution) {
    Collection<VariableInstanceEntity> variables = ((ExecutionEntity) execution).getVariablesInternal();

    if (variables != null && variables.size() > 0) {
      // trigger historic creation if the history is not presented already
      for (VariableInstanceEntity variable : variables) {

        if (variable.wasCreatedBefore713()) {
          VariableInstanceHistoryListener.INSTANCE.onCreate(variable, variable.getExecution());
        }
      }
    }
  }

}
