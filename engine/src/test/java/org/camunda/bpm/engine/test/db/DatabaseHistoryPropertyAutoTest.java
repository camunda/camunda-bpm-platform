package org.camunda.bpm.engine.test.db;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.SchemaOperationsProcessEngineBuild;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.HistoryLevelAudit;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.test.TestHelper;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DatabaseHistoryPropertyAutoTest {
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
  public void fail_when_second_engine_does_not_have_the_same_historyLevel() {
    processEngineImpl = (ProcessEngineImpl) config("true", ProcessEngineConfiguration.HISTORY_FULL).buildProcessEngine();

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("historyLevel mismatch: configuration says HistoryLevelAudit(name=audit, id=2) and database says 3");

    config(ProcessEngineConfiguration.HISTORY_AUDIT).buildProcessEngine();
  }

  @Test
  public void second_engine_copies_historyLevel_from_first() {
    processEngineImpl = (ProcessEngineImpl) config("true", ProcessEngineConfiguration.HISTORY_FULL).buildProcessEngine();

    final ProcessEngineImpl processEngine = (ProcessEngineImpl) config(ProcessEngineConfiguration.HISTORY_AUTO).buildProcessEngine();

    assertThat(processEngine.getProcessEngineConfiguration().getHistory(), is(ProcessEngineConfiguration.HISTORY_FULL));
  }

  @Test
  public void uses_default_value_audit_when_no_value_is_configured() {
    ProcessEngineConfigurationImpl config = config("true", ProcessEngineConfiguration.HISTORY_AUTO);
    processEngineImpl = (ProcessEngineImpl) config.buildProcessEngine();

    config.getCommandExecutorSchemaOperations().execute(new Command<Void>() {
      @Override
      public Void execute(CommandContext commandContext) {
        Integer level = SchemaOperationsProcessEngineBuild.databaseHistoryLevel(commandContext.getSession(DbEntityManager.class));

        assertThat(level, equalTo(HistoryLevel.HISTORY_LEVEL_AUDIT.getId()));

        return null;
      }
    });

  }

  @After
  public void after() {
    TestHelper.dropSchema(processEngineImpl.getProcessEngineConfiguration());
    processEngineImpl.close();
    processEngineImpl = null;
  }

}
