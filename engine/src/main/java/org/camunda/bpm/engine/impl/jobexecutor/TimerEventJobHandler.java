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
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;


/**
 * @author Roman Smirnov
 *
 */
public abstract class TimerEventJobHandler implements JobHandler {

  public static final String JOB_HANDLER_CONFIG_PROPERTY_DELIMITER = "$";
  public static final String JOB_HANDLER_CONFIG_PROPERTY_FOLLOW_UP_TIMER_JOB_CREATED = "followUpJobCreated";

  public String getKey(String configuration) {
    if (containsDelimiter(configuration)) {
      String[] configParts = getConfigParts(configuration);
      return configParts[0];
    }

    return configuration;
  }

  public boolean isFollowUpJobCreated(String configuration) {
    if (containsDelimiter(configuration)) {
      String[] configParts = getConfigParts(configuration);
      String property = configParts[1];

      return JOB_HANDLER_CONFIG_PROPERTY_FOLLOW_UP_TIMER_JOB_CREATED.equals(property);
    }

    return false;
  }

  public void setFollowUpJobCreated(TimerEntity timer) {
    String configuration = timer.getJobHandlerConfiguration();

    if (configuration == null) {
      configuration = "";
    }

    configuration += JOB_HANDLER_CONFIG_PROPERTY_DELIMITER + JOB_HANDLER_CONFIG_PROPERTY_FOLLOW_UP_TIMER_JOB_CREATED;

    timer.setJobHandlerConfiguration(configuration);
  }

  protected boolean containsDelimiter(String configuration) {
    return configuration != null && configuration.contains(JOB_HANDLER_CONFIG_PROPERTY_DELIMITER);
  }

  protected String[] getConfigParts(String configuration) {
    String[] configParts = configuration.split("\\" + JOB_HANDLER_CONFIG_PROPERTY_DELIMITER);

    if (configParts.length != 2) {
      throw new ProcessEngineException("Illegal timer job handler configuration: '" + configuration + "': exprecting two parts seperated by '" + JOB_HANDLER_CONFIG_PROPERTY_DELIMITER + "'.");
    }

    return configParts;
  }

}
