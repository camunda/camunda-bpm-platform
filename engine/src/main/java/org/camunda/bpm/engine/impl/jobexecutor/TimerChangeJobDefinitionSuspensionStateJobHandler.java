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

import org.camunda.bpm.engine.impl.util.json.JSONObject;

/**
 * @author roman.smirnov
 */
public abstract class TimerChangeJobDefinitionSuspensionStateJobHandler implements JobHandler {

  protected static final String JOB_HANDLER_CFG_BY = "by";
  protected static final String JOB_HANDLER_CFG_JOB_DEFINITION_ID = "jobDefinitionId";
  protected static final String JOB_HANDLER_CFG_PROCESS_DEFINITION_ID = "processDefinitionId";
  protected static final String JOB_HANDLER_CFG_PROCESS_DEFINITION_KEY = "processDefinitionKey";

  protected static final String JOB_HANDLER_CFG_INCLUDE_JOBS = "includeJobs";

  public static String createJobHandlerConfigurationByJobDefinitionId(String jobDefinitionId, boolean includeJobs) {
    JSONObject json = new JSONObject();

    json.put(JOB_HANDLER_CFG_BY, JOB_HANDLER_CFG_JOB_DEFINITION_ID);
    json.put(JOB_HANDLER_CFG_JOB_DEFINITION_ID, jobDefinitionId);
    json.put(JOB_HANDLER_CFG_INCLUDE_JOBS, includeJobs);

    return json.toString();
  }

  public static String createJobHandlerConfigurationByProcessDefinitionId(String processDefinitionId, boolean includeJobs) {
    JSONObject json = new JSONObject();

    json.put(JOB_HANDLER_CFG_BY, JOB_HANDLER_CFG_PROCESS_DEFINITION_ID);
    json.put(JOB_HANDLER_CFG_PROCESS_DEFINITION_ID, processDefinitionId);
    json.put(JOB_HANDLER_CFG_INCLUDE_JOBS, includeJobs);

    return json.toString();
  }

  public static String createJobHandlerConfigurationByProcessDefinitionKey(String processDefinitionKey, boolean includeJobs) {
    JSONObject json = new JSONObject();

    json.put(JOB_HANDLER_CFG_BY, JOB_HANDLER_CFG_PROCESS_DEFINITION_KEY);
    json.put(JOB_HANDLER_CFG_PROCESS_DEFINITION_KEY, processDefinitionKey);
    json.put(JOB_HANDLER_CFG_INCLUDE_JOBS, includeJobs);

    return json.toString();
  }

  protected String getJobDefinitionId(JSONObject configuration) {
    return configuration.getString(JOB_HANDLER_CFG_JOB_DEFINITION_ID);
  }

  protected String getProcessDefinitionId(JSONObject configuration) {
    return configuration.getString(JOB_HANDLER_CFG_PROCESS_DEFINITION_ID);
  }

  protected String getProcessDefinitionKey(JSONObject configuration) {
    return configuration.getString(JOB_HANDLER_CFG_PROCESS_DEFINITION_KEY);
  }

  protected boolean getIncludeJobs(JSONObject configuration) {
    return configuration.getBoolean(JOB_HANDLER_CFG_INCLUDE_JOBS);
  }

  protected String getBy(JSONObject configuration) {
    return configuration.getString(JOB_HANDLER_CFG_BY);
  }
}
