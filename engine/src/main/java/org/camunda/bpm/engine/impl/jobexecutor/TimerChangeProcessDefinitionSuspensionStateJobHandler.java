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
 * @author Joram Barrez
 * @author roman.smirnov
 */
public abstract class TimerChangeProcessDefinitionSuspensionStateJobHandler implements JobHandler {

  protected static final String JOB_HANDLER_CFG_BY = "by";
  protected static final String JOB_HANDLER_CFG_PROCESS_DEFINITION_ID = "processDefinitionId";
  protected static final String JOB_HANDLER_CFG_PROCESS_DEFINITION_KEY = "processDefinitionKey";

  private static final String JOB_HANDLER_CFG_INCLUDE_PROCESS_INSTANCES = "includeProcessInstances";

  public static String createJobHandlerConfigurationByProcessDefinitionId(String processDefinitionId, boolean includeProcessInstances) {
    JSONObject json = new JSONObject();

    json.put(JOB_HANDLER_CFG_BY, JOB_HANDLER_CFG_PROCESS_DEFINITION_ID);
    json.put(JOB_HANDLER_CFG_PROCESS_DEFINITION_ID, processDefinitionId);
    json.put(JOB_HANDLER_CFG_INCLUDE_PROCESS_INSTANCES, includeProcessInstances);

    return json.toString();
  }

  public static String createJobHandlerConfigurationByProcessDefinitionKey(String processDefinitionKey, boolean includeProcessInstances) {
    JSONObject json = new JSONObject();

    json.put(JOB_HANDLER_CFG_BY, JOB_HANDLER_CFG_PROCESS_DEFINITION_KEY);
    json.put(JOB_HANDLER_CFG_PROCESS_DEFINITION_KEY, processDefinitionKey);
    json.put(JOB_HANDLER_CFG_INCLUDE_PROCESS_INSTANCES, includeProcessInstances);

    return json.toString();
  }

  protected String getProcessDefinitionId(JSONObject config) {
    return config.getString(JOB_HANDLER_CFG_PROCESS_DEFINITION_ID);
  }

  protected String getProcessDefinitionKey(JSONObject config) {
    return config.getString(JOB_HANDLER_CFG_PROCESS_DEFINITION_KEY);
  }

  protected String getBy(JSONObject config) {
    return config.getString(JOB_HANDLER_CFG_BY);
  }

  protected boolean getIncludeProcessInstances(JSONObject config) {
    return config.getBoolean(JOB_HANDLER_CFG_INCLUDE_PROCESS_INSTANCES);
  }

}
