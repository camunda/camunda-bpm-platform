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
package org.camunda.bpm.engine.impl;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.DetermineHistoryLevelCmd;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyEntity;

public final class HistoryLevelSetupCommand implements Command<Void> {

  private final static EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  public Void execute(CommandContext commandContext) {

    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();

    checkStartupLockExists(commandContext);

    HistoryLevel databaseHistoryLevel = new DetermineHistoryLevelCmd(processEngineConfiguration.getHistoryLevels()).execute(commandContext);
    determineAutoHistoryLevel(processEngineConfiguration, databaseHistoryLevel);

    HistoryLevel configuredHistoryLevel = processEngineConfiguration.getHistoryLevel();

    if (databaseHistoryLevel == null) {

      commandContext.getPropertyManager().acquireExclusiveLockForStartup();
      databaseHistoryLevel = new DetermineHistoryLevelCmd(processEngineConfiguration.getHistoryLevels()).execute(commandContext);

      if (databaseHistoryLevel == null) {
        LOG.noHistoryLevelPropertyFound();
        dbCreateHistoryLevel(commandContext);
      }
    } else {
      if (configuredHistoryLevel.getId() != databaseHistoryLevel.getId()) {
        throw new ProcessEngineException("historyLevel mismatch: configuration says " + configuredHistoryLevel
            + " and database says " + databaseHistoryLevel);
      }
    }

    return null;
  }

  public static void dbCreateHistoryLevel(CommandContext commandContext) {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    HistoryLevel configuredHistoryLevel = processEngineConfiguration.getHistoryLevel();
    PropertyEntity property = new PropertyEntity("historyLevel", Integer.toString(configuredHistoryLevel.getId()));
    commandContext.getSession(DbEntityManager.class).insert(property);
    LOG.creatingHistoryLevelPropertyInDatabase(configuredHistoryLevel);
  }

  /**
   *
   * @return Integer value representing the history level or <code>null</code> if none found
   */
  public static Integer databaseHistoryLevel(CommandContext commandContext) {

    try {
      PropertyEntity historyLevelProperty =  commandContext.getPropertyManager().findPropertyById("historyLevel");
      return historyLevelProperty != null ? new Integer(historyLevelProperty.getValue()) : null;
    }
    catch (Exception e) {
      LOG.couldNotSelectHistoryLevel(e.getMessage());
      return null;
    }

  }

  protected void determineAutoHistoryLevel(ProcessEngineConfigurationImpl engineConfiguration, HistoryLevel databaseHistoryLevel) {
    HistoryLevel configuredHistoryLevel = engineConfiguration.getHistoryLevel();

    if (configuredHistoryLevel == null
        && ProcessEngineConfiguration.HISTORY_AUTO.equals(engineConfiguration.getHistory())) {

      // automatically determine history level or use default AUDIT
      if (databaseHistoryLevel != null) {
        engineConfiguration.setHistoryLevel(databaseHistoryLevel);
      }
      else {
        engineConfiguration.setHistoryLevel(engineConfiguration.getDefaultHistoryLevel());
      }
    }
  }

  protected void checkStartupLockExists(CommandContext commandContext) {
    PropertyEntity historyStartupProperty = commandContext.getPropertyManager().findPropertyById("startup.lock");
    if (historyStartupProperty == null) {
      LOG.noStartupLockPropertyFound();
    }
  }

}
