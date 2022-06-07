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
package org.camunda.bpm.engine.test;

import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class DeadlockTest {

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected SQLException sqlException;

  @Before
  public void createTestTables() throws SQLException {
    Connection conn = engineRule.getProcessEngineConfiguration().getDataSource().getConnection();
    Statement statement = conn.createStatement();
    statement.execute("CREATE TABLE deadlock_test1 (FOO INTEGER)");
    statement.execute("CREATE TABLE deadlock_test2 (FOO INTEGER)");
    statement.executeUpdate("INSERT INTO deadlock_test1 VALUES (0)");
    statement.executeUpdate("INSERT INTO deadlock_test2 VALUES (0)");

    sqlException = null;
  }

  @After
  public void cleanTables() throws SQLException {
    Connection conn = engineRule.getProcessEngineConfiguration().getDataSource().getConnection();
    Statement statement = conn.createStatement();
    statement.execute("DROP TABLE deadlock_test1");
    statement.execute("DROP TABLE deadlock_test2");
  }

  @Test
  public void should() throws InterruptedException {
    String databaseType = engineRule.getProcessEngineConfiguration().getDatabaseType();
    switch (databaseType) {
    case DbSqlSessionFactory.MYSQL:
      deadlock();
      assertThat(sqlException.getSQLState()).isEqualTo("40001");
      assertThat(sqlException.getErrorCode()).isEqualTo(1213);
      break;
    case DbSqlSessionFactory.MARIADB:
      deadlock();
      assertThat(sqlException.getSQLState()).isEqualTo("40001");
      assertThat(sqlException.getErrorCode()).isEqualTo(1213);
      break;
    case DbSqlSessionFactory.MSSQL:
      deadlock();
      assertThat(sqlException.getSQLState()).isEqualTo("40001");
      assertThat(sqlException.getErrorCode()).isEqualTo(1205);
      break;
    case DbSqlSessionFactory.DB2:
      deadlock();
      assertThat(sqlException.getSQLState()).isEqualTo("40001");
      assertThat(sqlException.getErrorCode()).isEqualTo(-911);
      break;
    case DbSqlSessionFactory.ORACLE:
      deadlock();
      assertThat(sqlException.getSQLState()).isEqualTo("61000");
      assertThat(sqlException.getErrorCode()).isEqualTo(60);
      break;
    case DbSqlSessionFactory.POSTGRES:
      deadlock();
      assertThat(sqlException.getSQLState()).isEqualTo("40P01");
      assertThat(sqlException.getErrorCode()).isEqualTo(0);
      break;
    case DbSqlSessionFactory.CRDB:
      deadlock();
      assertThat(sqlException.getSQLState()).isEqualTo("40P01");
      assertThat(sqlException.getErrorCode()).isEqualTo(0);
      break;
    case DbSqlSessionFactory.H2:
      deadlock();
      assertThat(sqlException.getSQLState()).isEqualTo("40001");
      assertThat(sqlException.getErrorCode()).isEqualTo(40001);
      break;
    default:
      fail("database unknown");
    }
  }

  public void deadlock() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(2);

    Thread t1 = new Thread(() -> {
      Connection conn = null;
      try {
        conn = engineRule.getProcessEngineConfiguration().getDataSource().getConnection();
      } catch (SQLException e) {
        e.printStackTrace();
      }
      try {
        conn.setAutoCommit(false);
        Statement statement = conn.createStatement();
        statement.executeUpdate("UPDATE deadlock_test1 SET FOO=1");
        latch.countDown();
        try {
          latch.await();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        statement.executeUpdate("UPDATE deadlock_test2 SET FOO=1");
        conn.commit();
      } catch (SQLException e) {
        sqlException = e;
        System.out.println(e);
        System.out.println("STATE: " + e.getSQLState());
        System.out.println("CODE: " + e.getErrorCode());
        try {
          conn.rollback();
        } catch (SQLException ex) {
          ex.printStackTrace();
        }
      }
    });

    Thread t2 = new Thread(() -> {
      Connection conn = null;
      try {
        conn = engineRule.getProcessEngineConfiguration().getDataSource().getConnection();
      } catch (SQLException e) {
        e.printStackTrace();
      }
      try {
        conn.setAutoCommit(false);
        Statement statement = conn.createStatement();
        statement.executeUpdate("UPDATE deadlock_test2 SET FOO=1");
        latch.countDown();
        try {
          latch.await();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        statement.executeUpdate("UPDATE deadlock_test1 SET FOO=1");
        conn.commit();
      } catch (SQLException e) {
        sqlException = e;
        System.out.println(e);
        System.out.println("STATE: " + e.getSQLState());
        System.out.println("CODE: " + e.getErrorCode());
        try {
          conn.rollback();
        } catch (SQLException ex) {
          ex.printStackTrace();
        }
      }
    });
    t1.start();
    t2.start();
    t1.join();
    t2.join();
  }
}
