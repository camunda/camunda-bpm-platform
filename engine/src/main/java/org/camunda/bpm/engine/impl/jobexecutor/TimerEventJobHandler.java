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
  public static final String JOB_HANDLER_CONFIG_TASK_LISTENER_PREFIX = "taskListener~";

  @Override
  public TimerJobConfiguration newConfiguration(String canonicalString) {
    String[] configParts = canonicalString.split("\\" + JOB_HANDLER_CONFIG_PROPERTY_DELIMITER);

    if (configParts.length > 3) {
      throw new ProcessEngineException("Illegal timer job handler configuration: '" + canonicalString
          + "': exprecting a one, two or three part configuration seperated by '" + JOB_HANDLER_CONFIG_PROPERTY_DELIMITER + "'.");
    }

    TimerJobConfiguration configuration = new TimerJobConfiguration();
    configuration.timerElementKey = configParts[0];

    // depending on the job configuration, the next parts can be a task listener id and/or the follow-up-job flag
    for (int i = 1; i < configParts.length; i++) {
      adjustConfiguration(configuration, configParts[i]);
    }

    return configuration;
  }

  protected void adjustConfiguration(TimerJobConfiguration configuration, String configPart) {
    if (configPart.startsWith(JOB_HANDLER_CONFIG_TASK_LISTENER_PREFIX)) {
      configuration.setTimerElementSecondaryKey(configPart.substring(JOB_HANDLER_CONFIG_TASK_LISTENER_PREFIX.length()));
    } else {
      configuration.followUpJobCreated = JOB_HANDLER_CONFIG_PROPERTY_FOLLOW_UP_JOB_CREATED.equals(configPart);
    }
  }

  public static class TimerJobConfiguration implements JobHandlerConfiguration {

    protected String timerElementKey;
    protected String timerElementSecondaryKey;
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

    public String getTimerElementSecondaryKey() {
      return timerElementSecondaryKey;
    }

    public void setTimerElementSecondaryKey(String timerElementSecondaryKey) {
      this.timerElementSecondaryKey = timerElementSecondaryKey;
    }

    @Override
    public String toCanonicalString() {
      String canonicalString = timerElementKey;

      if (timerElementSecondaryKey != null) {
        canonicalString += JOB_HANDLER_CONFIG_PROPERTY_DELIMITER + JOB_HANDLER_CONFIG_TASK_LISTENER_PREFIX + timerElementSecondaryKey;
      }

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
