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

import jakarta.transaction.TransactionManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.jta.JakartaTransactionContextFactory;
import org.camunda.bpm.engine.impl.interceptor.CommandInterceptor;
import org.camunda.bpm.engine.impl.interceptor.JakartaTransactionInterceptor;

/**
 * Jakarta Transactions-based implementation of the {@link AbstractTransactionProcessEngineConfiguration}
 */
public class JakartaTransactionProcessEngineConfiguration extends AbstractTransactionProcessEngineConfiguration {

  private final static ConfigurationLogger LOG = ProcessEngineLogger.CONFIG_LOGGER;

  protected TransactionManager transactionManager;

  @Override
  protected CommandInterceptor createTransactionInterceptor(boolean requiresNew) {
    return new JakartaTransactionInterceptor(transactionManager, requiresNew, this);
  }

  @Override
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
      transactionContextFactory = new JakartaTransactionContextFactory(transactionManager);
    }
  }

  public TransactionManager getTransactionManager() {
    return transactionManager;
  }

  public void setTransactionManager(TransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }
}
