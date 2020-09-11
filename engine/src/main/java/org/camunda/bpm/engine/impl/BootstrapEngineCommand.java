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
package org.camunda.bpm.engine.impl;

import java.util.UUID;

import org.camunda.bpm.engine.ProcessEngineBootstrapCommand;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.db.entitymanager.OptimisticLockingListener;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.EverLivingJobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyEntity;
import org.camunda.bpm.engine.impl.telemetry.reporter.TelemetryReporter;
import org.camunda.bpm.engine.impl.util.ClockUtil;

/**
 * @author Nikola Koevski
 */
public class BootstrapEngineCommand implements ProcessEngineBootstrapCommand {

  private final static EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  protected static final String TELEMETRY_PROPERTY_NAME = "camunda.telemetry.enabled";
  protected static final String INSTALLATION_PROPERTY_NAME = "camunda.installation.id";

  @Override
  public Void execute(CommandContext commandContext) {

    initializeInstallationId(commandContext);

    checkDeploymentLockExists(commandContext);

    if (isHistoryCleanupEnabled(commandContext)) {
      checkHistoryCleanupLockExists(commandContext);
      createHistoryCleanupJob(commandContext);
    }

    initializeTelemetryProperty(commandContext);
    // installationId needs to be updated in the telemetry data
    updateTelemetryData(commandContext);
    startTelemetryReporter(commandContext);

    return null;
  }

  protected void createHistoryCleanupJob(CommandContext commandContext) {
    if (Context.getProcessEngineConfiguration().getManagementService().getTableMetaData("ACT_RU_JOB") != null) {
      // CAM-9671: avoid transaction rollback due to the OLE being caught in CommandContext#close
      commandContext.getDbEntityManager().registerOptimisticLockingListener(new OptimisticLockingListener() {

        @Override
        public Class<? extends DbEntity> getEntityType() {
          return EverLivingJobEntity.class;
        }

        @Override
        public void failedOperation(DbOperation operation) {
          // nothing do to, reconfiguration will be handled later on
        }
      });
      Context.getProcessEngineConfiguration().getHistoryService().cleanUpHistoryAsync();
    }
  }

  public void checkDeploymentLockExists(CommandContext commandContext) {
    PropertyEntity deploymentLockProperty = commandContext.getPropertyManager().findPropertyById("deployment.lock");
    if (deploymentLockProperty == null) {
      LOG.noDeploymentLockPropertyFound();
    }
  }

  public void checkHistoryCleanupLockExists(CommandContext commandContext) {
    PropertyEntity historyCleanupLockProperty = commandContext.getPropertyManager().findPropertyById("history.cleanup.job.lock");
    if (historyCleanupLockProperty == null) {
      LOG.noHistoryCleanupLockPropertyFound();
    }
  }

  protected boolean isHistoryCleanupEnabled(CommandContext commandContext) {
    return commandContext.getProcessEngineConfiguration()
        .isHistoryCleanupEnabled();
  }

  public void initializeTelemetryProperty(CommandContext commandContext) {
    try {

      checkTelemetryLockExists(commandContext);

      commandContext.getPropertyManager().acquireExclusiveLockForTelemetry();
      PropertyEntity databaseTelemetryProperty = databaseTelemetryConfiguration(commandContext);

      if (databaseTelemetryProperty == null) {
        LOG.noTelemetryPropertyFound();
        createTelemetryProperty(commandContext);
      }

    } catch (Exception e) {
      LOG.errorConfiguringTelemetryProperty(e);
    }
  }

  protected void checkTelemetryLockExists(CommandContext commandContext) {
    PropertyEntity telemetryLockProperty = commandContext.getPropertyManager().findPropertyById("telemetry.lock");
    if (telemetryLockProperty == null) {
      LOG.noTelemetryLockPropertyFound();
    }
  }

