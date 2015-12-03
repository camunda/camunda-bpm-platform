/* Licensed under the Apache License, Version 2.0 (the "License");
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

import org.apache.ibatis.datasource.pooled.PoolState;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Daniel Meyer
 *
 */
public class ForceCloseMybatisConnectionPoolTest {


  @Test
  public void testForceCloseMybatisConnectionPoolTrue() {

    // given
    // that the process engine is configured with forceCloseMybatisConnectionPool = true
    ProcessEngineConfigurationImpl configurationImpl = new StandaloneInMemProcessEngineConfiguration()
     .setJdbcUrl("jdbc:h2:mem:camunda-forceclose")
     .setProcessEngineName("engine-forceclose")
     .setForceCloseMybatisConnectionPool(true);

    ProcessEngine processEngine = configurationImpl
     .buildProcessEngine();

    PooledDataSource pooledDataSource = (PooledDataSource) configurationImpl.getDataSource();
    PoolState state = pooledDataSource.getPoolState();


    // then
    // if the process engine is closed
    processEngine.close();

    // the idle connections are closed
    Assert.assertTrue(state.getIdleConnectionCount() == 0);

  }

  @Test
  public void testForceCloseMybatisConnectionPoolFalse() {

    // given
    // that the process engine is configured with forceCloseMybatisConnectionPool = false
    ProcessEngineConfigurationImpl configurationImpl = new StandaloneInMemProcessEngineConfiguration()
     .setJdbcUrl("jdbc:h2:mem:camunda-forceclose")
     .setProcessEngineName("engine-forceclose")
     .setForceCloseMybatisConnectionPool(false);

    ProcessEngine processEngine = configurationImpl
     .buildProcessEngine();

    PooledDataSource pooledDataSource = (PooledDataSource) configurationImpl.getDataSource();
    PoolState state = pooledDataSource.getPoolState();
    int idleConnections = state.getIdleConnectionCount();


    // then
    // if the process engine is closed
    processEngine.close();

    // the idle connections are not closed
    Assert.assertEquals(state.getIdleConnectionCount(), idleConnections);

    pooledDataSource.forceCloseAll();

    Assert.assertTrue(state.getIdleConnectionCount() == 0);
  }

}
