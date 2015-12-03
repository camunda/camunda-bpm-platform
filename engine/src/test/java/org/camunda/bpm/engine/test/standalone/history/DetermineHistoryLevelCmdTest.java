package org.camunda.bpm.engine.test.standalone.history;

import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cmd.DetermineHistoryLevelCmd;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoryEventType;
import org.camunda.bpm.engine.impl.test.TestHelper;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DetermineHistoryLevelCmdTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  private ProcessEngineImpl processEngineImpl;


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
  public void readLevelFullfromDB() throws Exception {
    final ProcessEngineConfigurationImpl config = config("true", ProcessEngineConfiguration.HISTORY_FULL);

    // init the db with level=full
    processEngineImpl = (ProcessEngineImpl) config.buildProcessEngine();

    HistoryLevel historyLevel = config.getCommandExecutorSchemaOperations().execute(new DetermineHistoryLevelCmd(config.getHistoryLevels()));

    assertThat(historyLevel, CoreMatchers.equalTo(HistoryLevel.HISTORY_LEVEL_FULL));
  }


  @Test
  public void useDefaultLevelAudit() throws Exception {
    ProcessEngineConfigurationImpl config = config("true", ProcessEngineConfiguration.HISTORY_AUTO);

    // init the db with level=auto -> audit
    processEngineImpl = (ProcessEngineImpl) config.buildProcessEngine();
    // the history Level has been overwritten with audit
    assertThat(config.getHistoryLevel(), CoreMatchers.equalTo(HistoryLevel.HISTORY_LEVEL_AUDIT));

    // and this is written to the database
    HistoryLevel databaseLevel =
        config.getCommandExecutorSchemaOperations().execute(new DetermineHistoryLevelCmd(config.getHistoryLevels()));
    assertThat(databaseLevel, CoreMatchers.equalTo(HistoryLevel.HISTORY_LEVEL_AUDIT));
  }

  @Test
  public void failWhenExistingHistoryLevelIsNotRegistered() {
    // init the db with custom level
    HistoryLevel customLevel = new HistoryLevel() {
      @Override
      public int getId() {
        return 99;
      }

      @Override
      public String getName() {
        return "custom";
      }

      @Override
      public boolean isHistoryEventProduced(HistoryEventType eventType, Object entity) {
        return false;
      }
    };
    ProcessEngineConfigurationImpl config = config("true", "custom");
    config.setCustomHistoryLevels(Arrays.asList(customLevel));
    processEngineImpl = (ProcessEngineImpl) config.buildProcessEngine();

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("The configured history level with id='99' is not registered in this config.");

    config.getCommandExecutorSchemaOperations().execute(
        new DetermineHistoryLevelCmd(Collections.<HistoryLevel>emptyList()));
  }

  @After
  public void after() {
    TestHelper.dropSchema(processEngineImpl.getProcessEngineConfiguration());
    processEngineImpl.close();
    processEngineImpl = null;
  }
}