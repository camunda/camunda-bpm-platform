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
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.TimerChangeProcessDefinitionSuspensionStateJobHandler.ProcessDefinitionSuspensionStateConfiguration;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.repository.UpdateProcessDefinitionSuspensionStateBuilderImpl;
import org.camunda.bpm.engine.impl.util.json.JSONObject;

/**
 * @author Joram Barrez
 * @author roman.smirnov
 */
public abstract class TimerChangeProcessDefinitionSuspensionStateJobHandler implements JobHandler<ProcessDefinitionSuspensionStateConfiguration> {

  protected static final String JOB_HANDLER_CFG_BY = "by";
  protected static final String JOB_HANDLER_CFG_PROCESS_DEFINITION_ID = "processDefinitionId";
  protected static final String JOB_HANDLER_CFG_PROCESS_DEFINITION_KEY = "processDefinitionKey";
  protected static final String JOB_HANDLER_CFG_PROCESS_DEFINITION_TENANT_ID = "processDefinitionTenantId";

  protected static final String JOB_HANDLER_CFG_INCLUDE_PROCESS_INSTANCES = "includeProcessInstances";

  public void execute(ProcessDefinitionSuspensionStateConfiguration configuration, ExecutionEntity execution, CommandContext commandContext, String tenantId) {
    AbstractSetProcessDefinitionStateCmd cmd = getCommand(configuration);
    cmd.disableLogUserOperation();
    cmd.execute(commandContext);
  }

  protected abstract AbstractSetProcessDefinitionStateCmd getCommand(ProcessDefinitionSuspensionStateConfiguration configuration);

  @Override
  public ProcessDefinitionSuspensionStateConfiguration newConfiguration(String canonicalString) {
    JSONObject jsonObject = new JSONObject(canonicalString);

    return ProcessDefinitionSuspensionStateConfiguration.fromJson(jsonObject);
  }

  public static class ProcessDefinitionSuspensionStateConfiguration implements JobHandlerConfiguration {

    protected String processDefinitionKey;
    protected String processDefinitionId;
    protected boolean includeProcessInstances;
    protected String tenantId;
    protected boolean isTenantIdSet;
    protected String by;

    @Override
    public String toCanonicalString() {
      JSONObject json = new JSONObject();

      json.put(JOB_HANDLER_CFG_BY, by);
      json.put(JOB_HANDLER_CFG_PROCESS_DEFINITION_KEY, processDefinitionKey);
      json.put(JOB_HANDLER_CFG_INCLUDE_PROCESS_INSTANCES, includeProcessInstances);
      json.put(JOB_HANDLER_CFG_PROCESS_DEFINITION_ID, processDefinitionId);

      if (isTenantIdSet) {
        if (tenantId != null) {
          json.put(JOB_HANDLER_CFG_PROCESS_DEFINITION_TENANT_ID, tenantId);
        } else {
          json.put(JOB_HANDLER_CFG_PROCESS_DEFINITION_TENANT_ID, JSONObject.NULL);
        }
      }

      return json.toString();
    }

    public UpdateProcessDefinitionSuspensionStateBuilderImpl createBuilder() {
      UpdateProcessDefinitionSuspensionStateBuilderImpl builder = new UpdateProcessDefinitionSuspensionStateBuilderImpl();

      if (by.equals(JOB_HANDLER_CFG_PROCESS_DEFINITION_ID)) {
        builder.byProcessDefinitionId(processDefinitionId);

      } else if (by.equals(JOB_HANDLER_CFG_PROCESS_DEFINITION_KEY)) {
        builder.byProcessDefinitionKey(processDefinitionKey);

        if (isTenantIdSet) {

          if (tenantId != null) {
            builder.processDefinitionTenantId(tenantId);

          } else {
            builder.processDefinitionWithoutTenantId();
          }
        }

      } else {
        throw new ProcessEngineException("Unexpected job handler configuration for property '" + JOB_HANDLER_CFG_BY + "': " + by);
      }

      builder.includeProcessInstances(includeProcessInstances);

      return builder;
    }

    public static ProcessDefinitionSuspensionStateConfiguration fromJson(JSONObject jsonObject) {
      ProcessDefinitionSuspensionStateConfiguration config = new ProcessDefinitionSuspensionStateConfiguration();

      config.by = jsonObject.getString(JOB_HANDLER_CFG_BY);
      if (jsonObject.has(JOB_HANDLER_CFG_PROCESS_DEFINITION_ID)) {
        config.processDefinitionId = jsonObject.getString(JOB_HANDLER_CFG_PROCESS_DEFINITION_ID);
      }
      if (jsonObject.has(JOB_HANDLER_CFG_PROCESS_DEFINITION_KEY)) {
        config.processDefinitionKey = jsonObject.getString(JOB_HANDLER_CFG_PROCESS_DEFINITION_KEY);
      }
      if (jsonObject.has(JOB_HANDLER_CFG_PROCESS_DEFINITION_TENANT_ID)) {
        config.isTenantIdSet = true;
        if (!jsonObject.isNull(JOB_HANDLER_CFG_PROCESS_DEFINITION_TENANT_ID)) {
          config.tenantId = jsonObject.getString(JOB_HANDLER_CFG_PROCESS_DEFINITION_TENANT_ID);
        }
      }
      if (jsonObject.has(JOB_HANDLER_CFG_INCLUDE_PROCESS_INSTANCES)) {
        config.includeProcessInstances = jsonObject.getBoolean(JOB_HANDLER_CFG_INCLUDE_PROCESS_INSTANCES);
      }

      return config;
    }

    public static ProcessDefinitionSuspensionStateConfiguration byProcessDefinitionId(String processDefinitionId, boolean includeProcessInstances) {
      ProcessDefinitionSuspensionStateConfiguration configuration = new ProcessDefinitionSuspensionStateConfiguration();

      configuration.by = JOB_HANDLER_CFG_PROCESS_DEFINITION_ID;
      configuration.processDefinitionId = processDefinitionId;
      configuration.includeProcessInstances = includeProcessInstances;

      return configuration;
    }

    public static ProcessDefinitionSuspensionStateConfiguration byProcessDefinitionKey(String processDefinitionKey, boolean includeProcessInstances) {
      ProcessDefinitionSuspensionStateConfiguration configuration = new ProcessDefinitionSuspensionStateConfiguration();

      configuration.by = JOB_HANDLER_CFG_PROCESS_DEFINITION_KEY;
      configuration.processDefinitionKey = processDefinitionKey;
      configuration.includeProcessInstances = includeProcessInstances;

      return configuration;
    }

    public static ProcessDefinitionSuspensionStateConfiguration byProcessDefinitionKeyAndTenantId(String processDefinitionKey, String tenantId, boolean includeProcessInstances) {
      ProcessDefinitionSuspensionStateConfiguration configuration = byProcessDefinitionKey(processDefinitionKey, includeProcessInstances);

      configuration.isTenantIdSet = true;
      configuration.tenantId = tenantId;

      return configuration;

    }


  }

  public void onDelete(ProcessDefinitionSuspensionStateConfiguration configuration, JobEntity jobEntity) {
    // do nothing
  }

}
