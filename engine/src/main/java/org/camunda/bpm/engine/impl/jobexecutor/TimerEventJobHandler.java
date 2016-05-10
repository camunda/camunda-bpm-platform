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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.jobexecutor.TimerEventJobHandler.TimerJobConfiguration;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;


/**
 * @author Roman Smirnov
 *
 */
public abstract class TimerEventJobHandler implements JobHandler<TimerJobConfiguration> {

  public static final String JOB_HANDLER_CONFIG_PROPERTY_DELIMITER = "$";
  public static final String JOB_HANDLER_CONFIG_PROPERTY_FOLLOW_UP_JOB_CREATED = "followUpJobCreated";

  @Override
  public TimerJobConfiguration newConfiguration(String canonicalString) {
    String[] configParts = canonicalString.split("\\" + JOB_HANDLER_CONFIG_PROPERTY_DELIMITER);

    if (configParts.length > 2) {
      throw new ProcessEngineException("Illegal timer job handler configuration: '" + canonicalString
          + "': exprecting a one or two part configuration seperated by '" + JOB_HANDLER_CONFIG_PROPERTY_DELIMITER + "'.");
    }

    TimerJobConfiguration configuration = new TimerJobConfiguration();
    configuration.timerElementKey = configParts[0];

    if (configParts.length == 2) {
      configuration.followUpJobCreated = JOB_HANDLER_CONFIG_PROPERTY_FOLLOW_UP_JOB_CREATED.equals(configParts[1]);
    }

    return configuration;
  }

  public static class TimerJobConfiguration implements JobHandlerConfiguration {

    protected String timerElementKey;
    protected boolean followUpJobCreated;

    public String getTimerElementKey() {
      return timerElementKey;
    }

    public void setTimerElementKey(String timerElementKey) {
      this.timerElementKey = timerElementKey;
    }

    public boolean isFollowUpJobCreated() {
      return followUpJobCreated;
    }

    public void setFollowUpJobCreated(boolean followUpJobCreated) {
      this.followUpJobCreated = followUpJobCreated;
    }

    @Override
    public String toCanonicalString() {
      String canonicalString = timerElementKey;

      if (followUpJobCreated) {
        canonicalString += JOB_HANDLER_CONFIG_PROPERTY_DELIMITER + JOB_HANDLER_CONFIG_PROPERTY_FOLLOW_UP_JOB_CREATED;
      }

      return canonicalString;
    }

  }

  public void onDelete(TimerJobConfiguration configuration, JobEntity jobEntity) {
    // do nothing
  }

}
