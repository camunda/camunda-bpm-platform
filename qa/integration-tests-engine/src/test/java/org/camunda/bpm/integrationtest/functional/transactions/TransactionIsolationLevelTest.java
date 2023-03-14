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
package org.camunda.bpm.integrationtest.functional.transactions;

import org.apache.ibatis.session.SqlSession;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;

import static org.camunda.bpm.integrationtest.util.TestContainer.addContainerSpecificResourcesForNonPaWithoutWeld;
import static org.junit.Assert.assertEquals;


@RunWith(Arquillian.class)
public class TransactionIsolationLevelTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive processArchive() {
    WebArchive archive = initWebArchiveDeployment();
    addContainerSpecificResourcesForNonPaWithoutWeld(archive);
    return archive;
  }

  @Inject
  private ProcessEngine processEngine;

  @Test
  public void testTransactionIsolationLevelOnConnection() {
    ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
    SqlSession sqlSession = processEngineConfiguration.getDbSqlSessionFactory()
        .getSqlSessionFactory()
        .openSession();
    try {
      int transactionIsolation = sqlSession.getConnection().getTransactionIsolation();
      assertEquals("TransactionIsolationLevel for connection is " + transactionIsolation + " instead of " + Connection.TRANSACTION_READ_COMMITTED,
          Connection.TRANSACTION_READ_COMMITTED, transactionIsolation);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
