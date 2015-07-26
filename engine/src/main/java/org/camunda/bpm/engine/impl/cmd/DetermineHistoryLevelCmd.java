package org.camunda.bpm.engine.impl.cmd;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.SchemaOperationsProcessEngineBuild;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

import java.util.List;

/**
 * Read the already configured historyLevel from DB and map to given list of total levels.
 */
public class DetermineHistoryLevelCmd implements Command<HistoryLevel> {

  private final List<HistoryLevel> historyLevels;

  public DetermineHistoryLevelCmd(final List<HistoryLevel> historyLevels) {
    this.historyLevels = historyLevels;
  }

  @Override
  public HistoryLevel execute(final CommandContext commandContext) {
    final Integer databaseHistoryLevel = SchemaOperationsProcessEngineBuild.databaseHistoryLevel(commandContext.getSession(DbEntityManager.class));

    HistoryLevel result = null;

    if (databaseHistoryLevel != null) {
      for (final HistoryLevel historyLevel : historyLevels) {
        if (historyLevel.getId() == databaseHistoryLevel) {
          result = historyLevel;
          break;
        }
      }
      // if a custom non-null value is not registered, throw an exception.
      if (result == null) {
        throw new ProcessEngineException(String.format("The configured history level with id='%s' is not registered in this config.", databaseHistoryLevel));
      }
    }

    if (result == null) {
      result = HistoryLevel.HISTORY_LEVEL_AUDIT;
    }

    return result;
  }


}
