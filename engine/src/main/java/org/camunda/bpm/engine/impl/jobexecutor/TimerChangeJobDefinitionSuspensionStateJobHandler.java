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
import org.camunda.bpm.engine.impl.cmd.AbstractSetJobDefinitionStateCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.TimerChangeJobDefinitionSuspensionStateJobHandler.JobDefinitionSuspensionStateConfiguration;
import org.camunda.bpm.engine.impl.management.UpdateJobDefinitionSuspensionStateBuilderImpl;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.JsonUtil;
import com.google.gson.JsonObject;

/**
 * @author roman.smirnov
 */
public abstract class TimerChangeJobDefinitionSuspensionStateJobHandler implements JobHandler<JobDefinitionSuspensionStateConfiguration> {

  protected static final String JOB_HANDLER_CFG_BY = "by";
  protected static final String JOB_HANDLER_CFG_JOB_DEFINITION_ID = "jobDefinitionId";
  protected static final String JOB_HANDLER_CFG_PROCESS_DEFINITION_ID = "processDefinitionId";
  protected static final String JOB_HANDLER_CFG_PROCESS_DEFINITION_KEY = "processDefinitionKey";
  protected static final String JOB_HANDLER_CFG_PROCESS_DEFINITION_TENANT_ID = "processDefinitionTenantId";

  protected static final String JOB_HANDLER_CFG_INCLUDE_JOBS = "includeJobs";

  public void execute(JobDefinitionSuspensionStateConfiguration configuration, ExecutionEntity execution, CommandContext commandContext, String tenantId) {
    AbstractSetJobDefinitionStateCmd cmd = getCommand(configuration);
    cmd.disableLogUserOperation();
    cmd.execute(commandContext);
  }

  protected abstract AbstractSetJobDefinitionStateCmd getCommand(JobDefinitionSuspensionStateConfiguration configuration);

  @Override
  public JobDefinitionSuspensionStateConfiguration newConfiguration(String canonicalString) {
    JsonObject jsonObject = JsonUtil.asObject(canonicalString);

    return JobDefinitionSuspensionStateConfiguration.fromJson(jsonObject);
  }

  public static class JobDefinitionSuspensionStateConfiguration implements JobHandlerConfiguration {

    protected String jobDefinitionId;
    protected String processDefinitionKey;
    protected String processDefinitionId;
    protected boolean includeJobs;
    protected String tenantId;
    protected boolean isTenantIdSet;
    protected String by;

    @Override
    public String toCanonicalString() {
      JsonObject json = JsonUtil.createObject();

      JsonUtil.addField(json, JOB_HANDLER_CFG_BY, by);
      JsonUtil.addField(json, JOB_HANDLER_CFG_JOB_DEFINITION_ID, jobDefinitionId);
      JsonUtil.addField(json, JOB_HANDLER_CFG_PROCESS_DEFINITION_KEY, processDefinitionKey);
      JsonUtil.addField(json, JOB_HANDLER_CFG_INCLUDE_JOBS, includeJobs);
      JsonUtil.addField(json, JOB_HANDLER_CFG_PROCESS_DEFINITION_ID, processDefinitionId);

      if (isTenantIdSet) {
        if (tenantId != null) {
          JsonUtil.addField(json, JOB_HANDLER_CFG_PROCESS_DEFINITION_TENANT_ID, tenantId);
        } else {
          JsonUtil.addNullField(json, JOB_HANDLER_CFG_PROCESS_DEFINITION_TENANT_ID);
        }
      }

      return json.toString();
    }

