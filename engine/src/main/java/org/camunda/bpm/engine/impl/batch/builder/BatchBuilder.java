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
package org.camunda.bpm.engine.impl.batch.builder;

import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.batch.BatchConfiguration;
import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.batch.BatchJobHandler;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandler;
import org.camunda.bpm.engine.impl.util.ClockUtil;

public class BatchBuilder {

  protected CommandContext commandContext;

  protected BatchConfiguration config;
  protected String tenantId;
  protected String type;

  protected Integer totalJobsCount;

  protected Permission permission;
  protected PermissionHandler permissionHandler;

  protected OperationLogInstanceCountHandler operationLogInstanceCountHandler;
  protected OperationLogHandler operationLogHandler;

  public BatchBuilder(CommandContext commandContext) {
    this.commandContext = commandContext;
  }

  public BatchBuilder tenantId(String tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  public BatchBuilder config(BatchConfiguration config) {
    this.config = config;
    return this;
  }

  public BatchBuilder type(String batchType) {
    this.type = batchType;
    return this;
  }

  public BatchBuilder totalJobs(int totalJobsCount) {
    this.totalJobsCount = totalJobsCount;
    return this;
  }

  public BatchBuilder permission(Permission permission) {
    this.permission = permission;
    return this;
  }

  public BatchBuilder permissionHandler(PermissionHandler permissionCheckHandler) {
    this.permissionHandler = permissionCheckHandler;
    return this;
  }

  public BatchBuilder operationLogHandler(OperationLogInstanceCountHandler operationLogHandler) {
    this.operationLogInstanceCountHandler = operationLogHandler;
    return this;
  }

  public BatchBuilder operationLogHandler(OperationLogHandler operationLogHandler) {
    this.operationLogHandler = operationLogHandler;
    return this;
  }

  public Batch build() {
    checkPermissions();

    BatchEntity batch = new BatchEntity();
    configure(batch);
    save(batch);

    writeOperationLog();

    return batch;
  }

  protected void checkPermissions() {
    if (permission == null && permissionHandler == null) {
      throw new ProcessEngineException("No permission check performed!");
    }

    if (permission != null) {
      commandContext.getProcessEngineConfiguration()
          .getCommandCheckers()
          .forEach(checker -> checker.checkCreateBatch(permission));

    }

    if (permissionHandler != null) {
      permissionHandler.check(commandContext);

    }
  }

  @SuppressWarnings(value = "unchecked")
  protected BatchEntity configure(BatchEntity batch) {
    ProcessEngineConfigurationImpl engineConfig = commandContext.getProcessEngineConfiguration();

    Map<String, JobHandler> jobHandlers = engineConfig.getJobHandlers();
    BatchJobHandler jobHandler = (BatchJobHandler) jobHandlers.get(type);

    String type = jobHandler.getType();
    batch.setType(type);

    int invocationPerBatchJobCount = jobHandler.calculateInvocationsPerBatchJob(type, config);
    batch.setInvocationsPerBatchJob(invocationPerBatchJobCount);

    batch.setTenantId(tenantId);
    batch.setStartTime(ClockUtil.getCurrentTime());

    byte[] configAsBytes = jobHandler.writeConfiguration(config);
    batch.setConfigurationBytes(configAsBytes);

    setTotalJobs(batch, invocationPerBatchJobCount);

    int jobCount = engineConfig.getBatchJobsPerSeed();
    batch.setBatchJobsPerSeed(jobCount);

    return batch;
  }

  protected void setTotalJobs(BatchEntity batch, int invocationPerBatchJobCount) {
    if (totalJobsCount != null) {
      batch.setTotalJobs(totalJobsCount);

    } else {
      List<String> instanceIds = config.getIds();

      int instanceCount = instanceIds.size();
      int totalJobsCount = calculateTotalJobs(instanceCount, invocationPerBatchJobCount);

      batch.setTotalJobs(totalJobsCount);
    }
  }

  protected void save(BatchEntity batch) {
    commandContext.getBatchManager().insertBatch(batch);

    String seedDeploymentId = null;
    if (config.getIdMappings() != null && !config.getIdMappings().isEmpty()) {
      seedDeploymentId = config.getIdMappings().get(0).getDeploymentId();
    }

    batch.createSeedJobDefinition(seedDeploymentId);
    batch.createMonitorJobDefinition();
    batch.createBatchJobDefinition();

    batch.fireHistoricStartEvent();

    batch.createSeedJob();
  }

  protected void writeOperationLog() {
    if (operationLogInstanceCountHandler == null && operationLogHandler == null) {
      throw new ProcessEngineException("No operation log handler specified!");
    }

    if (operationLogInstanceCountHandler != null) {
      List<String> instanceIds = config.getIds();

      int instanceCount = instanceIds.size();
      operationLogInstanceCountHandler.write(commandContext, instanceCount);

    } else {
      operationLogHandler.write(commandContext);

    }
  }

  protected int calculateTotalJobs(int instanceCount, int invocationPerBatchJobCount) {
    if (instanceCount == 0 || invocationPerBatchJobCount == 0) {
      return 0;
    }

    if (instanceCount % invocationPerBatchJobCount == 0) {
      return instanceCount / invocationPerBatchJobCount;
    }

    return (instanceCount / invocationPerBatchJobCount) + 1;
  }

}
