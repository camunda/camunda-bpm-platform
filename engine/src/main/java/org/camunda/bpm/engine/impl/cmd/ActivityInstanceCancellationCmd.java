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

import java.util.concurrent.Callable;

import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.ActivityExecutionTreeMapping;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.util.EnsureUtil;
import org.camunda.bpm.engine.runtime.ActivityInstance;

/**
 * @author Thorben Lindhauer
 *
 */
public class ActivityInstanceCancellationCmd extends AbstractInstanceCancellationCmd {

  protected String activityInstanceId;

  public ActivityInstanceCancellationCmd(String processInstanceId, String activityInstanceId) {
    super(processInstanceId);
    this.activityInstanceId = activityInstanceId;
  }

  public ActivityInstanceCancellationCmd(String processInstanceId, String activityInstanceId, String cancellationReason) {
    super(processInstanceId, cancellationReason);
    this.activityInstanceId = activityInstanceId;
  }

  public String getActivityInstanceId() {
    return activityInstanceId;
  }

  protected ExecutionEntity determineSourceInstanceExecution(final CommandContext commandContext) {
    ExecutionEntity processInstance = commandContext.getExecutionManager().findExecutionById(processInstanceId);

    // rebuild the mapping because the execution tree changes with every iteration
    ActivityExecutionTreeMapping mapping = new ActivityExecutionTreeMapping(commandContext, processInstanceId);

    ActivityInstance instance = commandContext.runWithoutAuthorization(new Callable<ActivityInstance>() {
      public ActivityInstance call() throws Exception {
        return new GetActivityInstanceCmd(processInstanceId).execute(commandContext);
      }
    });

    ActivityInstance instanceToCancel = findActivityInstance(instance, activityInstanceId);
    EnsureUtil.ensureNotNull(NotValidException.class,
        describeFailure("Activity instance '" + activityInstanceId + "' does not exist"),
        "activityInstance",
        instanceToCancel);
    ExecutionEntity scopeExecution = getScopeExecutionForActivityInstance(processInstance, mapping, instanceToCancel);

    return scopeExecution;
  }

  protected String describe() {
    return "Cancel activity instance '" + activityInstanceId + "'";
  }


}
