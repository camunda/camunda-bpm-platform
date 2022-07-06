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
package org.camunda.bpm.engine.spring;

import javax.sql.DataSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipInputStream;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.interceptor.CommandContextInterceptor;
import org.camunda.bpm.engine.impl.interceptor.CommandCounterInterceptor;
import org.camunda.bpm.engine.impl.interceptor.CommandInterceptor;
import org.camunda.bpm.engine.impl.interceptor.LogInterceptor;
import org.camunda.bpm.engine.impl.interceptor.ProcessApplicationContextInterceptor;
import org.camunda.bpm.engine.impl.variable.serializer.jpa.EntityManagerSession;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ContextResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author Tom Baeyens
 * @author David Syer
 * @author Joram Barrez
 * @author Daniel Meyer
 */
public class SpringTransactionsProcessEngineConfiguration extends ProcessEngineConfigurationImpl {

  protected PlatformTransactionManager transactionManager;
  protected String deploymentName = "SpringAutoDeployment";
  protected Resource[] deploymentResources = new Resource[0];
  protected String deploymentTenantId;
  protected boolean deployChangedOnly;

  public SpringTransactionsProcessEngineConfiguration() {
    transactionsExternallyManaged = true;
  }

  @Override
  public ProcessEngine buildProcessEngine() {
    ProcessEngine processEngine = super.buildProcessEngine();
    autoDeployResources(processEngine);
    return processEngine;
  }

  protected Collection< ? extends CommandInterceptor> getDefaultCommandInterceptorsTxRequired() {
    if (transactionManager==null) {
      throw new ProcessEngineException("transactionManager is required property for SpringProcessEngineConfiguration, use "+StandaloneProcessEngineConfiguration.class.getName()+" otherwise");
    }

    List<CommandInterceptor> defaultCommandInterceptorsTxRequired = new ArrayList<CommandInterceptor>();
    // CRDB interceptor is added before the SpringTransactionInterceptor,
    // so that a Spring TX may be rolled back before retrying.
    if (DbSqlSessionFactory.CRDB.equals(databaseType)) {
      defaultCommandInterceptorsTxRequired.add(getCrdbRetryInterceptor());
    }
    if (!isDisableExceptionCode()) {
      defaultCommandInterceptorsTxRequired.add(getExceptionCodeInterceptor());
    }
    defaultCommandInterceptorsTxRequired.add(new LogInterceptor());
    defaultCommandInterceptorsTxRequired.add(new CommandCounterInterceptor(this));
    defaultCommandInterceptorsTxRequired.add(new ProcessApplicationContextInterceptor(this));
    defaultCommandInterceptorsTxRequired.add(new SpringTransactionInterceptor(transactionManager, TransactionTemplate.PROPAGATION_REQUIRED, this));
    CommandContextInterceptor commandContextInterceptor = new CommandContextInterceptor(commandContextFactory, this);
    defaultCommandInterceptorsTxRequired.add(commandContextInterceptor);
    return defaultCommandInterceptorsTxRequired;
  }

  protected Collection< ? extends CommandInterceptor> getDefaultCommandInterceptorsTxRequiresNew() {
    List<CommandInterceptor> defaultCommandInterceptorsTxRequiresNew = new ArrayList<CommandInterceptor>();
    // CRDB interceptor is added before the SpringTransactionInterceptor,
    // so that a Spring TX may be rolled back before retrying.
    if (DbSqlSessionFactory.CRDB.equals(databaseType)) {
      defaultCommandInterceptorsTxRequiresNew.add(getCrdbRetryInterceptor());
    }
    if (!isDisableExceptionCode()) {
      defaultCommandInterceptorsTxRequiresNew.add(getExceptionCodeInterceptor());
    }
    defaultCommandInterceptorsTxRequiresNew.add(new LogInterceptor());
    defaultCommandInterceptorsTxRequiresNew.add(new CommandCounterInterceptor(this));
    defaultCommandInterceptorsTxRequiresNew.add(new ProcessApplicationContextInterceptor(this));
    defaultCommandInterceptorsTxRequiresNew.add(new SpringTransactionInterceptor(transactionManager, TransactionTemplate.PROPAGATION_REQUIRES_NEW, this));
    CommandContextInterceptor commandContextInterceptor = new CommandContextInterceptor(commandContextFactory, this, true);
    defaultCommandInterceptorsTxRequiresNew.add(commandContextInterceptor);
    return defaultCommandInterceptorsTxRequiresNew;
  }

