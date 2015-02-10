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
package org.camunda.bpm.engine.impl.jobexecutor;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;


public class TimerCatchIntermediateEventJobHandler extends TimerEventJobHandler {

  private static Logger log = Logger.getLogger(TimerCatchIntermediateEventJobHandler.class.getName());

  public static final String TYPE = "timer-intermediate-transition";

  public String getType() {
    return TYPE;
  }

  public void execute(String configuration, ExecutionEntity execution, CommandContext commandContext) {
    String activityId = getKey(configuration);
    ActivityImpl intermediateEventActivity = execution.getProcessDefinition().findActivity(activityId);

    ensureNotNull("Error while firing timer: intermediate event activity " + configuration + " not found", "intermediateEventActivity", intermediateEventActivity);

    try {
      if(activityId.equals(execution.getActivityId())) {
        // Regular Intermediate timer catch
        execution.signal("signal", null);
      }
      else {
        // Event based gateway
        execution.executeEventHandlerActivity(intermediateEventActivity);
      }

    } catch (RuntimeException e) {
      log.log(Level.SEVERE, "exception during timer execution", e);
      throw e;
    } catch (Exception e) {
      log.log(Level.SEVERE, "exception during timer execution", e);
      throw new ProcessEngineException("exception during timer execution: " + e.getMessage(), e);
    }
  }
}
