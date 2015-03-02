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
import java.util.List;

import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.runtime.ActivityInstance;

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

  public Void execute(CommandContext commandContext) {
    ActivityInstance activityInstanceTree = new GetActivityInstanceCmd(processInstanceId).execute(commandContext);

    List<ActivityInstance> childrenForActivity = getInstancesForActivity(activityInstanceTree, activityId);
    for (ActivityInstance instance : childrenForActivity) {
      ActivityInstanceCancellationCmd cmd = new ActivityInstanceCancellationCmd(processInstanceId, instance.getId());
      cmd.setSkipCustomListeners(skipCustomListeners);
      cmd.setSkipIoMappings(skipIoMappings);
      cmd.execute(commandContext);
    }

    return null;
  }

  protected List<ActivityInstance> getInstancesForActivity(ActivityInstance tree, String activityId) {
    List<ActivityInstance> instances = new ArrayList<ActivityInstance>();

    if (activityId.equals(tree.getActivityId())) {
      instances.add(tree);
    }

    for (ActivityInstance child : tree.getChildActivityInstances()) {
      instances.addAll(getInstancesForActivity(child, activityId));
    }

    return instances;
  }
}
