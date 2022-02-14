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
package org.camunda.bpm.engine.impl.cfg;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.jta.JtaTransactionContextFactory;
import org.camunda.bpm.engine.impl.cfg.standalone.StandaloneTransactionContextFactory;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.interceptor.CommandContextFactory;
import org.camunda.bpm.engine.impl.interceptor.CommandContextInterceptor;
import org.camunda.bpm.engine.impl.interceptor.CommandCounterInterceptor;
import org.camunda.bpm.engine.impl.interceptor.CommandInterceptor;
import org.camunda.bpm.engine.impl.interceptor.JtaTransactionInterceptor;
import org.camunda.bpm.engine.impl.interceptor.LogInterceptor;
import org.camunda.bpm.engine.impl.interceptor.ProcessApplicationContextInterceptor;
import org.camunda.bpm.engine.impl.interceptor.TxContextCommandContextFactory;


/**
 * @author Tom Baeyens
 */
public class JtaProcessEngineConfiguration extends ProcessEngineConfigurationImpl {

  private final static ConfigurationLogger LOG = ProcessEngineLogger.CONFIG_LOGGER;

  protected TransactionManager transactionManager;

  protected String transactionManagerJndiName;

  /** {@link CommandContextFactory} to be used for DbSchemaOperations */
  protected CommandContextFactory dbSchemaOperationsCommandContextFactory;

  public JtaProcessEngineConfiguration() {
    transactionsExternallyManaged = true;
  }

  protected void init() {
    initTransactionManager();
    initDbSchemaOperationsCommandContextFactory();
    super.init();
  }

  @Override
  protected Collection< ? extends CommandInterceptor> getDefaultCommandInterceptorsTxRequired() {
    List<CommandInterceptor> defaultCommandInterceptorsTxRequired = new ArrayList<CommandInterceptor>();
    // CRDB interceptor is added before the JtaTransactionInterceptor,
    // so that a Java EE managed TX may be rolled back before retrying.
    if (DbSqlSessionFactory.CRDB.equals(databaseType)) {
      defaultCommandInterceptorsTxRequired.add(getCrdbRetryInterceptor());
    }
    defaultCommandInterceptorsTxRequired.add(new LogInterceptor());
    defaultCommandInterceptorsTxRequired.add(new CommandCounterInterceptor(this));
    defaultCommandInterceptorsTxRequired.add(new ProcessApplicationContextInterceptor(this));
    defaultCommandInterceptorsTxRequired.add(new JtaTransactionInterceptor(transactionManager, false, this));
    defaultCommandInterceptorsTxRequired.add(new CommandContextInterceptor(commandContextFactory, this));
    return defaultCommandInterceptorsTxRequired;
  }

  @Override
  protected Collection< ? extends CommandInterceptor> getDefaultCommandInterceptorsTxRequiresNew() {
    List<CommandInterceptor> defaultCommandInterceptorsTxRequiresNew = new ArrayList<CommandInterceptor>();
    // CRDB interceptor is added before the JtaTransactionInterceptor,
    // so that a Java EE managed TX may be rolled back before retrying.
    if (DbSqlSessionFactory.CRDB.equals(databaseType)) {
      defaultCommandInterceptorsTxRequiresNew.add(getCrdbRetryInterceptor());
    }
    defaultCommandInterceptorsTxRequiresNew.add(new LogInterceptor());
    defaultCommandInterceptorsTxRequiresNew.add(new CommandCounterInterceptor(this));
    defaultCommandInterceptorsTxRequiresNew.add(new ProcessApplicationContextInterceptor(this));
    defaultCommandInterceptorsTxRequiresNew.add(new JtaTransactionInterceptor(transactionManager, true, this));
    defaultCommandInterceptorsTxRequiresNew.add(new CommandContextInterceptor(commandContextFactory, this, true));
    return defaultCommandInterceptorsTxRequiresNew;
  }

  /**
   * provide custom command executor that uses NON-JTA transactions
   */
  @Override
  protected void initCommandExecutorDbSchemaOperations() {
    if(commandExecutorSchemaOperations == null) {
      List<CommandInterceptor> commandInterceptorsDbSchemaOperations = new ArrayList<CommandInterceptor>();
      commandInterceptorsDbSchemaOperations.add(new LogInterceptor());
      commandInterceptorsDbSchemaOperations.add(new CommandCounterInterceptor(this));
      commandInterceptorsDbSchemaOperations.add(new CommandContextInterceptor(dbSchemaOperationsCommandContextFactory, this));
      commandInterceptorsDbSchemaOperations.add(actualCommandExecutor);
      commandExecutorSchemaOperations = initInterceptorChain(commandInterceptorsDbSchemaOperations);
    }
  }

  protected void initDbSchemaOperationsCommandContextFactory() {
    if(dbSchemaOperationsCommandContextFactory == null) {
      TxContextCommandContextFactory cmdContextFactory = new TxContextCommandContextFactory();
      cmdContextFactory.setProcessEngineConfiguration(this);
      cmdContextFactory.setTransactionContextFactory(new StandaloneTransactionContextFactory());
      dbSchemaOperationsCommandContextFactory = cmdContextFactory;
    }
  }

  protected void initTransactionManager() {
    if(transactionManager == null){

      if(transactionManagerJndiName == null || transactionManagerJndiName.length() == 0) {
        throw LOG.invalidConfigTransactionManagerIsNull();
      }

      try {
        transactionManager = (TransactionManager) new InitialContext().lookup(transactionManagerJndiName);

      } catch(NamingException e) {
        throw LOG.invalidConfigCannotFindTransactionManger(transactionManagerJndiName+"'.", e);
      }
    }
  }

  @Override
  protected void initTransactionContextFactory() {
    if(transactionContextFactory == null) {
      transactionContextFactory = new JtaTransactionContextFactory(transactionManager);
    }
  }

  public TransactionManager getTransactionManager() {
    return transactionManager;
  }

  public void setTransactionManager(TransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }

  public String getTransactionManagerJndiName() {
    return transactionManagerJndiName;
  }

  public void setTransactionManagerJndiName(String transactionManagerJndiName) {
    this.transactionManagerJndiName = transactionManagerJndiName;
  }

  public CommandContextFactory getDbSchemaOperationsCommandContextFactory() {
    return dbSchemaOperationsCommandContextFactory;
  }

  public void setDbSchemaOperationsCommandContextFactory(CommandContextFactory dbSchemaOperationsCommandContextFactory) {
    this.dbSchemaOperationsCommandContextFactory = dbSchemaOperationsCommandContextFactory;
  }
}
