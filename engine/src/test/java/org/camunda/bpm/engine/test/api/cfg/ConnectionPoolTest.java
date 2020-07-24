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
package org.camunda.bpm.engine.test.api.cfg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.sql.DataSource;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.session.Configuration;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.junit.Test;


/**
 * @author Joram Barrez
 */
public class ConnectionPoolTest {

  @Test
  public void testMyBatisConnectionPoolProperlyConfigured() {
    ProcessEngineConfigurationImpl config = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
      .createProcessEngineConfigurationFromResource("org/camunda/bpm/engine/test/api/cfg/connection-pool.camunda.cfg.xml");

    ProcessEngine engine = config.buildProcessEngine();

    // Expected values
    int maxActive = 25;
    int maxIdle = 10;
    int maxCheckoutTime = 30000;
    int maxWaitTime = 25000;
    Integer jdbcStatementTimeout = 300;

    assertEquals(maxActive, config.getJdbcMaxActiveConnections());
    assertEquals(maxIdle, config.getJdbcMaxIdleConnections());
    assertEquals(maxCheckoutTime, config.getJdbcMaxCheckoutTime());
    assertEquals(maxWaitTime, config.getJdbcMaxWaitTime());
    assertEquals(jdbcStatementTimeout, config.getJdbcStatementTimeout());

    // Verify that these properties are correctly set in the MyBatis datasource
    Configuration sessionFactoryConfiguration = config.getDbSqlSessionFactory().getSqlSessionFactory().getConfiguration();
    DataSource datasource = sessionFactoryConfiguration.getEnvironment().getDataSource();
    assertTrue(datasource instanceof PooledDataSource);

    PooledDataSource pooledDataSource = (PooledDataSource) datasource;
    assertEquals(maxActive, pooledDataSource.getPoolMaximumActiveConnections());
    assertEquals(maxIdle, pooledDataSource.getPoolMaximumIdleConnections());
    assertEquals(maxCheckoutTime, pooledDataSource.getPoolMaximumCheckoutTime());
    assertEquals(maxWaitTime, pooledDataSource.getPoolTimeToWait());

    assertEquals(jdbcStatementTimeout, sessionFactoryConfiguration.getDefaultStatementTimeout());

    engine.close();
  }

}
