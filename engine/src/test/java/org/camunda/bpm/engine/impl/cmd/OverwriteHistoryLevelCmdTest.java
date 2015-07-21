package org.camunda.bpm.engine.impl.cmd;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.test.TestHelper;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.ExternalResource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import static org.junit.Assert.assertThat;

public class OverwriteHistoryLevelCmdTest {


  private  final ProcessEngineConfigurationImpl config = config("true", ProcessEngineConfiguration.HISTORY_FULL);
    private ProcessEngineImpl processEngineImpl;


  @Rule
  public final ExpectedException thrown = ExpectedException.none();

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
  public void update_history_level_in_configuration() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

    processEngineImpl = (ProcessEngineImpl) config.buildProcessEngine();

    // we have to invoke init(), or several NPE occur.
    ProcessEngineConfigurationImpl secondConfig = config(ProcessEngineConfiguration.HISTORY_AUTO);
    final Method init = ProcessEngineConfigurationImpl.class.getDeclaredMethod("init");
    init.setAccessible(true);
    init.invoke(secondConfig);

    config.getCommandExecutorSchemaOperations().execute(new OverwriteHistoryLevelCmd(secondConfig));

    assertThat(secondConfig.getHistoryLevel(), CoreMatchers.equalTo(HistoryLevel.HISTORY_LEVEL_FULL));
  }

  

  @After
  public void after() {
    TestHelper.dropSchema(processEngineImpl.getProcessEngineConfiguration());
    processEngineImpl.close();
    processEngineImpl = null;
  }
}