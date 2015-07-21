package org.camunda.bpm.engine.impl.cmd;

import org.camunda.bpm.engine.impl.SchemaOperationsProcessEngineBuild;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;


public class OverwriteHistoryLevelCmd implements Command<Void> {

  private final ProcessEngineConfigurationImpl processEngineConfiguration;

  public OverwriteHistoryLevelCmd(ProcessEngineConfigurationImpl processEngineConfiguration) {
    this.processEngineConfiguration = processEngineConfiguration;
  }

  @Override
  public Void execute(CommandContext commandContext) {
    final Integer databaseHistoryLevel = SchemaOperationsProcessEngineBuild.databaseHistoryLevel(commandContext.getSession(DbEntityManager.class));


    if (databaseHistoryLevel != null) {
      for (HistoryLevel historyLevel : processEngineConfiguration.getHistoryLevels()) {
        if (historyLevel.getId() == databaseHistoryLevel) {
          processEngineConfiguration.setHistoryLevel(historyLevel);
          processEngineConfiguration.setHistory(historyLevel.getName());
          break;
        }
      }
    }

    if (processEngineConfiguration.getHistoryLevel() == null) {
      processEngineConfiguration.setHistoryLevel(HistoryLevel.HISTORY_LEVEL_AUDIT);
      processEngineConfiguration.setHistory(HistoryLevel.HISTORY_LEVEL_AUDIT.getName());
    }

    return null;
  }


}
