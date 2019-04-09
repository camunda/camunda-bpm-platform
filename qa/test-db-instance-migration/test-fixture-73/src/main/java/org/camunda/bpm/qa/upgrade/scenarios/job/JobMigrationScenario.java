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
package org.camunda.bpm.qa.upgrade.scenarios.job;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.ibatis.session.SqlSession;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.camunda.bpm.qa.upgrade.DescribesScenario;
import org.camunda.bpm.qa.upgrade.ScenarioSetup;

/**
 * This actually simulates creation of a job in Camunda 7.0;
 * we use 7.3
 *
 * @author Thorben Lindhauer
 */
public class JobMigrationScenario {

  @DescribesScenario("createJob")
  public static ScenarioSetup triggerEntryCriterion() {
    return new ScenarioSetup() {
      public void execute(ProcessEngine engine, final String scenarioName) {

        final ProcessEngineConfigurationImpl engineConfiguration = (ProcessEngineConfigurationImpl) engine.getProcessEngineConfiguration();
        CommandExecutor commandExecutor = engineConfiguration.getCommandExecutorTxRequired();

        // create a job with the scenario name as id and a null suspension state
        commandExecutor.execute(new Command<Void>() {
          public Void execute(CommandContext commandContext) {
            Connection connection = null;
            Statement statement = null;
            ResultSet rs = null;

            try {
              SqlSession sqlSession = commandContext.getDbSqlSession().getSqlSession();
              connection = sqlSession.getConnection();
              statement = connection
                  .createStatement();
              statement.executeUpdate("INSERT INTO ACT_RU_JOB(ID_, REV_, RETRIES_, TYPE_, EXCLUSIVE_, HANDLER_TYPE_) " +
                  "VALUES (" +
                  "'" + scenarioName + "'," +
                  "1," +
                  "3," +
                  "'timer'," +
                  DbSqlSessionFactory.databaseSpecificTrueConstant.get(engineConfiguration.getDatabaseType()) + "," +
                  "'" + TimerStartEventJobHandler.TYPE + "'" +
                  ")");
              connection.commit();
              statement.close();
            } catch (SQLException e) {
              throw new RuntimeException(e);
            } finally {
              try {
                if (statement != null) {
                  statement.close();
                }
                if (rs != null) {
                  rs.close();
                }
                if (connection != null) {
                  connection.close();
                }
              } catch (SQLException e) {
                throw new RuntimeException(e);
              }
            }
            return null;
          }
        });

      }
    };
  }
}
