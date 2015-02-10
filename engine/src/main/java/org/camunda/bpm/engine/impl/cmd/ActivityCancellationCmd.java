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


  public ActivityCancellationCmd(String processInstanceId, String activityId) {
    super(processInstanceId);
    this.activityId = activityId;

  }

  public Void execute(final CommandContext commandContext) {
    ActivityInstance activityInstanceTree = commandContext.runWithoutAuthentication(new Callable<ActivityInstance>() {
      public ActivityInstance call() throws Exception {
        return new GetActivityInstanceCmd(processInstanceId).execute(commandContext);
      }
    });

    ExecutionEntity processInstance = commandContext.getExecutionManager().findExecutionById(processInstanceId);
    ProcessDefinitionImpl processDefinition = processInstance.getProcessDefinition();
    Set<String> parentScopeIds = collectParentScopeIdsForActivity(processDefinition, activityId);

    List<ActivityInstance> childrenForActivity = getActivityInstancesForActivity(activityInstanceTree, parentScopeIds);
    for (ActivityInstance instance : childrenForActivity) {
      ActivityInstanceCancellationCmd cmd = new ActivityInstanceCancellationCmd(processInstanceId, instance.getId());
      cmd.setSkipCustomListeners(skipCustomListeners);
      cmd.setSkipIoMappings(skipIoMappings);
      cmd.execute(commandContext);
    }

    List<TransitionInstance> transitionInstancesForActivity = getTransitionInstancesForActivity(activityInstanceTree, parentScopeIds);
    for (TransitionInstance instance : transitionInstancesForActivity) {
      TransitionInstanceCancellationCmd cmd = new TransitionInstanceCancellationCmd(processInstanceId, instance.getId());
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
}
