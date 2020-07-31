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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.TransactionFactory;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Daniel Meyer
 *
 */
public class SharedSqlSessionFactoryCfgTest {

  @Before
  @After
  public void cleanCachedSessionFactory() {
    ProcessEngineConfigurationImpl.cachedSqlSessionFactory = null;
  }

  @Test
  public void shouldNotReuseSqlSessionFactoryByDefault() {
    assertFalse(new StandaloneInMemProcessEngineConfiguration().isUseSharedSqlSessionFactory());
  }

  @Test
  public void shouldCacheDbSqlSessionFactoryIfConfigured() {
    final TestEngineCfg cfg = new TestEngineCfg();

    // given
    cfg.setUseSharedSqlSessionFactory(true);

    // if
    cfg.initSqlSessionFactory();

    // then
    assertNotNull(ProcessEngineConfigurationImpl.cachedSqlSessionFactory);
  }

  @Test
  public void shouldNotCacheDbSqlSessionFactoryIfNotConfigured() {
    final TestEngineCfg cfg = new TestEngineCfg();

    // if
    cfg.initSqlSessionFactory();

    // then
    assertNull(ProcessEngineConfigurationImpl.cachedSqlSessionFactory);
    assertNotNull(cfg.getSqlSessionFactory());
  }

  @Test
  public void shouldReuseCachedSqlSessionFactoryIfConfigured() {
    final TestEngineCfg cfg = new TestEngineCfg();
    SqlSessionFactory existingSessionFactory = mock(SqlSessionFactory.class);

    // given
    ProcessEngineConfigurationImpl.cachedSqlSessionFactory = existingSessionFactory;
    cfg.setUseSharedSqlSessionFactory(true);

    // if
    cfg.initSqlSessionFactory();

    // then
    assertSame(existingSessionFactory, ProcessEngineConfigurationImpl.cachedSqlSessionFactory);
    assertSame(existingSessionFactory, cfg.getSqlSessionFactory());
  }

  @Test
  public void shouldNotReuseCachedSqlSessionIfNotConfigured() {
    final TestEngineCfg cfg = new TestEngineCfg();
    SqlSessionFactory existingSessionFactory = mock(SqlSessionFactory.class);

    // given
    ProcessEngineConfigurationImpl.cachedSqlSessionFactory = existingSessionFactory;

    // if
    cfg.initSqlSessionFactory();

    // then
    assertSame(existingSessionFactory, ProcessEngineConfigurationImpl.cachedSqlSessionFactory);
    assertNotSame(existingSessionFactory, cfg.getSqlSessionFactory());
  }

  static class TestEngineCfg extends StandaloneInMemProcessEngineConfiguration {

    public TestEngineCfg() {
      dataSource = mock(DataSource.class);
      transactionFactory = mock(TransactionFactory.class);
    }

    @Override
    public void initSqlSessionFactory() {
      super.initSqlSessionFactory();
    }

    @Override
    public SqlSessionFactory getSqlSessionFactory() {
      return super.getSqlSessionFactory();
    }

  }

}
