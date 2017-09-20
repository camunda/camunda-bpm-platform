package org.camunda.bpm.engine.test.api.cfg;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.HistoryLevelSetupCommand;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DatabaseHistoryPropertyAutoTest {

  protected List<ProcessEngineImpl> processEngines = new ArrayList<ProcessEngineImpl>();

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  private static ProcessEngineConfigurationImpl config(final String historyLevel) {

    return config("false", historyLevel);
  }

  private static ProcessEngineConfigurationImpl config(final String schemaUpdate, final String historyLevel) {
    StandaloneInMemProcessEngineConfiguration engineConfiguration = new StandaloneInMemProcessEngineConfiguration();
    engineConfiguration.setProcessEngineName(UUID.randomUUID().toString());
    engineConfiguration.setDatabaseSchemaUpdate(schemaUpdate);
    engineConfiguration.setHistory(historyLevel);
    engineConfiguration.setDbMetricsReporterActivate(false);
    engineConfiguration.setJdbcUrl("jdbc:h2:mem:DatabaseHistoryPropertyAutoTest");

    return engineConfiguration;
  }


  @Test
  public void failWhenSecondEngineDoesNotHaveTheSameHistoryLevel() {
    buildEngine(config("true", ProcessEngineConfiguration.HISTORY_FULL));

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("historyLevel mismatch: configuration says HistoryLevelAudit(name=audit, id=2) and database says HistoryLevelFull(name=full, id=3)");

    buildEngine(config(ProcessEngineConfiguration.HISTORY_AUDIT));
  }

  @Test
  public void secondEngineCopiesHistoryLevelFromFirst() {
    // given
    buildEngine(config("true", ProcessEngineConfiguration.HISTORY_FULL));

    // when
    ProcessEngineImpl processEngineTwo = buildEngine(config("true", ProcessEngineConfiguration.HISTORY_AUTO));

    // then
    assertThat(processEngineTwo.getProcessEngineConfiguration().getHistory(), is(ProcessEngineConfiguration.HISTORY_AUTO));
    assertThat(processEngineTwo.getProcessEngineConfiguration().getHistoryLevel(), is(HistoryLevel.HISTORY_LEVEL_FULL));

  }

  @Test
  public void usesDefaultValueAuditWhenNoValueIsConfigured() {
    final ProcessEngineConfigurationImpl config = config("true", ProcessEngineConfiguration.HISTORY_AUTO);
    ProcessEngineImpl processEngine = buildEngine(config);

    final Integer level = config.getCommandExecutorSchemaOperations().execute(new Command<Integer>() {
      @Override
      public Integer execute(CommandContext commandContext) {
        return HistoryLevelSetupCommand.databaseHistoryLevel(commandContext);
      }
    });

    assertThat(level, equalTo(HistoryLevel.HISTORY_LEVEL_AUDIT.getId()));

    assertThat(processEngine.getProcessEngineConfiguration().getHistoryLevel(), equalTo(HistoryLevel.HISTORY_LEVEL_AUDIT));
  }

  @After
  public void after() {
    for (ProcessEngineImpl engine : processEngines) {
      // no need to drop schema when testing with h2
      engine.close();
    }

    processEngines.clear();
  }

  protected ProcessEngineImpl buildEngine(ProcessEngineConfigurationImpl engineConfiguration) {
    ProcessEngineImpl engine = (ProcessEngineImpl) engineConfiguration.buildProcessEngine();
    processEngines.add(engine);

    return engine;
  }

}
