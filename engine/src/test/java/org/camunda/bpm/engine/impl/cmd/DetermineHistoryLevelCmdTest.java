package org.camunda.bpm.engine.impl.cmd;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.test.TestHelper;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import static org.junit.Assert.assertThat;

public class DetermineHistoryLevelCmdTest {


  private ProcessEngineImpl processEngineImpl;


  private static ProcessEngineConfigurationImpl config(final String historyLevel) {
    return config("false", historyLevel);
  }

  private static ProcessEngineConfigurationImpl config(final String schemaUpdate, final String historyLevel) {
    final StandaloneInMemProcessEngineConfiguration engineConfiguration = new StandaloneInMemProcessEngineConfiguration();
    engineConfiguration.setProcessEngineName(UUID.randomUUID().toString());
    engineConfiguration.setDatabaseSchemaUpdate(schemaUpdate);
    engineConfiguration.setHistory(historyLevel);
    engineConfiguration.setJdbcUrl("jdbc:h2:mem:DatabaseHistoryPropertyAutoTest");

    return engineConfiguration;
  }


  @Test
  public void readLevelFullfromDB() throws Exception {
    final ProcessEngineConfigurationImpl config = config("true", ProcessEngineConfiguration.HISTORY_FULL);

    // init the db with level=full
    processEngineImpl = (ProcessEngineImpl) config.buildProcessEngine();

    final HistoryLevel historyLevel = config.getCommandExecutorSchemaOperations().execute(new DetermineHistoryLevelCmd(config.getHistoryLevels()));

    assertThat(historyLevel, CoreMatchers.equalTo(HistoryLevel.HISTORY_LEVEL_FULL));
  }


  @Test
  public void use_default_level_audit() throws Exception {
    final ProcessEngineConfigurationImpl config = config("true", ProcessEngineConfiguration.HISTORY_AUTO);

    // init the db with level=auto -> audit
    processEngineImpl = (ProcessEngineImpl) config.buildProcessEngine();

    config.getCommandExecutorSchemaOperations().execute(new DetermineHistoryLevelCmd(config.getHistoryLevels()));

    // the history Level has been overwritten with audit
    assertThat(config.getHistoryLevel(), CoreMatchers.equalTo(HistoryLevel.HISTORY_LEVEL_AUDIT));
  }

  @After
  public void after() {
    TestHelper.dropSchema(processEngineImpl.getProcessEngineConfiguration());
    processEngineImpl.close();
    processEngineImpl = null;
  }
}