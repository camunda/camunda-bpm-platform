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
import org.camunda.bpm.engine.impl.cmd.AbstractSetProcessDefinitionStateCmd;
import org.camunda.bpm.engine.impl.core.instance.CoreExecution;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.repository.UpdateProcessDefinitionSuspensionStateBuilderImpl;
import org.camunda.bpm.engine.impl.util.json.JSONObject;

/**
 * @author Joram Barrez
 * @author roman.smirnov
 */
public abstract class TimerChangeProcessDefinitionSuspensionStateJobHandler implements JobHandler {

  protected static final String JOB_HANDLER_CFG_BY = "by";
  protected static final String JOB_HANDLER_CFG_PROCESS_DEFINITION_ID = "processDefinitionId";
  protected static final String JOB_HANDLER_CFG_PROCESS_DEFINITION_KEY = "processDefinitionKey";
  protected static final String JOB_HANDLER_CFG_PROCESS_DEFINITION_TENANT_ID = "processDefinitionTenantId";

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

  public static String createJobHandlerConfigurationByProcessDefinitionKeyAndTenantId(String processDefinitionKey, String tenantId, boolean includeProcessInstances) {
    JSONObject json = new JSONObject();

    json.put(JOB_HANDLER_CFG_BY, JOB_HANDLER_CFG_PROCESS_DEFINITION_KEY);
    json.put(JOB_HANDLER_CFG_PROCESS_DEFINITION_KEY, processDefinitionKey);
    json.put(JOB_HANDLER_CFG_INCLUDE_PROCESS_INSTANCES, includeProcessInstances);

    if (tenantId != null) {
      json.put(JOB_HANDLER_CFG_PROCESS_DEFINITION_TENANT_ID, tenantId);
    } else {
      json.put(JOB_HANDLER_CFG_PROCESS_DEFINITION_TENANT_ID, JSONObject.NULL);
    }

    return json.toString();
  }

  public void execute(String configuration, CoreExecution context, CommandContext commandContext, String tenantId) {
    AbstractSetProcessDefinitionStateCmd cmd = getCommand(configuration);
    cmd.disableLogUserOperation();
    cmd.execute(commandContext);
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

  protected boolean isTenantIdSet(JSONObject config) {
    return config.has(JOB_HANDLER_CFG_PROCESS_DEFINITION_TENANT_ID);
  }

  protected String getTenantId(JSONObject config) {
    if (config.isNull(JOB_HANDLER_CFG_PROCESS_DEFINITION_TENANT_ID)) {
      return null;
    } else {
      return config.getString(JOB_HANDLER_CFG_PROCESS_DEFINITION_TENANT_ID);
    }
  }

  protected UpdateProcessDefinitionSuspensionStateBuilderImpl createBuilder(JSONObject config) {
    String by = getBy(config);

    if (by.equals(JOB_HANDLER_CFG_PROCESS_DEFINITION_ID)) {
      return UpdateProcessDefinitionSuspensionStateBuilderImpl.byId(getProcessDefinitionId(config));

    } else if (by.equals(JOB_HANDLER_CFG_PROCESS_DEFINITION_KEY)) {
      return createBuilderForKey(config);

    } else {
      throw new ProcessEngineException("Unexpected job handler configuration for property '" + JOB_HANDLER_CFG_BY + "': " + by);
    }
  }

  protected UpdateProcessDefinitionSuspensionStateBuilderImpl createBuilderForKey(JSONObject config) {
    UpdateProcessDefinitionSuspensionStateBuilderImpl builder = UpdateProcessDefinitionSuspensionStateBuilderImpl.byKey(getProcessDefinitionKey(config));

    if (isTenantIdSet(config)) {

      String tenantId = getTenantId(config);
      if (tenantId != null) {
        builder.processDefinitionTenantId(tenantId);
      } else {
        builder.processDefinitionWithoutTenantId();
      }
    }
    return builder;
  }

  protected abstract AbstractSetProcessDefinitionStateCmd getCommand(String configuration);

}