    public UpdateJobDefinitionSuspensionStateBuilderImpl createBuilder() {
      UpdateJobDefinitionSuspensionStateBuilderImpl builder = new UpdateJobDefinitionSuspensionStateBuilderImpl();

      if (JOB_HANDLER_CFG_PROCESS_DEFINITION_ID.equals(by)) {
        builder.byProcessDefinitionId(processDefinitionId);

      }
      else if (JOB_HANDLER_CFG_JOB_DEFINITION_ID.equals(by)) {
        builder.byJobDefinitionId(jobDefinitionId);
      }
      else if (JOB_HANDLER_CFG_PROCESS_DEFINITION_KEY.equals(by)) {
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

      builder.includeJobs(includeJobs);

      return builder;
    }

    public static JobDefinitionSuspensionStateConfiguration fromJson(JsonObject jsonObject) {
      JobDefinitionSuspensionStateConfiguration config = new JobDefinitionSuspensionStateConfiguration();

      config.by = JsonUtil.getString(jsonObject, JOB_HANDLER_CFG_BY);
      if (jsonObject.has(JOB_HANDLER_CFG_JOB_DEFINITION_ID)) {
        config.jobDefinitionId = JsonUtil.getString(jsonObject, JOB_HANDLER_CFG_JOB_DEFINITION_ID);
      }
      if (jsonObject.has(JOB_HANDLER_CFG_PROCESS_DEFINITION_ID)) {
        config.processDefinitionId = JsonUtil.getString(jsonObject, JOB_HANDLER_CFG_PROCESS_DEFINITION_ID);
      }
      if (jsonObject.has(JOB_HANDLER_CFG_PROCESS_DEFINITION_KEY)) {
        config.processDefinitionKey = JsonUtil.getString(jsonObject, JOB_HANDLER_CFG_PROCESS_DEFINITION_KEY);
      }
      if (jsonObject.has(JOB_HANDLER_CFG_PROCESS_DEFINITION_TENANT_ID)) {
        config.isTenantIdSet = true;
        if (!JsonUtil.isNull(jsonObject, JOB_HANDLER_CFG_PROCESS_DEFINITION_TENANT_ID)) {
          config.tenantId = JsonUtil.getString(jsonObject, JOB_HANDLER_CFG_PROCESS_DEFINITION_TENANT_ID);
        }
      }
      if (jsonObject.has(JOB_HANDLER_CFG_INCLUDE_JOBS)) {
        config.includeJobs = JsonUtil.getBoolean(jsonObject, JOB_HANDLER_CFG_INCLUDE_JOBS);
      }

      return config;
    }

    public static JobDefinitionSuspensionStateConfiguration byJobDefinitionId(String jobDefinitionId, boolean includeJobs) {
      JobDefinitionSuspensionStateConfiguration configuration = new JobDefinitionSuspensionStateConfiguration();
      configuration.by = JOB_HANDLER_CFG_JOB_DEFINITION_ID;
      configuration.jobDefinitionId = jobDefinitionId;
      configuration.includeJobs = includeJobs;

      return configuration;
    }

    public static JobDefinitionSuspensionStateConfiguration byProcessDefinitionId(String processDefinitionId, boolean includeJobs) {
      JobDefinitionSuspensionStateConfiguration configuration = new JobDefinitionSuspensionStateConfiguration();

      configuration.by = JOB_HANDLER_CFG_PROCESS_DEFINITION_ID;
      configuration.processDefinitionId = processDefinitionId;
      configuration.includeJobs = includeJobs;

      return configuration;
    }

    public static JobDefinitionSuspensionStateConfiguration byProcessDefinitionKey(String processDefinitionKey, boolean includeJobs) {
      JobDefinitionSuspensionStateConfiguration configuration = new JobDefinitionSuspensionStateConfiguration();

      configuration.by = JOB_HANDLER_CFG_PROCESS_DEFINITION_KEY;
      configuration.processDefinitionKey = processDefinitionKey;
      configuration.includeJobs = includeJobs;

      return configuration;
    }

    public static JobDefinitionSuspensionStateConfiguration ByProcessDefinitionKeyAndTenantId(String processDefinitionKey, String tenantId, boolean includeProcessInstances) {
      JobDefinitionSuspensionStateConfiguration configuration = byProcessDefinitionKey(processDefinitionKey, includeProcessInstances);

      configuration.isTenantIdSet = true;
      configuration.tenantId = tenantId;

      return configuration;

    }


  }

  public void onDelete(JobDefinitionSuspensionStateConfiguration configuration, JobEntity jobEntity) {
    // do nothing
  }

}
