/**
 * Copyright (C) 2011, 2012 camunda services GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.camunda.fox.platform.impl.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import org.activiti.engine.impl.interceptor.CommandContextInterceptor;
import org.activiti.engine.impl.interceptor.CommandInterceptor;
import org.activiti.engine.impl.interceptor.JtaTransactionInterceptor;
import org.activiti.engine.impl.interceptor.LogInterceptor;

import com.camunda.fox.platform.FoxPlatformException;
import com.camunda.fox.platform.impl.service.AbstractPlatformService;

/**
 * @author Daniel Meyer
 */
public class JtaCmpeProcessEngineConfiguration extends CmpeProcessEngineConfiguration {

  public JtaCmpeProcessEngineConfiguration(AbstractPlatformService processEngineServiceBean) {
    super(processEngineServiceBean);
  }
  
  @Override
  protected void init() {
    initTransactionManager();
    super.init();
  }

  protected void initTransactionManager() {
    if(transactionManager == null) {
      try {
        transactionManager = InitialContext.doLookup(transactionManagerLookup);
      } catch (NamingException e) {
        throw new FoxPlatformException("Could not lookup JtaTransactionManager using name '"+transactionManagerLookup+".", e);
      }
    }
  }

  /** the string used to lookup the transaction manager */
  protected String transactionManagerLookup;
  
  /** the jta transaction manager */
  protected TransactionManager transactionManager;
  
  @Override
  protected Collection< ? extends CommandInterceptor> getDefaultCommandInterceptorsTxRequired() {
    List<CommandInterceptor> defaultCommandInterceptorsTxRequired = new ArrayList<CommandInterceptor>();
    defaultCommandInterceptorsTxRequired.add(new LogInterceptor());
    defaultCommandInterceptorsTxRequired.add(new JtaTransactionInterceptor(transactionManager, false));
    defaultCommandInterceptorsTxRequired.add(new CommandContextInterceptor(commandContextFactory, this));
    return defaultCommandInterceptorsTxRequired;
  }

  @Override
  protected Collection< ? extends CommandInterceptor> getDefaultCommandInterceptorsTxRequiresNew() {
    List<CommandInterceptor> defaultCommandInterceptorsTxRequiresNew = new ArrayList<CommandInterceptor>();
    defaultCommandInterceptorsTxRequiresNew.add(new LogInterceptor());
    defaultCommandInterceptorsTxRequiresNew.add(new JtaTransactionInterceptor(transactionManager, true));
    defaultCommandInterceptorsTxRequiresNew.add(new CommandContextInterceptor(commandContextFactory, this));
    return defaultCommandInterceptorsTxRequiresNew;
  }

  public TransactionManager getTransactionManager() {
    return transactionManager;
  }

  public void setTransactionManager(TransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }
  
  public void setTransactionManagerLookup(String transactionManagerLookup) {
    this.transactionManagerLookup = transactionManagerLookup;
  }
    
  public String getTransactionManagerLookup() {
    return transactionManagerLookup;
  }

}