  @Override
  protected void initTransactionContextFactory() {
    if(transactionContextFactory == null && transactionManager != null) {
      transactionContextFactory = new SpringTransactionContextFactory(transactionManager);
    }
  }

  @Override
  protected void initJpa() {
    super.initJpa();
    if (jpaEntityManagerFactory != null) {
      sessionFactories.put(EntityManagerSession.class,
              new SpringEntityManagerSessionFactory(jpaEntityManagerFactory, jpaHandleTransaction, jpaCloseEntityManager));
    }
  }

  protected void autoDeployResources(ProcessEngine processEngine) {
    if (deploymentResources!=null && deploymentResources.length>0) {
      RepositoryService repositoryService = processEngine.getRepositoryService();

      DeploymentBuilder deploymentBuilder = repositoryService
        .createDeployment()
        .enableDuplicateFiltering(deployChangedOnly)
        .name(deploymentName)
        .tenantId(deploymentTenantId);

      for (Resource resource : deploymentResources) {
        String resourceName = null;

        if (resource instanceof ContextResource) {
          resourceName = ((ContextResource) resource).getPathWithinContext();

        } else if (resource instanceof ByteArrayResource) {
          resourceName = resource.getDescription();

        } else {
          resourceName = getFileResourceName(resource);
        }

        try {
          if ( resourceName.endsWith(".bar")
               || resourceName.endsWith(".zip")
               || resourceName.endsWith(".jar") ) {
            deploymentBuilder.addZipInputStream(new ZipInputStream(resource.getInputStream()));
          } else {
            deploymentBuilder.addInputStream(resourceName, resource.getInputStream());
          }
        } catch (IOException e) {
          throw new ProcessEngineException("couldn't auto deploy resource '"+resource+"': "+e.getMessage(), e);
        }
      }

      deploymentBuilder.deploy();
    }
  }

  protected String getFileResourceName(Resource resource) {
    try {
      return resource.getFile().getAbsolutePath();
    } catch (IOException e) {
      return resource.getFilename();
    }
  }

  @Override
  public ProcessEngineConfigurationImpl setDataSource(DataSource dataSource) {
    if(dataSource instanceof TransactionAwareDataSourceProxy) {
      return super.setDataSource(dataSource);
    } else {
      // Wrap datasource in Transaction-aware proxy
      DataSource proxiedDataSource = new TransactionAwareDataSourceProxy(dataSource);
      return super.setDataSource(proxiedDataSource);
    }
  }

  public PlatformTransactionManager getTransactionManager() {
    return transactionManager;
  }

  public void setTransactionManager(PlatformTransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }

  public String getDeploymentName() {
    return deploymentName;
  }

  public void setDeploymentName(String deploymentName) {
    this.deploymentName = deploymentName;
  }

  public Resource[] getDeploymentResources() {
    return deploymentResources;
  }

  public void setDeploymentResources(Resource[] deploymentResources) {
    this.deploymentResources = deploymentResources;
  }

  public String getDeploymentTenantId() {
    return deploymentTenantId;
  }

  public void setDeploymentTenantId(String deploymentTenantId) {
    this.deploymentTenantId = deploymentTenantId;
  }

  public boolean isDeployChangedOnly() {
    return deployChangedOnly;
  }

  public void setDeployChangedOnly(boolean deployChangedOnly) {
    this.deployChangedOnly = deployChangedOnly;
  }

}
