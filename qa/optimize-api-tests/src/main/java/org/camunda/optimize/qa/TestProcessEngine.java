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
package org.camunda.optimize.qa;


import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration;

public class TestProcessEngine {


  public static ProcessEngine getInstance(TestProperties properties) {
    javax.sql.DataSource datasource = createDatasource(properties);
    return createProcessEngine(datasource, properties);
  }

  protected static ProcessEngine createProcessEngine(javax.sql.DataSource datasource, TestProperties properties) {

    ProcessEngineConfigurationImpl processEngineConfiguration = new StandaloneProcessEngineConfiguration()
      .setDataSource(datasource)
      .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE)
      .setHistory(ProcessEngineConfiguration.HISTORY_FULL);
    processEngineConfiguration.setJdbcBatchProcessing(
      Boolean.parseBoolean(properties.getProperty("jdbcBatchProcessing", "true"))
    );

    return processEngineConfiguration.buildProcessEngine();
  }

  protected static javax.sql.DataSource createDatasource(TestProperties properties) {

    PoolProperties p = new PoolProperties();
    p.setUrl(properties.getProperty("databaseUrl", "jdbc:h2:mem:optimize-test;DB_CLOSE_DELAY=-1"));
    p.setDriverClassName(properties.getProperty("databaseDriver", "org.h2.Driver"));
    p.setUsername(properties.getProperty("databaseUser", "sa"));
    p.setPassword(properties.getProperty("databasePassword", ""));

    p.setJmxEnabled(false);
    p.setMaxActive(100);
    p.setInitialSize(10);

    DataSource datasource = new DataSource();
    datasource.setPoolProperties(p);

    return datasource;
  }
}