  protected PropertyEntity databaseTelemetryConfiguration(CommandContext commandContext) {
    try {
      return commandContext.getPropertyManager().findPropertyById(TELEMETRY_PROPERTY_NAME);
    } catch (Exception e) {
      LOG.errorFetchingTelemetryPropertyInDatabase(e);
      return null;
    }
  }

  protected void createTelemetryProperty(CommandContext commandContext) {
    Boolean telemetryEnabled = Context.getProcessEngineConfiguration().isInitializeTelemetry();
    PropertyEntity property = null;
    if (telemetryEnabled != null) {
      property = new PropertyEntity(TELEMETRY_PROPERTY_NAME, Boolean.toString(telemetryEnabled));
    } else {
      property = new PropertyEntity(TELEMETRY_PROPERTY_NAME, "null");
    }
    commandContext.getPropertyManager().insert(property);
    LOG.creatingTelemetryPropertyInDatabase(telemetryEnabled);
  }

  public void initializeInstallationId(CommandContext commandContext) {
    checkInstallationIdLockExists(commandContext);

    String databaseInstallationId = databaseInstallationId(commandContext);

    if (databaseInstallationId == null || databaseInstallationId.isEmpty()) {

      commandContext.getPropertyManager().acquireExclusiveLockForInstallationId();
      databaseInstallationId = databaseInstallationId(commandContext);

      if (databaseInstallationId == null || databaseInstallationId.isEmpty()) {
        LOG.noInstallationIdPropertyFound();
        createInstallationProperty(commandContext);
      }
    } else {
      LOG.installationIdPropertyFound(databaseInstallationId);
      commandContext.getProcessEngineConfiguration().setInstallationId(databaseInstallationId);
    }
  }

  protected void createInstallationProperty(CommandContext commandContext) {
    String installationId = UUID.randomUUID().toString();
    PropertyEntity property = new PropertyEntity(INSTALLATION_PROPERTY_NAME, installationId);
    commandContext.getPropertyManager().insert(property);
    LOG.creatingInstallationPropertyInDatabase(property.getValue());
    commandContext.getProcessEngineConfiguration().setInstallationId(installationId);
  }

  protected String databaseInstallationId(CommandContext commandContext) {
    try {
      PropertyEntity installationIdProperty = commandContext.getPropertyManager().findPropertyById(INSTALLATION_PROPERTY_NAME);
      return installationIdProperty != null ? installationIdProperty.getValue() : null;
    } catch (Exception e) {
      LOG.couldNotSelectInstallationId(e.getMessage());
      return null;
    }
  }

  protected void checkInstallationIdLockExists(CommandContext commandContext) {
    PropertyEntity installationIdProperty = commandContext.getPropertyManager().findPropertyById("installationId.lock");
    if (installationIdProperty == null) {
      LOG.noInstallationIdLockPropertyFound();
    }
  }

  protected void updateTelemetryData(CommandContext commandContext) {
    ProcessEngineConfigurationImpl processEngineConfiguration = commandContext.getProcessEngineConfiguration();
    String installationId = processEngineConfiguration.getInstallationId();

    // set installationId in the telemetry data
    processEngineConfiguration.getTelemetryData().setInstallation(installationId);
  }

  protected void startTelemetryReporter(CommandContext commandContext) {
    ProcessEngineConfigurationImpl processEngineConfiguration = commandContext.getProcessEngineConfiguration();

    TelemetryReporter telemetryReporter = processEngineConfiguration.getTelemetryReporter();

    // Always start telemetry reporter, regardless of engine configuration.
    // If telemetry is disabled, the reporter simply does nothing when it runs.
    // That way, the reporter will detect if telemetry was activated via a different process engine.
    if (telemetryReporter != null) {
      try {
        telemetryReporter.start();
        // set start report time
        processEngineConfiguration.getTelemetryRegistry().setStartReportTime(ClockUtil.getCurrentTime());
      } catch (Exception e) {
        ProcessEngineLogger.TELEMETRY_LOGGER.schedulingTaskFailsOnEngineStart(e);
      }
    }
  }
}
