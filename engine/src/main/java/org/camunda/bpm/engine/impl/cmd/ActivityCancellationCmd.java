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
package org.camunda.bpm.engine.impl.cmd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.TransitionInstance;

/**
 * @author Thorben Lindhauer
 *
 */
public class ActivityCancellationCmd extends AbstractProcessInstanceModificationCommand {

  protected String activityId;
  protected boolean cancelCurrentActiveActivityInstances;
  protected ActivityInstance activityInstanceTree;

  public ActivityCancellationCmd(String activityId) {
    this(null, activityId);
  }

  public ActivityCancellationCmd(String processInstanceId, String activityId) {
    super(processInstanceId);
    this.activityId = activityId;
  }

  @Override
  public Void execute(final CommandContext commandContext) {
    ActivityInstance activityInstanceTree = getActivityInstanceTree(commandContext);
    List<AbstractInstanceCancellationCmd> commands = createActivityInstanceCancellations(activityInstanceTree, commandContext);

    for (AbstractInstanceCancellationCmd cmd : commands) {
      cmd.setSkipCustomListeners(skipCustomListeners);
      cmd.setSkipIoMappings(skipIoMappings);
      cmd.execute(commandContext);
    }

    return null;
  }

  protected Set<String> collectParentScopeIdsForActivity(ProcessDefinitionImpl processDefinition, String activityId) {
    Set<String> parentScopeIds = new HashSet<String>();
    ScopeImpl scope = processDefinition.findActivity(activityId);

    while (scope != null) {
      parentScopeIds.add(scope.getId());
      scope = scope.getFlowScope();
    }

    return parentScopeIds;
  }

  protected List<TransitionInstance> getTransitionInstancesForActivity(ActivityInstance tree, Set<String> parentScopeIds) {
    // prune all search paths that are not in the scope hierarchy of the activity in question
    if (!parentScopeIds.contains(tree.getActivityId())) {
      return Collections.emptyList();
    }

    List<TransitionInstance> instances = new ArrayList<TransitionInstance>();
    TransitionInstance[] transitionInstances = tree.getChildTransitionInstances();

    for (TransitionInstance transitionInstance : transitionInstances) {
      if (activityId.equals(transitionInstance.getActivityId())) {
        instances.add(transitionInstance);
      }
    }

    for (ActivityInstance child : tree.getChildActivityInstances()) {
      instances.addAll(getTransitionInstancesForActivity(child, parentScopeIds));
    }

    return instances;
  }

  protected List<ActivityInstance> getActivityInstancesForActivity(ActivityInstance tree, Set<String> parentScopeIds) {
    // prune all search paths that are not in the scope hierarchy of the activity in question
    if (!parentScopeIds.contains(tree.getActivityId())) {
      return Collections.emptyList();
    }

    List<ActivityInstance> instances = new ArrayList<ActivityInstance>();

    if (activityId.equals(tree.getActivityId())) {
      instances.add(tree);
    }

    for (ActivityInstance child : tree.getChildActivityInstances()) {
      instances.addAll(getActivityInstancesForActivity(child, parentScopeIds));
    }

    return instances;
  }

  public ActivityInstance getActivityInstanceTree(final CommandContext commandContext) {
    return commandContext.runWithoutAuthorization(new Callable<ActivityInstance>() {
      @Override
      public ActivityInstance call() throws Exception {
        return new GetActivityInstanceCmd(processInstanceId).execute(commandContext);
      }
    });
  }

  public String getActivityId() {
    return activityId;
  }

  public void setActivityInstanceTreeToCancel(ActivityInstance activityInstanceTreeToCancel) {
    this.activityInstanceTree = activityInstanceTreeToCancel;
  }

  @Override
  protected String describe() {
    return "Cancel all instances of activity '" + activityId + "'";
  }

  public List<AbstractInstanceCancellationCmd> createActivityInstanceCancellations(ActivityInstance activityInstanceTree, CommandContext commandContext) {
    List<AbstractInstanceCancellationCmd> commands = new ArrayList<AbstractInstanceCancellationCmd>();

    ExecutionEntity processInstance = commandContext.getExecutionManager().findExecutionById(processInstanceId);
    ProcessDefinitionImpl processDefinition = processInstance.getProcessDefinition();
    Set<String> parentScopeIds = collectParentScopeIdsForActivity(processDefinition, activityId);

    List<ActivityInstance> childrenForActivity = getActivityInstancesForActivity(activityInstanceTree, parentScopeIds);
    for (ActivityInstance instance : childrenForActivity) {
      commands.add(new ActivityInstanceCancellationCmd(processInstanceId, instance.getId()));
    }

    List<TransitionInstance> transitionInstancesForActivity = getTransitionInstancesForActivity(activityInstanceTree, parentScopeIds);
    for (TransitionInstance instance : transitionInstancesForActivity) {
      commands.add(new TransitionInstanceCancellationCmd(processInstanceId, instance.getId()));
    }
    return commands;

  }

  public boolean isCancelCurrentActiveActivityInstances() {
    return cancelCurrentActiveActivityInstances;
  }

  public void setCancelCurrentActiveActivityInstances(boolean cancelCurrentActiveActivityInstances) {
    this.cancelCurrentActiveActivityInstances = cancelCurrentActiveActivityInstances;
  }
}
