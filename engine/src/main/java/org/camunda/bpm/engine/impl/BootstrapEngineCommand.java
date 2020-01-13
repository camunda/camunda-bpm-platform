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

import org.camunda.bpm.engine.ProcessEngineBootstrapCommand;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.db.entitymanager.OptimisticLockingListener;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.EverLivingJobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyEntity;

/**
 * @author Nikola Koevski
 */
public class BootstrapEngineCommand implements ProcessEngineBootstrapCommand {


  private final static EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  @Override
  public Void execute(CommandContext commandContext) {

    checkDeploymentLockExists(commandContext);

    if (isHistoryCleanupEnabled(commandContext)) {
      checkHistoryCleanupLockExists(commandContext);
      createHistoryCleanupJob(commandContext);
    }

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

}
