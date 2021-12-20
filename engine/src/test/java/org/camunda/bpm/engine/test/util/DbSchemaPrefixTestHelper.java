/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.util;

import java.sql.Connection;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * <p>Test utility allowing to run the testsuite with a database
 * table prefix</p>
 *
 * @author Daniel Meyer
 *
 */
public class DbSchemaPrefixTestHelper implements InitializingBean, DisposableBean {

  private PooledDataSource dataSource;

  public void afterPropertiesSet() throws Exception {

    dataSource = new PooledDataSource(ReflectUtil.getClassLoader(),
            "org.h2.Driver",
            "jdbc:h2:mem:DatabaseTablePrefixTest;DB_CLOSE_DELAY=1000;",
            "sa",
            "" );

    // create schema in the
    Connection connection = dataSource.getConnection();
    connection.createStatement().execute("drop schema if exists SCHEMA1 cascade");
    connection.createStatement().execute("create schema SCHEMA1");
    connection.close();

    ProcessEngineConfigurationImpl config1 = createCustomProcessEngineConfiguration()
            .setProcessEngineName("DatabaseTablePrefixTest-engine1")
            .setDataSource(dataSource)
            .setDbMetricsReporterActivate(false)
            .setDatabaseSchemaUpdate("NO_CHECK"); // disable auto create/drop schema
    config1.setDatabaseTablePrefix("SCHEMA1.");
    ProcessEngine engine1 = config1.buildProcessEngine();

    // create the tables in SCHEMA1
    connection = dataSource.getConnection();
    connection.createStatement().execute("set schema SCHEMA1");
    engine1.getManagementService().databaseSchemaUpgrade(connection, "", "SCHEMA1");
    connection.close();

    engine1.close();

  }

  public void destroy() throws Exception {
    Connection connection = dataSource.getConnection();
    connection.createStatement().execute("drop schema if exists SCHEMA1 cascade");
    connection.close();
  }

  //----------------------- TEST HELPERS -----------------------

  // allows to return a process engine configuration which doesn't create a schema when it's build.
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
