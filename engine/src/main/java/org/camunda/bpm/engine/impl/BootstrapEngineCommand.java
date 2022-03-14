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
import org.camunda.bpm.engine.impl.db.entitymanager.OptimisticLockingResult;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.EverLivingJobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyEntity;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyManager;
import org.camunda.bpm.engine.impl.telemetry.dto.TelemetryDataImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.LicenseKeyDataImpl;
import org.camunda.bpm.engine.impl.telemetry.reporter.TelemetryReporter;
import org.camunda.bpm.engine.impl.util.TelemetryUtil;

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
        public OptimisticLockingResult failedOperation(DbOperation operation) {

          // nothing to do, reconfiguration will be handled later on
          return OptimisticLockingResult.IGNORE;
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

      acquireExclusiveTelemetryLock(commandContext);
      PropertyEntity databaseTelemetryProperty = databaseTelemetryConfiguration(commandContext);

      ProcessEngineConfigurationImpl processEngineConfiguration = commandContext.getProcessEngineConfiguration();
      if (databaseTelemetryProperty == null) {
        LOG.noTelemetryPropertyFound();
        createTelemetryProperty(commandContext);
      }

      // reset collected dynamic data
      if ((databaseTelemetryProperty == null && processEngineConfiguration.isInitializeTelemetry())
          || Boolean.valueOf(databaseTelemetryProperty.getValue())) {
        TelemetryUtil.toggleLocalTelemetry(true,
            processEngineConfiguration.getTelemetryRegistry(),
            processEngineConfiguration.getMetricsRegistry());
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
    Boolean telemetryEnabled = commandContext.getProcessEngineConfiguration().isInitializeTelemetry();
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

      acquireExclusiveInstallationIdLock(commandContext);
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

    TelemetryDataImpl telemetryData = processEngineConfiguration.getTelemetryData();

    // set installationId in the telemetry data
    telemetryData.setInstallation(installationId);

    // set the persisted license key in the telemetry data and registry
    ManagementServiceImpl managementService = (ManagementServiceImpl) processEngineConfiguration.getManagementService();
    String licenseKey = managementService.getLicenseKey();
    if (licenseKey != null) {
      LicenseKeyDataImpl licenseKeyData = LicenseKeyDataImpl.fromRawString(licenseKey);
      managementService.setLicenseKeyForTelemetry(licenseKeyData);
      telemetryData.getProduct().getInternals().setLicenseKey(licenseKeyData);
    }
  }

  protected void startTelemetryReporter(CommandContext commandContext) {
    ProcessEngineConfigurationImpl processEngineConfiguration = commandContext.getProcessEngineConfiguration();

    TelemetryReporter telemetryReporter = processEngineConfiguration.getTelemetryReporter();
    boolean telemetryReporterActivate = processEngineConfiguration.isTelemetryReporterActivate();

    // Start telemetry if the reporter is generally activated via the engine configuration.
    // Note that there are two conditions for telemetry to be sent: The reporter runs and
    // telemetry is enabled via API. If the latter is not the case, the reporter will do nothing.
    // However, it is important that it is always running so that it will detect if telemetry
    // was enabled by another engine in the cluster.
    if (telemetryReporter != null && telemetryReporterActivate) {
      try {
        telemetryReporter.start();
      } catch (Exception e) {
        ProcessEngineLogger.TELEMETRY_LOGGER.schedulingTaskFailsOnEngineStart(e);
      }
    }
  }

  protected void acquireExclusiveTelemetryLock(CommandContext commandContext) {
    PropertyManager propertyManager = commandContext.getPropertyManager();
    //exclusive lock
    propertyManager.acquireExclusiveLockForTelemetry();
  }

  protected void acquireExclusiveInstallationIdLock(CommandContext commandContext) {
    PropertyManager propertyManager = commandContext.getPropertyManager();
    //exclusive lock
    propertyManager.acquireExclusiveLockForInstallationId();
  }

  /**
   * When CockroachDB is used, this command may be retried multiple times until
   * it is successful, or the retries are exhausted. CockroachDB uses a stricter,
   * SERIALIZABLE transaction isolation which ensures a serialized manner
   * of transaction execution. A concurrent transaction that attempts to modify
   * the same data as another transaction is required to abort, rollback and retry.
   * This also makes our use-case of pessimistic locks redundant since we only use
   * them as synchronization barriers, and not to lock actual data which would
   * protect it from concurrent modifications.
   *
   * The BootstrapEngine command only executes internal code, so we are certain that
   * a retry of a failed engine bootstrap will not impact user data, and may be
   * performed multiple times.
   */
  @Override
  public boolean isRetryable() {
    return true;
  }
}
