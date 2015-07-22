package org.camunda.bpm.engine.impl.cmd;

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

  public DetermineHistoryLevelCmd(List<HistoryLevel> historyLevels) {
    this.historyLevels = historyLevels;
  }

  @Override
  public HistoryLevel execute(CommandContext commandContext) {
    final Integer databaseHistoryLevel = SchemaOperationsProcessEngineBuild.databaseHistoryLevel(commandContext.getSession(DbEntityManager.class));

    HistoryLevel result = null;

    if (databaseHistoryLevel != null) {
      for (HistoryLevel historyLevel : historyLevels) {
        if (historyLevel.getId() == databaseHistoryLevel) {
          result = historyLevel;
          break;
        }
      }
    }

    if (result == null) {
      result = HistoryLevel.HISTORY_LEVEL_AUDIT;
    }

    return result;
  }


}
