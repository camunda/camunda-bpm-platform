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
package org.camunda.bpm.engine.test.errorcode;

import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.test.RequiredDatabase;
import org.camunda.bpm.engine.impl.util.ExceptionUtil;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.camunda.bpm.engine.impl.util.ExceptionUtil.DEADLOCK_CODES.CRDB;
import static org.camunda.bpm.engine.impl.util.ExceptionUtil.DEADLOCK_CODES.DB2;
import static org.camunda.bpm.engine.impl.util.ExceptionUtil.DEADLOCK_CODES.H2;
import static org.camunda.bpm.engine.impl.util.ExceptionUtil.DEADLOCK_CODES.MARIADB_MYSQL;
import static org.camunda.bpm.engine.impl.util.ExceptionUtil.DEADLOCK_CODES.MSSQL;
import static org.camunda.bpm.engine.impl.util.ExceptionUtil.DEADLOCK_CODES.ORACLE;
import static org.camunda.bpm.engine.impl.util.ExceptionUtil.DEADLOCK_CODES.POSTGRES;

/**
 * HEADS-UP: If a test fails, please make sure to adjust the error code / sql state for the respective
 * database in {@link ExceptionUtil.DEADLOCK_CODES}.
 */
public class DeadlockTest {

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected SQLException sqlException;

  @Before
  public void createTestTables() throws SQLException {
    Connection conn = engineRule.getProcessEngineConfiguration().getDataSource().getConnection();

    conn.setAutoCommit(false);

    Statement statement = conn.createStatement();
    statement.execute("CREATE TABLE deadlock_test1 (FOO INTEGER)");
    statement.execute("CREATE TABLE deadlock_test2 (FOO INTEGER)");
    statement.executeUpdate("INSERT INTO deadlock_test1 VALUES (0)");
    statement.executeUpdate("INSERT INTO deadlock_test2 VALUES (0)");

    conn.commit();

    sqlException = null;
  }

  @After
  public void cleanTables() throws SQLException {
    Connection conn = engineRule.getProcessEngineConfiguration().getDataSource().getConnection();

    conn.setAutoCommit(false);

    Statement statement = conn.createStatement();
    statement.execute("DROP TABLE deadlock_test1");
    statement.execute("DROP TABLE deadlock_test2");

    conn.commit();
  }

  @Test
  public void shouldProvokeDeadlock() throws InterruptedException {
    String databaseType = engineRule.getProcessEngineConfiguration().getDatabaseType();
    switch (databaseType) {
    case DbSqlSessionFactory.MARIADB:
    case DbSqlSessionFactory.MYSQL:
      provokeDeadlock();
      assertThat(sqlException.getSQLState()).isEqualTo(MARIADB_MYSQL.getSqlState());
      assertThat(sqlException.getErrorCode()).isEqualTo(MARIADB_MYSQL.getErrorCode());
      break;
    case DbSqlSessionFactory.MSSQL:
      provokeDeadlock();
      assertThat(sqlException.getSQLState()).isEqualTo(MSSQL.getSqlState());
      assertThat(sqlException.getErrorCode()).isEqualTo(MSSQL.getErrorCode());
      break;
    case DbSqlSessionFactory.DB2:
      provokeDeadlock();
      assertThat(sqlException.getSQLState()).isEqualTo(DB2.getSqlState());
      assertThat(sqlException.getErrorCode()).isEqualTo(DB2.getErrorCode());
      break;
    case DbSqlSessionFactory.ORACLE:
      provokeDeadlock();
      assertThat(sqlException.getSQLState()).isEqualTo(ORACLE.getSqlState());
      assertThat(sqlException.getErrorCode()).isEqualTo(ORACLE.getErrorCode());
      break;
    case DbSqlSessionFactory.POSTGRES:
      provokeDeadlock();
      assertThat(sqlException.getSQLState()).isEqualTo(POSTGRES.getSqlState());
      assertThat(sqlException.getErrorCode()).isEqualTo(POSTGRES.getErrorCode());
      break;
    case DbSqlSessionFactory.CRDB:
      provokeDeadlock();
      assertThat(sqlException.getSQLState()).isEqualTo(CRDB.getSqlState());
      assertThat(sqlException.getErrorCode()).isEqualTo(CRDB.getErrorCode());
      break;
    case DbSqlSessionFactory.H2:
      provokeDeadlock();
      assertThat(sqlException.getSQLState()).isEqualTo(H2.getSqlState());
      assertThat(sqlException.getErrorCode()).isEqualTo(H2.getErrorCode());
      break;
    default:
      fail("database unknown");
    }
  }

  public void provokeDeadlock() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(2);

    DataSource dataSource = engineRule.getProcessEngineConfiguration().getDataSource();

    Thread t1 = new Thread(() -> {
      try (Connection conn = dataSource.getConnection()) {
        try {
          conn.setAutoCommit(false);

          Statement statement = conn.createStatement();
          statement.executeUpdate("UPDATE deadlock_test1 SET FOO=1");

          latch.countDown();
          try {
            latch.await();
          } catch (InterruptedException e) {
          }

          statement.executeUpdate("UPDATE deadlock_test2 SET FOO=1");
          conn.commit();

        } catch (SQLException e) {
          sqlException = e;
          try {
            conn.rollback();
          } catch (SQLException ex) {
          }
        }
      } catch (SQLException e) {
      }
    });

    Thread t2 = new Thread(() -> {
      try (Connection conn = dataSource.getConnection()) {
        try {
          conn.setAutoCommit(false);

          Statement statement = conn.createStatement();
          statement.executeUpdate("UPDATE deadlock_test2 SET FOO=1");

          latch.countDown();
          try {
            latch.await();
          } catch (InterruptedException e) {
          }

          statement.executeUpdate("UPDATE deadlock_test1 SET FOO=1");
          conn.commit();

        } catch (SQLException e) {
          sqlException = e;
          try {
            conn.rollback();
          } catch (SQLException ex) {
          }
        }
      } catch (SQLException e) {
      }
    });
    t1.start();
    t2.start();
    t1.join();
    t2.join();
  }
}
