package org.camunda.bpm.integrationtest.functional.database;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.ManagementServiceImpl;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.SchemaOperationsProcessEngineBuild;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.db.PersistenceSession;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
@RunWith(Arquillian.class)
public class PurgeDatabaseTest extends AbstractFoxPlatformIntegrationTest {

  public static final List<String> TABLENAMES_EXCLUDED_FROM_DB_CLEAN_CHECK = Arrays.asList(
    "ACT_GE_PROPERTY"
  );

  @Deployment
  public static WebArchive processArchive() {
    return initWebArchiveDeployment()
      .addAsResource("org/camunda/bpm/integrationtest/testDeployProcessArchive.bpmn20.xml");
  }

  @Test
  public void testPurgeDatabase() {
    Assert.assertNotNull(processEngine);
    VariableMap variableMap = Variables.putValue("var", "value");
    runtimeService.startProcessInstanceByKey("testDeployProcessArchive", variableMap);
    runtimeService.startProcessInstanceByKey("testDeployProcessArchive", variableMap);

    ManagementServiceImpl managementServiceImpl = (ManagementServiceImpl) managementService;
    managementServiceImpl.purge();

    assertAndEnsureCleanDb(processEngine);
  }

  /**
   * Ensures that the database is clean after the test. This means the test has to remove
   * all resources it entered to the database.
   * If the DB is not clean, it is cleaned by performing a create a drop.
   *
   * @param processEngine the {@link ProcessEngine} to check
   * @param fail if true the method will throw an {@link AssertionError} if the database is not clean
   * @return the database summary if fail is set to false or null if database was clean
   * @throws AssertionError if the database was not clean and fail is set to true
   */
  public static void assertAndEnsureCleanDb(ProcessEngine processEngine) {
    ProcessEngineConfigurationImpl processEngineConfiguration = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration();
    String databaseTablePrefix = processEngineConfiguration.getDatabaseTablePrefix().trim();

    Map<String, Long> tableCounts = processEngine.getManagementService().getTableCount();

    StringBuilder outputMessage = new StringBuilder();
    for (String tableName : tableCounts.keySet()) {
      String tableNameWithoutPrefix = tableName.replace(databaseTablePrefix, "");
      if (!TABLENAMES_EXCLUDED_FROM_DB_CLEAN_CHECK.contains(tableNameWithoutPrefix)) {
        Long count = tableCounts.get(tableName);
        if (count!=0L) {
          outputMessage.append("\t").append(tableName).append(": ").append(count).append(" record(s)\n");
        }
      }
    }

    if (outputMessage.length() > 0) {
      outputMessage.insert(0, "DB NOT CLEAN: \n");
      /** skip drop and recreate if a table prefix is used */
      if (databaseTablePrefix.isEmpty()) {
        processEngineConfiguration
          .getCommandExecutorSchemaOperations()
          .execute(new Command<Object>() {
            public Object execute(CommandContext commandContext) {
              PersistenceSession persistenceSession = commandContext.getSession(PersistenceSession.class);
              persistenceSession.dbSchemaDrop();
              persistenceSession.dbSchemaCreate();
              SchemaOperationsProcessEngineBuild.dbCreateHistoryLevel(commandContext.getDbEntityManager());
              return null;
            }
          });
      }
      Assert.fail(outputMessage.toString());
    }
  }

}
