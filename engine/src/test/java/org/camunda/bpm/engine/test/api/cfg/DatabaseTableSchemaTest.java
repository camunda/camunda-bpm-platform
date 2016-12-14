/* Licensed under the Apache License, Version 20.0 (the "License");
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

package org.camunda.bpm.engine.test.api.cfg;

import java.sql.Connection;
import java.sql.SQLException;

import junit.framework.TestCase;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSession;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.util.ReflectUtil;

/**
 * @author Ronny Bräunlich
 */
public class DatabaseTableSchemaTest extends TestCase {

  private static final String SCHEMA_NAME = "SCHEMA1";
  private static final String PREFIX_NAME = "PREFIX1_";

  public void testPerformDatabaseSchemaOperationCreateTwice() throws Exception {

    // both process engines will be using this datasource.
    PooledDataSource pooledDataSource = new PooledDataSource(ReflectUtil.getClassLoader(), "org.h2.Driver",
        "jdbc:h2:mem:DatabaseTablePrefixTest;DB_CLOSE_DELAY=1000", "sa", "");

    Connection connection = pooledDataSource.getConnection();
    connection.createStatement().execute("drop schema if exists " + SCHEMA_NAME);
    connection.createStatement().execute("create schema " + SCHEMA_NAME);
    connection.close();

    ProcessEngineConfigurationImpl config1 = createCustomProcessEngineConfiguration().setProcessEngineName("DatabaseTablePrefixTest-engine1")
    // disable auto create/drop schema
        .setDataSource(pooledDataSource).setDatabaseSchemaUpdate("NO_CHECK");
    config1.setDatabaseTablePrefix(SCHEMA_NAME + ".");
    config1.setDatabaseSchema(SCHEMA_NAME);
    config1.setDbMetricsReporterActivate(false);
    ProcessEngine engine1 = config1.buildProcessEngine();

    // create the tables for the first time
    connection = pooledDataSource.getConnection();
    connection.createStatement().execute("set schema " + SCHEMA_NAME);
    engine1.getManagementService().databaseSchemaUpgrade(connection, "", SCHEMA_NAME);
    connection.close();
    // create the tables for the second time; here we shouldn't crash since the
    // session should tell us that the tables are already present and
    // databaseSchemaUpdate is set to noop
    connection = pooledDataSource.getConnection();
    connection.createStatement().execute("set schema " + SCHEMA_NAME);
    engine1.getManagementService().databaseSchemaUpgrade(connection, "", SCHEMA_NAME);
    engine1.close();
  }

  public void testTablePresentWithSchemaAndPrefix() throws SQLException {
    PooledDataSource pooledDataSource = new PooledDataSource(ReflectUtil.getClassLoader(), "org.h2.Driver",
        "jdbc:h2:mem:DatabaseTablePrefixTest;DB_CLOSE_DELAY=1000", "sa", "");

    Connection connection = pooledDataSource.getConnection();
    connection.createStatement().execute("drop schema if exists " + SCHEMA_NAME);
    connection.createStatement().execute("create schema " + SCHEMA_NAME);
    connection.createStatement().execute("create table " + SCHEMA_NAME + "." + PREFIX_NAME + "SOME_TABLE(id varchar(64));");
    connection.close();

    ProcessEngineConfigurationImpl config1 = createCustomProcessEngineConfiguration().setProcessEngineName("DatabaseTablePrefixTest-engine1")
    // disable auto create/drop schema
        .setDataSource(pooledDataSource).setDatabaseSchemaUpdate("NO_CHECK");
    config1.setDatabaseTablePrefix(SCHEMA_NAME + "." + PREFIX_NAME);
    config1.setDatabaseSchema(SCHEMA_NAME);
    config1.setDbMetricsReporterActivate(false);
    ProcessEngine engine = config1.buildProcessEngine();
    CommandExecutor commandExecutor = config1.getCommandExecutorTxRequired();

    commandExecutor.execute(new Command<Void>(){
      public Void execute(CommandContext commandContext) {
        DbSqlSession sqlSession = commandContext.getSession(DbSqlSession.class);
        assertTrue(sqlSession.isTablePresent("SOME_TABLE"));
        return null;
      }
    });

    engine.close();

  }

  public void testCreateConfigurationWithMismatchtingSchemaAndPrefix() {
    try {
      StandaloneInMemProcessEngineConfiguration configuration = new StandaloneInMemProcessEngineConfiguration();
      configuration.setDatabaseSchema("foo");
      configuration.setDatabaseTablePrefix("bar");
      configuration.buildProcessEngine();
      fail("Should throw exception");
    } catch (ProcessEngineException e) {
      // as expected
      assertTrue(e.getMessage().contains("When setting a schema the prefix has to be schema + '.'"));
    }
  }

  public void testCreateConfigurationWithMissingDotInSchemaAndPrefix() {
    try {
      StandaloneInMemProcessEngineConfiguration configuration = new StandaloneInMemProcessEngineConfiguration();
      configuration.setDatabaseSchema("foo");
      configuration.setDatabaseTablePrefix("foo");
      configuration.buildProcessEngine();
      fail("Should throw exception");
    } catch (ProcessEngineException e) {
      // as expected
      assertTrue(e.getMessage().contains("When setting a schema the prefix has to be schema + '.'"));
    }
  }

  // ----------------------- TEST HELPERS -----------------------

  // allows to return a process engine configuration which doesn't create a
  // schema when it's build.
  private static class CustomStandaloneInMemProcessEngineConfiguration extends StandaloneInMemProcessEngineConfiguration {

    public ProcessEngine buildProcessEngine() {
      init();
      return new NoSchemaProcessEngineImpl(this);
    }

    class NoSchemaProcessEngineImpl extends ProcessEngineImpl {
      public NoSchemaProcessEngineImpl(ProcessEngineConfigurationImpl processEngineConfiguration) {
        super(processEngineConfiguration);
      }

      protected void executeSchemaOperations() {
        // nop - do not execute create schema operations
      }
    }

  }

  private static ProcessEngineConfigurationImpl createCustomProcessEngineConfiguration() {
    return new CustomStandaloneInMemProcessEngineConfiguration().setHistory(ProcessEngineConfiguration.HISTORY_FULL);
  }

}
