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
package org.camunda.bpm.webapp.impl.db;

import java.util.List;

import org.camunda.bpm.webapp.db.CommandExecutor;
import org.camunda.bpm.webapp.db.QueryParameters;
import org.camunda.bpm.webapp.db.QueryService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.db.AuthorizationCheck;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.util.QueryMaxResultsLimitUtil;

public class QueryServiceImpl implements QueryService {

  private CommandExecutor commandExecutor;

  public QueryServiceImpl(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }

  public <T> List<T> executeQuery(final String statement, final QueryParameters parameter) {
    return commandExecutor.executeCommand(new ExecuteListQueryCmd<T>(statement, parameter));
  }

  public <T> T executeQuery(String statement, Object parameter, Class<T> clazz) {
    return commandExecutor.executeCommand(new ExecuteSingleQueryCmd<T>(statement, parameter, clazz));
  }

  public Long executeQueryRowCount(String statement, ListQueryParameterObject parameter) {
    return commandExecutor.executeCommand(new QueryServiceRowCountCmd(statement, parameter));
  }

  protected ProcessEngineConfigurationImpl getProcessEngineConfiguration(
    CommandContext commandContext) {
    QuerySessionFactory querySessionFactory =
      (QuerySessionFactory) commandContext.getProcessEngineConfiguration();

    ProcessEngineConfigurationImpl processEngineConfiguration = null;
    if (querySessionFactory != null) {
      processEngineConfiguration = querySessionFactory.getWrappedConfiguration();
    }

    if (processEngineConfiguration == null) {
      throw new ProcessEngineException("Process Engine Configuration missing!");
    }

    return processEngineConfiguration;
  }

  protected  <T> void configureAuthCheck(ListQueryParameterObject parameter,
                                         ProcessEngineConfigurationImpl engineConfig,
                                         CommandContext commandContext) {
    AuthorizationCheck authCheck = parameter.getAuthCheck();

    commandContext.getAuthorizationManager()
      .enableQueryAuthCheck(authCheck);

    boolean isEnableHistoricInstancePermissions = engineConfig.isEnableHistoricInstancePermissions();
    authCheck.setHistoricInstancePermissionsEnabled(isEnableHistoricInstancePermissions);
  }

  protected class QueryServiceRowCountCmd implements Command<Long> {

    protected String statement;
    protected ListQueryParameterObject parameter;

    public QueryServiceRowCountCmd(String statement, ListQueryParameterObject parameter) {
      this.statement = statement;
      this.parameter = parameter;
    }

    @Override
    public Long execute(CommandContext commandContext) {
      ProcessEngineConfigurationImpl engineConfig = getProcessEngineConfiguration(commandContext);

      configureAuthCheck(parameter, engineConfig, commandContext);

      return (Long) commandContext.getDbSqlSession().selectOne(statement, parameter);
    }
  }

  protected class ExecuteListQueryCmd<T> implements Command<List<T>> {

    protected String statement;
    protected QueryParameters parameter;

    public ExecuteListQueryCmd(String statement, QueryParameters parameter) {
      this.statement = statement;
      this.parameter = parameter;
    }

    @Override
    public List<T> execute(CommandContext commandContext) {
      ProcessEngineConfigurationImpl engineConfig = getProcessEngineConfiguration(commandContext);

      configureAuthCheck(parameter, engineConfig, commandContext);

      if (parameter.isMaxResultsLimitEnabled()) {
        QueryMaxResultsLimitUtil.checkMaxResultsLimit(parameter.getMaxResults(), engineConfig);
      }

      return (List<T>) commandContext.getDbSqlSession().selectList(statement, parameter);
    }
  }

  protected class ExecuteSingleQueryCmd<T> implements Command<T> {

    protected String statement;
    protected Object parameter;
    protected Class clazz;

    public <T> ExecuteSingleQueryCmd(String statement, Object parameter, Class<T> clazz) {
      this.statement = statement;
      this.parameter = parameter;
      this.clazz = clazz;
    }

    @Override
    public T execute(CommandContext commandContext) {
      return (T) commandContext.getDbSqlSession().selectOne(statement, parameter);
    }
  }
}
