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
package org.camunda.bpm.engine.test.concurrency;

import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManagerFactory;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.test.RequiredDatabase;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.junit.After;
import org.junit.Test;

/**
 * @author Daniel Meyer
 *
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
@RequiredDatabase(excludes = {DbSqlSessionFactory.CRDB })
public class TransactionIsolationReadCommittedTest extends ConcurrencyTestCase {

  private ThreadControl thread1;
  private ThreadControl thread2;

  /**
   * In this test, we run two transactions concurrently.
   * The transactions have the following behavior:
   *
   * (1) INSERT row into a table
   * (2) SELECT ALL rows from that table
   *
   * We execute it with two threads in the following interleaving:
   *
   *      Thread 1             Thread 2
   *      ========             ========
   * ------INSERT---------------------------   |
   * ---------------------------INSERT------   |
   * ---------------------------SELECT------   v time
   * ------SELECT---------------------------
   *
   * Deadlocks may occur if readers are not properly isolated from writers.
   *
   */
  @Test
  public void testTransactionIsolation() {

    thread1 = executeControllableCommand(new TestCommand("p1"));

    // wait for Thread 1 to perform INSERT
    thread1.waitForSync();

    thread2 = executeControllableCommand(new TestCommand("p2"));

    // wait for Thread 2 to perform INSERT
    thread2.waitForSync();

    // wait for Thread 2 to perform SELECT
    thread2.makeContinue();

    // wait for Thread 1  to perform same SELECT => deadlock
    thread1.makeContinue();

    thread2.waitForSync();
    thread1.waitForSync();

  }

  static class TestCommand extends ControllableCommand<Void> {

    protected String id;

    public TestCommand(String id) {
      this.id = id;
    }

    public Void execute(CommandContext commandContext) {
      DbEntityManagerFactory dbEntityManagerFactory = new DbEntityManagerFactory(Context.getProcessEngineConfiguration().getIdGenerator());
      DbEntityManager newEntityManager = dbEntityManagerFactory.openSession();

      HistoricProcessInstanceEventEntity hpi = new HistoricProcessInstanceEventEntity();
      hpi.setId(id);
      hpi.setProcessInstanceId(id);
      hpi.setProcessDefinitionId("someProcDefId");
      hpi.setStartTime(new Date());
      hpi.setState(HistoricProcessInstance.STATE_ACTIVE);

      newEntityManager.insert(hpi);
      newEntityManager.flush();

      monitor.sync();

      DbEntityManager cmdEntityManager = commandContext.getDbEntityManager();
      cmdEntityManager.createHistoricProcessInstanceQuery().list();

      monitor.sync();

      return null;
    }

  }

  @After
  public void tearDown() throws Exception {

    // end interaction with Thread 2
    thread2.waitUntilDone();

    // end interaction with Thread 1
    thread1.waitUntilDone();

    processEngineConfiguration.getCommandExecutorTxRequired()
      .execute((Command<Void>) commandContext -> {
        List<HistoricProcessInstance> list = commandContext.getDbEntityManager().createHistoricProcessInstanceQuery().list();
        for (HistoricProcessInstance historicProcessInstance : list) {
          commandContext.getDbEntityManager().delete(HistoricProcessInstanceEventEntity.class, "deleteHistoricProcessInstance", historicProcessInstance.getId());
        }
        return null;
      });
  }

}
