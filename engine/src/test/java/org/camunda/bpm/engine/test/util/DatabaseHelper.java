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

import java.sql.SQLException;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

public class DatabaseHelper {

  public static Integer getTransactionIsolationLevel(ProcessEngineConfigurationImpl processEngineConfiguration) {
    final Integer[] transactionIsolation = new Integer[1];
    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Object>() {
      @Override
      public Object execute(CommandContext commandContext) {
        try {
          transactionIsolation[0] = commandContext.getDbSqlSession().getSqlSession().getConnection().getTransactionIsolation();
        } catch (SQLException e) {

        }
        return null;
      }
    });
    return transactionIsolation[0];
  }

  public static String getDatabaseType(ProcessEngineConfigurationImpl processEngineConfiguration) {
    return processEngineConfiguration.getDbSqlSessionFactory().getDatabaseType();
  }

}
