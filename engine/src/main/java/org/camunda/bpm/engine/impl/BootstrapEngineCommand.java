/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
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
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyEntity;

/**
 * @author Nikola Koevski
 */
public class BootstrapEngineCommand implements ProcessEngineBootstrapCommand {


  private final static EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  @Override
  public Void execute(CommandContext commandContext) {

    checkDeploymentLockExists(commandContext);
    checkHistoryCleanupLockExists(commandContext);
    createHistoryCleanupJob();

    return null;
  }

  protected void createHistoryCleanupJob() {
    if (Context.getProcessEngineConfiguration().getManagementService().getTableMetaData("ACT_RU_JOB") != null) {
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
}
